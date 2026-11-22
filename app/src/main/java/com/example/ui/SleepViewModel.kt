package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bedtime.BedtimeGuardService
import com.example.bedtime.BedtimeLockActivity
import com.example.bedtime.BedtimePermissionsHelper
import com.example.bedtime.BedtimePermissionsStatus
import com.example.bedtime.BedtimeScheduler
import com.example.data.ActiveSleepSessionEntity
import com.example.data.SleepAnalyzer
import com.example.data.SleepCycleAnalysis
import com.example.data.SleepDayEntity
import com.example.data.SleepGoalEntity
import com.example.data.SleepRepository
import com.example.data.WEEK_DAY_SLOTS
import com.example.data.WeeklySleepSummary
import com.example.data.currentMondayZeroDayIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

enum class ClockMode {
    CLOCK_24H,
    SLEEP_TIMER
}

data class LiveClockState(
    val hour24: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
    val dayOfWeekIndex: Int = 0
) {
    val hhmm: String
        get() = String.format(Locale.US, "%02d:%02d", hour24, minute)

    val ss: String
        get() = String.format(Locale.US, "%02d", second)

    val dayProgressFraction: Float
        get() = ((hour24 * 3600) + (minute * 60) + second) / 86400f
}

data class SleepTimerDisplay(
    val mode: ClockMode = ClockMode.CLOCK_24H,
    val isBedtimeActive: Boolean = false,
    val isAutoTriggered: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val blockedAttemptsCount: Int = 0,
    val screenWakeCount: Int = 0,
    val lastBlockedAppName: String = ""
) {
    val formattedHHMM: String
        get() {
            val h = elapsedSeconds / 3600
            val m = (elapsedSeconds % 3600) / 60
            return String.format(Locale.US, "%02d:%02d", h, m)
        }

    val formattedSS: String
        get() = String.format(Locale.US, "%02d", elapsedSeconds % 60)
}

data class SleepUiState(
    val liveClock: LiveClockState = LiveClockState(),
    val timerDisplay: SleepTimerDisplay = SleepTimerDisplay(),
    val recordedDaysByIndex: Map<Int, SleepDayEntity> = emptyMap(),
    val selectedDayIndex: Int = currentMondayZeroDayIndex(),
    val goal: SleepGoalEntity = SleepGoalEntity(),
    val selectedAnalysis: SleepCycleAnalysis? = null,
    val weeklySummary: WeeklySleepSummary = WeeklySleepSummary(0, 0, null, null, 0, 0, 0, 0, 0, 0, 0),
    val bedtimeCountdownText: String = "",
    val isCurrentlyInScheduledWindow: Boolean = false,
    val permissions: BedtimePermissionsStatus = BedtimePermissionsStatus(),
    val showGoalSheet: Boolean = false,
    val showEditDaySheet: Boolean = false,
    val showProtectionSheet: Boolean = false,
    val snackbarMessage: String? = null
) {
    val selectedSlotName: String
        get() = WEEK_DAY_SLOTS.getOrNull(selectedDayIndex)?.dayFull ?: "Today"
}

