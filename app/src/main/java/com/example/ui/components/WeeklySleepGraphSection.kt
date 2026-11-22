package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SleepDayEntity
import com.example.data.SleepGoalEntity
import com.example.data.WEEK_DAY_SLOTS
import com.example.data.WeeklySleepSummary
import com.example.ui.theme.CrimsonBarBottom
import com.example.ui.theme.CrimsonBarTop
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonVivid
import com.example.ui.theme.FadedWhite
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite
import java.util.Locale

@Composable
fun WeeklySleepGraphSection(
    recordedDaysByIndex: Map<Int, SleepDayEntity>,
    selectedDayIndex: Int,
    goal: SleepGoalEntity,
    weeklySummary: WeeklySleepSummary,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxChartHours = 10f
    val goalHours = goal.targetHoursFloat.coerceIn(1f, 10f)
    val chartCd = stringResource(R.string.cd_weekly_graph)

    Surface(
        modifier = modifier.fillMaxWidth(),
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CrimsonPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.section_weekly_routine),
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (weeklySummary.recordedDaysCount == 0) {
                                "Real sleep bars appear after your first session"
                            } else {
                                "Tap any day to inspect real sleep cycles"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = PureWhite
                ) {
                    Text(
                        text = "${weeklySummary.recordedDaysCount}/7 LOGGED",
                        style = MaterialTheme.typography.labelSmall,
                        color = JetBlack,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(2.dp)
                            .background(PureWhite)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TARGET ${String.format(Locale.US, "%.1fh", goalHours)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite,
                        fontSize = 10.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LegendDot(color = PureWhite, label = "Selected")
                    LegendDot(color = CrimsonVivid, label = "Recorded")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive 7-Day Real Bar Graph
            val barAreaHeight = 166.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barAreaHeight + 34.dp)
                    .semantics { contentDescription = chartCd }
            ) {
                // Y-Axis (10h .. 0h)
                Column(
                    modifier = Modifier
                        .width(30.dp)
                        .height(barAreaHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    listOf("10h", "8h", "6h", "4h", "2h", "0h").forEach { tick ->
                        Text(
                            text = tick,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FadedWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Horizontal grid + dynamic Goal Line
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barAreaHeight)
                    ) {
                        val h = size.height
                        val w = size.width
                        for (step in 0..5) {
                            val y = h * (step / 5f)
                            drawLine(
                                color = PureWhite.copy(alpha = if (step == 5) 0.35f else 0.10f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = if (step == 5) 1.5.dp.toPx() else 1.dp.toPx()
                            )
                        }

                        val goalY = h * (1f - (goalHours / maxChartHours).coerceIn(0f, 1f))
                        drawLine(
                            color = PureWhite.copy(alpha = 0.85f),
                            start = Offset(0f, goalY),
                            end = Offset(w, goalY),
                            strokeWidth = 1.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }

                    // 7 Interactive Day Columns (Mon..Sun)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        WEEK_DAY_SLOTS.forEach { slot ->
                            val dayRecord = recordedDaysByIndex[slot.dayIndex]
                            val isSelected = slot.dayIndex == selectedDayIndex
                            val targetFraction = if (dayRecord != null) {
                                (dayRecord.durationHoursFloat / maxChartHours).coerceIn(0.08f, 1f)
                            } else {
                                0f
                            }
                            val animatedFraction by animateFloatAsState(
                                targetValue = targetFraction,
                                animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
                                label = "barHeight_${slot.dayIndex}"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onSelectDay(slot.dayIndex) }
                                    )
                                    .testTag("week_bar_${slot.dayShort.lowercase()}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barAreaHeight),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    if (dayRecord != null) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier.fillMaxHeight()
                                        ) {
                                            Spacer(
                                                modifier = Modifier.weight(
                                                    (1f - animatedFraction).coerceAtLeast(0.01f)
                                                )
                                            )
                                            Text(
                                                text = dayRecord.durationDecimalLabel,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSelected) PureWhite else SubtleWhite,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(if (isSelected) 28.dp else 22.dp)
                                                    .weight(animatedFraction.coerceAtLeast(0.08f))
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topStart = 8.dp,
                                                            topEnd = 8.dp,
                                                            bottomStart = 2.dp,
                                                            bottomEnd = 2.dp
                                                        )
                                                    )
                                                    .background(
                                                        if (isSelected) {
                                                            Brush.verticalGradient(
                                                                colors = listOf(
                                                                    PureWhite,
                                                                    PureWhite.copy(alpha = 0.88f)
                                                                )
                                                            )
                                                        } else {
                                                            Brush.verticalGradient(
                                                                colors = listOf(
                                                                    CrimsonBarTop,
                                                                    CrimsonBarBottom
                                                                )
                                                            )
                                                        }
                                                    )
                                            ) {
                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopCenter)
                                                            .padding(top = 4.dp)
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(CrimsonPrimary)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Honest unrecorded day slot
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom
                                        ) {
                                            Text(
                                                text = "--",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.sp,
                                                color = FadedWhite
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(20.dp)
                                                    .height(10.dp)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(
                                                        if (isSelected) PureWhite.copy(alpha = 0.28f)
                                                        else PureWhite.copy(alpha = 0.08f)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) PureWhite else PureWhite.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                                    )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) PureWhite else Color.Transparent)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = slot.dayShort,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) JetBlack else PureWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = PureWhite.copy(alpha = 0.14f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Weekly Real Summary Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyFooterMetric(
                    label = stringResource(R.string.label_best_night),
                    value = weeklySummary.bestDay?.let { "${it.dayShort} ${it.durationCompact}" } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                WeeklyFooterMetric(
                    label = "BLOCKED",
                    value = "${weeklySummary.totalDistractionsBlocked} apps",
                    modifier = Modifier.weight(1f)
                )
                WeeklyFooterMetric(
                    label = stringResource(R.string.label_consistency),
                    value = if (weeklySummary.recordedDaysCount == 0) "--" else "${weeklySummary.weeklyConsistencyPercent}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SubtleWhite,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun WeeklyFooterMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = PureWhite.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SubtleWhite,
                fontSize = 9.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = PureWhite,
                maxLines = 1
            )
        }
    }
}
