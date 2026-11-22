package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bedtime.BedtimePermissionsStatus
import com.example.data.SleepGoalEntity
import com.example.ui.SleepTimerDisplay
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonVivid
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite

@Composable
fun BedtimeGuardPanel(
    goal: SleepGoalEntity,
    timerDisplay: SleepTimerDisplay,
    permissions: BedtimePermissionsStatus,
    onOpenProtectionSheet: () -> Unit,
    onOpenBatterySaver: () -> Unit,
    onPreviewRedBlocker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianCard,
        shadowElevation = 6.dp,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (timerDisplay.isBedtimeActive) PureWhite.copy(alpha = 0.7f) else CrimsonPrimary.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (timerDisplay.isBedtimeActive) PureWhite else CrimsonPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = if (timerDisplay.isBedtimeActive) JetBlack else PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (timerDisplay.isBedtimeActive) {
                                "BEDTIME GUARD ACTIVE"
                            } else if (goal.autoBedtimeEnabled) {
                                "AUTO BEDTIME ARMED"
                            } else {
                                "BEDTIME GUARD PAUSED"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Window ${goal.targetBedtimeFormatted} - ${goal.targetWakeFormatted} · ${permissions.readyCount}/4 protections ready",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (permissions.allCoreProtectionsReady) PureWhite else CrimsonPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenProtectionSheet)
                        .testTag("guard_setup_button")
                ) {
                    Text(
                        text = if (permissions.allCoreProtectionsReady) "ALL READY" else "SETUP",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (permissions.allCoreProtectionsReady) JetBlack else PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Live Telemetry Strip when Bedtime Mode is actively running
            if (timerDisplay.isBedtimeActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = CrimsonPrimary.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, CrimsonVivid.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiveGuardStat(
                            label = "BLOCKED",
                            value = "${timerDisplay.blockedAttemptsCount}"
                        )
                        LiveGuardStat(
                            label = "WAKE-UPS",
                            value = "${timerDisplay.screenWakeCount}"
                        )
                        LiveGuardStat(
                            label = "CALLS/SMS",
                            value = if (permissions.hasDndAccess && goal.silenceCallsAndSms) "SILENT" else "NEED DND"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 Real Protection Engine Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GuardStatusChip(
                    icon = Icons.Rounded.DoNotDisturbOn,
                    title = "Calls & SMS",
                    status = if (permissions.hasDndAccess) "DND Ready" else "Grant DND",
                    isReady = permissions.hasDndAccess,
                    onClick = onOpenProtectionSheet,
                    modifier = Modifier.weight(1f)
                )
                GuardStatusChip(
                    icon = Icons.Rounded.Block,
                    title = "App Blocker",
                    status = if (permissions.hasUsageStatsAccess) "Usage Ready" else "Grant Usage",
                    isReady = permissions.hasUsageStatsAccess,
                    onClick = onOpenProtectionSheet,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GuardStatusChip(
                    icon = Icons.Rounded.Layers,
                    title = "Red Overlay",
                    status = if (permissions.hasOverlayPermission) "Foreground OK" else "Grant Overlay",
                    isReady = permissions.hasOverlayPermission,
                    onClick = onOpenProtectionSheet,
                    modifier = Modifier.weight(1f)
                )
                val batteryText = when {
                    permissions.batteryPercent < 0 -> "Battery Saver"
                    permissions.isPowerSaveMode -> "${permissions.batteryPercent}% · Saver ON"
                    permissions.isCharging -> "${permissions.batteryPercent}% · Charging"
                    else -> "${permissions.batteryPercent}% · Low-Drain"
                }
                GuardStatusChip(
                    icon = if (permissions.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatterySaver,
                    title = "Battery Guard",
                    status = batteryText,
                    isReady = true,
                    onClick = onOpenBatterySaver,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PureWhite.copy(alpha = 0.12f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Row: Preview Red Foreground Blocker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Zero-poll screen-off engine saves battery",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite,
                    fontSize = 10.sp
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PureWhite.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onPreviewRedBlocker)
                        .testTag("preview_red_blocker_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "TEST RED LOCK",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuardStatusChip(
    icon: ImageVector,
    title: String,
    status: String,
    isReady: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = PureWhite.copy(alpha = if (isReady) 0.10f else 0.06f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isReady) PureWhite.copy(alpha = 0.22f) else CrimsonVivid.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isReady) PureWhite else CrimsonPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isReady) JetBlack else PureWhite,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = if (isReady) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = if (isReady) PureWhite else CrimsonVivid,
                        modifier = Modifier.size(11.dp)
                    )
                }
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isReady) SubtleWhite else PureWhite,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LiveGuardStat(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SubtleWhite,
            fontSize = 9.sp
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = PureWhite
        )
    }
}
