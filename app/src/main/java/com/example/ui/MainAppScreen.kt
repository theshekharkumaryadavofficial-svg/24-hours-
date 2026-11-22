package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.home.HomeDashboardScreen
import com.example.ui.naamjap.NaamJapFullScreenFocus
import com.example.ui.naamjap.NaamJapGoalBottomSheet
import com.example.ui.naamjap.NaamJapTabContent
import com.example.ui.naamjap.RadhaGameModeScreen
import com.example.ui.naamshakti.NaamShaktiRootScreen
import com.example.ui.naamshakti.formatShaktiNumber
import com.example.ui.plans.AddEditPlanBlockSheet
import com.example.ui.plans.DisciplineReminderSheet
import com.example.ui.plans.PlansTabContent
import com.example.ui.plans.SmartFullDayScheduleSheet
import com.example.ui.plans.StudyModeConfigSheet
import com.example.ui.theme.CrimsonDeep
import com.example.ui.theme.CrimsonObsidian
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.JetBlack
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite

enum class AppTab(val label: String, val icon: ImageVector, val testTag: String) {
    HOME("Home", Icons.Rounded.Home, "nav_tab_home"),
    SLEEP("Sleep", Icons.Rounded.Bedtime, "nav_tab_sleep"),
    NAAM_JAP("Naam Jap", Icons.Rounded.AutoAwesome, "nav_tab_naam_jap"),
    PLANS("Plans", Icons.Rounded.EventNote, "nav_tab_plans")
}