class SleepViewModel(
    private val appContext: Context,
    private val repository: SleepRepository
) : ViewModel() {

    private val liveClockFlow = MutableStateFlow(readCurrentClock())
    private val clockModeFlow = MutableStateFlow(ClockMode.CLOCK_24H)
    private val selectedDayIndexFlow = MutableStateFlow(currentMondayZeroDayIndex())
    private val permissionsFlow = MutableStateFlow(BedtimePermissionsHelper.checkStatus(appContext))
    private val showGoalSheetFlow = MutableStateFlow(false)
    private val showEditDaySheetFlow = MutableStateFlow(false)
    private val showProtectionSheetFlow = MutableStateFlow(false)
    private val snackbarMessageFlow = MutableStateFlow<String?>(null)

    private data class UiFlags(
        val clockMode: ClockMode,
        val selectedDayIndex: Int,
        val showGoalSheet: Boolean,
        val showEditDaySheet: Boolean,
        val showProtectionSheet: Boolean,
        val snackbarMessage: String?
    )

    private val uiFlagsFlow = combine(
        clockModeFlow,
        selectedDayIndexFlow,
        showGoalSheetFlow,
        showEditDaySheetFlow,
        showProtectionSheetFlow
    ) { mode, selectedIdx, goalSheet, editSheet, protSheet ->
        UiFlags(mode, selectedIdx, goalSheet, editSheet, protSheet, null)
    }.combine(snackbarMessageFlow) { flags, msg ->
        flags.copy(snackbarMessage = msg)
    }

    private data class SystemSnapshot(
        val clock: LiveClockState,
        val activeSession: ActiveSleepSessionEntity,
        val permissions: BedtimePermissionsStatus
    )

    private val systemSnapshotFlow = combine(
        liveClockFlow,
        repository.activeSession,
        permissionsFlow
    ) { clock, activeSession, permissions ->
        SystemSnapshot(clock, activeSession, permissions)
    }

    val uiState: StateFlow<SleepUiState> = combine(
        repository.recordedSleepDays,
        repository.sleepGoal,
        systemSnapshotFlow,
        uiFlagsFlow
    ) { days, goal, sys, flags ->
        val dayMap = days.associateBy { it.dayIndex }
        val safeIdx = flags.selectedDayIndex.coerceIn(0, 6)
        val selectedDay = dayMap[safeIdx]
        val analysis = selectedDay?.let { SleepAnalyzer.analyzeDay(it, goal) }
        val summary = SleepAnalyzer.summarizeWeek(days, goal)
        val inWindow = goal.isTimeWithinBedtimeWindow(sys.clock.hour24, sys.clock.minute)

        val elapsedSec = if (sys.activeSession.isActive && sys.activeSession.startEpochMillis > 0L) {
            ((System.currentTimeMillis() - sys.activeSession.startEpochMillis) / 1000L).coerceAtLeast(0L)
        } else {
            0L
        }

        val effectiveMode = if (sys.activeSession.isActive) ClockMode.SLEEP_TIMER else flags.clockMode

        val timerDisplay = SleepTimerDisplay(
            mode = effectiveMode,
            isBedtimeActive = sys.activeSession.isActive,
            isAutoTriggered = sys.activeSession.isAutoTriggered,
            elapsedSeconds = elapsedSec,
            blockedAttemptsCount = sys.activeSession.blockedAttemptsCount,
            screenWakeCount = sys.activeSession.screenWakeCount,
            lastBlockedAppName = sys.activeSession.lastBlockedAppName
        )

        val countdown = if (sys.activeSession.isActive) {
            "Wake-up at ${goal.targetWakeFormatted}"
        } else {
            computeCountdownToBedtime(
                nowHour = sys.clock.hour24,
                nowMin = sys.clock.minute,
                targetBedHour = goal.targetBedtimeHour,
                targetBedMin = goal.targetBedtimeMinute
            )
        }

        SleepUiState(
            liveClock = sys.clock,
            timerDisplay = timerDisplay,
            recordedDaysByIndex = dayMap,
            selectedDayIndex = safeIdx,
            goal = goal,
            selectedAnalysis = analysis,
            weeklySummary = summary,
            bedtimeCountdownText = countdown,
            isCurrentlyInScheduledWindow = inWindow,
            permissions = sys.permissions,
            showGoalSheet = flags.showGoalSheet,
            showEditDaySheet = flags.showEditDaySheet,
            showProtectionSheet = flags.showProtectionSheet,
            snackbarMessage = flags.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SleepUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureGoalInitialized()
            val goal = repository.getGoalOnce()
            BedtimeScheduler.syncSchedule(appContext, goal)
        }
        viewModelScope.launch {
            var tickCounter = 0
            while (isActive) {
                liveClockFlow.value = readCurrentClock()
                tickCounter++
                // Refresh battery & system status periodically every 15 seconds while foregrounded
                if (tickCounter % 15 == 0) {
                    permissionsFlow.value = BedtimePermissionsHelper.checkStatus(appContext)
                }
                delay(1000L)
            }
        }
    }

    fun refreshSystemStatus() {
        permissionsFlow.value = BedtimePermissionsHelper.checkStatus(appContext)
    }

    fun selectClockMode(mode: ClockMode) {
        clockModeFlow.value = mode
    }

    fun toggleBedtimeGuard() {
        val active = uiState.value.timerDisplay.isBedtimeActive
        if (!active) {
            clockModeFlow.value = ClockMode.SLEEP_TIMER
            BedtimeGuardService.startGuard(appContext, isAutoTriggered = false)
            snackbarMessageFlow.value = "Bedtime Guard active: silencing calls/SMS & blocking distractions"
        } else {
            BedtimeGuardService.stopGuard(appContext)
            clockModeFlow.value = ClockMode.CLOCK_24H
            snackbarMessageFlow.value = "Bedtime ended · Real sleep session saved"
        }
    }

    fun previewRedForegroundBlocker() {
        val goal = uiState.value.goal
        val intent = Intent(appContext, BedtimeLockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(BedtimeLockActivity.EXTRA_BLOCKED_APP, "Social / Video App")
            putExtra(
                BedtimeLockActivity.EXTRA_SLEEP_WINDOW,
                "${goal.targetBedtimeFormatted} - ${goal.targetWakeFormatted}"
            )
            putExtra(
                BedtimeLockActivity.EXTRA_BLOCKED_COUNT,
                uiState.value.timerDisplay.blockedAttemptsCount.coerceAtLeast(1)
            )
            putExtra(BedtimeLockActivity.EXTRA_IS_PREVIEW, true)
        }
        appContext.startActivity(intent)
    }

    fun selectDay(dayIndex: Int) {
        selectedDayIndexFlow.value = dayIndex.coerceIn(0, 6)
    }

    fun openGoalSheet() {
        showGoalSheetFlow.value = true
    }

    fun dismissGoalSheet() {
        showGoalSheetFlow.value = false
    }

    fun openEditDaySheet() {
        showEditDaySheetFlow.value = true
    }

    fun dismissEditDaySheet() {
        showEditDaySheetFlow.value = false
    }

    fun openProtectionSheet() {
        refreshSystemStatus()
        showProtectionSheetFlow.value = true
    }

    fun dismissProtectionSheet() {
        showProtectionSheetFlow.value = false
    }

    fun clearSnackbar() {
        snackbarMessageFlow.value = null
    }

    fun saveSleepGoal(
        bedHour: Int,
        bedMinute: Int,
        wakeHour: Int,
        wakeMinute: Int,
        autoBedtimeEnabled: Boolean,
        silenceCallsAndSms: Boolean,
        blockAppsInBedtime: Boolean,
        batterySaverAssist: Boolean
    ) {
        viewModelScope.launch {
            val savedGoal = repository.saveSleepGoal(
                targetBedtimeHour = bedHour,
                targetBedtimeMinute = bedMinute,
                targetWakeHour = wakeHour,
                targetWakeMinute = wakeMinute,
                autoBedtimeEnabled = autoBedtimeEnabled,
                silenceCallsAndSms = silenceCallsAndSms,
                blockAppsInBedtime = blockAppsInBedtime,
                batterySaverAssist = batterySaverAssist
            )
            BedtimeScheduler.syncSchedule(appContext, savedGoal)

            // If user just enabled Auto Bedtime and we are ALREADY inside that sleep window, activate immediately!
            val now = readCurrentClock()
            val alreadyActive = repository.getActiveSessionOnce().isActive
            if (autoBedtimeEnabled && !alreadyActive && savedGoal.isTimeWithinBedtimeWindow(now.hour24, now.minute)) {
                BedtimeGuardService.startGuard(appContext, isAutoTriggered = true)
                snackbarMessageFlow.value = "Inside ${savedGoal.targetBedtimeFormatted}-${savedGoal.targetWakeFormatted} · Auto Bedtime activated"
            } else if (autoBedtimeEnabled) {
                snackbarMessageFlow.value = "Auto Bedtime armed for ${savedGoal.targetBedtimeFormatted} - ${savedGoal.targetWakeFormatted}"
            } else {
                snackbarMessageFlow.value = "Sleep schedule saved (Auto Bedtime paused)"
            }
            showGoalSheetFlow.value = false
        }
    }

    fun saveManualDaySleep(
        bedHour: Int,
        bedMinute: Int,
        wakeHour: Int,
        wakeMinute: Int,
        deepPercent: Int,
        remPercent: Int,
        awakeMinutes: Int
    ) {
        val dayIdx = uiState.value.selectedDayIndex
        val dayName = WEEK_DAY_SLOTS[dayIdx].dayFull
        viewModelScope.launch {
            repository.saveManualDaySleep(
                dayIndex = dayIdx,
                bedtimeHour = bedHour,
                bedtimeMinute = bedMinute,
                wakeHour = wakeHour,
                wakeMinute = wakeMinute,
                deepPercent = deepPercent,
                remPercent = remPercent,
                awakeMinutes = awakeMinutes
            )
            showEditDaySheetFlow.value = false
            snackbarMessageFlow.value = "Saved real sleep log for $dayName"
        }
    }

    fun deleteSelectedDaySleep() {
        val dayIdx = uiState.value.selectedDayIndex
        val dayName = WEEK_DAY_SLOTS[dayIdx].dayFull
        viewModelScope.launch {
            repository.deleteDaySleep(dayIdx)
            showEditDaySheetFlow.value = false
            snackbarMessageFlow.value = "Cleared $dayName sleep entry"
        }
    }

    private fun readCurrentClock(): LiveClockState {
        val cal = Calendar.getInstance()
        return LiveClockState(
            hour24 = cal.get(Calendar.HOUR_OF_DAY),
            minute = cal.get(Calendar.MINUTE),
            second = cal.get(Calendar.SECOND),
            dayOfWeekIndex = currentMondayZeroDayIndex()
        )
    }

    private fun computeCountdownToBedtime(
        nowHour: Int,
        nowMin: Int,
        targetBedHour: Int,
        targetBedMin: Int
    ): String {
        val nowTotal = nowHour * 60 + nowMin
        val bedTotal = targetBedHour * 60 + targetBedMin
        val diff = if (bedTotal >= nowTotal) bedTotal - nowTotal else (24 * 60 - nowTotal) + bedTotal
        val h = diff / 60
        val m = diff % 60
        return if (diff == 0) "Bedtime starting now" else "Auto Bedtime in ${h}h ${m}m"
    }

    class Factory(
        private val appContext: Context,
        private val repository: SleepRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepViewModel(appContext.applicationContext, repository) as T
        }
    }
}
