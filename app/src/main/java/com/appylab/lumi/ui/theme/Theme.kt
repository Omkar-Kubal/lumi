package com.appylab.lumi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = Color.White,
    background = BackgroundLight,
    onBackground = ForegroundLight,
    surface = BackgroundLight,
    onSurface = ForegroundLight,
    surfaceContainerLow = RoseCard,
    surfaceContainer = MutedLight,
    surfaceContainerHigh = SecondaryLight,
    surfaceContainerHighest = SecondaryLight,
    onSurfaceVariant = MutedForegroundLight,
    outline = ForegroundLight,
    outlineVariant = OutlineLight,
    error = ErrorLight,
    onError = Color.White,
    secondary = RosePrimary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = ForegroundLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = RosePrimary,
    onPrimary = Color.Black,
    background = BackgroundDark,
    onBackground = ForegroundDark,
    surface = BackgroundDark,
    onSurface = ForegroundDark,
    surfaceContainerLow = CardDark,
    surfaceContainer = MutedDark,
    surfaceContainerHigh = SecondaryDark,
    surfaceContainerHighest = SecondaryDark,
    onSurfaceVariant = MutedForegroundDark,
    outline = MutedForegroundDark,
    outlineVariant = OutlineDark,
    error = ErrorDark,
    onError = Color.Black,
    secondary = RosePrimary,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = ForegroundDark,
)

@Composable
fun LumiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
