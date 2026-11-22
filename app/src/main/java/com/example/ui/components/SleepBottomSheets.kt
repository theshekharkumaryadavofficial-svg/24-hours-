package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bedtime.BedtimePermissionsStatus
import com.example.data.SleepDayEntity
import com.example.data.SleepGoalEntity
import com.example.data.computeDurationMinutesAcrossMidnight
import com.example.data.format24h
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LightTrack
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PureWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepGoalBottomSheet(
    currentGoal: SleepGoalEntity,
    onDismiss: () -> Unit,
    onSave: (
        bedHour: Int,
        bedMinute: Int,
        wakeHour: Int,
        wakeMinute: Int,
        autoBedtimeEnabled: Boolean,
        silenceCallsAndSms: Boolean,
        blockAppsInBedtime: Boolean,
        batterySaverAssist: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var bedHour by remember(currentGoal) { mutableIntStateOf(currentGoal.targetBedtimeHour) }
    var bedMinute by remember(currentGoal) { mutableIntStateOf(currentGoal.targetBedtimeMinute) }
    var wakeHour by remember(currentGoal) { mutableIntStateOf(currentGoal.targetWakeHour) }
    var wakeMinute by remember(currentGoal) { mutableIntStateOf(currentGoal.targetWakeMinute) }
    var autoEnabled by remember(currentGoal) { mutableStateOf(currentGoal.autoBedtimeEnabled) }
    var silenceDnd by remember(currentGoal) { mutableStateOf(currentGoal.silenceCallsAndSms) }
    var blockApps by remember(currentGoal) { mutableStateOf(currentGoal.blockAppsInBedtime) }
    var batteryAssist by remember(currentGoal) { mutableStateOf(currentGoal.batterySaverAssist) }

    val calculatedMins = computeDurationMinutesAcrossMidnight(bedHour, bedMinute, wakeHour, wakeMinute)
    val durationLabel = "${calculatedMins / 60}h ${calculatedMins % 60}m"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        contentColor = JetBlack,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = CircleShape, color = CrimsonPrimary) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Auto Bedtime & Sleep Goal",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JetBlack
                    )
                    Text(
                        text = "Set your sleep window — guard activates automatically",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Schedule Presets (including user's 02:00 - 07:00 window!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GoalPresetChip(
                    label = "02:00-07:00",
                    sub = "5h Night Owl",
                    selected = bedHour == 2 && bedMinute == 0 && wakeHour == 7 && wakeMinute == 0,
                    onClick = { bedHour = 2; bedMinute = 0; wakeHour = 7; wakeMinute = 0 },
                    modifier = Modifier.weight(1f)
                )
                GoalPresetChip(
                    label = "23:00-07:00",
                    sub = "8h Standard",
                    selected = bedHour == 23 && bedMinute == 0 && wakeHour == 7 && wakeMinute == 0,
                    onClick = { bedHour = 23; bedMinute = 0; wakeHour = 7; wakeMinute = 0 },
                    modifier = Modifier.weight(1f)
                )
                GoalPresetChip(
                    label = "00:00-07:30",
                    sub = "7.5h Late",
                    selected = bedHour == 0 && bedMinute == 0 && wakeHour == 7 && wakeMinute == 30,
                    onClick = { bedHour = 0; bedMinute = 0; wakeHour = 7; wakeMinute = 30 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TimeStepperRow(
                icon = Icons.Rounded.NightsStay,
                title = "Bedtime Start",
                timeText = format24h(bedHour, bedMinute),
                onDecrease = {
                    val total = (bedHour * 60 + bedMinute - 15 + 1440) % 1440
                    bedHour = total / 60
                    bedMinute = total % 60
                },
                onIncrease = {
                    val total = (bedHour * 60 + bedMinute + 15) % 1440
                    bedHour = total / 60
                    bedMinute = total % 60
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            TimeStepperRow(
                icon = Icons.Rounded.WbSunny,
                title = "Wake-Up End",
                timeText = format24h(wakeHour, wakeMinute),
                onDecrease = {
                    val total = (wakeHour * 60 + wakeMinute - 15 + 1440) % 1440
                    wakeHour = total / 60
                    wakeMinute = total % 60
                },
                onIncrease = {
                    val total = (wakeHour * 60 + wakeMinute + 15) % 1440
                    wakeHour = total / 60
                    wakeMinute = total % 60
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CrimsonSoftTint,
                border = BorderStroke(1.dp, CrimsonBorderLight)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TARGET SLEEP WINDOW",
                        style = MaterialTheme.typography.labelMedium,
                        color = JetBlack
                    )
                    Text(
                        text = durationLabel,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = CrimsonPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Automation Toggles
            AutomationToggleRow(
                title = "Auto Bedtime Mode",
                subtitle = "Automatically turns ON at ${format24h(bedHour, bedMinute)} and OFF at ${format24h(wakeHour, wakeMinute)}",
                checked = autoEnabled,
                onCheckedChange = { autoEnabled = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AutomationToggleRow(
                title = "Silence Calls & SMS (DND)",
                subtitle = "Mutes incoming calls, SMS & notifications while allowing wake alarms",
                checked = silenceDnd,
                onCheckedChange = { silenceDnd = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AutomationToggleRow(
                title = "Block Apps + Red Foreground",
                subtitle = "Blocks distracting apps opened during sleep with full-screen red lock",
                checked = blockApps,
                onCheckedChange = { blockApps = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            AutomationToggleRow(
                title = "Low-Power Battery Guard",
                subtitle = "Zero polling on screen-off & dimmed red lock to preserve overnight battery",
                checked = batteryAssist,
                onCheckedChange = { batteryAssist = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = JetBlack
                    )
                }
                Button(
                    onClick = {
                        onSave(
                            bedHour,
                            bedMinute,
                            wakeHour,
                            wakeMinute,
                            autoEnabled,
                            silenceDnd,
                            blockApps,
                            batteryAssist
                        )
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("save_goal_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "SAVE & ARM GUARD",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectionSetupBottomSheet(
    permissions: BedtimePermissionsStatus,
    onDismiss: () -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenDndSettings: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onOpenBatterySaverSettings: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        contentColor = JetBlack,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = CircleShape, color = JetBlack) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Bedtime Guard Permissions",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JetBlack
                    )
                    Text(
                        text = "Grant real Android system access for automatic sleep protection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PermissionActionCard(
                icon = Icons.Rounded.DoNotDisturbOn,
                title = "Silence Calls & SMS (Do Not Disturb)",
                description = "Allows 24 Hrs to automatically mute incoming calls, SMS & pings during your sleep window.",
                isGranted = permissions.hasDndAccess,
                actionLabel = "ENABLE DND",
                onAction = onOpenDndSettings
            )
            Spacer(modifier = Modifier.height(10.dp))

            PermissionActionCard(
                icon = Icons.Rounded.Block,
                title = "Detect Opened Apps (Usage Access)",
                description = "Detects if you open distracting apps during bedtime (paused automatically when screen is off).",
                isGranted = permissions.hasUsageStatsAccess,
                actionLabel = "ALLOW USAGE",
                onAction = onOpenUsageSettings
            )
            Spacer(modifier = Modifier.height(10.dp))

            PermissionActionCard(
                icon = Icons.Rounded.Layers,
                title = "Red Foreground Blocker (Overlay)",
                description = "Displays the full-screen crimson red bedtime lock screen immediately over blocked apps.",
                isGranted = permissions.hasOverlayPermission,
                actionLabel = "ALLOW OVERLAY",
                onAction = onOpenOverlaySettings
            )
            Spacer(modifier = Modifier.height(10.dp))

            PermissionActionCard(
                icon = Icons.Rounded.NotificationsActive,
                title = "Guard Status Notification",
                description = "Keeps the low-power Bedtime Guard service active reliably overnight.",
                isGranted = permissions.hasNotificationPermission,
                actionLabel = "ALLOW",
                onAction = onRequestNotifications
            )
            Spacer(modifier = Modifier.height(10.dp))

            PermissionActionCard(
                icon = Icons.Rounded.Alarm,
                title = "Exact Auto Bedtime Alarms",
                description = "Triggers Bedtime Mode on the exact minute you scheduled (e.g. 02:00 AM - 07:00 AM).",
                isGranted = permissions.hasExactAlarmPermission,
                actionLabel = "ALLOW EXACT",
                onAction = onOpenExactAlarmSettings
            )
            Spacer(modifier = Modifier.height(10.dp))

            PermissionActionCard(
                icon = Icons.Rounded.BatterySaver,
                title = "Android Battery Saver",
                description = if (permissions.isPowerSaveMode) {
                    "System Battery Saver is ON (${permissions.batteryPercent}% battery)."
                } else {
                    "Pair with Android Battery Saver overnight for extra battery life (${permissions.batteryPercent}% battery)."
                },
                isGranted = permissions.isPowerSaveMode,
                actionLabel = "OPEN SAVER",
                onAction = onOpenBatterySaverSettings
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JetBlack,
                    contentColor = PureWhite
                )
            ) {
                Text(
                    text = "DONE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSleepDayBottomSheet(
    dayFullName: String,
    existingDay: SleepDayEntity?,
    defaultGoal: SleepGoalEntity,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (
        bedHour: Int,
        bedMinute: Int,
        wakeHour: Int,
        wakeMinute: Int,
        deepPercent: Int,
        remPercent: Int,
        awakeMinutes: Int
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var bedHour by remember(existingDay, defaultGoal) {
        mutableIntStateOf(existingDay?.bedtimeHour ?: defaultGoal.targetBedtimeHour)
    }
    var bedMinute by remember(existingDay, defaultGoal) {
        mutableIntStateOf(existingDay?.bedtimeMinute ?: defaultGoal.targetBedtimeMinute)
    }
    var wakeHour by remember(existingDay, defaultGoal) {
        mutableIntStateOf(existingDay?.wakeHour ?: defaultGoal.targetWakeHour)
    }
    var wakeMinute by remember(existingDay, defaultGoal) {
        mutableIntStateOf(existingDay?.wakeMinute ?: defaultGoal.targetWakeMinute)
    }
    var deepPct by remember(existingDay) { mutableIntStateOf(existingDay?.deepPercent ?: 22) }
    var remPct by remember(existingDay) { mutableIntStateOf(existingDay?.remPercent ?: 21) }
    var awakeMins by remember(existingDay) { mutableIntStateOf(existingDay?.awakeMinutes ?: 12) }

    val calculatedMins = computeDurationMinutesAcrossMidnight(bedHour, bedMinute, wakeHour, wakeMinute)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        contentColor = JetBlack,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (existingDay == null) "Log $dayFullName Sleep" else "Edit $dayFullName Sleep",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JetBlack
                    )
                    Text(
                        text = "Enter your real sleep times & cycle proportions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (existingDay != null && onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete sleep log",
                                tint = CrimsonPrimary
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = JetBlack) {
                        Text(
                            text = "${calculatedMins / 60}h ${calculatedMins % 60}m",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TimeStepperRow(
                icon = Icons.Rounded.NightsStay,
                title = "Bedtime",
                timeText = format24h(bedHour, bedMinute),
                onDecrease = {
                    val total = (bedHour * 60 + bedMinute - 15 + 1440) % 1440
                    bedHour = total / 60
                    bedMinute = total % 60
                },
                onIncrease = {
                    val total = (bedHour * 60 + bedMinute + 15) % 1440
                    bedHour = total / 60
                    bedMinute = total % 60
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            TimeStepperRow(
                icon = Icons.Rounded.WbSunny,
                title = "Wake-Up Time",
                timeText = format24h(wakeHour, wakeMinute),
                onDecrease = {
                    val total = (wakeHour * 60 + wakeMinute - 15 + 1440) % 1440
                    wakeHour = total / 60
                    wakeMinute = total % 60
                },
                onIncrease = {
                    val total = (wakeHour * 60 + wakeMinute + 15) % 1440
                    wakeHour = total / 60
                    wakeMinute = total % 60
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            CycleSliderRow(
                label = "Deep Sleep",
                valueText = "$deepPct%",
                value = deepPct.toFloat(),
                range = 10f..35f,
                onValueChange = { deepPct = it.toInt() }
            )
            CycleSliderRow(
                label = "REM Sleep",
                valueText = "$remPct%",
                value = remPct.toFloat(),
                range = 10f..32f,
                onValueChange = { remPct = it.toInt() }
            )
            CycleSliderRow(
                label = "Awake Time",
                valueText = "${awakeMins}m",
                value = awakeMins.toFloat(),
                range = 0f..60f,
                onValueChange = { awakeMins = it.toInt() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = JetBlack
                    )
                }
                Button(
                    onClick = {
                        onSave(bedHour, bedMinute, wakeHour, wakeMinute, deepPct, remPct, awakeMins)
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("save_day_sleep_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JetBlack,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "SAVE REAL LOG",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun AutomationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = LightTrack.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = JetBlack,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureWhite,
                    checkedTrackColor = CrimsonPrimary,
                    uncheckedThumbColor = JetBlack,
                    uncheckedTrackColor = PureWhite
                )
            )
        }
    }
}

@Composable
private fun PermissionActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isGranted) LightTrack.copy(alpha = 0.42f) else CrimsonSoftTint,
        border = BorderStroke(1.dp, if (isGranted) LightTrack else CrimsonBorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isGranted) JetBlack else CrimsonPrimary
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Rounded.CheckCircle else icon,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = JetBlack,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isGranted) PureWhite else CrimsonPrimary,
                border = if (isGranted) BorderStroke(1.dp, JetBlack.copy(alpha = 0.2f)) else null,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAction)
            ) {
                Text(
                    text = if (isGranted) "READY" else actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isGranted) JetBlack else PureWhite,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalPresetChip(
    label: String,
    sub: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) JetBlack else LightTrack.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, if (selected) JetBlack else CrimsonBorderLight)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = if (selected) PureWhite else JetBlack,
                maxLines = 1
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = if (selected) PureWhite.copy(alpha = 0.78f) else MutedInk,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TimeStepperRow(
    icon: ImageVector,
    title: String,
    timeText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LightTrack.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = JetBlack,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(1.dp, JetBlack.copy(alpha = 0.15f))
                ) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease $title by 15 minutes",
                            tint = JetBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = timeText,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = JetBlack
                )

                Surface(
                    shape = CircleShape,
                    color = JetBlack
                ) {
                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase $title by 15 minutes",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleSliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = JetBlack,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = valueText,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = CrimsonPrimary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = JetBlack,
                activeTrackColor = CrimsonPrimary,
                inactiveTrackColor = LightTrack
            )
        )
    }
}
