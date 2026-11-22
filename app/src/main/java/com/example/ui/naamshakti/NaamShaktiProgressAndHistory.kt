package com.example.ui.naamshakti

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NaamShaktiUiState
import com.example.ui.theme.CrimsonDeep
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonVivid
import com.example.ui.theme.FadedWhite
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==================== SCREEN 6: WARRIOR PROGRESS, UPGRADES & ACHIEVEMENTS ====================

@Composable
fun NaamShaktiProgressScreen(
    uiState: NaamShaktiUiState,
    onUpgradeStat: (String) -> Unit,
    onAwakenTransformation: () -> Unit
) {
    val p = uiState.profile

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Battle Stats Summary
            item(key = "prog_summary") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = CrimsonDeep.copy(alpha = 0.85f),
                    border = BorderStroke(1.5.dp, PureWhite.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "WARRIOR PROGRESS & XP",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProgressMiniBox("WARRIOR XP", formatShaktiNumber(p.warriorXp), Modifier.weight(1f))
                            ProgressMiniBox("BATTLES WON", "${p.battlesWon}", Modifier.weight(1f))
                            ProgressMiniBox("MISSIONS", "${p.highestMissionCleared}/8", Modifier.weight(1f))
                        }
                    }
                }
            }

            // Character Upgrades Card
            item(key = "prog_upgrades") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = PureWhite
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = CrimsonPrimary) {
                                Icon(Icons.Rounded.Upgrade, contentDescription = null, tint = PureWhite, modifier = Modifier.padding(6.dp).size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "CHARACTER UPGRADES",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = JetBlack,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Upgrade cost: ${formatShaktiNumber(uiState.nextUpgradeCostXp)} Battle XP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5D5658)
                                )
                            }
                        }

                        UpgradeStatRow(
                            title = "Lightning Attack",
                            level = p.attackLevel,
                            bonusText = "+${(p.attackLevel - 1) * 10}% Ability DMG",
                            canAfford = p.warriorXp >= uiState.nextUpgradeCostXp,
                            testTag = "upgrade_attack_button",
                            onUpgrade = { onUpgradeStat("ATTACK") }
                        )
                        UpgradeStatRow(
                            title = "Defence Shield & HP",
                            level = p.defenseLevel,
                            bonusText = "+${(p.defenseLevel - 1) * 90} Max HP · +${(p.defenseLevel - 1) * 12}% Shield",
                            canAfford = p.warriorXp >= uiState.nextUpgradeCostXp,
                            testTag = "upgrade_defense_button",
                            onUpgrade = { onUpgradeStat("DEFENSE") }
                        )
                        UpgradeStatRow(
                            title = "Shakti Aura Mastery",
                            level = p.auraLevel,
                            bonusText = "+${(p.auraLevel - 1) * 5}% Barrier Synergy",
                            canAfford = p.warriorXp >= uiState.nextUpgradeCostXp,
                            testTag = "upgrade_aura_button",
                            onUpgrade = { onUpgradeStat("AURA") }
                        )
                    }
                }
            }

            // Special Transformations Card
            item(key = "prog_transform") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.5.dp, CrimsonPrimary.copy(alpha = 0.75f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = CrimsonVivid)
                            Text(
                                text = "SPECIAL TRANSFORMATIONS",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Current Form: ${uiState.currentTransformationName} (Stage ${p.transformationStage}/3 · +${p.transformationStage * 16}% Power)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        uiState.selectedWarrior.transformations.forEachIndexed { idx, formName ->
                            val stageNum = idx + 1
                            val reached = p.transformationStage >= stageNum
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Stage $stageNum · $formName",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (reached) PureWhite else FadedWhite,
                                    fontWeight = if (reached) FontWeight.ExtraBold else FontWeight.Medium
                                )
                                Icon(
                                    imageVector = if (reached) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = if (reached) CrimsonVivid else FadedWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (p.transformationStage < 3) {
                            val cost = uiState.nextTransformationCostXp
                            Button(
                                onClick = onAwakenTransformation,
                                enabled = p.warriorXp >= cost,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("awaken_transformation_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrimsonPrimary,
                                    contentColor = PureWhite,
                                    disabledContainerColor = JetBlack,
                                    disabledContentColor = FadedWhite
                                )
                            ) {
                                Text(
                                    text = "AWAKEN NEXT FORM (${formatShaktiNumber(cost)} XP) ⚡",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PureWhite,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "SUPREME TRANSFORMATION MASTERED ⚡",
                                    color = JetBlack,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Achievements Section
            item(key = "prog_ach_header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = PureWhite)
                    Text(
                        text = "ACHIEVEMENTS (${uiState.achievements.count { it.unlocked }}/${uiState.achievements.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            items(uiState.achievements, key = { it.id }) { ach ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (ach.unlocked) CrimsonDeep.copy(alpha = 0.72f) else ObsidianCard,
                    border = BorderStroke(
                        1.dp,
                        if (ach.unlocked) PureWhite.copy(alpha = 0.45f) else PureWhite.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (ach.unlocked) PureWhite else JetBlack
                        ) {
                            Icon(
                                imageVector = if (ach.unlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (ach.unlocked) CrimsonPrimary else FadedWhite,
                                modifier = Modifier.padding(8.dp).size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ach.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = ach.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = SubtleWhite,
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
private fun ProgressMiniBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = JetBlack.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SubtleWhite, fontSize = 8.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = PureWhite
            )
        }
    }
}

@Composable
private fun UpgradeStatRow(
    title: String,
    level: Int,
    bonusText: String,
    canAfford: Boolean,
    testTag: String,
    onUpgrade: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$title · Lv.$level",
                style = MaterialTheme.typography.titleSmall,
                color = JetBlack,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = bonusText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D5658),
                fontSize = 11.sp
            )
        }
        Button(
            onClick = onUpgrade,
            enabled = canAfford,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CrimsonPrimary,
                contentColor = PureWhite,
                disabledContainerColor = Color(0xFFE6D8DA),
                disabledContentColor = Color(0xFF7B6F71)
            ),
            modifier = Modifier.testTag(testTag)
        ) {
            Text("UPGRADE", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        }
    }
}

// ==================== SCREEN 7: NAAM JAP & SHAKTI ENERGY HISTORY ====================

@Composable
fun NaamShaktiHistoryScreen(
    uiState: NaamShaktiUiState
) {
    val p = uiState.profile
    val timeFmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Lifetime Ledger Summary
            item(key = "hist_ledger") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.5.dp, CrimsonPrimary.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                Icon(Icons.Rounded.History, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "NAAM JAP & SHAKTI LEDGER",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Real Naam Jap converted 1:1 into battle Shakti Energy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SubtleWhite,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProgressMiniBox("ALL NAAM", formatShaktiNumber(uiState.allTimeNaamCount), Modifier.weight(1f))
                            ProgressMiniBox("EARNED ⚡", formatShaktiNumber(p.lifetimeShaktiEarned), Modifier.weight(1f))
                            ProgressMiniBox("SPENT ⚡", formatShaktiNumber(p.lifetimeShaktiSpent), Modifier.weight(1f))
                        }
                    }
                }
            }

            // Battle & Shakti Events History
            item(key = "hist_events_title") {
                Text(
                    text = "SHAKTI ENERGY & BATTLE EVENTS",
                    style = MaterialTheme.typography.titleSmall,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (uiState.shaktiHistory.isEmpty()) {
                item(key = "hist_empty") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f))
                    ) {
                        Text(
                            text = "No battle or Shakti events recorded yet. Complete 10,000 real Naam Jap to unlock your first 10,000 Shakti Points!",
                            style = MaterialTheme.typography.bodySmall,
                            color = SubtleWhite,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }
            } else {
                items(uiState.shaktiHistory, key = { it.eventId }) { ev ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = ev.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SubtleWhite,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${timeFmt.format(Date(ev.createdEpoch))} · Naam ${formatShaktiNumber(ev.naamCountSnapshot)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FadedWhite,
                                    fontSize = 10.sp
                                )
                            }
                            if (ev.shaktiDelta != 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (ev.shaktiDelta > 0) PureWhite else CrimsonPrimary
                                ) {
                                    Text(
                                        text = "${if (ev.shaktiDelta > 0) "+" else ""}${formatShaktiNumber(ev.shaktiDelta)} ⚡",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = if (ev.shaktiDelta > 0) JetBlack else PureWhite,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Daily Naam Jap Records
            if (uiState.naamHistory.isNotEmpty()) {
                item(key = "hist_daily_naam") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = PureWhite
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "DAILY NAAM JAP ENERGY SOURCE",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = JetBlack,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            uiState.naamHistory.take(10).forEachIndexed { idx, day ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${day.displayDate} (${day.dayName})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = JetBlack,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${day.completedMalas} Malas · Real Count",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF5D5658),
                                            fontSize = 10.sp
                                        )
                                    }
                                    Text(
                                        text = "+${formatShaktiNumber(day.count)} ⚡",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CrimsonPrimary,
                                        fontSize = 15.sp
                                    )
                                }
                                if (idx < minOf(uiState.naamHistory.size, 10) - 1) {
                                    HorizontalDivider(color = JetBlack.copy(alpha = 0.08f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
