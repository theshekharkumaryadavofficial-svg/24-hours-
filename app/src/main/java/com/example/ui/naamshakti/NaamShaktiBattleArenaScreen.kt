package com.example.ui.naamshakti

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.naamshakti.LightningAbility
import com.example.naamshakti.NaamShaktiCatalog
import com.example.naamshakti.NaamShaktiRules
import com.example.naamshakti.StoryMission
import com.example.ui.ActiveBattleState
import com.example.ui.NaamShaktiUiState
import com.example.ui.theme.CrimsonDeep
import com.example.ui.theme.CrimsonObsidian
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonVivid
import com.example.ui.theme.FadedWhite
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite
import kotlin.math.roundToInt
import kotlin.random.Random

// ==================== SCREEN 4 (BATTLE ARENA) & SCREEN 5 (ENERGY RECOVERY) ====================

@Composable
fun NaamShaktiBattleArenaScreen(
    uiState: NaamShaktiUiState,
    onStartMission: (StoryMission) -> Unit,
    onCastAbility: (LightningAbility) -> Unit,
    onExitBattle: () -> Unit,
    onReleaseFinalSpark: () -> Unit,
    onRealNaamJapTap: () -> Unit,
    onOpenFullScreenJap: () -> Unit
) {
    when {
        !uiState.profile.unlockedBattlefield -> {
            BattlefieldLockedGate(
                uiState = uiState,
                onRealNaamJapTap = onRealNaamJapTap,
                onOpenFullScreenJap = onOpenFullScreenJap
            )
        }
        uiState.profile.koRecoveryActive -> {
            EnergyRecoveryModeScreen(
                uiState = uiState,
                onRealNaamJapTap = onRealNaamJapTap,
                onOpenFullScreenJap = onOpenFullScreenJap
            )
        }
        uiState.activeBattle != null -> {
            LiveLightningBattlefield(
                uiState = uiState,
                battle = uiState.activeBattle,
                onCastAbility = onCastAbility,
                onExitBattle = onExitBattle,
                onReplayMission = { onStartMission(uiState.activeBattle.mission) },
                onReleaseFinalSpark = onReleaseFinalSpark,
                onOpenFullScreenJap = onOpenFullScreenJap
            )
        }
        else -> {
            FantasyKingdomsMissionList(
                uiState = uiState,
                onStartMission = onStartMission
            )
        }
    }
}

