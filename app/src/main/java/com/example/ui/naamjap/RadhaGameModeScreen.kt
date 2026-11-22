package com.example.ui.naamjap

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

private enum class GamePace(val label: String, val speedMultiplier: Float, val spawnIntervalSec: Float) {
    CALM("CALM", 0.72f, 1.05f),
    FLOW("FLOW", 1.0f, 0.78f),
    FAST("FAST", 1.38f, 0.56f)
}

private data class RadhaOrb(
    val id: Long,
    val xFraction: Float,   // 0.14..0.86
    var yFraction: Float,   // 1.08 down to -0.12
    val speedPerSec: Float, // fraction of height per second
    val radiusDp: Float,
    val isSacredWhite: Boolean,
    val displayWord: String
)

private data class CatchBurst(
    val id: Long,
    val xPx: Float,
    val yPx: Float,
    var ageSec: Float = 0f,
    val label: String = "+1 राधा"
)

@Composable
fun RadhaGameModeScreen(
    uiState: NaamJapUiState,
    onOrbCaught: () -> Unit,
    onExit: () -> Unit
) {
    BackHandler { onExit() }

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var pace by remember { mutableStateOf(GamePace.FLOW) }
    var sessionCombo by remember { mutableIntStateOf(0) }
    var bestCombo by remember { mutableIntStateOf(0) }
    var sessionCaught by remember { mutableIntStateOf(0) }
    var showMalaCelebration by remember { mutableStateOf(false) }

    val orbs = remember { mutableStateListOf<RadhaOrb>() }
    val bursts = remember { mutableStateListOf<CatchBurst>() }
    var nextId by remember { mutableLongStateOf(1L) }

    LaunchedEffect(uiState.todayCount) {
        if (uiState.todayCount > 0 && uiState.todayCount % 108 == 0 && sessionCaught > 0) {
            showMalaCelebration = true
            if (uiState.goal.hapticEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            delay(2200L)
            showMalaCelebration = false
        }
    }

    // 60fps smooth game loop spawning and floating RADHA orbs upward
    LaunchedEffect(pace) {
        var lastFrameNanos = 0L
        var spawnAccumulator = 0.65f
        val lanes = listOf(0.18f, 0.34f, 0.50f, 0.66f, 0.82f)
        var lastLaneIdx = -1

        // Seed initial 3 orbs immediately so the arena feels alive right away
        if (orbs.isEmpty()) {
            listOf(0.24f to 0.72f, 0.52f to 0.54f, 0.78f to 0.82f).forEachIndexed { i, (x, y) ->
                orbs.add(
                    RadhaOrb(
                        id = nextId++,
                        xFraction = x,
                        yFraction = y,
                        speedPerSec = (0.17f + i * 0.02f) * pace.speedMultiplier,
                        radiusDp = 42f,
                        isSacredWhite = i % 2 == 0,
                        displayWord = if (i % 2 == 0) "राधा" else "RADHA"
                    )
                )
            }
        }

        while (true) {
            val frameNanos = withFrameNanos { it }
            if (lastFrameNanos == 0L) {
                lastFrameNanos = frameNanos
                continue
            }
            val dt = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
            lastFrameNanos = frameNanos

            // Advance orbs upward
            val orbIter = orbs.listIterator()
            while (orbIter.hasNext()) {
                val orb = orbIter.next()
                val nextY = orb.yFraction - (orb.speedPerSec * dt)
                if (nextY < -0.14f) {
                    orbIter.remove()
                } else {
                    orbIter.set(orb.copy(yFraction = nextY))
                }
            }

            // Advance burst animations
            val burstIter = bursts.listIterator()
            while (burstIter.hasNext()) {
                val burst = burstIter.next()
                val nextAge = burst.ageSec + dt
                if (nextAge >= 0.65f) {
                    burstIter.remove()
                } else {
                    burstIter.set(burst.copy(ageSec = nextAge))
                }
            }

            // Spawn new RADHA orbs gently
            spawnAccumulator += dt
            if (spawnAccumulator >= pace.spawnIntervalSec && orbs.size < 7) {
                spawnAccumulator = 0f
                var laneIdx = Random.nextInt(lanes.size)
                if (laneIdx == lastLaneIdx) laneIdx = (laneIdx + 1) % lanes.size
                lastLaneIdx = laneIdx
                val sacredWhite = Random.nextFloat() < 0.48f
                orbs.add(
                    RadhaOrb(
                        id = nextId++,
                        xFraction = lanes[laneIdx] + (Random.nextFloat() * 0.06f - 0.03f),
                        yFraction = 1.08f,
                        speedPerSec = (0.16f + Random.nextFloat() * 0.09f) * pace.speedMultiplier,
                        radiusDp = if (sacredWhite) 44f else 40f,
                        isSacredWhite = sacredWhite,
                        displayWord = if (sacredWhite) "राधा" else "RADHA"
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CrimsonObsidian, CrimsonDeep, CrimsonPrimary)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Game HUD Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "RADHA MALA FLOW",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PureWhite
                        ) {
                            Text(
                                text = "GAME",
                                style = MaterialTheme.typography.labelSmall,
                                color = JetBlack,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Tap floating RADHA orbs · Every tap saves +1 real count",
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtleWhite,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = PureWhite
                ) {
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("game_exit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Exit Radha Game Mode",
                            tint = JetBlack
                        )
                    }
                }
            }

            // Score & Pace Controls Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Live Count & Mala Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = JetBlack,
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "TODAY",
                                style = MaterialTheme.typography.labelSmall,
                                color = SubtleWhite,
                                fontSize = 8.sp
                            )
                            Text(
                                text = NumberFormat.getNumberInstance(Locale.US).format(uiState.todayCount),
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = PureWhite,
                                modifier = Modifier.testTag("game_today_count")
                            )
                        }
                        Column {
                            Text(
                                text = "MALA",
                                style = MaterialTheme.typography.labelSmall,
                                color = SubtleWhite,
                                fontSize = 8.sp
                            )
                            Text(
                                text = "${uiState.currentMalaBead}/108",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = CrimsonVivid
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "x$sessionCombo",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = PureWhite
                            )
                        }
                    }
                }

                // Pace Selector (CALM | FLOW | FAST)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = JetBlack.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        GamePace.entries.forEach { p ->
                            val selected = p == pace
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) PureWhite else Color.Transparent)
                                    .clickable { pace = p }
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = p.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) JetBlack else PureWhite,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Floating Orbs Arena
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("radha_game_arena")
            ) {
                val arenaW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val arenaH = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                // Catch detection across entire arena (generous hit radius for effortless joyful tapping!)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(arenaW, arenaH, uiState.goal.hapticEnabled) {
                            detectTapGestures { tapOffset ->
                                var bestMatchIdx = -1
                                var bestDist = Float.MAX_VALUE
                                orbs.forEachIndexed { idx, orb ->
                                    val cx = orb.xFraction * arenaW
                                    val cy = orb.yFraction * arenaH
                                    val hitRadiusPx = with(density) { (orb.radiusDp + 22f).dp.toPx() }
                                    val dist = hypot(tapOffset.x - cx, tapOffset.y - cy)
                                    if (dist <= hitRadiusPx && dist < bestDist) {
                                        bestDist = dist
                                        bestMatchIdx = idx
                                    }
                                }
                                if (bestMatchIdx >= 0) {
                                    val caught = orbs.removeAt(bestMatchIdx)
                                    sessionCaught++
                                    sessionCombo++
                                    if (sessionCombo > bestCombo) bestCombo = sessionCombo
                                    if (uiState.goal.hapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onOrbCaught()
                                    bursts.add(
                                        CatchBurst(
                                            id = nextId++,
                                            xPx = caught.xFraction * arenaW,
                                            yPx = caught.yFraction * arenaH,
                                            label = "+1 ${caught.displayWord}"
                                        )
                                    )
                                }
                            }
                        }
                ) {
                    // Decorative arena guide rings & burst ripples
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            color = PureWhite.copy(alpha = 0.06f),
                            radius = size.minDimension * 0.42f,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = PureWhite.copy(alpha = 0.04f),
                            radius = size.minDimension * 0.25f,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )

                        bursts.forEach { burst ->
                            val t = (burst.ageSec / 0.65f).coerceIn(0f, 1f)
                            val radius = (32f + t * 88f).dp.toPx()
                            drawCircle(
                                color = PureWhite.copy(alpha = (1f - t) * 0.85f),
                                radius = radius,
                                center = Offset(burst.xPx, burst.yPx),
                                style = Stroke(width = (3f * (1f - t) + 1f).dp.toPx())
                            )
                        }
                    }

                    // Render Floating RADHA Orbs
                    orbs.forEach { orb ->
                        val diameterDp = (orb.radiusDp * 2f).dp
                        val radiusPx = with(density) { orb.radiusDp.dp.toPx() }
                        val offsetX = (orb.xFraction * arenaW - radiusPx).roundToInt()
                        val offsetY = (orb.yFraction * arenaH - radiusPx).roundToInt()

                        Surface(
                            modifier = Modifier
                                .offset { IntOffset(offsetX, offsetY) }
                                .size(diameterDp),
                            shape = CircleShape,
                            color = if (orb.isSacredWhite) PureWhite else JetBlack,
                            border = BorderStroke(
                                width = 2.5.dp,
                                color = if (orb.isSacredWhite) JetBlack else PureWhite
                            ),
                            shadowElevation = 8.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = orb.displayWord,
                                        fontFamily = if (orb.displayWord == "RADHA") SpaceGroteskFontFamily else null,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = if (orb.displayWord == "RADHA") 17.sp else 22.sp,
                                        color = if (orb.isSacredWhite) CrimsonPrimary else PureWhite
                                    )
                                    Text(
                                        text = "+1",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (orb.isSacredWhite) JetBlack else SubtleWhite
                                    )
                                }
                            }
                        }
                    }

                    // Render Floating "+1 राधा" Catch Labels
                    bursts.forEach { burst ->
                        val t = (burst.ageSec / 0.65f).coerceIn(0f, 1f)
                        val liftPx = with(density) { (t * 60f).dp.toPx() }
                        Text(
                            text = burst.label,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = PureWhite.copy(alpha = (1f - t).coerceIn(0f, 1f)),
                            modifier = Modifier.offset {
                                IntOffset(
                                    x = (burst.xPx - 44f).roundToInt(),
                                    y = (burst.yPx - liftPx - 24f).roundToInt()
                                )
                            }
                        )
                    }

                    // Mala Completion Celebration Toast
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showMalaCelebration,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PureWhite,
                            shadowElevation = 8.dp
                        ) {
                            Text(
                                text = "108 MALA COMPLETE · RADHE RADHE",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrimsonPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Session Footer
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(18.dp),
                color = JetBlack.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SESSION: +$sessionCaught",
                        style = MaterialTheme.typography.labelMedium,
                        color = PureWhite
                    )
                    Text(
                        text = "GOAL: ${uiState.goalProgressPercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = CrimsonVivid
                    )
                    Text(
                        text = "ALL-TIME: ${NumberFormat.getNumberInstance(Locale.US).format(uiState.allTimeCount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SubtleWhite
                    )
                }
            }
        }
    }
}
