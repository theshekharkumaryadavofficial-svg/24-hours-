package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SleepGoalEntity
import com.example.data.WeeklySleepSummary
import com.example.ui.ClockMode
import com.example.ui.LiveClockState
import com.example.ui.SleepTimerDisplay
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonDeep
import com.example.ui.theme.CrimsonObsidian
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonVivid
import com.example.ui.theme.FadedWhite
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HeroClockTimerSection(
    liveClock: LiveClockState,
    timerDisplay: SleepTimerDisplay,
    weeklySummary: WeeklySleepSummary,
    goal: SleepGoalEntity,
    bedtimeCountdownText: String,
    onSelectMode: (ClockMode) -> Unit,
    onToggleBedtimeGuard: () -> Unit,
    onOpenGoalSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTimerMode = timerDisplay.mode == ClockMode.SLEEP_TIMER || timerDisplay.isBedtimeActive
    val rawProgress = if (isTimerMode) {
        (timerDisplay.elapsedSeconds.toFloat() / (goal.targetDurationMinutes * 60f).coerceAtLeast(1f))
            .coerceIn(0.02f, 1f)
    } else {
        liveClock.dayProgressFraction.coerceIn(0.02f, 1f)
    }
    val animatedSweep by animateFloatAsState(
        targetValue = rawProgress * 360f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "clockSweep"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CrimsonVivid,
                        CrimsonPrimary,
                        CrimsonDeep
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PureWhite.copy(alpha = 0.35f),
                        JetBlack.copy(alpha = 0.45f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_red_clock_backdrop_1795248955813),
            contentDescription = stringResource(R.string.cd_backdrop),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .scale(1.22f)
                .alpha(0.24f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Switcher Pill (24H CLOCK | SLEEP TIMER)
            ClockModeToggle(
                selectedMode = if (isTimerMode) ClockMode.SLEEP_TIMER else ClockMode.CLOCK_24H,
                onSelectMode = onSelectMode
            )

            Spacer(modifier = Modifier.height(16.dp))

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val showSideStats = maxWidth >= 340.dp
                val dialSize: Dp = when {
                    maxWidth >= 420.dp -> 212.dp
                    maxWidth >= 350.dp -> 186.dp
                    else -> 204.dp
                }

                val badgeText = when {
                    timerDisplay.isBedtimeActive && timerDisplay.isAutoTriggered -> "AUTO BEDTIME"
                    timerDisplay.isBedtimeActive -> "BEDTIME LOCK"
                    isTimerMode -> "TIMER READY"
                    else -> "24H CLOCK"
                }

                if (showSideStats) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FlankingStatCard(
                            icon = Icons.Rounded.Hotel,
                            label = "7d Real",
                            value = weeklySummary.totalWeekCompact,
                            subLabel = "${weeklySummary.recordedDaysCount}/7 days",
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        CenterClockDial(
                            dialSize = dialSize,
                            animatedSweep = animatedSweep,
                            primaryHHMM = if (isTimerMode) timerDisplay.formattedHHMM else liveClock.hhmm,
                            secondsText = if (isTimerMode) timerDisplay.formattedSS else liveClock.ss,
                            badgeText = badgeText,
                            subCaption = bedtimeCountdownText,
                            isBedtimeActive = timerDisplay.isBedtimeActive
                        )

                        FlankingStatCard(
                            icon = Icons.Rounded.NightsStay,
                            label = "Avg Sleep",
                            value = weeklySummary.avgNightCompact,
                            subLabel = "/Night",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CenterClockDial(
                            dialSize = dialSize,
                            animatedSweep = animatedSweep,
                            primaryHHMM = if (isTimerMode) timerDisplay.formattedHHMM else liveClock.hhmm,
                            secondsText = if (isTimerMode) timerDisplay.formattedSS else liveClock.ss,
                            badgeText = badgeText,
                            subCaption = bedtimeCountdownText,
                            isBedtimeActive = timerDisplay.isBedtimeActive
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FlankingStatCard(
                                icon = Icons.Rounded.Hotel,
                                label = "7d Real",
                                value = weeklySummary.totalWeekCompact,
                                subLabel = "${weeklySummary.recordedDaysCount}/7 days",
                                modifier = Modifier.weight(1f)
                            )
                            FlankingStatCard(
                                icon = Icons.Rounded.NightsStay,
                                label = "Avg Sleep",
                                value = weeklySummary.avgNightCompact,
                                subLabel = "/Night",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Bedtime Guard Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onToggleBedtimeGuard,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("toggle_sleep_timer_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (timerDisplay.isBedtimeActive) PureWhite else JetBlack,
                        contentColor = if (timerDisplay.isBedtimeActive) JetBlack else PureWhite
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = if (timerDisplay.isBedtimeActive) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (timerDisplay.isBedtimeActive) {
                            stringResource(R.string.action_stop_save)
                        } else {
                            stringResource(R.string.action_start_sleep)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite,
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenGoalSheet)
                        .testTag("hero_goal_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = "Auto Bedtime Schedule",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (goal.autoBedtimeEnabled) "AUTO ON" else "MANUAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonPrimary,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "${goal.targetBedtimeFormatted}-${goal.targetWakeFormatted}",
                                style = MaterialTheme.typography.labelMedium,
                                color = JetBlack,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClockModeToggle(
    selectedMode: ClockMode,
    onSelectMode: (ClockMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = JetBlack.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ClockModeChip(
                label = stringResource(R.string.mode_clock_24h),
                selected = selectedMode == ClockMode.CLOCK_24H,
                onClick = { onSelectMode(ClockMode.CLOCK_24H) },
                testTag = "mode_24h_clock_tab"
            )
            ClockModeChip(
                label = stringResource(R.string.mode_sleep_timer),
                selected = selectedMode == ClockMode.SLEEP_TIMER,
                onClick = { onSelectMode(ClockMode.SLEEP_TIMER) },
                testTag = "mode_sleep_timer_tab"
            )
        }
    }
}

@Composable
private fun ClockModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bg by animateColorAsState(
        targetValue = if (selected) PureWhite else Color.Transparent,
        animationSpec = tween(220),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) JetBlack else PureWhite.copy(alpha = 0.85f),
        animationSpec = tween(220),
        label = "chipFg"
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun CenterClockDial(
    dialSize: Dp,
    animatedSweep: Float,
    primaryHHMM: String,
    secondsText: String,
    badgeText: String,
    subCaption: String,
    isBedtimeActive: Boolean
) {
    val cdDial = stringResource(R.string.cd_timer_ring)
    Box(
        modifier = Modifier
            .size(dialSize)
            .semantics { contentDescription = cdDial },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 10.dp.toPx()
            val radius = (size.minDimension - strokePx) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CrimsonDark, CrimsonObsidian, JetBlack),
                    center = center,
                    radius = radius
                ),
                radius = radius - strokePx * 0.45f,
                center = center
            )

            drawCircle(
                color = JetBlack.copy(alpha = 0.65f),
                radius = radius,
                center = center,
                style = Stroke(width = strokePx)
            )

            val tickOuter = radius - strokePx * 0.85f
            for (i in 0 until 24) {
                val angleRad = ((i * 15.0) - 90.0) * PI / 180.0
                val isCardinal = i % 6 == 0
                val tickLen = if (isCardinal) 8.dp.toPx() else 4.dp.toPx()
                val start = Offset(
                    x = center.x + (tickOuter * cos(angleRad)).toFloat(),
                    y = center.y + (tickOuter * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = center.x + ((tickOuter - tickLen) * cos(angleRad)).toFloat(),
                    y = center.y + ((tickOuter - tickLen) * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = if (isCardinal) PureWhite else PureWhite.copy(alpha = 0.35f),
                    start = start,
                    end = end,
                    strokeWidth = if (isCardinal) 2.2.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(PureWhite, CrimsonVivid, PureWhite)
                ),
                startAngle = -90f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            val tipAngleRad = (animatedSweep - 90f) * PI / 180.0
            val tipCenter = Offset(
                x = center.x + (radius * cos(tipAngleRad)).toFloat(),
                y = center.y + (radius * sin(tipAngleRad)).toFloat()
            )
            drawCircle(
                color = PureWhite,
                radius = 5.5.dp.toPx(),
                center = tipCenter
            )
            drawCircle(
                color = JetBlack,
                radius = 2.5.dp.toPx(),
                center = tipCenter
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Bedtime,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = if (isBedtimeActive) PureWhite else CrimsonPrimary,
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.4f))
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isBedtimeActive) JetBlack else PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = primaryHHMM,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (dialSize >= 200.dp) 38.sp else 33.sp,
                    lineHeight = 40.sp,
                    letterSpacing = (-1.5).sp,
                    color = PureWhite,
                    modifier = Modifier.testTag("hero_clock_digits")
                )
                Text(
                    text = ":$secondsText",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PureWhite.copy(alpha = 0.78f),
                    modifier = Modifier.padding(bottom = 5.dp, start = 1.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subCaption,
                style = MaterialTheme.typography.bodySmall,
                color = SubtleWhite,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FlankingStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    subLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = JetBlack.copy(alpha = 0.46f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(PureWhite.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = SubtleWhite,
                fontSize = 10.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = PureWhite,
                maxLines = 1
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = FadedWhite,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
