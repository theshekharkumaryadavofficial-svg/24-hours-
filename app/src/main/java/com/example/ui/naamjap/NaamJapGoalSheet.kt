package com.example.ui.naamjap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NaamJapGoalEntity
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LightTrack
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PureWhite
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaamJapGoalBottomSheet(
    currentGoal: NaamJapGoalEntity,
    onDismiss: () -> Unit,
    onSave: (dailyGoal: Int, hapticEnabled: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var targetCount by remember(currentGoal) { mutableIntStateOf(currentGoal.dailyGoal) }
    var hapticEnabled by remember(currentGoal) { mutableStateOf(currentGoal.hapticEnabled) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        contentColor = JetBlack,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = CircleShape, color = CrimsonPrimary) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Daily Naam Jap Goal",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JetBlack
                    )
                    Text(
                        text = "Choose your daily Radha Naam sankalp (1 Mala = 108)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Mala Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    108 to "1 Mala",
                    540 to "5 Malas",
                    1080 to "10 Malas",
                    2160 to "20 Malas"
                ).forEach { (count, label) ->
                    val selected = targetCount == count
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { targetCount = count },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) JetBlack else LightTrack.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, if (selected) JetBlack else CrimsonBorderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$count",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (selected) PureWhite else JetBlack
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (selected) PureWhite.copy(alpha = 0.78f) else MutedInk
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Mala Stepper (+/- 108)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CrimsonSoftTint,
                border = BorderStroke(1.dp, CrimsonBorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PureWhite,
                        border = BorderStroke(1.dp, JetBlack.copy(alpha = 0.15f))
                    ) {
                        IconButton(
                            onClick = { targetCount = (targetCount - 108).coerceAtLeast(108) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Decrease goal by 1 Mala",
                                tint = JetBlack
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = NumberFormat.getNumberInstance(Locale.US).format(targetCount),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = CrimsonPrimary
                        )
                        Text(
                            text = "${targetCount / 108} MALAS / DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = JetBlack
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = JetBlack
                    ) {
                        IconButton(
                            onClick = { targetCount = (targetCount + 108).coerceAtMost(108_000) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Increase goal by 1 Mala",
                                tint = PureWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Haptic Feedback Toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = LightTrack.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tap Haptic Pulse",
                            style = MaterialTheme.typography.titleSmall,
                            color = JetBlack,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Gentle vibration on every tap & 108 mala completion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = { hapticEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = CrimsonPrimary,
                            uncheckedThumbColor = JetBlack,
                            uncheckedTrackColor = PureWhite
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = JetBlack
                    )
                }
                Button(
                    onClick = { onSave(targetCount, hapticEnabled) },
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("save_naam_jap_goal_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "SAVE GOAL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
