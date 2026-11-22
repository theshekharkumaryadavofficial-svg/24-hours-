package com.example.ui.plans

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DailyPlanBlockEntity
import com.example.ui.PlansUiState
import com.example.ui.theme.CharcoalInk
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonDeep
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.CrimsonVivid
import com.example.ui.theme.FadedWhite
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LightTrack
import com.example.ui.theme.MutedInk
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PlansTabContent(
    uiState: PlansUiState,
    onToggleStudyModeNow: () -> Unit,
    onOpenStudyConfig: () -> Unit,
    onPreviewStudyRedLock: () -> Unit,
    onAddWater: (Int) -> Unit,
    onSetLightMeals: (Int) -> Unit,
    onToggle10MinReminder: (Boolean) -> Unit,
    onOpenReminderSheet: () -> Unit,
    onSendTestReminder: () -> Unit,
    onOpenSmartSchedule: () -> Unit,
    onOpenAddBlock: () -> Unit,
    onToggleBlockDone: (DailyPlanBlockEntity) -> Unit,
    onEditBlock: (DailyPlanBlockEntity) -> Unit,
    onOpenPermissionsHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
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
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item(key = "plans_header") {
                PlansHeaderBar(
                    onOpenSmartSchedule = onOpenSmartSchedule,
                    onOpenAddBlock = onOpenAddBlock
                )
            }

            // 1. TOP CARD (Crimson Red Hero): STUDY MODE SOCIAL-MEDIA BLOCKER (e.g. 17:00 - 22:00)
            item(key = "plans_study_shield") {
                StudyModeHeroCard(
                    uiState = uiState,
                    onToggleStudyMode = onToggleStudyModeNow,
                    onConfigureStudy = onOpenStudyConfig,
                    onPreviewStudyLock = onPreviewStudyRedLock,
                    onGrantPermissions = onOpenPermissionsHelp
                )
            }

            // 2. MIDDLE CARD (Pure White): 4L WATER + LIGHT BALANCED MEALS + 20,000 NAAM JAP (10-MIN REMINDERS)
            item(key = "plans_discipline_reminders") {
                DailyDisciplineReminderCard(
                    uiState = uiState,
                    onAddWater = onAddWater,
                    onSetLightMeals = onSetLightMeals,
                    onToggleReminder = onToggle10MinReminder,
                    onConfigureReminders = onOpenReminderSheet,
                    onSendTestReminder = onSendTestReminder
                )
            }

            // 3. BOTTOM CARD (Jet Black Obsidian): FULL-DAY 24H DINCHARYA & WORK/STUDY TIMETABLE
            item(key = "plans_timetable") {
                FullDayTimetableCard(
                    uiState = uiState,
                    onOpenSmartSchedule = onOpenSmartSchedule,
                    onOpenAddBlock = onOpenAddBlock,
                    onToggleDone = onToggleBlockDone,
                    onEditBlock = onEditBlock
                )
            }
        }
    }
}

