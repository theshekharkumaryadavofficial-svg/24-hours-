package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.bedtime.BedtimePermissionsHelper
import com.example.ui.components.BedtimeGuardPanel
import com.example.ui.components.EditSleepDayBottomSheet
import com.example.ui.components.HeroClockTimerSection
import com.example.ui.components.ProtectionSetupBottomSheet
import com.example.ui.components.SleepGoalBottomSheet
import com.example.ui.components.SleepReviewSection
import com.example.ui.components.WeeklySleepGraphSection
import com.example.ui.theme.CrimsonDeep
import com.example.ui.theme.CrimsonObsidian
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.JetBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite

@Composable
fun SleepDashboardScreen(
    viewModel: SleepViewModel,
    embeddedInTabScaffold: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Runtime notification permission launcher (Android 13+)
    val notificationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshSystemStatus()
    }

    // Automatically re-check DND, Usage Access, Overlay, and Battery status whenever returning from System Settings
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshSystemStatus()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (embeddedInTabScaffold) Modifier
                else Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CrimsonPrimary,
                            CrimsonDeep,
                            CrimsonObsidian
                        )
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.navigationBarsPadding()
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = JetBlack,
                        contentColor = PureWhite,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    item(key = "header") {
                        TopHeaderBar(
                            isBedtimeActive = uiState.timerDisplay.isBedtimeActive,
                            onOpenGoalSheet = viewModel::openGoalSheet,
                            onOpenProtectionSheet = viewModel::openProtectionSheet
                        )
                    }

                    // 1. TOP MIDDLE: Large 24-Hour Clock Timer on Red Background
                    item(key = "hero_clock") {
                        HeroClockTimerSection(
                            liveClock = uiState.liveClock,
                            timerDisplay = uiState.timerDisplay,
                            weeklySummary = uiState.weeklySummary,
                            goal = uiState.goal,
                            bedtimeCountdownText = uiState.bedtimeCountdownText,
                            onSelectMode = viewModel::selectClockMode,
                            onToggleBedtimeGuard = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !uiState.permissions.hasNotificationPermission
                                ) {
                                    notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                viewModel.toggleBedtimeGuard()
                            },
                            onOpenGoalSheet = viewModel::openGoalSheet
                        )
                    }

                    // 2. REAL BEDTIME GUARD ENGINE: DND Silence, App Blocker, Red Overlay & Battery Telemetry
                    item(key = "bedtime_guard_panel") {
                        BedtimeGuardPanel(
                            goal = uiState.goal,
                            timerDisplay = uiState.timerDisplay,
                            permissions = uiState.permissions,
                            onOpenProtectionSheet = viewModel::openProtectionSheet,
                            onOpenBatterySaver = { BedtimePermissionsHelper.openBatterySaverSettings(context) },
                            onPreviewRedBlocker = viewModel::previewRedForegroundBlocker
                        )
                    }

                    // 3. MIDDLE: Real Sleep Time Review, Cycle Analysis & Daily Goal Progress
                    item(key = "sleep_review") {
                        SleepReviewSection(
                            analysis = uiState.selectedAnalysis,
                            selectedDayName = uiState.selectedSlotName,
                            goal = uiState.goal,
                            isBedtimeActive = uiState.timerDisplay.isBedtimeActive,
                            onStartBedtime = viewModel::toggleBedtimeGuard,
                            onEditDayClick = viewModel::openEditDaySheet,
                            onEditGoalClick = viewModel::openGoalSheet
                        )
                    }

                    // 4. BOTTOM: Full Week Real Sleep Routine Review Graph
                    item(key = "weekly_graph") {
                        WeeklySleepGraphSection(
                            recordedDaysByIndex = uiState.recordedDaysByIndex,
                            selectedDayIndex = uiState.selectedDayIndex,
                            goal = uiState.goal,
                            weeklySummary = uiState.weeklySummary,
                            onSelectDay = viewModel::selectDay
                        )
                    }

                    if (!embeddedInTabScaffold) {
                        item(key = "bottom_inset") {
                            Spacer(modifier = Modifier.navigationBarsPadding())
                        }
                    }
                }
            }
        }

        // Modal Sheet 1: Auto Bedtime Schedule & Sleep Goal
        if (uiState.showGoalSheet) {
            SleepGoalBottomSheet(
                currentGoal = uiState.goal,
                onDismiss = viewModel::dismissGoalSheet,
                onSave = viewModel::saveSleepGoal
            )
        }

        // Modal Sheet 2: Real Android Permissions & Protection Setup
        if (uiState.showProtectionSheet) {
            ProtectionSetupBottomSheet(
                permissions = uiState.permissions,
                onDismiss = viewModel::dismissProtectionSheet,
                onRequestNotifications = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onOpenDndSettings = { BedtimePermissionsHelper.openDndAccessSettings(context) },
                onOpenUsageSettings = { BedtimePermissionsHelper.openUsageAccessSettings(context) },
                onOpenOverlaySettings = { BedtimePermissionsHelper.openOverlaySettings(context) },
                onOpenExactAlarmSettings = { BedtimePermissionsHelper.openExactAlarmSettings(context) },
                onOpenBatterySaverSettings = { BedtimePermissionsHelper.openBatterySaverSettings(context) }
            )
        }

        // Modal Sheet 3: Log / Edit Real Sleep Entry
        if (uiState.showEditDaySheet) {
            val existing = uiState.recordedDaysByIndex[uiState.selectedDayIndex]
            EditSleepDayBottomSheet(
                dayFullName = uiState.selectedSlotName,
                existingDay = existing,
                defaultGoal = uiState.goal,
                onDismiss = viewModel::dismissEditDaySheet,
                onDelete = if (existing != null) viewModel::deleteSelectedDaySleep else null,
                onSave = viewModel::saveManualDaySleep
            )
        }
    }
}

@Composable
private fun TopHeaderBar(
    isBedtimeActive: Boolean,
    onOpenGoalSheet: () -> Unit,
    onOpenProtectionSheet: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = JetBlack,
                border = BorderStroke(1.2.dp, PureWhite.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.brand_badge),
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isBedtimeActive) JetBlack else PureWhite
                    ) {
                        Text(
                            text = if (isBedtimeActive) "LOCKED" else "REAL DATA",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBedtimeActive) PureWhite else JetBlack,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Auto Bedtime · DND · App Blocker",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite,
                    fontSize = 11.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = JetBlack,
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f)),
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenGoalSheet)
                    .testTag("header_set_goal_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.labelMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = PureWhite
            ) {
                IconButton(
                    onClick = onOpenProtectionSheet,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("header_guard_setup_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = "Configure Bedtime Guard Permissions",
                        tint = JetBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