@Composable
fun MainAppScreen(
    sleepViewModel: SleepViewModel,
    naamJapViewModel: NaamJapViewModel,
    plansViewModel: PlansViewModel,
    naamShaktiViewModel: NaamShaktiViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    val sleepState by sleepViewModel.uiState.collectAsStateWithLifecycle()
    val naamJapState by naamJapViewModel.uiState.collectAsStateWithLifecycle()
    val plansState by plansViewModel.uiState.collectAsStateWithLifecycle()
    val naamShaktiState by naamShaktiViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        sleepViewModel.refreshSystemStatus()
        plansViewModel.refreshPermissions()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        sleepViewModel.refreshSystemStatus()
        plansViewModel.refreshPermissions()
    }

    BackHandler(enabled = selectedTab != AppTab.HOME && naamJapState.overlayMode == NaamJapOverlayMode.NONE) {
        selectedTab = AppTab.HOME
    }

    LaunchedEffect(naamJapState.snackbarMessage) {
        val msg = naamJapState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            naamJapViewModel.clearSnackbar()
        }
    }

    LaunchedEffect(plansState.snackbarMessage) {
        val msg = plansState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            plansViewModel.clearSnackbar()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CrimsonPrimary, CrimsonDeep, CrimsonObsidian)
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = JetBlack,
                        contentColor = PureWhite,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            },
            bottomBar = {
                if (naamJapState.overlayMode == NaamJapOverlayMode.NONE) {
                    RedBlackBottomNavBar(
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it }
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "mainTabTransition"
            ) { tab ->
                when (tab) {
                    AppTab.HOME -> {
                        HomeDashboardScreen(
                            sleepState = sleepState,
                            naamJapState = naamJapState,
                            plansState = plansState,
                            onQuickRadhaTap = naamJapViewModel::incrementCount,
                            onOpenFullScreenJap = naamJapViewModel::openFullScreenFocusMode,
                            onOpenRadhaGame = naamJapViewModel::openRadhaGameMode,
                            onToggleSlotGuardNow = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !plansState.permissions.hasNotificationPermission
                                ) {
                                    notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                plansViewModel.toggleStudyModeNow()
                            },
                            onPreviewNaamJapBlocker = plansViewModel::previewStudyRedLock,
                            onAddWaterQuick = plansViewModel::addWater,
                            onOpenAddSlot = plansViewModel::openAddBlockSheet,
                            onEditSlot = plansViewModel::openEditBlockSheet,
                            onToggleSlotDone = plansViewModel::toggleBlockDone,
                            onOpenPermissionsSheet = {
                                selectedTab = AppTab.SLEEP
                                sleepViewModel.openProtectionSheet()
                            },
                            onNavigateTab = { selectedTab = it }
                        )
                    }
                    AppTab.SLEEP -> {
                        SleepDashboardScreen(
                            viewModel = sleepViewModel,
                            embeddedInTabScaffold = true
                        )
                    }
                    AppTab.NAAM_JAP -> {
                        val shaktiBadge = when {
                            naamShaktiState.profile.koRecoveryActive -> "KO · REVIVE"
                            naamShaktiState.profile.unlockedBattlefield ->
                                "${formatShaktiNumber(naamShaktiState.profile.currentShaktiEnergy)} ⚡"
                            else -> "${formatShaktiNumber(naamShaktiState.unlockProgressCount)}/10K"
                        }
                        NaamJapTabContent(
                            uiState = naamJapState,
                            naamShaktiBadgeText = shaktiBadge,
                            onQuickIncrement = naamJapViewModel::incrementCount,
                            onUndoDecrement = naamJapViewModel::decrementCount,
                            onOpenFocusMode = naamJapViewModel::openFullScreenFocusMode,
                            onOpenGameMode = naamJapViewModel::openRadhaGameMode,
                            onOpenNaamShaktiMode = naamJapViewModel::openNaamShaktiMode,
                            onOpenGoalSheet = naamJapViewModel::openGoalSheet
                        )
                    }
                    AppTab.PLANS -> {
                        PlansTabContent(
                            uiState = plansState,
                            onToggleStudyModeNow = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !plansState.permissions.hasNotificationPermission
                                ) {
                                    notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                plansViewModel.toggleStudyModeNow()
                            },
                            onOpenStudyConfig = plansViewModel::openStudyConfigSheet,
                            onPreviewStudyRedLock = plansViewModel::previewStudyRedLock,
                            onAddWater = plansViewModel::addWater,
                            onSetLightMeals = plansViewModel::setLightMealsLogged,
                            onToggle10MinReminder = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !plansState.permissions.hasNotificationPermission
                                ) {
                                    notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                plansViewModel.toggle10MinReminderQuick(enabled)
                            },
                            onOpenReminderSheet = plansViewModel::openReminderSheet,
                            onSendTestReminder = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !plansState.permissions.hasNotificationPermission
                                ) {
                                    notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                plansViewModel.sendTestDisciplineReminder()
                            },
                            onOpenSmartSchedule = plansViewModel::openSmartScheduleSheet,
                            onOpenAddBlock = plansViewModel::openAddBlockSheet,
                            onToggleBlockDone = plansViewModel::toggleBlockDone,
                            onEditBlock = plansViewModel::openEditBlockSheet,
                            onOpenPermissionsHelp = {
                                selectedTab = AppTab.SLEEP
                                sleepViewModel.openProtectionSheet()
                            }
                        )
                    }
                }
            }
        }

        // Naam Jap Goal Bottom Sheet
        if (naamJapState.showGoalSheet) {
            NaamJapGoalBottomSheet(
                currentGoal = naamJapState.goal,
                onDismiss = naamJapViewModel::dismissGoalSheet,
                onSave = naamJapViewModel::saveGoal
            )
        }

        // Plans Bottom Sheets (accessible from both Home & Plans tabs!)
        if (plansState.showStudyConfigSheet) {
            StudyModeConfigSheet(
                current = plansState.studyConfig,
                onDismiss = plansViewModel::dismissStudyConfigSheet,
                onSave = plansViewModel::saveStudyConfig
            )
        }
        if (plansState.showReminderSheet) {
            DisciplineReminderSheet(
                current = plansState.disciplineConfig,
                onDismiss = plansViewModel::dismissReminderSheet,
                onSave = plansViewModel::saveDisciplineSettings
            )
        }
        if (plansState.showSmartScheduleSheet) {
            SmartFullDayScheduleSheet(
                currentStudy = plansState.studyConfig,
                onDismiss = plansViewModel::dismissSmartScheduleSheet,
                onGenerate = plansViewModel::generateSmartFullDaySchedule
            )
        }
        if (plansState.showAddBlockSheet) {
            AddEditPlanBlockSheet(
                existing = plansState.editingBlock,
                installedApps = plansState.installedApps,
                isLoadingApps = plansState.isLoadingApps,
                onDismiss = plansViewModel::dismissBlockSheet,
                onDelete = plansViewModel::deletePlanBlock,
                onSave = plansViewModel::savePlanBlock
            )
        }

        // Full-Screen Naam Jap Mode OR Radha Gameplay Mode
        when (naamJapState.overlayMode) {
            NaamJapOverlayMode.FULLSCREEN_FOCUS -> {
                NaamJapFullScreenFocus(
                    uiState = naamJapState,
                    onTapIncrement = naamJapViewModel::incrementCount,
                    onUndoDecrement = naamJapViewModel::decrementCount,
                    onExit = naamJapViewModel::exitOverlayMode
                )
            }
            NaamJapOverlayMode.RADHA_GAME -> {
                RadhaGameModeScreen(
                    uiState = naamJapState,
                    onOrbCaught = naamJapViewModel::incrementCount,
                    onExit = naamJapViewModel::exitOverlayMode
                )
            }
            NaamJapOverlayMode.NAAM_SHAKTI -> {
                NaamShaktiRootScreen(
                    uiState = naamShaktiState,
                    viewModel = naamShaktiViewModel,
                    onRealNaamJapIncrement = naamJapViewModel::incrementCount,
                    onOpenFullScreenJap = naamJapViewModel::openFullScreenFocusMode,
                    onExitToNaamJapTab = naamJapViewModel::exitOverlayMode
                )
            }
            NaamJapOverlayMode.NONE -> Unit
        }
    }
}

@Composable
private fun RedBlackBottomNavBar(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit
) {
    Surface(
        color = ObsidianCard,
        border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.55f)),
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) CrimsonPrimary else PureWhite.copy(alpha = 0.06f),
                    animationSpec = tween(220),
                    label = "tabBg_${tab.name}"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) PureWhite else SubtleWhite,
                    animationSpec = tween(220),
                    label = "tabFg_${tab.name}"
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTab(tab) }
                        .testTag(tab.testTag),
                    shape = RoundedCornerShape(16.dp),
                    color = containerColor,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) PureWhite.copy(alpha = 0.42f) else PureWhite.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = contentColor,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
