package com.example.ui.naamshakti

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NaamShaktiSubScreen
import com.example.ui.NaamShaktiUiState
import com.example.ui.NaamShaktiViewModel
import com.example.ui.theme.CrimsonDeep
import com.example.ui.theme.CrimsonObsidian
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonVivid
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@Composable
fun NaamShaktiRootScreen(
    uiState: NaamShaktiUiState,
    viewModel: NaamShaktiViewModel,
    onRealNaamJapIncrement: () -> Unit,
    onOpenFullScreenJap: () -> Unit,
    onExitToNaamJapTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.attemptSilentSignIn(context)
    }

    LaunchedEffect(uiState.statusBanner) {
        if (uiState.statusBanner != null) {
            delay(3200L)
            viewModel.clearStatusBanner()
        }
    }

    BackHandler {
        when {
            uiState.showUnlockCelebration -> viewModel.dismissUnlockCelebration(goToBattle = false)
            uiState.activeBattle != null -> viewModel.exitActiveBattle()
            uiState.currentSubScreen != NaamShaktiSubScreen.DASHBOARD ->
                viewModel.selectSubScreen(NaamShaktiSubScreen.DASHBOARD)
            else -> onExitToNaamJapTab()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(JetBlack, CrimsonObsidian, CrimsonDeep.copy(alpha = 0.72f), JetBlack)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Header Bar
            NaamShaktiTopBar(
                uiState = uiState,
                onBack = {
                    if (uiState.activeBattle != null) {
                        viewModel.exitActiveBattle()
                    } else {
                        onExitToNaamJapTab()
                    }
                },
                onToggleSound = viewModel::toggleSound
            )

            // 5-Screen Inner Navigation Strip
            NaamShaktiSubNavBar(
                current = uiState.currentSubScreen,
                koActive = uiState.profile.koRecoveryActive,
                onSelect = viewModel::selectSubScreen
            )

            // Optional floating status banner
            if (uiState.statusBanner != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = CrimsonPrimary,
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = uiState.statusBanner,
                        style = MaterialTheme.typography.labelMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Active Sub-Screen Content
            AnimatedContent(
                targetState = uiState.currentSubScreen,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(160))
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "naamShaktiSubScreen"
            ) { screen ->
                when (screen) {
                    NaamShaktiSubScreen.DASHBOARD -> {
                        NaamShaktiDashboardScreen(
                            uiState = uiState,
                            onRealNaamJapTap = onRealNaamJapIncrement,
                            onOpenFullScreenJap = onOpenFullScreenJap,
                            onGoToBattle = { viewModel.selectSubScreen(NaamShaktiSubScreen.BATTLE) },
                            onGoToWarriors = { viewModel.selectSubScreen(NaamShaktiSubScreen.WARRIORS) },
                            onGoogleSignIn = { viewModel.signInWithGoogle(context) },
                            onGoogleSignOut = viewModel::signOutGoogle
                        )
                    }
                    NaamShaktiSubScreen.WARRIORS -> {
                        NaamShaktiWarriorSelectionScreen(
                            uiState = uiState,
                            onSelectWarrior = viewModel::selectWarrior
                        )
                    }
                    NaamShaktiSubScreen.BATTLE -> {
                        NaamShaktiBattleArenaScreen(
                            uiState = uiState,
                            onStartMission = viewModel::startMissionBattle,
                            onCastAbility = viewModel::castLightningAbility,
                            onExitBattle = viewModel::exitActiveBattle,
                            onReleaseFinalSpark = viewModel::releaseFinalSparkBelow50,
                            onRealNaamJapTap = onRealNaamJapIncrement,
                            onOpenFullScreenJap = onOpenFullScreenJap
                        )
                    }
                    NaamShaktiSubScreen.PROGRESS -> {
                        NaamShaktiProgressScreen(
                            uiState = uiState,
                            onUpgradeStat = viewModel::upgradeWarriorStat,
                            onAwakenTransformation = viewModel::awakenTransformation
                        )
                    }
                    NaamShaktiSubScreen.HISTORY -> {
                        NaamShaktiHistoryScreen(
                            uiState = uiState
                        )
                    }
                }
            }
        }

        // Screen 3: Cinematic Game Unlock Overlay when 10,000 Naam Completes
        if (uiState.showUnlockCelebration) {
            NaamShaktiUnlockCelebrationOverlay(
                uiState = uiState,
                onEnterBattlefield = { viewModel.dismissUnlockCelebration(goToBattle = true) },
                onStayOnDashboard = { viewModel.dismissUnlockCelebration(goToBattle = false) }
            )
        }
    }
}

@Composable
private fun NaamShaktiTopBar(
    uiState: NaamShaktiUiState,
    onBack: () -> Unit,
    onToggleSound: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = ObsidianCard,
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.24f))
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("naam_shakti_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite
                    )
                }
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "NAAM SHAKTI",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp
                    )
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = CrimsonVivid,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Turn Your Naam Jap Into Power",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite,
                    fontSize = 10.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Shakti Energy Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = if (uiState.profile.koRecoveryActive) JetBlack else CrimsonPrimary,
                border = BorderStroke(1.2.dp, PureWhite.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (uiState.profile.koRecoveryActive) "KO · 0"
                        else formatShaktiNumber(uiState.profile.currentShaktiEnergy),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = PureWhite,
                        modifier = Modifier.testTag("naam_shakti_energy_pill")
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = ObsidianCard,
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.2f))
            ) {
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("naam_shakti_sound_toggle")
                ) {
                    Icon(
                        imageVector = if (uiState.soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp
                        else Icons.AutoMirrored.Rounded.VolumeOff,
                        contentDescription = if (uiState.soundEnabled) "Mute lightning SFX" else "Enable lightning SFX",
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NaamShaktiSubNavBar(
    current: NaamShaktiSubScreen,
    koActive: Boolean,
    onSelect: (NaamShaktiSubScreen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        NaamShaktiSubScreen.entries.forEach { tab ->
            val selected = tab == current
            val label = if (tab == NaamShaktiSubScreen.BATTLE && koActive) "Revive" else tab.label
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(tab) }
                    .testTag("naam_shakti_tab_${tab.name.lowercase()}"),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) CrimsonPrimary else ObsidianCard,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) PureWhite.copy(alpha = 0.55f) else PureWhite.copy(alpha = 0.14f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) PureWhite else SubtleWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

internal fun formatShaktiNumber(value: Int): String =
    NumberFormat.getNumberInstance(Locale.US).format(value)
