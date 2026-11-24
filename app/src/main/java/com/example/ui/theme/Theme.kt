package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ColorDarkCoralContainer = androidx.compose.ui.graphics.Color(0xFF5E220D)
private val ColorDarkSkyContainer = androidx.compose.ui.graphics.Color(0xFF0A3A54)
private val ColorDarkSunContainer = androidx.compose.ui.graphics.Color(0xFF4D3304)
private val ColorDarkErrorContainer = androidx.compose.ui.graphics.Color(0xFF5C0A22)

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimary,
    onPrimary = CoralOnPrimary,
    primaryContainer = ColorDarkCoralContainer,
    onPrimaryContainer = CoralContainer,
    secondary = SkySecondary,
    onSecondary = SkyOnSecondary,
    secondaryContainer = ColorDarkSkyContainer,
    onSecondaryContainer = SkyContainer,
    tertiary = SunTertiary,
    onTertiary = SunOnTertiary,
    tertiaryContainer = ColorDarkSunContainer,
    onTertiaryContainer = SunContainer,
    background = NightBackground,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightMutedInk,
    outline = NightOutline,
    error = BerryError,
    onError = CoralOnPrimary,
    errorContainer = ColorDarkErrorContainer,
    onErrorContainer = BerryErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = CoralOnPrimary,
    primaryContainer = CoralContainer,
    onPrimaryContainer = CoralOnContainer,
    secondary = SkySecondary,
    onSecondary = SkyOnSecondary,
    secondaryContainer = SkyContainer,
    onSecondaryContainer = SkyOnContainer,
    tertiary = SunTertiary,
    onTertiary = SunOnTertiary,
    tertiaryContainer = SunContainer,
    onTertiaryContainer = SunOnContainer,
    background = CreamBackground,
    onBackground = PlumInk,
    surface = StorybookSurface,
    onSurface = PlumInk,
    surfaceVariant = OatSurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = WarmOutline,
    error = BerryError,
    onError = CoralOnPrimary,
    errorContainer = BerryErrorContainer,
    onErrorContainer = BerryErrorOnContainer
)

val PlayfulShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun KuisCeriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PlayfulShapes,
        content = content
    )
}
