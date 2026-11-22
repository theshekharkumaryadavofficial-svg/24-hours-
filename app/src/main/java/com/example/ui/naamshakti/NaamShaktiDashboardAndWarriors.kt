package com.example.ui.naamshakti

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.naamshakti.AnimeWarrior
import com.example.naamshakti.LightningAbility
import com.example.naamshakti.NaamShaktiCatalog
import com.example.naamshakti.NaamShaktiRules
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

// ==================== SCREEN 1: NAAM SHAKTI DASHBOARD ====================

@Composable
fun NaamShaktiDashboardScreen(
    uiState: NaamShaktiUiState,
    onRealNaamJapTap: () -> Unit,
    onOpenFullScreenJap: () -> Unit,
    onGoToBattle: () -> Unit,
    onGoToWarriors: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onGoogleSignOut: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val unlockFill by animateFloatAsState(
        targetValue = uiState.unlockProgressFraction,
        animationSpec = tween(480, easing = FastOutSlowInEasing),
        label = "shaktiUnlockFill"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Arena + Active Warrior + Real Naam -> Shakti Status
            item(key = "shakti_hero") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(ObsidianCard)
                        .border(1.5.dp, CrimsonPrimary.copy(alpha = 0.8f), RoundedCornerShape(26.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_naam_shakti_arena_1795308861531),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.35f)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        JetBlack.copy(alpha = 0.45f),
                                        CrimsonObsidian.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )

                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Image(
                                painter = painterResource(id = uiState.selectedWarrior.portraitRes),
                                contentDescription = uiState.selectedWarrior.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(2.dp, CrimsonVivid, RoundedCornerShape(20.dp))
                                    .clickable(onClick = onGoToWarriors)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (uiState.profile.koRecoveryActive) JetBlack
                                    else if (uiState.profile.unlockedBattlefield) CrimsonPrimary
                                    else PureWhite
                                ) {
                                    Text(
                                        text = when {
                                            uiState.profile.koRecoveryActive -> "ANIME KO · RECOVERY MODE"
                                            uiState.profile.unlockedBattlefield -> "BATTLEFIELD UNLOCKED ⚡"
                                            else -> "LOCKED · 10,000 NAAM RULE"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (!uiState.profile.unlockedBattlefield && !uiState.profile.koRecoveryActive) JetBlack else PureWhite,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = uiState.selectedWarrior.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${uiState.currentTransformationName} · HP ${uiState.warriorMaxHealth}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SubtleWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Main Energy / Unlock Counter
                        if (!uiState.profile.unlockedBattlefield) {
                            Text(
                                text = "UNLOCK PROGRESS (1 NAAM = 1 SHAKTI)",
                                style = MaterialTheme.typography.labelSmall,
                                color = SubtleWhite,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = formatShaktiNumber(uiState.unlockProgressCount),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp,
                                    color = PureWhite
                                )
                                Text(
                                    text = "/ 10,000 NAAM",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CrimsonVivid,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(PureWhite.copy(alpha = 0.14f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(unlockFill.coerceIn(0.02f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(listOf(CrimsonPrimary, PureWhite))
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${formatShaktiNumber(uiState.unlockRemainingNaam)} more real Naam Jap to unlock the Battlefield & receive 10,000 Shakti Points!",
                                style = MaterialTheme.typography.bodySmall,
                                color = SubtleWhite
                            )
                        } else if (uiState.profile.koRecoveryActive) {
                            Text(
                                text = "SHAKTI DEPLETED — RECOVERY REQUIREMENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonVivid,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatShaktiNumber(uiState.recoveryNaamDone)} / 1,000 NEW NAAM",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Chant ${formatShaktiNumber(uiState.recoveryNaamRemaining)} new Naam Jap to revive ${uiState.selectedWarrior.name} with +1,000 Shakti Points.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SubtleWhite
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ShaktiStatTile(
                                    label = "SHAKTI ENERGY",
                                    value = formatShaktiNumber(uiState.profile.currentShaktiEnergy),
                                    accent = CrimsonPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                ShaktiStatTile(
                                    label = "TODAY NAAM",
                                    value = formatShaktiNumber(uiState.todayNaamCount),
                                    accent = JetBlack,
                                    modifier = Modifier.weight(1f)
                                )
                                ShaktiStatTile(
                                    label = "WINS",
                                    value = "${uiState.profile.battlesWon}",
                                    accent = JetBlack,
                                    modifier = Modifier.weight(0.75f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Real Naam Jap Charge CTA + Battle CTA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onRealNaamJapTap()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("naam_shakti_real_radha_tap"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrimsonPrimary,
                                    contentColor = PureWhite
                                )
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+1 RADHA", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = if (uiState.profile.unlockedBattlefield) onGoToBattle else onOpenFullScreenJap,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("naam_shakti_primary_action"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PureWhite,
                                    contentColor = JetBlack
                                )
                            ) {
                                Icon(
                                    imageVector = if (uiState.profile.unlockedBattlefield) Icons.Rounded.Bolt else Icons.Rounded.TouchApp,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        uiState.profile.koRecoveryActive -> "REVIVE NOW"
                                        uiState.profile.unlockedBattlefield -> "BATTLE ARENA"
                                        else -> "FOCUS JAP"
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Firebase Cloud Backup & Google Sign-In Card
            item(key = "shakti_cloud_sync") {
                FirebaseCloudSyncCard(
                    uiState = uiState,
                    onSignIn = onGoogleSignIn,
                    onSignOut = onGoogleSignOut
                )
            }

            // Lightning Power System Table (50 / 100 / 150 / 200 / 500 / 1,000)
            item(key = "shakti_powers_guide") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = PureWhite
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = CrimsonPrimary) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.padding(6.dp).size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "LIGHTNING POWER SYSTEM",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = JetBlack,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Every attack spends real Naam Shakti Energy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5D5658)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LightningAbility.entries.forEach { ability ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ability.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = JetBlack,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = ability.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF5D5658),
                                        fontSize = 11.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (ability.shaktiCost >= 500) CrimsonPrimary else JetBlack
                                ) {
                                    Text(
                                        text = "${formatShaktiNumber(ability.shaktiCost)} ⚡",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sacred Rule Footer
            item(key = "shakti_rules_card") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.16f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "SACRED NAAM SHAKTI RULES",
                            style = MaterialTheme.typography.labelMedium,
                            color = CrimsonVivid,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "• 1 Real Naam Jap = 1 Shakti Point (Zero fake energy ever generated).",
                            style = MaterialTheme.typography.bodySmall,
                            color = PureWhite
                        )
                        Text(
                            text = "• 10,000 Real Naam unlocks the Fantasy Battlefield with 10,000 Shakti.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite
                        )
                        Text(
                            text = "• At 0 Shakti, Warrior enters KO Recovery Mode — chant 1,000 NEW Naam Jap to revive with 1,000 Shakti.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShaktiStatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SubtleWhite,
                fontSize = 8.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = PureWhite,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FirebaseCloudSyncCard(
    uiState: NaamShaktiUiState,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    val auth = uiState.cloudAuth
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = ObsidianCard,
        border = BorderStroke(1.2.dp, if (auth.signedIn) PureWhite.copy(alpha = 0.35f) else CrimsonPrimary.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (auth.signedIn) PureWhite else CrimsonPrimary
                    ) {
                        Icon(
                            imageVector = if (auth.signedIn) Icons.Rounded.CloudDone else Icons.Rounded.CloudUpload,
                            contentDescription = null,
                            tint = if (auth.signedIn) JetBlack else PureWhite,
                            modifier = Modifier.padding(8.dp).size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (auth.signedIn) "FIREBASE CLOUD SYNC ACTIVE" else "SAVE PROGRESS TO CLOUD",
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = auth.errorMessage ?: auth.lastSyncLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (auth.errorMessage != null) CrimsonVivid else SubtleWhite,
                            fontSize = 11.sp
                        )
                    }
                }

                if (auth.authBusy || auth.syncingNow) {
                    CircularProgressIndicator(
                        color = PureWhite,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!auth.signedIn) {
                Button(
                    onClick = onSignIn,
                    enabled = !auth.authBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_sign_in_cloud_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = JetBlack
                    )
                ) {
                    Text(
                        text = "Sign in with Google to Sync Naam Jap & Shakti",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = auth.displayName.ifBlank { auth.email },
                        style = MaterialTheme.typography.labelMedium,
                        color = PureWhite
                    )
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("google_sign_out_cloud_button")
                    ) {
                        Text("Sign Out", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 2: ANIME WARRIOR SELECTION ====================

@Composable
fun NaamShaktiWarriorSelectionScreen(
    uiState: NaamShaktiUiState,
    onSelectWarrior: (String) -> Unit
) {
    val unlockedSet = uiState.unlockedWarriorIds

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "ANIME WARRIORS",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Every warrior channels your real Naam Shakti Energy into lightning abilities.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite
                )
            }

            items(NaamShaktiCatalog.warriors, key = { it.id }) { warrior ->
                WarriorRosterCard(
                    warrior = warrior,
                    isUnlocked = unlockedSet.contains(warrior.id),
                    isSelected = uiState.profile.selectedWarriorId == warrior.id,
                    onSelect = { onSelectWarrior(warrior.id) }
                )
            }
        }
    }
}

@Composable
private fun WarriorRosterCard(
    warrior: AnimeWarrior,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) CrimsonDeep.copy(alpha = 0.85f) else ObsidianCard,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.2.dp,
            color = if (isSelected) PureWhite else CrimsonPrimary.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Image(
                    painter = painterResource(id = warrior.portraitRes),
                    contentDescription = warrior.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 96.dp, height = 128.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .alpha(if (isUnlocked) 1f else 0.4f)
                        .border(1.5.dp, Color(warrior.accentHex), RoundedCornerShape(18.dp))
                )
                if (!isUnlocked) {
                    Surface(
                        shape = CircleShape,
                        color = JetBlack.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Locked warrior",
                            tint = PureWhite,
                            modifier = Modifier.padding(10.dp).size(22.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = warrior.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Selected",
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = warrior.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonVivid,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "HP ${warrior.baseHealth} · ATK ${(warrior.attackPowerMultiplier * 100).toInt()}% · SHIELD ${(warrior.shieldMultiplier * 100).toInt()}%",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = SubtleWhite
                )
                Text(
                    text = "Special: ${warrior.specialPowerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PureWhite,
                    fontSize = 11.sp
                )
                Text(
                    text = "Ultimate: ${warrior.ultimateName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FadedWhite,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (isUnlocked) {
                    Button(
                        onClick = onSelect,
                        enabled = !isSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("select_warrior_${warrior.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) JetBlack else PureWhite,
                            contentColor = if (isSelected) PureWhite else JetBlack,
                            disabledContainerColor = JetBlack.copy(alpha = 0.7f),
                            disabledContentColor = PureWhite
                        )
                    ) {
                        Text(
                            text = if (isSelected) "ACTIVE WARRIOR" else "SELECT WARRIOR",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = JetBlack.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.16f))
                    ) {
                        Text(
                            text = warrior.unlockRequirementText,
                            style = MaterialTheme.typography.labelSmall,
                            color = SubtleWhite,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 3: 10,000 NAAM GAME UNLOCK OVERLAY ====================

@Composable
fun NaamShaktiUnlockCelebrationOverlay(
    uiState: NaamShaktiUiState,
    onEnterBattlefield: () -> Unit,
    onStayOnDashboard: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "unlockAura")
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JetBlack.copy(alpha = 0.94f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp),
            shape = RoundedCornerShape(28.dp),
            color = ObsidianCard,
            border = BorderStroke(2.dp, CrimsonVivid.copy(alpha = glowAlpha))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CrimsonPrimary
                ) {
                    Text(
                        text = "10,000 NAAM JAP COMPLETE ⚡",
                        style = MaterialTheme.typography.labelMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = uiState.selectedWarrior.portraitRes),
                    contentDescription = uiState.selectedWarrior.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(3.dp, PureWhite.copy(alpha = glowAlpha), CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "BATTLEFIELD UNLOCKED!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+${formatShaktiNumber(NaamShaktiRules.UNLOCK_NAAM_TARGET)} SHAKTI ENERGY AWARDED",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = CrimsonVivid,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${uiState.selectedWarrior.name} is now charged with your real Naam Jap devotion. Unleash Lightning Attack, Thunder Strike, Energy Shield, Mega Thunder & Ultimate Power!",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onEnterBattlefield,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("unlock_enter_battlefield_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ENTER BATTLE ARENA ⚡", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onStayOnDashboard,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.3f))
                ) {
                    Text("View Shakti Dashboard", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
