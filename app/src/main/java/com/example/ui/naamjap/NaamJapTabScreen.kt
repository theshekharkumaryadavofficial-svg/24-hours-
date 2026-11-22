package com.example.ui.naamjap

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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.NaamJapDayEntity
import com.example.ui.NaamJapUiState
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
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.SubtleWhite
import java.text.NumberFormat
import java.util.Locale

@Composable
fun NaamJapTabContent(
    uiState: NaamJapUiState,
    naamShaktiBadgeText: String,
    onQuickIncrement: () -> Unit,
    onUndoDecrement: () -> Unit,
    onOpenFocusMode: () -> Unit,
    onOpenGameMode: () -> Unit,
    onOpenNaamShaktiMode: () -> Unit,
    onOpenGoalSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

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
            item(key = "nj_header") {
                NaamJapHeaderBar(
                    onOpenGoalSheet = onOpenGoalSheet
                )
            }

            // 1. TOP: All-Over (Lifetime) Naam Jap Count + Mode Launch Buttons (Red Hero Card)
            item(key = "nj_all_time_hero") {
                AllTimeNaamJapHeroCard(
                    uiState = uiState,
                    naamShaktiBadgeText = naamShaktiBadgeText,
                    onOpenFocusMode = onOpenFocusMode,
                    onOpenGameMode = onOpenGameMode,
                    onOpenNaamShaktiMode = onOpenNaamShaktiMode
                )
            }

            // 2. MIDDLE: Today's Count (Aaj Ka Count) + 108 Mala Progress + Daily Goal (Pure White Card)
            item(key = "nj_today_card") {
                TodayNaamJapCard(
                    uiState = uiState,
                    onQuickTap = {
                        if (uiState.goal.hapticEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onQuickIncrement()
                    },
                    onUndo = onUndoDecrement,
                    onEditGoal = onOpenGoalSheet
                )
            }

            // 3. BOTTOM: Permanent Real Naam Jap History (Jet Black Obsidian Card)
            item(key = "nj_history_card") {
                NaamJapHistoryCard(
                    history = uiState.history
                )
            }
        }
    }
}

