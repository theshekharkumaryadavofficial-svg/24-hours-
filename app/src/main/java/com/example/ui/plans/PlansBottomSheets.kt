package com.example.ui.plans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyDisciplineConfigEntity
import com.example.data.DailyPlanBlockEntity
import com.example.data.STUDY_APP_PRESETS
import com.example.data.StudyModeConfigEntity
import com.example.data.format24h
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LightTrack
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PureWhite
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudyModeConfigSheet(
    current: StudyModeConfigEntity,
    onDismiss: () -> Unit,
    onSave: (
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        autoEnabled: Boolean,
        blockSocialMedia: Boolean,
        allowedKeywordsCsv: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var startHour by remember(current) { mutableIntStateOf(current.startHour) }
    var startMinute by remember(current) { mutableIntStateOf(current.startMinute) }
    var endHour by remember(current) { mutableIntStateOf(current.endHour) }
    var endMinute by remember(current) { mutableIntStateOf(current.endMinute) }
    var autoEnabled by remember(current) { mutableStateOf(current.autoStudyEnabled) }
    var blockSocial by remember(current) { mutableStateOf(current.blockSocialMedia) }

    val initialSelectedPresets = remember(current) {
        val kws = current.allowedKeywordsList().toSet()
        STUDY_APP_PRESETS.filter { preset -> preset.keywords.any { it in kws } }.map { it.id }.toSet()
    }
    var selectedPresetIds by remember(current) { mutableStateOf(initialSelectedPresets) }

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
                        imageVector = Icons.Rounded.MenuBook,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.padding(8.dp).size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Study Mode & Social Blocker",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JetBlack
                    )
                    Text(
                        text = "Blocks social media with Red Lock · Allows study apps only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Study Window Presets (including 17:00 - 22:00 / 5 PM - 10 PM!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetPill(
                    title = "17:00-22:00",
                    sub = "5PM - 10PM",
                    selected = startHour == 17 && endHour == 22,
                    onClick = { startHour = 17; startMinute = 0; endHour = 22; endMinute = 0 },
                    modifier = Modifier.weight(1f)
                )
                PresetPill(
                    title = "06:00-10:00",
                    sub = "Morning 4h",
                    selected = startHour == 6 && endHour == 10,
                    onClick = { startHour = 6; startMinute = 0; endHour = 10; endMinute = 0 },
                    modifier = Modifier.weight(1f)
                )
                PresetPill(
                    title = "14:00-18:00",
                    sub = "Afternoon 4h",
                    selected = startHour == 14 && endHour == 18,
                    onClick = { startHour = 14; startMinute = 0; endHour = 18; endMinute = 0 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            PlanTimeStepper(
                label = "Study Start",
                timeText = format24h(startHour, startMinute),
                onMinus = {
                    val t = (startHour * 60 + startMinute - 30 + 1440) % 1440
                    startHour = t / 60; startMinute = t % 60
                },
                onPlus = {
                    val t = (startHour * 60 + startMinute + 30) % 1440
                    startHour = t / 60; startMinute = t % 60
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlanTimeStepper(
                label = "Study End",
                timeText = format24h(endHour, endMinute),
                onMinus = {
                    val t = (endHour * 60 + endMinute - 30 + 1440) % 1440
                    endHour = t / 60; endMinute = t % 60
                },
                onPlus = {
                    val t = (endHour * 60 + endMinute + 30) % 1440
                    endHour = t / 60; endMinute = t % 60
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            PlanToggleRow(
                title = "Auto Study Schedule",
                subtitle = "Automatically locks distractions from ${format24h(startHour, startMinute)} to ${format24h(endHour, endMinute)}",
                checked = autoEnabled,
                onCheckedChange = { autoEnabled = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlanToggleRow(
                title = "Block Social Media + Red Lock",
                subtitle = "Instagram, WhatsApp, YouTube, Facebook, Games blocked in red foreground",
                checked = blockSocial,
                onCheckedChange = { blockSocial = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ALLOWED STUDY APPS (ALL OTHERS BLOCKED)",
                style = MaterialTheme.typography.labelSmall,
                color = JetBlack
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                STUDY_APP_PRESETS.forEach { preset ->
                    val isSelected = preset.id in selectedPresetIds
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) JetBlack else LightTrack.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, if (isSelected) JetBlack else CrimsonBorderLight),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                selectedPresetIds = if (isSelected) {
                                    selectedPresetIds - preset.id
                                } else {
                                    selectedPresetIds + preset.id
                                }
                            }
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) PureWhite else JetBlack,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = JetBlack)
                }
                Button(
                    onClick = {
                        val mergedKeywords = STUDY_APP_PRESETS
                            .filter { it.id in selectedPresetIds }
                            .flatMap { it.keywords }
                            .joinToString(",")
                        onSave(
                            startHour,
                            startMinute,
                            endHour,
                            endMinute,
                            autoEnabled,
                            blockSocial,
                            mergedKeywords
                        )
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("save_study_mode_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "SAVE STUDY MODE",
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
fun DisciplineReminderSheet(
    current: DailyDisciplineConfigEntity,
    onDismiss: () -> Unit,
    onSave: (
        waterGoalMl: Int,
        lightMealsTarget: Int,
        naamJapTarget: Int,
        reminderEnabled: Boolean,
        intervalMinutes: Int
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var waterMl by remember(current) { mutableIntStateOf(current.waterGoalMl) }
    var mealsTarget by remember(current) { mutableIntStateOf(current.lightMealsTarget) }
    var naamJapTarget by remember(current) { mutableIntStateOf(current.naamJapReminderTarget) }
    var reminderEnabled by remember(current) { mutableStateOf(current.reminderEnabled) }
    var intervalMins by remember(current) { mutableIntStateOf(current.reminderIntervalMinutes) }

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
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.padding(8.dp).size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "10-Min Smart Reminders",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JetBlack
                    )
                    Text(
                        text = "4L Water · Light Balanced Eating · 20,000 Naam Jap",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PlanToggleRow(
                title = "Periodic Reminder Alerts",
                subtitle = "Sends live progress check-in every $intervalMins minutes (auto-paused during sleep)",
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("REMINDER INTERVAL", style = MaterialTheme.typography.labelSmall, color = JetBlack)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10, 15, 30).forEach { mins ->
                    PresetPill(
                        title = "${mins} MIN",
                        sub = if (mins == 10) "Recommended" else "Interval",
                        selected = intervalMins == mins,
                        onClick = { intervalMins = mins },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PlanTimeStepper(
                label = "Daily Water Target",
                timeText = String.format(Locale.US, "%.1f L", waterMl / 1000f),
                onMinus = { waterMl = (waterMl - 500).coerceAtLeast(1500) },
                onPlus = { waterMl = (waterMl + 500).coerceAtMost(7000) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlanTimeStepper(
                label = "Light Balanced Meals",
                timeText = "$mealsTarget / day",
                onMinus = { mealsTarget = (mealsTarget - 1).coerceAtLeast(2) },
                onPlus = { mealsTarget = (mealsTarget + 1).coerceAtMost(4) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlanTimeStepper(
                label = "Naam Jap Target",
                timeText = NumberFormat.getNumberInstance(Locale.US).format(naamJapTarget),
                onMinus = { naamJapTarget = (naamJapTarget - 2000).coerceAtLeast(1000) },
                onPlus = { naamJapTarget = (naamJapTarget + 2000).coerceAtMost(100_000) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = JetBlack)
                }
                Button(
                    onClick = {
                        onSave(waterMl, mealsTarget, naamJapTarget, reminderEnabled, intervalMins)
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("save_discipline_reminders_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "SAVE REMINDERS",
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
fun SmartFullDayScheduleSheet(
    currentStudy: StudyModeConfigEntity,
    onDismiss: () -> Unit,
    onGenerate: (
        wakeHour: Int,
        studyStartHour: Int,
        studyEndHour: Int,
        workStartHour: Int,
        workEndHour: Int,
        enable10MinReminders: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var wakeHour by remember { mutableIntStateOf(6) }
    var workStart by remember { mutableIntStateOf(9) }
    var workEnd by remember { mutableIntStateOf(16) }
    var studyStart by remember(currentStudy) { mutableIntStateOf(currentStudy.startHour) }
    var studyEnd by remember(currentStudy) { mutableIntStateOf(currentStudy.endHour) }
    var enable10Min by remember { mutableStateOf(true) }

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
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.padding(8.dp).size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Smart Full-Day Dincharya",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JetBlack
                    )
                    Text(
                        text = "Auto-builds your 24h Study, Work, 4L Water & 20k Naam Jap plan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            PlanTimeStepper(
                label = "Morning Wake-Up",
                timeText = format24h(wakeHour, 0),
                onMinus = { wakeHour = (wakeHour - 1).coerceAtLeast(4) },
                onPlus = { wakeHour = (wakeHour + 1).coerceAtMost(9) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlanTimeStepper(
                label = "Work Block Start",
                timeText = format24h(workStart, 0),
                onMinus = { workStart = (workStart - 1).coerceAtLeast(7) },
                onPlus = { workStart = (workStart + 1).coerceAtMost(13) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlanTimeStepper(
                label = "Study Mode Start",
                timeText = format24h(studyStart, 0),
                onMinus = { studyStart = (studyStart - 1 + 24) % 24 },
                onPlus = { studyStart = (studyStart + 1) % 24 }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlanTimeStepper(
                label = "Study Mode End",
                timeText = format24h(studyEnd, 0),
                onMinus = { studyEnd = (studyEnd - 1 + 24) % 24 },
                onPlus = { studyEnd = (studyEnd + 1) % 24 }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PlanToggleRow(
                title = "Activate 10-Min Smart Reminders",
                subtitle = "Sets 4L water, 3 light meals & 20,000 Naam Jap reminders every 10 min",
                checked = enable10Min,
                onCheckedChange = { enable10Min = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onGenerate(wakeHour, studyStart, studyEnd, workStart, workEnd, enable10Min)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_smart_schedule_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = PureWhite
                )
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERATE FULL 24H DINCHARYA",
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
fun AddEditPlanBlockSheet(
    existing: DailyPlanBlockEntity?,
    installedApps: List<com.example.plans.InstalledAppInfo>,
    isLoadingApps: Boolean,
    onDismiss: () -> Unit,
    onDelete: ((Int) -> Unit)?,
    onSave: (DailyPlanBlockEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "STUDY") }
    var startHour by remember(existing) { mutableIntStateOf(existing?.startHour ?: 17) }
    var startMinute by remember(existing) { mutableIntStateOf(existing?.startMinute ?: 0) }
    var endHour by remember(existing) { mutableIntStateOf(existing?.endHour ?: 19) }
    var endMinute by remember(existing) { mutableIntStateOf(existing?.endMinute ?: 0) }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    var strictAppLock by remember(existing) { mutableStateOf(existing?.strictAppLockEnabled ?: true) }
    var selectedPackages by remember(existing) {
        mutableStateOf(existing?.allowedPackagesSet() ?: emptySet())
    }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = CrimsonPrimary)
                    Text(
                        text = if (existing == null) "New 24H Time Slot" else "Edit Time Slot & Apps",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JetBlack
                    )
                }
                if (existing != null && onDelete != null) {
                    IconButton(onClick = { onDelete(existing.id) }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete block",
                            tint = CrimsonPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Slot Title (Unlimited Slots)") },
                placeholder = { Text("e.g. Film Watching, Deep Study, Office Work") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("slot_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonPrimary,
                    focusedLabelColor = CrimsonPrimary,
                    cursorColor = CrimsonPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf("STUDY", "WORK", "LEISURE", "HEALTH", "SPIRITUAL", "ROUTINE").forEach { cat ->
                    val selected = category == cat
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { category = cat },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) CrimsonPrimary else LightTrack.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = cat.take(5),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) PureWhite else JetBlack,
                            fontSize = 8.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PlanTimeStepper(
                label = "Start Time",
                timeText = format24h(startHour, startMinute),
                onMinus = {
                    val t = (startHour * 60 + startMinute - 15 + 1440) % 1440
                    startHour = t / 60; startMinute = t % 60
                },
                onPlus = {
                    val t = (startHour * 60 + startMinute + 15) % 1440
                    startHour = t / 60; startMinute = t % 60
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlanTimeStepper(
                label = "End Time",
                timeText = format24h(endHour, endMinute),
                onMinus = {
                    val t = (endHour * 60 + endMinute - 15 + 1440) % 1440
                    endHour = t / 60; endMinute = t % 60
                },
                onPlus = {
                    val t = (endHour * 60 + endMinute + 15) % 1440
                    endHour = t / 60; endMinute = t % 60
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PlanToggleRow(
                title = "Permanent Allowed-Apps Lock",
                subtitle = "Every day in this slot, ONLY apps ON below open. Any other/new app triggers Full-Screen Radha Naam Jap Blocker!",
                checked = strictAppLock,
                onCheckedChange = { strictAppLock = it }
            )

            if (strictAppLock) {
                Spacer(modifier = Modifier.height(10.dp))
                SlotAppPickerSection(
                    installedApps = installedApps,
                    isLoadingApps = isLoadingApps,
                    selectedPackages = selectedPackages,
                    onTogglePackage = { pkg ->
                        selectedPackages = if (pkg in selectedPackages) {
                            selectedPackages - pkg
                        } else {
                            selectedPackages + pkg
                        }
                    },
                    onClearAll = { selectedPackages = emptySet() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Focus Target (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonPrimary,
                    focusedLabelColor = CrimsonPrimary,
                    cursorColor = CrimsonPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = JetBlack)
                }
                Button(
                    onClick = {
                        val finalTitle = title.trim().ifEmpty { "$category Session" }
                        onSave(
                            DailyPlanBlockEntity(
                                id = existing?.id ?: 0,
                                title = finalTitle,
                                category = category,
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                notes = notes.trim(),
                                enablesStudyBlock = category == "STUDY" || strictAppLock,
                                completedDateKey = existing?.completedDateKey ?: "",
                                strictAppLockEnabled = strictAppLock,
                                allowedPackagesCsv = selectedPackages.sorted().joinToString(",")
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("save_plan_block_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = if (strictAppLock) "SAVE SLOT (${selectedPackages.size} APPS)" else "SAVE TIME SLOT",
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
private fun PresetPill(
    title: String,
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
                text = title,
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
private fun PlanTimeStepper(
    label: String,
    timeText: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = LightTrack.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = JetBlack,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(1.dp, JetBlack.copy(alpha = 0.15f))
                ) {
                    IconButton(onClick = onMinus, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Decrease $label", tint = JetBlack, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = timeText,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = JetBlack
                )
                Surface(shape = CircleShape, color = JetBlack) {
                    IconButton(onClick = onPlus, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Rounded.Add, contentDescription = "Increase $label", tint = PureWhite, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = CrimsonSoftTint.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, CrimsonBorderLight)
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
