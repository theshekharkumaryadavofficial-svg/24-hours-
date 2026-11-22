package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val TwentyFourHrsColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    onPrimary = PureWhite,
    primaryContainer = CrimsonDeep,
    onPrimaryContainer = PureWhite,
    secondary = JetBlack,
    onSecondary = PureWhite,
    secondaryContainer = CrimsonSoftTint,
    onSecondaryContainer = JetBlack,
    tertiary = CrimsonVivid,
    onTertiary = PureWhite,
    background = CrimsonPrimary,
    onBackground = PureWhite,
    surface = PureWhite,
    onSurface = JetBlack,
    surfaceVariant = CrimsonSoftTint,
    onSurfaceVariant = CharcoalInk,
    outline = CrimsonBorderLight,
    outlineVariant = LightTrack
)

val TwentyFourHrsShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TwentyFourHrsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TwentyFourHrsColorScheme,
        typography = AppTypography,
        shapes = TwentyFourHrsShapes,
        content = content
    )
}
