package com.example.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DailyPlanBlockEntity
import com.example.ui.AppTab
import com.example.ui.NaamJapUiState
import com.example.ui.PlansUiState
import com.example.ui.SleepUiState
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
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeDashboardScreen(
    sleepState: SleepUiState,
    naamJapState: NaamJapUiState,
    plansState: PlansUiState,
    onQuickRadhaTap: () -> Unit,
    onOpenFullScreenJap: () -> Unit,
    onOpenRadhaGame: () -> Unit,
    onToggleSlotGuardNow: () -> Unit,
    onPreviewNaamJapBlocker: () -> Unit,
    onAddWaterQuick: (Int) -> Unit,
    onOpenAddSlot: () -> Unit,
    onEditSlot: (DailyPlanBlockEntity) -> Unit,
    onToggleSlotDone: (DailyPlanBlockEntity) -> Unit,
    onOpenPermissionsSheet: () -> Unit,
    onNavigateTab: (AppTab) -> Unit,
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
            item(key = "home_top_bar") {
                HomeTopCommandBar(
                    clockHHMM = sleepState.liveClock.hhmm,
                    clockSS = sleepState.liveClock.ss,
                    permissionsReady = sleepState.permissions.hasUsageStatsAccess && sleepState.permissions.hasOverlayPermission,
                    onOpenPermissions = onOpenPermissionsSheet,
                    onOpenAddSlot = onOpenAddSlot
                )
            }

            // 1. HERO CARD (Crimson Red): LIVE RADHA NAAM JAP COUNTER + ACTIVE SLOT LOCK BANNER
            item(key = "home_hero_naam_jap") {
                HomeNaamJapAndSlotHeroCard(
                    sleepState = sleepState,
                    naamJapState = naamJapState,
                    plansState = plansState,
                    onQuickRadhaTap = onQuickRadhaTap,
                    onOpenFullScreenJap = onOpenFullScreenJap,
                    onOpenRadhaGame = onOpenRadhaGame,
                    onToggleSlotGuardNow = onToggleSlotGuardNow,
                    onPreviewNaamJapBlocker = onPreviewNaamJapBlocker
                )
            }

            // 2. MIDDLE CARD (Pure White): ALL-IN-ONE 24H PROGRESS MATRIX
            item(key = "home_all_progress") {
                HomeAllProgressCard(
                    sleepState = sleepState,
                    naamJapState = naamJapState,
                    plansState = plansState,
                    onAddWaterQuick = onAddWaterQuick,
                    onNavigateTab = onNavigateTab
                )
            }

            // 3. BOTTOM CARD (Obsidian Jet Black): LIVE 24H DINCHARYA SLOTS & ALLOWED APPS
            item(key = "home_dincharya_slots") {
                HomeSlotsTimelineCard(
                    plansState = plansState,
                    onOpenAddSlot = onOpenAddSlot,
                    onEditSlot = onEditSlot,
                    onToggleSlotDone = onToggleSlotDone,
                    onOpenAllPlans = { onNavigateTab(AppTab.PLANS) }
                )
            }
        }
    }
}