@Composable
private fun NaamJapHeaderBar(
    onOpenGoalSheet: () -> Unit
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
                    Text(
                        text = "राधा",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = PureWhite
                    )
                }
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "NAAM JAP",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PureWhite
                    ) {
                        Text(
                            text = "RADHA",
                            style = MaterialTheme.typography.labelSmall,
                            color = JetBlack,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Permanent Sankalp & Radha Mala Flow",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite,
                    fontSize = 11.sp
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = JetBlack,
            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f)),
            modifier = Modifier
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onOpenGoalSheet)
                .testTag("naam_jap_header_goal_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Flag,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Goal",
                    style = MaterialTheme.typography.labelMedium,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun AllTimeNaamJapHeroCard(
    uiState: NaamJapUiState,
    naamShaktiBadgeText: String,
    onOpenFocusMode: () -> Unit,
    onOpenGameMode: () -> Unit,
    onOpenNaamShaktiMode: () -> Unit
) {
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
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = JetBlack.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "ALL-OVER NAAM JAP COUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = PureWhite,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Huge Lifetime Count
            Text(
                text = formatCount(uiState.allTimeCount),
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 54.sp,
                lineHeight = 58.sp,
                letterSpacing = (-1.5).sp,
                color = PureWhite,
                modifier = Modifier.testTag("naam_jap_all_time_count")
            )

            Text(
                text = "राधा नाम · PERMANENT LIFETIME TOTAL",
                style = MaterialTheme.typography.labelSmall,
                color = SubtleWhite,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Lifetime Mini Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeroMiniStat(
                    label = "TOTAL MALAS",
                    value = "${uiState.allTimeMalas}",
                    modifier = Modifier.weight(1f)
                )
                HeroMiniStat(
                    label = "ACTIVE DAYS",
                    value = "${uiState.activeDaysCount}",
                    modifier = Modifier.weight(1f)
                )
                HeroMiniStat(
                    label = "DAILY GOAL",
                    value = formatCount(uiState.goal.dailyGoal),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary CTA: NAAM JAP MODE ON (Full Screen Clean Tap Mode)
            Button(
                onClick = onOpenFocusMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("naam_jap_mode_on_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JetBlack,
                    contentColor = PureWhite
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NAAM JAP MODE ON",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary CTA: RADHA GAMEPLAY MODE
            Button(
                onClick = onOpenGameMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("radha_game_mode_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = JetBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.SportsEsports,
                    contentDescription = null,
                    tint = CrimsonPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY RADHA MALA GAME",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = JetBlack
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // NAAM SHAKTI ⚡ Anime Lightning RPG Gameplay Mode CTA
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenNaamShaktiMode)
                    .testTag("naam_shakti_game_mode_button"),
                shape = RoundedCornerShape(18.dp),
                color = JetBlack,
                border = BorderStroke(1.6.dp, PureWhite.copy(alpha = 0.72f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CrimsonPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.padding(8.dp).size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "NAAM SHAKTI ⚡ ANIME GAME",
                                style = MaterialTheme.typography.labelLarge,
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "10,000 Naam Unlock · Lightning Warriors & Bosses",
                                style = MaterialTheme.typography.bodySmall,
                                color = SubtleWhite,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PureWhite
                    ) {
                        Text(
                            text = naamShaktiBadgeText,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = JetBlack,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = JetBlack.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SubtleWhite,
                fontSize = 8.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = PureWhite,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TodayNaamJapCard(
    uiState: NaamJapUiState,
    onQuickTap: () -> Unit,
    onUndo: () -> Unit,
    onEditGoal: () -> Unit
) {
    val malaSweep by animateFloatAsState(
        targetValue = (uiState.currentMalaBead / 108f) * 280f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "todayMalaSweep"
    )
    val goalFill by animateFloatAsState(
        targetValue = uiState.goalProgressFraction,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "todayGoalFill"
    )

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
                    Surface(shape = RoundedCornerShape(8.dp), color = CrimsonPrimary) {
                        Text(
                            text = "TODAY",
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AAJ KA NAAM JAP",
                            style = MaterialTheme.typography.titleMedium,
                            color = JetBlack,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Saved permanently on every tap",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk
                        )
                    }
                }

                if (uiState.todayCount > 0) {
                    Surface(shape = CircleShape, color = CrimsonSoftTint) {
                        IconButton(
                            onClick = onUndo,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Undo,
                                contentDescription = "Undo 1 count",
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Today Count + 108 Mala Ring Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 9.dp.toPx()
                        val inset = stroke / 2f
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        drawArc(
                            color = LightTrack,
                            startAngle = 130f,
                            sweepAngle = 280f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = CrimsonPrimary,
                            startAngle = 130f,
                            sweepAngle = malaSweep,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.currentMalaBead}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = JetBlack
                        )
                        Text(
                            text = "/ 108",
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
                            text = formatCount(uiState.todayCount),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            lineHeight = 40.sp,
                            color = JetBlack,
                            modifier = Modifier.testTag("naam_jap_today_count")
                        )
                        if (uiState.isTodayGoalMet) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = JetBlack,
                                modifier = Modifier.padding(bottom = 5.dp)
                            ) {
                                Text(
                                    text = "GOAL MET",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PureWhite,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Malas Completed Today: ${uiState.todayMalas}",
                        style = MaterialTheme.typography.titleSmall,
                        color = CrimsonPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${108 - uiState.currentMalaBead} beads left in current mala",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Goal Progress Box
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
                        Text(
                            text = "DAILY SANKALP · ${formatCount(uiState.goal.dailyGoal)} (${uiState.goal.targetMalas} Malas)",
                            style = MaterialTheme.typography.labelMedium,
                            color = JetBlack,
                            fontSize = 11.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = JetBlack,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onEditGoal)
                        ) {
                            Text(
                                text = "SET GOAL",
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
                                .fillMaxWidth(goalFill.coerceIn(0.03f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(if (uiState.isTodayGoalMet) JetBlack else CrimsonPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${uiState.goalProgressPercent}% completed (${formatCount(uiState.todayCount)} / ${formatCount(uiState.goal.dailyGoal)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = JetBlack,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick +1 RADHA Tap Button
            Button(
                onClick = onQuickTap,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("naam_jap_quick_tap_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = PureWhite
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+1  RADHA  (राधा)",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun NaamJapHistoryCard(
    history: List<NaamJapDayEntity>
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
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "NAAM JAP HISTORY",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Permanent daily Radha Naam records",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(50), color = PureWhite) {
                    Text(
                        text = "${history.size} DAYS",
                        style = MaterialTheme.typography.labelSmall,
                        color = JetBlack,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (history.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhite.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 22.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Naam Jap history yet",
                            style = MaterialTheme.typography.titleSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start Naam Jap Mode or Radha Mala Game above. Every single Radha count is saved permanently here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    history.forEachIndexed { idx, entry ->
                        HistoryEntryRow(entry = entry)
                        if (idx < history.lastIndex) {
                            HorizontalDivider(color = PureWhite.copy(alpha = 0.10f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryRow(entry: NaamJapDayEntity) {
    val progress = (entry.count.toFloat() / entry.goalAtDay.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    shape = RoundedCornerShape(10.dp),
                    color = if (entry.isGoalAchieved) PureWhite else CrimsonPrimary
                ) {
                    Text(
                        text = entry.displayDate.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.isGoalAchieved) JetBlack else PureWhite,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = entry.dayName,
                            style = MaterialTheme.typography.titleSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        if (entry.isGoalAchieved) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Daily goal achieved",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "${entry.completedMalas} Malas · Goal ${formatCount(entry.goalAtDay)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FadedWhite,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = formatCount(entry.count),
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(PureWhite.copy(alpha = 0.14f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0.04f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(if (entry.isGoalAchieved) PureWhite else CrimsonVivid)
            )
        }
    }
}

private fun formatCount(value: Int): String =
    NumberFormat.getNumberInstance(Locale.US).format(value)
