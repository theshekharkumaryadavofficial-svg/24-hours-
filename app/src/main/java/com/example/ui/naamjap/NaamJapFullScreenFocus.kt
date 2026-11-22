package com.example.ui.naamjap

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NaamJapUiState
import com.example.ui.theme.CrimsonDeep
import com.example.ui.theme.CrimsonObsidian
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonVivid
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.SubtleWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private data class TapPulseEffect(
    val id: Long,
    val position: Offset
)

@Composable
fun NaamJapFullScreenFocus(
    uiState: NaamJapUiState,
    onTapIncrement: () -> Unit,
    onUndoDecrement: () -> Unit,
    onExit: () -> Unit
) {
    BackHandler { onExit() }

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val wordScale = remember { Animatable(1f) }
    val tapEffects = remember { mutableStateListOf<TapPulseEffect>() }
    var nextEffectId by remember { mutableLongStateOf(1L) }
    var showMalaBanner by remember { mutableStateOf(false) }

    val malaProgress by animateFloatAsState(
        targetValue = if (uiState.todayCount > 0 && uiState.currentMalaBead == 0) {
            1f
        } else {
            uiState.currentMalaBead / 108f
        },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "focusMalaProgress"
    )

    val goalFill by animateFloatAsState(
        targetValue = uiState.goalProgressFraction,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "focusGoalFill"
    )

    LaunchedEffect(uiState.todayCount) {
        if (uiState.todayCount > 0 && uiState.todayCount % 108 == 0) {
            showMalaBanner = true
            if (uiState.goal.hapticEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            delay(2200L)
            showMalaBanner = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CrimsonVivid, CrimsonPrimary, CrimsonDeep, CrimsonObsidian)
                )
            )
            .pointerInput(uiState.goal.hapticEnabled) {
                detectTapGestures { offset ->
                    if (uiState.goal.hapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onTapIncrement()
                    val effectId = nextEffectId++
                    tapEffects.add(TapPulseEffect(effectId, offset))
                    scope.launch {
                        wordScale.snapTo(1.10f)
                        wordScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                    scope.launch {
                        delay(700L)
                        tapEffects.removeAll { it.id == effectId }
                    }
                }
            }
            .testTag("naam_jap_fullscreen_tap_area")
    ) {
        // Subtle sacred concentric rings in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.45f)
            val maxR = size.minDimension * 0.48f
            drawCircle(
                color = PureWhite.copy(alpha = 0.08f),
                radius = maxR,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = JetBlack.copy(alpha = 0.25f),
                radius = maxR * 0.80f,
                center = center,
                style = Stroke(width = 8.dp.toPx())
            )
            val ringRadius = maxR * 0.80f
            drawArc(
                color = PureWhite,
                startAngle = -90f,
                sweepAngle = malaProgress * 360f,
                useCenter = false,
                topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                size = Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Floating tap feedback "+1 राधा"
        tapEffects.forEach { effect ->
            FloatingTapMark(effect = effect)
        }

        // Main Clean Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Minimal Bar (Undo + Mode Badge + Exit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = JetBlack.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f))
                ) {
                    IconButton(
                        onClick = onUndoDecrement,
                        enabled = uiState.todayCount > 0,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("focus_undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Undo,
                            contentDescription = "Undo last count",
                            tint = if (uiState.todayCount > 0) PureWhite else PureWhite.copy(alpha = 0.35f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = JetBlack,
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.28f))
                ) {
                    Text(
                        text = "NAAM JAP MODE · TAP ANYWHERE",
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PureWhite
                ) {
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("focus_exit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Exit Naam Jap Mode",
                            tint = JetBlack
                        )
                    }
                }
            }

            // Center BIG RADHA + TODAY'S COUNT
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedVisibility(
                    visible = showMalaBanner,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PureWhite,
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = "108 MALA COMPLETE · RADHE RADHE",
                            style = MaterialTheme.typography.labelMedium,
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(wordScale.value)
                ) {
                    Text(
                        text = "राधा",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp,
                        color = PureWhite.copy(alpha = 0.92f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "RADHA",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 68.sp,
                        lineHeight = 72.sp,
                        letterSpacing = 4.sp,
                        color = PureWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("focus_radha_title")
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Today's Count Big High-Contrast Pill
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = JetBlack,
                    border = BorderStroke(1.5.dp, PureWhite.copy(alpha = 0.35f)),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 36.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TODAY'S COUNT",
                            style = MaterialTheme.typography.labelSmall,
                            color = SubtleWhite,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatNumber(uiState.todayCount),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 56.sp,
                            lineHeight = 60.sp,
                            color = PureWhite,
                            modifier = Modifier.testTag("focus_today_count")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "MALA ${uiState.todayMalas}  ·  BEAD ${uiState.currentMalaBead} / 108",
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Bottom Daily Goal Progress Bar + Permanent Save Indicator
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = JetBlack.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GOAL ${formatNumber(uiState.todayCount)} / ${formatNumber(uiState.goal.dailyGoal)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite
                        )
                        Text(
                            text = "ALL-TIME: ${formatNumber(uiState.allTimeCount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SubtleWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(PureWhite.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(goalFill.coerceIn(0.02f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(PureWhite)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingTapMark(effect: TapPulseEffect) {
    val progress = remember { Animatable(0f) }
    val density = LocalDensity.current
    LaunchedEffect(effect.id) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing))
    }
    val liftPx = with(density) { (progress.value * 76f).dp.toPx() }
    val alpha = (1f - progress.value).coerceIn(0f, 1f)

    Text(
        text = "+1 राधा",
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        color = PureWhite,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (effect.position.x - 48f).roundToInt(),
                    y = (effect.position.y - liftPx - 36f).roundToInt()
                )
            }
            .alpha(alpha)
    )
}

private fun formatNumber(value: Int): String =
    NumberFormat.getNumberInstance(Locale.US).format(value)
