package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SleepCycleAnalysis
import com.example.data.SleepCycleInsight
import com.example.data.SleepCycleStageMetric
import com.example.data.SleepCycleType
import com.example.data.SleepGoalEntity
import com.example.ui.theme.CharcoalInk
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.CycleAwakeColor
import com.example.ui.theme.CycleDeepColor
import com.example.ui.theme.CycleLightColor
import com.example.ui.theme.CycleRemColor
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LightTrack
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PureWhite

@Composable
fun SleepReviewSection(
    analysis: SleepCycleAnalysis?,
    selectedDayName: String,
    goal: SleepGoalEntity,
    isBedtimeActive: Boolean,
    onStartBedtime: () -> Unit,
    onEditDayClick: () -> Unit,
    onEditGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (analysis == null) {
        EmptyRealSleepReviewCard(
            selectedDayName = selectedDayName,
            goal = goal,
            isBedtimeActive = isBedtimeActive,
            onStartBedtime = onStartBedtime,
            onManualLogClick = onEditDayClick,
            onEditGoalClick = onEditGoalClick,
            modifier = modifier
        )
        return
    }

    val animatedScoreSweep by animateFloatAsState(
        targetValue = (analysis.qualityScore / 100f) * 260f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "qualityRingSweep"
    )
    val animatedGoalFill by animateFloatAsState(
        targetValue = analysis.goalAlignment.durationProgressFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "goalBarFill"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
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
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CrimsonPrimary
                    ) {
                        Text(
                            text = analysis.day.dayShort.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.section_sleep_review),
                                style = MaterialTheme.typography.titleMedium,
                                color = JetBlack,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = JetBlack
                            ) {
                                Text(
                                    text = analysis.day.sourceBadgeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PureWhite,
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${analysis.day.dayFull} · ${analysis.completedCycles} full ~90m cycles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = CrimsonSoftTint
                ) {
                    IconButton(
                        onClick = onEditDayClick,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("edit_day_sleep_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.action_edit_day_sleep),
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quality Gauge + Duration & Schedule
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 9.dp.toPx()
                        val inset = stroke / 2f
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        drawArc(
                            color = LightTrack,
                            startAngle = 140f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = CrimsonPrimary,
                            startAngle = 140f,
                            sweepAngle = animatedScoreSweep,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${analysis.qualityScore}%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = JetBlack
                        )
                        Text(
                            text = analysis.qualityVerdict,
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonPrimary,
                            fontSize = 9.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = analysis.day.durationCompact,
                            style = MaterialTheme.typography.displayMedium,
                            color = JetBlack,
                            fontSize = 30.sp,
                            lineHeight = 34.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (analysis.goalAlignment.isDurationGoalMet) JetBlack else CrimsonPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = if (analysis.goalAlignment.isDurationGoalMet) "GOAL MET" else "BELOW GOAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScheduleStamp(
                            icon = Icons.Rounded.NightsStay,
                            label = stringResource(R.string.label_bedtime),
                            time = analysis.day.bedtimeFormatted,
                            modifier = Modifier.weight(1f)
                        )
                        ScheduleStamp(
                            icon = Icons.Rounded.WbSunny,
                            label = stringResource(R.string.label_wake_time),
                            time = analysis.day.wakeTimeFormatted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real Bedtime Telemetry Strip (Distractions Blocked, Screen Wake-ups, Battery Delta)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RealTelemetryBadge(
                    icon = Icons.Rounded.Block,
                    label = "Blocked",
                    value = "${analysis.day.distractionBlocks}",
                    modifier = Modifier.weight(1f)
                )
                RealTelemetryBadge(
                    icon = Icons.Rounded.PhoneAndroid,
                    label = "Wake-Ups",
                    value = "${analysis.day.screenWakeUps}",
                    modifier = Modifier.weight(1f)
                )
                RealTelemetryBadge(
                    icon = Icons.Rounded.BatterySaver,
                    label = "Battery Used",
                    value = "${analysis.day.batteryUsedPercent}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Sleep Goal Progress Strip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CrimsonSoftTint,
                border = BorderStroke(1.dp, CrimsonBorderLight)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Flag,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAILY GOAL · ${goal.targetBedtimeFormatted} - ${goal.targetWakeFormatted}",
                                style = MaterialTheme.typography.labelMedium,
                                color = JetBlack,
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = JetBlack,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onEditGoalClick)
                                .testTag("review_edit_goal_chip")
                        ) {
                            Text(
                                text = "EDIT",
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(PureWhite)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedGoalFill.coerceIn(0.05f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (analysis.goalAlignment.isDurationGoalMet) JetBlack else CrimsonPrimary
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${analysis.goalAlignment.durationProgressPercent}% of ${goal.targetDurationCompact} (${analysis.goalAlignment.durationDeltaText})",
                            style = MaterialTheme.typography.bodySmall,
                            color = JetBlack,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bed: ${analysis.goalAlignment.bedtimeAlignmentText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sleep Cycles Breakdown Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SLEEP CYCLES ARCHITECTURE",
                    style = MaterialTheme.typography.labelMedium,
                    color = JetBlack,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Deep · Light · REM · Awake",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(50)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                analysis.stages.forEach { stage ->
                    val weight = stage.percent.coerceAtLeast(3).toFloat()
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(stageColor(stage.type))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val topPair = analysis.stages.take(2)
            val bottomPair = analysis.stages.drop(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    topPair.forEach { stage ->
                        SleepStageTile(stage = stage, modifier = Modifier.weight(1f))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bottomPair.forEach { stage ->
                        SleepStageTile(stage = stage, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = LightTrack, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CYCLE INSIGHTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = JetBlack,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                analysis.insights.forEach { insight ->
                    InsightRow(insight = insight)
                }
            }
        }
    }
}

@Composable
private fun EmptyRealSleepReviewCard(
    selectedDayName: String,
    goal: SleepGoalEntity,
    isBedtimeActive: Boolean,
    onStartBedtime: () -> Unit,
    onManualLogClick: () -> Unit,
    onEditGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = PureWhite,
        shadowElevation = 6.dp,
        border = BorderStroke(1.5.dp, JetBlack.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    Surface(shape = RoundedCornerShape(8.dp), color = CrimsonPrimary) {
                        Text(
                            text = selectedDayName.take(3).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.section_sleep_review),
                        style = MaterialTheme.typography.titleMedium,
                        color = JetBlack,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = JetBlack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onEditGoalClick)
                ) {
                    Text(
                        text = "${goal.targetBedtimeFormatted}-${goal.targetWakeFormatted}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CrimsonSoftTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = null,
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isBedtimeActive) {
                    "Recording Real Sleep Now..."
                } else {
                    "No Real Sleep Recorded for $selectedDayName"
                },
                style = MaterialTheme.typography.titleMedium,
                color = JetBlack,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isBedtimeActive) {
                    "Your live session is tracking screen wake-ups, blocked apps, motion stillness, and battery usage. Tap Wake Up & Save when you wake up."
                } else {
                    "100% real data mode is active. Auto Bedtime (${goal.targetBedtimeFormatted} - ${goal.targetWakeFormatted}) will automatically silence calls/SMS, block apps, and record your sleep cycles."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartBedtime,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("empty_start_bedtime_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBedtimeActive) "END & SAVE" else "START NOW",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onManualLogClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("empty_manual_log_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, JetBlack),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JetBlack)
                ) {
                    Text(
                        text = "LOG MANUALLY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RealTelemetryBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = JetBlack
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = PureWhite.copy(alpha = 0.78f),
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = PureWhite
            )
        }
    }
}

@Composable
private fun ScheduleStamp(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = LightTrack.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CrimsonPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk,
                    fontSize = 8.sp
                )
                Text(
                    text = time,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = JetBlack
                )
            }
        }
    }
}

@Composable
private fun SleepStageTile(
    stage: SleepCycleStageMetric,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = LightTrack.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, LightTrack)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(stageColor(stage.type))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stage.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalInk,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${stage.percent}%",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = if (stage.type == SleepCycleType.REM) CrimsonPrimary else JetBlack
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stage.formattedDuration,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = JetBlack
                )
                Text(
                    text = stage.statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (stage.isOptimal) MutedInk else CrimsonPrimary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun InsightRow(insight: SleepCycleInsight) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (insight.isPositive) LightTrack.copy(alpha = 0.35f) else CrimsonSoftTint.copy(alpha = 0.7f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (insight.isPositive) Icons.Rounded.CheckCircle else Icons.Rounded.Info,
            contentDescription = null,
            tint = if (insight.isPositive) JetBlack else CrimsonPrimary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.titleSmall,
                color = JetBlack,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = insight.detail,
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalInk,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

private fun stageColor(type: SleepCycleType): Color = when (type) {
    SleepCycleType.DEEP -> CycleDeepColor
    SleepCycleType.REM -> CycleRemColor
    SleepCycleType.LIGHT -> CycleLightColor
    SleepCycleType.AWAKE -> CycleAwakeColor
}