@Composable
private fun HomeTopCommandBar(
    clockHHMM: String,
    clockSS: String,
    permissionsReady: Boolean,
    onOpenPermissions: () -> Unit,
    onOpenAddSlot: () -> Unit
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
                border = BorderStroke(1.2.dp, PureWhite.copy(alpha = 0.28f))
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "24H",
                        fontFamily = JetBrainsMonoFontFamily,
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
                        text = "24 HRS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Surface(shape = RoundedCornerShape(6.dp), color = PureWhite) {
                        Text(
                            text = "$clockHHMM:$clockSS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = JetBlack,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "All Progress · Naam Jap · 24H Slot Lock",
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
                shape = CircleShape,
                color = if (permissionsReady) JetBlack else PureWhite,
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.28f))
            ) {
                IconButton(
                    onClick = onOpenPermissions,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("home_permissions_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = "Guard Permissions",
                        tint = if (permissionsReady) PureWhite else CrimsonPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(shape = CircleShape, color = PureWhite) {
                IconButton(
                    onClick = onOpenAddSlot,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("home_add_slot_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add new time slot",
                        tint = JetBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeNaamJapAndSlotHeroCard(
    sleepState: SleepUiState,
    naamJapState: NaamJapUiState,
    plansState: PlansUiState,
    onQuickRadhaTap: () -> Unit,
    onOpenFullScreenJap: () -> Unit,
    onOpenRadhaGame: () -> Unit,
    onToggleSlotGuardNow: () -> Unit,
    onPreviewNaamJapBlocker: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val fmt = remember { NumberFormat.getNumberInstance(Locale.US) }
    var pulseKey by remember { mutableIntStateOf(0) }
    var isPulsing by remember { mutableStateOf(false) }

    LaunchedEffect(pulseKey) {
        if (pulseKey > 0) {
            isPulsing = true
            delay(100L)
            isPulsing = false
        }
    }

    val radhaScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.07f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "homeRadhaScale"
    )
    val malaFill by animateFloatAsState(
        targetValue = (naamJapState.currentMalaBead / 108f).coerceIn(0f, 1f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "homeMalaBar"
    )

    val activeSlot = plansState.activeSlotNow
    val guardActive = plansState.studyConfig.isActiveNow || sleepState.timerDisplay.isBedtimeActive
    val statusBadgeText = when {
        sleepState.timerDisplay.isBedtimeActive -> "BEDTIME GUARD ACTIVE"
        activeSlot != null && activeSlot.strictAppLockEnabled ->
            "NOW: ${activeSlot.title.uppercase()} · ${activeSlot.allowedAppsCount} APPS"
        plansState.studyConfig.isActiveNow -> "SLOT & STUDY LOCK ACTIVE"
        activeSlot != null -> "NOW: ${activeSlot.title.uppercase()}"
        else -> "24H NAAM JAP BLOCKER READY"
    }

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
                    colors = listOf(PureWhite.copy(alpha = 0.40f), JetBlack.copy(alpha = 0.45f))
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
                .alpha(0.24f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top active slot + preview blocker pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (guardActive) PureWhite else JetBlack,
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.30f)),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (guardActive) Icons.Rounded.Lock else Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = if (guardActive) CrimsonPrimary else PureWhite,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (guardActive) JetBlack else PureWhite,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = JetBlack.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.28f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onPreviewNaamJapBlocker)
                        .testTag("home_preview_blocker_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Visibility, contentDescription = null, tint = PureWhite, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TEST LOCK", style = MaterialTheme.typography.labelSmall, color = PureWhite, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Centerpiece Interactive RADHA NAAM JAP Counter on Home
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "राधा",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 42.sp,
                        lineHeight = 46.sp,
                        color = PureWhite,
                        modifier = Modifier.scale(radhaScale)
                    )
                    Text(
                        text = "AAJ KA NAAM JAP",
                        style = MaterialTheme.typography.labelSmall,
                        color = SubtleWhite,
                        fontSize = 10.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = fmt.format(naamJapState.todayCount),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 44.sp,
                        lineHeight = 46.sp,
                        letterSpacing = (-1.5).sp,
                        color = PureWhite,
                        modifier = Modifier.testTag("home_today_naam_jap_count")
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = JetBlack.copy(alpha = 0.50f)
                    ) {
                        Text(
                            text = "ALL TIME: ${fmt.format(naamJapState.allTimeCount)} · ${naamJapState.todayMalas} MALAS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current 108-Bead Mala Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(JetBlack.copy(alpha = 0.45f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(malaFill.coerceIn(0.03f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(PureWhite)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mala Bead ${naamJapState.currentMalaBead} / 108",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = SubtleWhite
                )
                Text(
                    text = "Goal: ${fmt.format(plansState.disciplineConfig.naamJapReminderTarget)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = SubtleWhite
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Tactile +1 RADHA Tap Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    pulseKey++
                    onQuickRadhaTap()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("home_radha_tap_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JetBlack,
                    contentColor = PureWhite
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Rounded.TouchApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TAP +1  राधा  (SAVE PERMANENTLY)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Action Row: Full-Screen Jap | Radha Game | 24H Slot Lock Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeHeroActionPill(
                    icon = Icons.Rounded.Fullscreen,
                    label = "FULL JAP",
                    highlighted = false,
                    onClick = onOpenFullScreenJap,
                    modifier = Modifier.weight(1f),
                    tag = "home_fullscreen_jap_button"
                )
                HomeHeroActionPill(
                    icon = Icons.Rounded.SportsEsports,
                    label = "RADHA GAME",
                    highlighted = false,
                    onClick = onOpenRadhaGame,
                    modifier = Modifier.weight(1f),
                    tag = "home_radha_game_button"
                )
                HomeHeroActionPill(
                    icon = if (plansState.studyConfig.isActiveNow) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    label = if (plansState.studyConfig.isActiveNow) "LOCK ON" else "START LOCK",
                    highlighted = plansState.studyConfig.isActiveNow,
                    onClick = onToggleSlotGuardNow,
                    modifier = Modifier.weight(1f),
                    tag = "home_toggle_slot_lock_button"
                )
            }
        }
    }
}

@Composable
private fun HomeHeroActionPill(
    icon: ImageVector,
    label: String,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        color = if (highlighted) PureWhite else PureWhite.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, if (highlighted) PureWhite else PureWhite.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlighted) CrimsonPrimary else PureWhite,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (highlighted) JetBlack else PureWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HomeAllProgressCard(
    sleepState: SleepUiState,
    naamJapState: NaamJapUiState,
    plansState: PlansUiState,
    onAddWaterQuick: (Int) -> Unit,
    onNavigateTab: (AppTab) -> Unit
) {
    val fmt = remember { NumberFormat.getNumberInstance(Locale.US) }
    val disc = plansState.disciplineConfig

    val japFraction = plansState.naamJapProgressFraction
    val waterFraction = disc.waterProgressFraction
    val sleepGoalMins = sleepState.goal.targetDurationMinutes.coerceAtLeast(60)
    val latestSleepMins = sleepState.selectedAnalysis?.day?.durationMinutes
        ?: sleepState.weeklySummary.avgNightMinutes
    val sleepFraction = (latestSleepMins.toFloat() / sleepGoalMins).coerceIn(0f, 1f)
    val slotsFraction = if (plansState.blocks.isEmpty()) {
        0f
    } else {
        (plansState.completedBlocksToday.toFloat() / plansState.blocks.size).coerceIn(0f, 1f)
    }

    val overallPercent = (((japFraction + waterFraction + sleepFraction + slotsFraction) / 4f) * 100f)
        .roundToInt()
        .coerceIn(0, 100)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ALL PROGRESS · TODAY",
                        style = MaterialTheme.typography.titleMedium,
                        color = JetBlack,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Live synced from Naam Jap, Health, Sleep & 24H Plans",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        fontSize = 11.sp
                    )
                }

                Surface(shape = RoundedCornerShape(50), color = CrimsonPrimary) {
                    Text(
                        text = "$overallPercent% DAY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 1: Naam Jap + Water/Meals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgressMetricTile(
                    icon = Icons.Rounded.AutoAwesome,
                    title = "NAAM JAP",
                    bigValue = fmt.format(naamJapState.todayCount),
                    subtitle = "Target ${fmt.format(disc.naamJapReminderTarget)} · ${naamJapState.todayMalas} Malas",
                    progress = japFraction,
                    actionChipLabel = "OPEN JAP",
                    onTileClick = { onNavigateTab(AppTab.NAAM_JAP) },
                    onActionChipClick = { onNavigateTab(AppTab.NAAM_JAP) },
                    modifier = Modifier.weight(1f)
                )

                ProgressMetricTile(
                    icon = Icons.Rounded.WaterDrop,
                    title = "WATER & MEALS",
                    bigValue = String.format(Locale.US, "%.2fL", disc.effectiveWaterMl / 1000f),
                    subtitle = "Goal ${String.format(Locale.US, "%.1fL", disc.waterGoalMl / 1000f)} · Meals ${disc.effectiveLightMeals}/${disc.lightMealsTarget}",
                    progress = waterFraction,
                    actionChipLabel = "+250 ML",
                    onTileClick = { onNavigateTab(AppTab.PLANS) },
                    onActionChipClick = { onAddWaterQuick(250) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Sleep Guard + Dincharya Slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val sleepText = if (latestSleepMins > 0) {
                    "${latestSleepMins / 60}h ${latestSleepMins % 60}m"
                } else {
                    "0h 0m"
                }
                ProgressMetricTile(
                    icon = Icons.Rounded.Bedtime,
                    title = "SLEEP GUARD",
                    bigValue = sleepText,
                    subtitle = if (sleepState.timerDisplay.isBedtimeActive) {
                        "ACTIVE · ${sleepState.goal.targetBedtimeFormatted}-${sleepState.goal.targetWakeFormatted}"
                    } else {
                        "Bed ${sleepState.goal.targetBedtimeFormatted}-${sleepState.goal.targetWakeFormatted}"
                    },
                    progress = sleepFraction,
                    actionChipLabel = "SLEEP TAB",
                    onTileClick = { onNavigateTab(AppTab.SLEEP) },
                    onActionChipClick = { onNavigateTab(AppTab.SLEEP) },
                    modifier = Modifier.weight(1f)
                )

                ProgressMetricTile(
                    icon = Icons.Rounded.EventNote,
                    title = "24H DINCHARYA",
                    bigValue = "${plansState.completedBlocksToday}/${plansState.blocks.size}",
                    subtitle = "${plansState.strictLockedSlotsCount} Locked Slots · ${plansState.studyConfig.effectiveBlockedToday} Blocked",
                    progress = slotsFraction,
                    actionChipLabel = "PLANS TAB",
                    onTileClick = { onNavigateTab(AppTab.PLANS) },
                    onActionChipClick = { onNavigateTab(AppTab.PLANS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProgressMetricTile(
    icon: ImageVector,
    title: String,
    bigValue: String,
    subtitle: String,
    progress: Float,
    actionChipLabel: String,
    onTileClick: () -> Unit,
    onActionChipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedFill by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "tileProgress_$title"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onTileClick),
        shape = RoundedCornerShape(18.dp),
        color = CrimsonSoftTint.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, CrimsonBorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(icon, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(15.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = JetBlack,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = bigValue,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = JetBlack,
                maxLines = 1
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LightTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFill.coerceIn(0.04f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (progress >= 1f) JetBlack else CrimsonPrimary)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = JetBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionChipClick)
            ) {
                Text(
                    text = actionChipLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeSlotsTimelineCard(
    plansState: PlansUiState,
    onOpenAddSlot: () -> Unit,
    onEditSlot: (DailyPlanBlockEntity) -> Unit,
    onToggleSlotDone: (DailyPlanBlockEntity) -> Unit,
    onOpenAllPlans: () -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "24H DINCHARYA & APP SLOTS",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Create unlimited slots · Only allowed apps open; others show Radha Naam Jap",
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtleWhite,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CrimsonPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenAddSlot)
                        .testTag("home_create_slot_pill")
                ) {
                    Text(
                        text = "+ SLOT",
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (plansState.blocks.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = PureWhite.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.16f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Time Slots Added Yet",
                            style = MaterialTheme.typography.titleSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add unlimited slots like '17:00 - 19:00 Film Watching' and toggle ON only the apps allowed in that time!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenAddSlot,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary,
                                contentColor = PureWhite
                            )
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CREATE FIRST TIME SLOT", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            } else {
                val previewBlocks = remember(plansState.blocks, plansState.currentHour, plansState.currentMinute) {
                    val active = plansState.blocks.filter {
                        it.isCurrentTimeInside(plansState.currentHour, plansState.currentMinute)
                    }
                    val others = plansState.blocks.filterNot { it in active }
                    (active + others).take(4)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    previewBlocks.forEach { block ->
                        val isNow = block.isCurrentTimeInside(plansState.currentHour, plansState.currentMinute)
                        val isDone = block.isCompletedToday()
                        HomeCompactSlotRow(
                            block = block,
                            isNow = isNow,
                            isDone = isDone,
                            onToggleDone = { onToggleSlotDone(block) },
                            onEdit = { onEditSlot(block) }
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
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenAllPlans)
                    ) {
                        Text(
                            text = "ALL ${plansState.blocks.size} SLOTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold,
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
                            .clickable(onClick = onOpenAddSlot)
                    ) {
                        Text(
                            text = "+ NEW SLOT",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold,
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
private fun HomeCompactSlotRow(
    block: DailyPlanBlockEntity,
    isNow: Boolean,
    isDone: Boolean,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(15.dp),
        color = if (isNow) PureWhite else PureWhite.copy(alpha = 0.08f),
        border = BorderStroke(
            width = if (isNow) 2.dp else 1.dp,
            color = if (isNow) CrimsonVivid else PureWhite.copy(alpha = 0.14f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleDone,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Toggle slot done",
                    tint = if (isDone) CrimsonPrimary else if (isNow) JetBlack else PureWhite,
                    modifier = Modifier.size(20.dp)
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
                        color = if (isNow) CrimsonPrimary else FadedWhite
                    )
                    if (block.strictAppLockEnabled) {
                        Surface(shape = RoundedCornerShape(5.dp), color = CrimsonPrimary) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Block, contentDescription = null, tint = PureWhite, modifier = Modifier.size(9.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${block.allowedAppsCount} APPS ONLY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PureWhite,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                    if (isNow) {
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
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isNow) JetBlack else PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (block.notes.isNotEmpty()) {
                    Text(
                        text = block.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isNow) CharcoalInk else SubtleWhite,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