@Composable
private fun BattlefieldLockedGate(
    uiState: NaamShaktiUiState,
    onRealNaamJapTap: () -> Unit,
    onOpenFullScreenJap: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val fill by animateFloatAsState(
        targetValue = uiState.unlockProgressFraction,
        animationSpec = tween(420),
        label = "lockedGateFill"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(28.dp),
            color = ObsidianCard,
            border = BorderStroke(1.5.dp, CrimsonPrimary.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = CircleShape, color = CrimsonPrimary) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Battlefield locked",
                        tint = PureWhite,
                        modifier = Modifier.padding(16.dp).size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "BATTLEFIELD LOCKED",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Complete 10,000 real Naam Jap to unlock the Dark Fantasy Arenas and charge your Anime Warrior with 10,000 Shakti Points.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${formatShaktiNumber(uiState.unlockProgressCount)} / 10,000",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(PureWhite.copy(alpha = 0.14f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fill.coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(CrimsonPrimary, PureWhite)))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${formatShaktiNumber(uiState.unlockRemainingNaam)} Naam remaining",
                    style = MaterialTheme.typography.labelMedium,
                    color = CrimsonVivid,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRealNaamJapTap()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("locked_gate_radha_tap"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary, contentColor = PureWhite)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+1 REAL RADHA NAAM", fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onOpenFullScreenJap,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Rounded.TouchApp, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Full-Screen Naam Jap Mode", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== SCREEN 5: ANIME KO / ENERGY RECOVERY SCREEN ====================

@Composable
fun EnergyRecoveryModeScreen(
    uiState: NaamShaktiUiState,
    onRealNaamJapTap: () -> Unit,
    onOpenFullScreenJap: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val sweep by animateFloatAsState(
        targetValue = uiState.recoveryProgressFraction * 360f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "recoverySweep"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(28.dp),
            color = ObsidianCard,
            border = BorderStroke(2.dp, CrimsonVivid)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = RoundedCornerShape(50), color = CrimsonPrimary) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Rounded.SelfImprovement, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                        Text(
                            text = "ANIME KO · SHAKTI RECOVERY MODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meditative Recovery Ring with Warrior Portrait
                Box(modifier = Modifier.size(148.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 10.dp.toPx()
                        val inset = stroke / 2f
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        drawArc(
                            color = PureWhite.copy(alpha = 0.14f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = CrimsonVivid,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Image(
                        painter = painterResource(id = uiState.selectedWarrior.portraitRes),
                        contentDescription = uiState.selectedWarrior.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .alpha(0.68f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${uiState.selectedWarrior.name} is Resting",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Shakti Energy reached 0. All special powers are disabled until you chant 1,000 NEW Naam Jap (separate from your spent 10,000).",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${formatShaktiNumber(uiState.recoveryNaamDone)} / ${formatShaktiNumber(NaamShaktiRules.REVIVE_NAAM_TARGET)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = PureWhite,
                    modifier = Modifier.testTag("recovery_new_naam_counter")
                )
                Text(
                    text = "${formatShaktiNumber(uiState.recoveryNaamRemaining)} NEW Naam left to revive with +1,000 Shakti ⚡",
                    style = MaterialTheme.typography.labelMedium,
                    color = CrimsonVivid,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onRealNaamJapTap()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("recovery_radha_tap_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary, contentColor = PureWhite)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+1 NEW RADHA NAAM (REVIVE)", fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onOpenFullScreenJap,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Rounded.TouchApp, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Full-Screen Naam Jap Counter", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== STORY MISSIONS & FANTASY KINGDOMS LIST ====================

@Composable
private fun FantasyKingdomsMissionList(
    uiState: NaamShaktiUiState,
    onStartMission: (StoryMission) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "FANTASY KINGDOMS & BOSS ARENAS",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Use your Shakti Energy wisely across 4 kingdoms and 8 story missions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite
                )
            }

            items(NaamShaktiCatalog.missions, key = { it.index }) { mission ->
                val isCleared = uiState.profile.highestMissionCleared >= mission.index
                val isUnlocked = mission.index == 1 || uiState.profile.highestMissionCleared >= (mission.index - 1)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = if (mission.isBoss) CrimsonDeep.copy(alpha = 0.8f) else ObsidianCard,
                    border = BorderStroke(
                        width = if (mission.isBoss) 1.6.dp else 1.dp,
                        color = if (mission.isBoss) PureWhite.copy(alpha = 0.48f) else CrimsonPrimary.copy(alpha = 0.55f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                    color = if (mission.isBoss) PureWhite else CrimsonPrimary
                                ) {
                                    Text(
                                        text = if (mission.isBoss) "BOSS · M${mission.index}" else "MISSION ${mission.index}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (mission.isBoss) JetBlack else PureWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = mission.kingdomName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SubtleWhite,
                                    fontSize = 10.sp
                                )
                            }
                            if (isCleared) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Mission cleared",
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${mission.missionTitle} — ${mission.enemyName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = mission.loreLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "HP ${formatShaktiNumber(mission.enemyMaxHp)} · ATK ${mission.enemyAttack} · +${mission.xpReward} XP",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = FadedWhite
                            )
                            Button(
                                onClick = { onStartMission(mission) },
                                enabled = isUnlocked,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (mission.isBoss) PureWhite else CrimsonPrimary,
                                    contentColor = if (mission.isBoss) JetBlack else PureWhite,
                                    disabledContainerColor = JetBlack.copy(alpha = 0.5f),
                                    disabledContentColor = FadedWhite
                                ),
                                modifier = Modifier.testTag("start_mission_${mission.index}")
                            ) {
                                Text(
                                    text = if (isUnlocked) "FIGHT ⚡" else "LOCKED",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== LIVE CINEMATIC LIGHTNING BATTLEFIELD ====================

@Composable
private fun LiveLightningBattlefield(
    uiState: NaamShaktiUiState,
    battle: ActiveBattleState,
    onCastAbility: (LightningAbility) -> Unit,
    onExitBattle: () -> Unit,
    onReplayMission: () -> Unit,
    onReleaseFinalSpark: () -> Unit,
    onOpenFullScreenJap: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val boltAlpha = remember { Animatable(0f) }
    val shakeX = remember { Animatable(0f) }

    LaunchedEffect(battle.strikeEventId) {
        if (battle.strikeEventId > 0L) {
            boltAlpha.snapTo(1f)
            shakeX.snapTo(14f)
            shakeX.animateTo(-12f, tween(55))
            shakeX.animateTo(8f, tween(55))
            shakeX.animateTo(0f, tween(70))
            boltAlpha.animateTo(0f, tween(340))
        }
    }

    val auraAnim = rememberInfiniteTransition(label = "warriorAura")
    val auraPulse by auraAnim.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "auraPulse"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cinematic Arena Stage with Animated Canvas Lightning Bolts
            item(key = "arena_stage") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(276.dp)
                        .offset { IntOffset(shakeX.value.roundToInt(), 0) }
                        .clip(RoundedCornerShape(26.dp))
                        .border(2.dp, CrimsonPrimary.copy(alpha = 0.85f), RoundedCornerShape(26.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_naam_shakti_arena_1795308861531),
                        contentDescription = battle.mission.arenaName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        JetBlack.copy(alpha = 0.45f),
                                        CrimsonObsidian.copy(alpha = 0.35f),
                                        JetBlack.copy(alpha = 0.82f)
                                    )
                                )
                            )
                    )

                    // Animated Canvas Lightning & Energy Aura Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Warrior glowing energy aura
                        drawCircle(
                            color = CrimsonVivid.copy(alpha = 0.28f * auraPulse),
                            radius = 74.dp.toPx(),
                            center = Offset(size.width * 0.22f, size.height * 0.66f)
                        )
                        // Strike lightning bolts from sky & warrior to enemy
                        val alpha = boltAlpha.value
                        if (alpha > 0.02f) {
                            val rng = Random(battle.strikeEventId)
                            val boltCount = when (battle.lastAbilityUsed) {
                                LightningAbility.ULTIMATE_POWER -> 5
                                LightningAbility.MEGA_THUNDER -> 4
                                LightningAbility.THUNDER_STRIKE -> 2
                                else -> 1
                            }
                            repeat(boltCount) { idx ->
                                val startX = if (idx == 0) size.width * 0.24f else size.width * (0.55f + rng.nextFloat() * 0.35f)
                                val startY = if (idx == 0) size.height * 0.62f else 0f
                                val endX = size.width * (0.72f + (rng.nextFloat() - 0.5f) * 0.14f)
                                val endY = size.height * 0.34f
                                val path = Path().apply {
                                    moveTo(startX, startY)
                                    val steps = 7
                                    for (s in 1 until steps) {
                                        val t = s / steps.toFloat()
                                        val px = startX + (endX - startX) * t + (rng.nextFloat() - 0.5f) * 34.dp.toPx()
                                        val py = startY + (endY - startY) * t + (rng.nextFloat() - 0.5f) * 18.dp.toPx()
                                        lineTo(px, py)
                                    }
                                    lineTo(endX, endY)
                                }
                                drawPath(
                                    path = path,
                                    color = CrimsonVivid.copy(alpha = alpha * 0.85f),
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawPath(
                                    path = path,
                                    color = PureWhite.copy(alpha = alpha),
                                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            // Impact flash ring on enemy
                            drawCircle(
                                color = PureWhite.copy(alpha = alpha * 0.55f),
                                radius = 58.dp.toPx() * alpha,
                                center = Offset(size.width * 0.75f, size.height * 0.34f)
                            )
                        }
                    }

                    // Top Enemy HUD
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                            .fillMaxWidth(0.62f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (battle.mission.isBoss) CrimsonPrimary else JetBlack.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "${battle.mission.enemyTitle} · ${battle.mission.enemyName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val enemyFraction = (battle.enemyHp.toFloat() / battle.enemyMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(JetBlack.copy(alpha = 0.75f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(enemyFraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(CrimsonVivid)
                            )
                        }
                        Text(
                            text = "HP ${formatShaktiNumber(battle.enemyHp)} / ${formatShaktiNumber(battle.enemyMaxHp)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                    }

                    // Bottom Warrior Portrait & Vitals HUD
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = uiState.selectedWarrior.portraitRes),
                            contentDescription = uiState.selectedWarrior.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(82.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(2.dp, PureWhite.copy(alpha = auraPulse), RoundedCornerShape(18.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = uiState.selectedWarrior.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (battle.warriorShield > 0) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = PureWhite) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(11.dp))
                                            Text(
                                                text = "${battle.warriorShield}",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = JetBlack
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val hpFrac = (battle.warriorHp.toFloat() / battle.warriorMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(PureWhite.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(hpFrac)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(PureWhite)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "HP ${battle.warriorHp}/${battle.warriorMaxHp}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = SubtleWhite
                                )
                                Text(
                                    text = "SHAKTI ${formatShaktiNumber(uiState.profile.currentShaktiEnergy)} ⚡",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CrimsonVivid
                                )
                            }
                        }
                    }
                }
            }

            // Live Battle Log Banner
            item(key = "battle_log") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = battle.battleLog,
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Victory / Retreat / 6 Lightning Abilities Controls
            item(key = "battle_controls") {
                when {
                    battle.victoryAchieved -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            color = CrimsonPrimary,
                            border = BorderStroke(1.5.dp, PureWhite)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "VICTORY ACHIEVED ⚡",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${battle.mission.enemyName} defeated! Earned +${battle.mission.xpReward} Warrior XP · Spent ${formatShaktiNumber(battle.shaktiSpentThisBattle)} Shakti.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PureWhite,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onExitBattle,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = JetBlack, contentColor = PureWhite)
                                ) {
                                    Text("NEXT KINGDOM MISSION", fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                    battle.retreatedByHp -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            color = ObsidianCard,
                            border = BorderStroke(1.5.dp, CrimsonPrimary)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "GUARD BROKEN — REGROUP",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Use Energy Shield (150) and Lightning Dash (200) to block boss counterattacks.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SubtleWhite,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = onExitBattle,
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Missions", color = PureWhite)
                                    }
                                    Button(
                                        onClick = onReplayMission,
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary, contentColor = PureWhite)
                                    ) {
                                        Text("RETRY ⚡", fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            val pairs = LightningAbility.entries.chunked(2)
                            pairs.forEach { rowAbilities ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowAbilities.forEach { ability ->
                                        val canAfford = uiState.profile.currentShaktiEnergy >= ability.shaktiCost
                                        val isUltimate = ability == LightningAbility.ULTIMATE_POWER
                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onCastAbility(ability)
                                            },
                                            enabled = canAfford,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(68.dp)
                                                .testTag("ability_${ability.id}"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isUltimate) PureWhite else CrimsonPrimary,
                                                contentColor = if (isUltimate) JetBlack else PureWhite,
                                                disabledContainerColor = ObsidianCard,
                                                disabledContentColor = FadedWhite
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = ability.title,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${formatShaktiNumber(ability.shaktiCost)} SHAKTI ⚡",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // If Shakti is between 1..49 (cannot cast cheapest 50-cost ability), offer real Naam charge or final spark
                            if (uiState.profile.currentShaktiEnergy in 1..49) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = ObsidianCard,
                                    border = BorderStroke(1.dp, CrimsonVivid)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Low Shakti (${uiState.profile.currentShaktiEnergy} left — need 50 for Lightning Attack)",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = onOpenFullScreenJap,
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = JetBlack)
                                            ) {
                                                Text("Chant Real Naam", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                            }
                                            OutlinedButton(
                                                onClick = onReleaseFinalSpark,
                                                modifier = Modifier.weight(1f),
                                                border = BorderStroke(1.dp, CrimsonVivid)
                                            ) {
                                                Text("Exhaust to KO", color = CrimsonVivid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
