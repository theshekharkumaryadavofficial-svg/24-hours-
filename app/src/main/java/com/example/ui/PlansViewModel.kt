package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bedtime.BedtimeLockActivity
import com.example.bedtime.BedtimePermissionsHelper
import com.example.bedtime.BedtimePermissionsStatus
import com.example.data.DailyDisciplineConfigEntity
import com.example.data.DailyPlanBlockEntity
import com.example.data.NaamJapRepository
import com.example.data.PlansRepository
import com.example.data.StudyModeConfigEntity
import com.example.data.todayDateKey
import com.example.plans.DisciplineReminderReceiver
import com.example.plans.InstalledAppInfo
import com.example.plans.InstalledAppsProvider
import com.example.plans.StudyGuardService
import com.example.plans.StudyScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class PlansUiState(
    val blocks: List<DailyPlanBlockEntity> = emptyList(),
    val studyConfig: StudyModeConfigEntity = StudyModeConfigEntity(),
    val disciplineConfig: DailyDisciplineConfigEntity = DailyDisciplineConfigEntity(),
    val todayNaamJapCount: Int = 0,
    val currentHour: Int = 0,
    val currentMinute: Int = 0,
    val permissions: BedtimePermissionsStatus = BedtimePermissionsStatus(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val isLoadingApps: Boolean = false,
    val showStudyConfigSheet: Boolean = false,
    val showReminderSheet: Boolean = false,
    val showSmartScheduleSheet: Boolean = false,
    val editingBlock: DailyPlanBlockEntity? = null,
    val showAddBlockSheet: Boolean = false,
    val snackbarMessage: String? = null
) {
    val completedBlocksToday: Int
        get() = blocks.count { it.isCompletedToday() }

    val strictLockedSlotsCount: Int
        get() = blocks.count { it.strictAppLockEnabled }

    val activeSlotNow: DailyPlanBlockEntity?
        get() = blocks.firstOrNull { it.strictAppLockEnabled && it.isCurrentTimeInside(currentHour, currentMinute) }
            ?: blocks.firstOrNull { it.isCurrentTimeInside(currentHour, currentMinute) }

    val naamJapProgressFraction: Float
        get() = (todayNaamJapCount.toFloat() / disciplineConfig.naamJapReminderTarget.coerceAtLeast(108))
            .coerceIn(0f, 1f)
}

class PlansViewModel(
    private val appContext: Context,
    private val plansRepository: PlansRepository,
    private val naamJapRepository: NaamJapRepository
) : ViewModel() {

    private val clockMinuteFlow = MutableStateFlow(currentHourMinute())
    private val permissionsFlow = MutableStateFlow(BedtimePermissionsHelper.checkStatus(appContext))
    private val installedAppsFlow = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val isLoadingAppsFlow = MutableStateFlow(false)
    private val showStudySheetFlow = MutableStateFlow(false)
    private val showReminderSheetFlow = MutableStateFlow(false)
    private val showSmartSheetFlow = MutableStateFlow(false)
    private val showAddBlockFlow = MutableStateFlow(false)
    private val editingBlockFlow = MutableStateFlow<DailyPlanBlockEntity?>(null)
    private val snackbarFlow = MutableStateFlow<String?>(null)

    private data class SheetFlags(
        val studySheet: Boolean,
        val reminderSheet: Boolean,
        val smartSheet: Boolean,
        val addBlockSheet: Boolean,
        val editingBlock: DailyPlanBlockEntity?,
        val snackbar: String?
    )

    private val sheetFlagsFlow = combine(
        showStudySheetFlow,
        showReminderSheetFlow,
        showSmartSheetFlow,
        showAddBlockFlow,
        editingBlockFlow
    ) { s, r, sm, add, edit ->
        SheetFlags(s, r, sm, add, edit, null)
    }.combine(snackbarFlow) { flags, msg ->
        flags.copy(snackbar = msg)
    }

    private data class CorePlansData(
        val blocks: List<DailyPlanBlockEntity>,
        val study: StudyModeConfigEntity,
        val discipline: DailyDisciplineConfigEntity,
        val todayNaamJap: Int
    )

    private val coreDataFlow = combine(
        plansRepository.allBlocks,
        plansRepository.studyConfig,
        plansRepository.disciplineConfig,
        naamJapRepository.allHistory
    ) { blocks, study, discipline, naamJapHistory ->
        val todayCount = naamJapHistory.firstOrNull { it.dateKey == todayDateKey() }?.count ?: 0
        CorePlansData(blocks, study, discipline, todayCount)
    }

    private data class SystemAppsState(
        val clockHm: Pair<Int, Int>,
        val perms: BedtimePermissionsStatus,
        val apps: List<InstalledAppInfo>,
        val loadingApps: Boolean
    )

    private val systemAppsFlow = combine(
        clockMinuteFlow,
        permissionsFlow,
        installedAppsFlow,
        isLoadingAppsFlow
    ) { hm, perms, apps, loading ->
        SystemAppsState(hm, perms, apps, loading)
    }

    val uiState: StateFlow<PlansUiState> = combine(
        coreDataFlow,
        systemAppsFlow,
        sheetFlagsFlow
    ) { core, sys, flags ->
        PlansUiState(
            blocks = core.blocks,
            studyConfig = core.study,
            disciplineConfig = core.discipline,
            todayNaamJapCount = core.todayNaamJap,
            currentHour = sys.clockHm.first,
            currentMinute = sys.clockHm.second,
            permissions = sys.perms,
            installedApps = sys.apps,
            isLoadingApps = sys.loadingApps,
            showStudyConfigSheet = flags.studySheet,
            showReminderSheet = flags.reminderSheet,
            showSmartScheduleSheet = flags.smartSheet,
            editingBlock = flags.editingBlock,
            showAddBlockSheet = flags.addBlockSheet,
            snackbarMessage = flags.snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlansUiState()
    )

    init {
        viewModelScope.launch {
            plansRepository.ensureInitialized()
            val blocks = plansRepository.getAllBlocksOnce()
            StudyScheduler.syncStudySchedule(appContext, plansRepository.getStudyConfigOnce())
            StudyScheduler.syncAllSlotSchedules(appContext, blocks)
            StudyScheduler.syncDisciplineReminder(appContext, plansRepository.getDisciplineConfigOnce())
            refreshInstalledApps()
        }
        viewModelScope.launch {
            while (isActive) {
                clockMinuteFlow.value = currentHourMinute()
                delay(15_000L)
            }
        }
    }

    fun refreshInstalledApps() {
        if (isLoadingAppsFlow.value) return
        viewModelScope.launch {
            isLoadingAppsFlow.value = true
            installedAppsFlow.value = InstalledAppsProvider.loadInstalledLauncherApps(appContext)
            isLoadingAppsFlow.value = false
        }
    }

    fun refreshPermissions() {
        permissionsFlow.value = BedtimePermissionsHelper.checkStatus(appContext)
    }

    fun toggleStudyModeNow() {
        val active = uiState.value.studyConfig.isActiveNow
        if (!active) {
            StudyGuardService.startStudyMode(appContext)
            snackbarFlow.value = "24H Slot & Study Lock ON · Blocked apps show Full-Screen Radha Naam Jap"
        } else {
            StudyGuardService.stopStudyMode(appContext)
            snackbarFlow.value = "Slot & Study Lock ended"
        }
    }

    fun previewStudyRedLock() {
        val state = uiState.value
        val activeSlot = state.activeSlotNow
        val slotTitle = activeSlot?.title ?: "Study / Slot App Lock"
        val windowText = activeSlot?.timeRangeFormatted ?: state.studyConfig.windowFormatted
        val intent = Intent(appContext, BedtimeLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(BedtimeLockActivity.EXTRA_BLOCKED_APP, "Instagram / Any New App")
            putExtra(BedtimeLockActivity.EXTRA_SLOT_TITLE, slotTitle)
            putExtra(BedtimeLockActivity.EXTRA_SLEEP_WINDOW, windowText)
            putExtra(BedtimeLockActivity.EXTRA_BLOCKED_COUNT, state.studyConfig.effectiveBlockedToday.coerceAtLeast(1))
            putExtra(BedtimeLockActivity.EXTRA_IS_PREVIEW, true)
            putExtra(BedtimeLockActivity.EXTRA_IS_STUDY_MODE, true)
        }
        appContext.startActivity(intent)
    }

    fun saveStudyConfig(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        autoEnabled: Boolean,
        blockSocialMedia: Boolean,
        allowedKeywordsCsv: String
    ) {
        viewModelScope.launch {
            val saved = plansRepository.saveStudyConfig(
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                autoStudyEnabled = autoEnabled,
                blockSocialMedia = blockSocialMedia,
                allowedKeywordsCsv = allowedKeywordsCsv
            )
            StudyScheduler.syncStudySchedule(appContext, saved)
            val (h, m) = currentHourMinute()
            if (autoEnabled && !saved.isActiveNow && saved.isTimeInsideStudyWindow(h, m)) {
                StudyGuardService.startStudyMode(appContext)
                snackbarFlow.value = "Inside ${saved.windowFormatted} · Study Naam Jap Lock activated!"
            } else if (autoEnabled) {
                snackbarFlow.value = "Study Mode armed for ${saved.windowFormatted}"
            } else {
                snackbarFlow.value = "Study Mode schedule saved"
            }
            showStudySheetFlow.value = false
        }
    }

    fun addWater(deltaMl: Int) {
        viewModelScope.launch {
            plansRepository.addWaterMl(deltaMl)
        }
    }

    fun setLightMealsLogged(count: Int) {
        viewModelScope.launch {
            plansRepository.toggleLightMealCount(count)
        }
    }

    fun toggle10MinReminderQuick(enabled: Boolean) {
        val cfg = uiState.value.disciplineConfig
        saveDisciplineSettings(
            waterGoalMl = cfg.waterGoalMl,
            lightMealsTarget = cfg.lightMealsTarget,
            naamJapTarget = cfg.naamJapReminderTarget,
            reminderEnabled = enabled,
            intervalMinutes = cfg.reminderIntervalMinutes
        )
    }

    fun saveDisciplineSettings(
        waterGoalMl: Int,
        lightMealsTarget: Int,
        naamJapTarget: Int,
        reminderEnabled: Boolean,
        intervalMinutes: Int
    ) {
        viewModelScope.launch {
            val saved = plansRepository.saveDisciplineReminderSettings(
                waterGoalMl = waterGoalMl,
                lightMealsTarget = lightMealsTarget,
                naamJapTarget = naamJapTarget,
                reminderEnabled = reminderEnabled,
                intervalMinutes = intervalMinutes
            )
            StudyScheduler.syncDisciplineReminder(appContext, saved)
            showReminderSheetFlow.value = false
            snackbarFlow.value = if (reminderEnabled) {
                "Reminder ON · Alerts every ${saved.reminderIntervalMinutes} min for Water, Meals & Naam Jap"
            } else {
                "Discipline goals saved (periodic reminders paused)"
            }
        }
    }

    fun sendTestDisciplineReminder() {
        val state = uiState.value
        DisciplineReminderReceiver.postDisciplineReminderNow(
            context = appContext,
            waterText = state.disciplineConfig.waterLitersFormatted,
            lightMealsLogged = state.disciplineConfig.effectiveLightMeals,
            lightMealsTarget = state.disciplineConfig.lightMealsTarget,
            naamJapToday = state.todayNaamJapCount,
            naamJapTarget = state.disciplineConfig.naamJapReminderTarget
        )
        snackbarFlow.value = "Live discipline reminder notification sent"
    }

    fun generateSmartFullDaySchedule(
        wakeHour: Int,
        studyStartHour: Int,
        studyEndHour: Int,
        workStartHour: Int,
        workEndHour: Int,
        enable10MinReminders: Boolean
    ) {
        viewModelScope.launch {
            plansRepository.generateSmartFullDaySchedule(
                wakeHour = wakeHour,
                studyStartHour = studyStartHour,
                studyEndHour = studyEndHour,
                workStartHour = workStartHour,
                workEndHour = workEndHour
            )
            val currentStudy = plansRepository.getStudyConfigOnce()
            val savedStudy = plansRepository.saveStudyConfig(
                startHour = studyStartHour,
                startMinute = 0,
                endHour = studyEndHour,
                endMinute = 0,
                autoStudyEnabled = true,
                blockSocialMedia = true,
                allowedKeywordsCsv = currentStudy.allowedStudyKeywordsCsv
            )
            StudyScheduler.syncStudySchedule(appContext, savedStudy)
            StudyScheduler.syncAllSlotSchedules(appContext, plansRepository.getAllBlocksOnce())

            val savedDisc = plansRepository.saveDisciplineReminderSettings(
                waterGoalMl = 4000,
                lightMealsTarget = 3,
                naamJapTarget = 20000,
                reminderEnabled = enable10MinReminders,
                intervalMinutes = 10
            )
            StudyScheduler.syncDisciplineReminder(appContext, savedDisc)

            showSmartSheetFlow.value = false
            snackbarFlow.value = "Smart Full-Day Dincharya created & Study Shield armed!"
        }
    }

    fun toggleBlockDone(block: DailyPlanBlockEntity) {
        viewModelScope.launch {
            plansRepository.toggleBlockCompleted(block)
        }
    }

    fun savePlanBlock(block: DailyPlanBlockEntity) {
        viewModelScope.launch {
            plansRepository.savePlanBlock(block)
            val allBlocks = plansRepository.getAllBlocksOnce()
            StudyScheduler.syncAllSlotSchedules(appContext, allBlocks)

            val (h, m) = currentHourMinute()
            if (block.strictAppLockEnabled && block.isCurrentTimeInside(h, m)) {
                StudyGuardService.startStudyMode(appContext)
                snackbarFlow.value =
                    "'${block.title}' LIVE now! Only ${block.allowedAppsCount} app(s) allowed; others open Radha Naam Jap"
            } else if (block.strictAppLockEnabled) {
                snackbarFlow.value =
                    "'${block.title}' (${block.timeRangeFormatted}) locked permanently · ${block.allowedAppsCount} app(s) allowed"
            } else {
                snackbarFlow.value = "Timetable slot saved"
            }
            showAddBlockFlow.value = false
            editingBlockFlow.value = null
        }
    }

    fun deletePlanBlock(id: Int) {
        viewModelScope.launch {
            plansRepository.deletePlanBlock(id)
            StudyScheduler.syncAllSlotSchedules(appContext, plansRepository.getAllBlocksOnce())
            showAddBlockFlow.value = false
            editingBlockFlow.value = null
            snackbarFlow.value = "Timetable slot removed"
        }
    }

    fun openStudyConfigSheet() { showStudySheetFlow.value = true }
    fun dismissStudyConfigSheet() { showStudySheetFlow.value = false }
    fun openReminderSheet() { showReminderSheetFlow.value = true }
    fun dismissReminderSheet() { showReminderSheetFlow.value = false }
    fun openSmartScheduleSheet() { showSmartSheetFlow.value = true }
    fun dismissSmartScheduleSheet() { showSmartSheetFlow.value = false }
    fun openAddBlockSheet() {
        editingBlockFlow.value = null
        showAddBlockFlow.value = true
        if (installedAppsFlow.value.isEmpty()) refreshInstalledApps()
    }
    fun openEditBlockSheet(block: DailyPlanBlockEntity) {
        editingBlockFlow.value = block
        showAddBlockFlow.value = true
        if (installedAppsFlow.value.isEmpty()) refreshInstalledApps()
    }
    fun dismissBlockSheet() {
        showAddBlockFlow.value = false
        editingBlockFlow.value = null
    }
    fun clearSnackbar() { snackbarFlow.value = null }

    private fun currentHourMinute(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
    }

    class Factory(
        private val appContext: Context,
        private val plansRepository: PlansRepository,
        private val naamJapRepository: NaamJapRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlansViewModel(appContext.applicationContext, plansRepository, naamJapRepository) as T
        }
    }
}
