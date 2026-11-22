package com.example.bedtime

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Home
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.NaamJapRepository
import com.example.data.SleepDatabase
import com.example.data.todayDateKey
import com.example.plans.StudyGuardService
import com.example.ui.theme.CrimsonDeep
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonVivid
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SubtleWhite
import com.example.ui.theme.TwentyFourHrsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Full-Screen Red, Black & White RADHA NAAM JAP COUNTER BLOCKER Activity.
 * Used as both the immediate live blocker fallback and the interactive preview screen.
 * Every tap anywhere on the screen increments today's Radha Naam Jap permanently in Room!
 */
class BedtimeLockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BLOCKED_APP = "extra_blocked_app"
        const val EXTRA_SLEEP_WINDOW = "extra_sleep_window"
        const val EXTRA_SLOT_TITLE = "extra_slot_title"
        const val EXTRA_BLOCKED_COUNT = "extra_blocked_count"
        const val EXTRA_IS_PREVIEW = "extra_is_preview"
        const val EXTRA_IS_STUDY_MODE = "extra_is_study_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        val blockedApp = intent.getStringExtra(EXTRA_BLOCKED_APP) ?: "Disallowed App"
        val windowText = intent.getStringExtra(EXTRA_SLEEP_WINDOW) ?: "24H Schedule"
        val isPreview = intent.getBooleanExtra(EXTRA_IS_PREVIEW, false)
        val isStudyMode = intent.getBooleanExtra(EXTRA_IS_STUDY_MODE, false)
        val slotTitle = intent.getStringExtra(EXTRA_SLOT_TITLE)
            ?: if (isStudyMode) "Study / Slot App Lock" else "Bedtime Sleep Guard"
        val blockedCount = intent.getIntExtra(EXTRA_BLOCKED_COUNT, 1)

        if (!isStudyMode && !isPreview) {
            window.attributes = window.attributes.apply {
                screenBrightness = 0.14f
            }
        }

        val naamJapRepository = NaamJapRepository(SleepDatabase.getInstance(applicationContext).naamJapDao())

        setContent {
            TwentyFourHrsTheme {
                val todayRow by naamJapRepository.observeToday(todayDateKey()).collectAsStateWithLifecycle(initialValue = null)
                val allTimeTotal by naamJapRepository.allTimeTotal.collectAsStateWithLifecycle(initialValue = 0)
                val scope = rememberCoroutineScope()

                FullScreenNaamJapBlockerScreen(
                    blockedAppName = blockedApp,
                    slotTitle = slotTitle,
                    scheduleWindow = windowText,
                    blockedCount = blockedCount,
                    todayCount = todayRow?.count ?: 0,
                    allTimeTotal = allTimeTotal,
                    isPreview = isPreview,
                    isStudyMode = isStudyMode,
                    onTapRadha = {
                        scope.launch { naamJapRepository.incrementToday(1) }
                    },
                    onReturnHome = {
                        if (!isPreview) {
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            runCatching { startActivity(homeIntent) }
                        }
                        finish()
                    },
                    onEndGuard = {
                        if (!isPreview) {
                            if (isStudyMode) {
                                StudyGuardService.stopStudyMode(this@BedtimeLockActivity)
                            } else {
                                BedtimeGuardService.stopGuard(this@BedtimeLockActivity)
                            }
                        }
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun FullScreenNaamJapBlockerScreen(
    blockedAppName: String,
    slotTitle: String,
    scheduleWindow: String,
    blockedCount: Int,
    todayCount: Int,
    allTimeTotal: Int,
    isPreview: Boolean,
    isStudyMode: Boolean,
    onTapRadha: () -> Unit,
    onReturnHome: () -> Unit,
    onEndGuard: () -> Unit
) {
    BackHandler { onReturnHome() }

    val haptic = LocalHapticFeedback.current
    val fmt = remember { NumberFormat.getNumberInstance(Locale.US) }
    var tapPulseKey by remember { mutableIntStateOf(0) }
    var isPulsing by remember { mutableStateOf(false) }

    LaunchedEffect(tapPulseKey) {
        if (tapPulseKey > 0) {
            isPulsing = true
            delay(110L)
            isPulsing = false
        }
    }

    val radhaScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.10f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "blockerRadhaScale"
    )

    val topBadge = when {
        isPreview -> "NAAM JAP FULL-SCREEN BLOCKER PREVIEW"
        isStudyMode -> "24 HRS · SLOT APP LOCK ACTIVE"
        else -> "24 HRS · BEDTIME NAAM JAP BLOCKER"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CrimsonVivid, CrimsonPrimary, CrimsonDeep)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                tapPulseKey++
                onTapRadha()
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 20.dp)
            .testTag("naam_jap_blocker_tap_surface")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status + Blocked App Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = JetBlack,
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.30f))
                ) {
                    Text(
                        text = topBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = PureWhite,
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Block,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BLOCKED: $blockedAppName",
                                style = MaterialTheme.typography.titleSmall,
                                color = CrimsonPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "$slotTitle · $scheduleWindow  (Blocked #$blockedCount)",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = JetBlack,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Center Massive Interactive RADHA NAAM JAP COUNTER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "राधा",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 80.sp,
                    lineHeight = 86.sp,
                    color = PureWhite,
                    modifier = Modifier.scale(radhaScale)
                )
                Text(
                    text = "R A D H A   N A A M   J A P",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = SubtleWhite
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = JetBlack.copy(alpha = 0.45f),
                    border = BorderStroke(1.5.dp, PureWhite.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AAJ KA NAAM JAP",
                            style = MaterialTheme.typography.labelSmall,
                            color = SubtleWhite,
                            fontSize = 10.sp
                        )
                        Text(
                            text = fmt.format(todayCount),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 54.sp,
                            lineHeight = 58.sp,
                            color = PureWhite,
                            modifier = Modifier.testTag("blocker_today_naam_jap_count")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MALAS: ${todayCount / 108} (${todayCount % 108}/108)  ·  ALL TIME: ${fmt.format(allTimeTotal)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = PureWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = PureWhite.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TouchApp,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TAP ANYWHERE FOR +1 RADHA (AUTO-SAVED)",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Only apps allowed in this time slot can open. Any other or newly installed app opens Radha Naam Jap instead.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SubtleWhite,
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp
                )
            }

            // Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onReturnHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("lock_return_to_sleep_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JetBlack,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPreview) "CLOSE PREVIEW" else "GO HOME · BACK TO SCHEDULE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (!isPreview) {
                    OutlinedButton(
                        onClick = onEndGuard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("lock_end_bedtime_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, PureWhite),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                    ) {
                        Text(
                            text = if (isStudyMode) "EMERGENCY EXIT SLOT LOCK" else "EMERGENCY UNLOCK (END BEDTIME)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