@Composable
private fun PlansHeaderBar(
    onOpenSmartSchedule: () -> Unit,
    onOpenAddBlock: () -> Unit
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
                        imageVector = Icons.Rounded.Schedule,
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
                        text = "PLANS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Surface(shape = RoundedCornerShape(6.dp), color = PureWhite) {
                        Text(
                            text = "24H",
                            style = MaterialTheme.typography.labelSmall,
                            color = JetBlack,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Study Shield · Dincharya · 10m Alerts",
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
                    .clickable(onClick = onOpenSmartSchedule)
                    .testTag("plans_smart_schedule_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Smart",
                        style = MaterialTheme.typography.labelMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Surface(shape = CircleShape, color = PureWhite) {
                IconButton(
                    onClick = onOpenAddBlock,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("plans_add_block_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add timetable block",
                        tint = JetBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyModeHeroCard(
    uiState: PlansUiState,
    onToggleStudyMode: () -> Unit,
    onConfigureStudy: () -> Unit,
    onPreviewStudyLock: () -> Unit,
    onGrantPermissions: () -> Unit
) {
    val cfg = uiState.studyConfig
    val readyForBlocking = uiState.permissions.hasUsageStatsAccess && uiState.permissions.hasOverlayPermission

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(CrimsonVivid, CrimsonPrimary, CrimsonDeep)
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(PureWhite.copy(alpha = 0.35f), JetBlack.copy(alpha = 0.45f))
                ),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_red_clock_backdrop_1795248955813),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .scale(1.22f)
                .alpha(0.22f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (cfg.isActiveNow) PureWhite else JetBlack,
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MenuBook,
                            contentDescription = null,
                            tint = if (cfg.isActiveNow) CrimsonPrimary else PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cfg.isActiveNow) "STUDY LOCK ACTIVE" else if (cfg.autoStudyEnabled) "AUTO STUDY ARMED" else "STUDY MODE OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (cfg.isActiveNow) JetBlack else PureWhite,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = JetBlack.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onConfigureStudy)
                        .testTag("study_mode_edit_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = PureWhite, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EDIT TIME", style = MaterialTheme.typography.labelSmall, color = PureWhite, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Huge Bold Study Window
            Text(
                text = cfg.windowFormatted,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 40.sp,
                lineHeight = 44.sp,
                letterSpacing = (-1).sp,
                color = PureWhite,
                modifier = Modifier.testTag("study_window_text")
            )
            Text(
                text = "Blocked apps open Full-Screen Radha Naam Jap Counter · ${uiState.strictLockedSlotsCount} custom slot lock(s) armed (${cfg.durationCompact})",
                style = MaterialTheme.typography.bodySmall,
                color = SubtleWhite,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mini stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudyHeroChip(
                    label = "BLOCKED TODAY",
                    value = "${cfg.effectiveBlockedToday}",
                    modifier = Modifier.weight(1f)
                )
                StudyHeroChip(
                    label = "SOCIAL LOCK",
                    value = if (cfg.blockSocialMedia) "STRICT" else "OFF",
                    modifier = Modifier.weight(1f)
                )
                StudyHeroChip(
                    label = "SHIELD PERMS",
                    value = if (readyForBlocking) "READY" else "SETUP",
                    onClick = if (readyForBlocking) null else onGrantPermissions,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onToggleStudyMode,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("toggle_study_mode_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cfg.isActiveNow) PureWhite else JetBlack,
                        contentColor = if (cfg.isActiveNow) JetBlack else PureWhite
                    )
                ) {
                    Icon(
                        imageVector = if (cfg.isActiveNow) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (cfg.isActiveNow) "END STUDY MODE" else "START STUDY MODE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onPreviewStudyLock)
                        .testTag("preview_study_lock_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Visibility, contentDescription = "Preview Study Red Lock", tint = PureWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("TEST LOCK", style = MaterialTheme.typography.labelSmall, color = PureWhite, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyHeroChip(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(14.dp),
        color = JetBlack.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SubtleWhite, fontSize = 8.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = PureWhite, maxLines = 1)
        }
    }
}

@Composable
private fun DailyDisciplineReminderCard(
    uiState: PlansUiState,
    onAddWater: (Int) -> Unit,
    onSetLightMeals: (Int) -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onConfigureReminders: () -> Unit,
    onSendTestReminder: () -> Unit
) {
    val cfg = uiState.disciplineConfig
    val waterFill by animateFloatAsState(
        targetValue = cfg.waterProgressFraction,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "waterBar"
    )
    val japFill by animateFloatAsState(
        targetValue = uiState.naamJapProgressFraction,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "japBar"
    )
    val fmt = NumberFormat.getNumberInstance(Locale.US)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = PureWhite,
        shadowElevation = 6.dp,
        border = BorderStroke(1.5.dp, JetBlack.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 10-Min Reminder Master Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = CrimsonPrimary) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.padding(7.dp).size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "${cfg.reminderIntervalMinutes}-MIN SMART REMINDER",
                            style = MaterialTheme.typography.titleSmall,
                            color = JetBlack,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (cfg.reminderEnabled) "Active every ${cfg.reminderIntervalMinutes}m (Water · Meals · 20k Jap)" else "Tap switch to alert every ${cfg.reminderIntervalMinutes} minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = cfg.reminderEnabled,
                    onCheckedChange = onToggleReminder,
                    modifier = Modifier.testTag("toggle_10min_reminder_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureWhite,
                        checkedTrackColor = CrimsonPrimary,
                        uncheckedThumbColor = JetBlack,
                        uncheckedTrackColor = LightTrack
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = JetBlack,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onConfigureReminders)
                        .testTag("configure_reminders_button")
                ) {
                    Text(
                        text = "EDIT GOALS & INTERVAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CrimsonSoftTint,
                    border = BorderStroke(1.dp, CrimsonBorderLight),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onSendTestReminder)
                        .testTag("send_test_reminder_button")
                ) {
                    Text(
                        text = "TEST REMINDER NOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = LightTrack)
            Spacer(modifier = Modifier.height(14.dp))

            // 1. WATER TRACKER (4.0L / DAY)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WATER GOAL (4L / DAY)",
                        style = MaterialTheme.typography.labelMedium,
                        color = JetBlack,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = cfg.waterLitersFormatted,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = CrimsonPrimary,
                    modifier = Modifier.testTag("water_progress_text")
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LightTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(waterFill.coerceIn(0.03f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (cfg.effectiveWaterMl >= cfg.waterGoalMl) JetBlack else CrimsonPrimary)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WaterChip("+250 ml", isPrimary = true, onClick = { onAddWater(250) }, modifier = Modifier.weight(1f), tag = "water_add_250")
                WaterChip("+500 ml", isPrimary = true, onClick = { onAddWater(500) }, modifier = Modifier.weight(1f), tag = "water_add_500")
                WaterChip("-250 ml", isPrimary = false, onClick = { onAddWater(-250) }, modifier = Modifier.weight(0.8f), tag = "water_sub_250")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. LIGHT BALANCED EATING (Kam & Santulit Khana)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = JetBlack, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIGHT BALANCED MEALS",
                        style = MaterialTheme.typography.labelMedium,
                        color = JetBlack,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "${cfg.effectiveLightMeals} / ${cfg.lightMealsTarget} Mindful",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MutedInk
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mealLabels = listOf("Light Morning", "Balanced Lunch", "Early Light Dinner", "Snack")
                for (i in 1..cfg.lightMealsTarget) {
                    val checked = i <= cfg.effectiveLightMeals
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSetLightMeals(if (checked) i - 1 else i)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (checked) JetBlack else LightTrack.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, if (checked) JetBlack else CrimsonBorderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (checked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (checked) PureWhite else CrimsonPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = mealLabels.getOrElse(i - 1) { "Meal $i" },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (checked) PureWhite else JetBlack,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. DAILY 20,000 NAAM JAP TARGET (LIVE SYNCED WITH NAAM JAP TAB!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RADHA NAAM JAP TARGET",
                        style = MaterialTheme.typography.labelMedium,
                        color = JetBlack,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "${fmt.format(uiState.todayNaamJapCount)} / ${fmt.format(cfg.naamJapReminderTarget)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = CrimsonPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LightTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(japFill.coerceIn(0.03f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (uiState.todayNaamJapCount >= cfg.naamJapReminderTarget) JetBlack else CrimsonPrimary)
                )
            }
        }
    }
}

@Composable
private fun WaterChip(
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) CrimsonPrimary else LightTrack.copy(alpha = 0.65f)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            color = if (isPrimary) PureWhite else JetBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 9.dp)
        )
    }
}

@Composable
private fun FullDayTimetableCard(
    uiState: PlansUiState,
    onOpenSmartSchedule: () -> Unit,
    onOpenAddBlock: () -> Unit,
    onToggleDone: (DailyPlanBlockEntity) -> Unit,
    onEditBlock: (DailyPlanBlockEntity) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = ObsidianCard,
        shadowElevation = 6.dp,
        border = BorderStroke(1.5.dp, CrimsonPrimary.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "24H DINCHARYA · UNLIMITED SLOTS",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Tap any slot to pick permanent allowed apps · Others open Naam Jap",
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtleWhite,
                        fontSize = 11.sp
                    )
                }

                Surface(shape = RoundedCornerShape(50), color = PureWhite) {
                    Text(
                        text = "${uiState.completedBlocksToday}/${uiState.blocks.size} DONE",
                        style = MaterialTheme.typography.labelSmall,
                        color = JetBlack,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.blocks.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = PureWhite.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.16f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Build Your 24-Hour Dincharya",
                            style = MaterialTheme.typography.titleSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate a smart full-day timetable for study (5PM-10PM), work, 4L water, light meals, and 20,000 Radha Naam Jap in one tap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenSmartSchedule,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary,
                                contentColor = PureWhite
                            )
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SMART FULL-DAY SCHEDULE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.blocks.forEach { block ->
                        val isNow = block.isCurrentTimeInside(uiState.currentHour, uiState.currentMinute)
                        val done = block.isCompletedToday()
                        TimetableBlockRow(
                            block = block,
                            isCurrentSlot = isNow,
                            isCompleted = done,
                            onToggleDone = { onToggleDone(block) },
                            onEdit = { onEditBlock(block) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenSmartSchedule)
                    ) {
                        Text(
                            text = "SMART REBUILD",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CrimsonPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenAddBlock)
                    ) {
                        Text(
                            text = "+ ADD BLOCK",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableBlockRow(
    block: DailyPlanBlockEntity,
    isCurrentSlot: Boolean,
    isCompleted: Boolean,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit
) {
    val bgColor = when {
        isCurrentSlot -> PureWhite
        else -> PureWhite.copy(alpha = 0.08f)
    }
    val primaryTextColor = if (isCurrentSlot) JetBlack else PureWhite
    val secondaryTextColor = if (isCurrentSlot) CharcoalInk else SubtleWhite

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(
            width = if (isCurrentSlot) 2.dp else 1.dp,
            color = if (isCurrentSlot) CrimsonVivid else PureWhite.copy(alpha = 0.14f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleDone,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Mark block complete",
                    tint = if (isCompleted) CrimsonPrimary else if (isCurrentSlot) JetBlack else PureWhite,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = block.timeRangeFormatted,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = if (isCurrentSlot) CrimsonPrimary else FadedWhite
                    )
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = if (block.strictAppLockEnabled || block.enablesStudyBlock) CrimsonPrimary else if (isCurrentSlot) JetBlack else PureWhite.copy(alpha = 0.16f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (block.strictAppLockEnabled || block.enablesStudyBlock) {
                                Icon(Icons.Rounded.Block, contentDescription = null, tint = PureWhite, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = when {
                                    block.strictAppLockEnabled -> "LOCK · ${block.allowedAppsCount} APPS"
                                    block.enablesStudyBlock -> "STUDY LOCK"
                                    else -> block.category
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite,
                                fontSize = 8.sp
                            )
                        }
                    }
                    if (isCurrentSlot) {
                        Surface(shape = RoundedCornerShape(5.dp), color = JetBlack) {
                            Text(
                                text = "NOW",
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = primaryTextColor,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (block.notes.isNotEmpty()) {
                    Text(
                        text = block.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
