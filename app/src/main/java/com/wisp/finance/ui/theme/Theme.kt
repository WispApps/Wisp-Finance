package com.wisp.finance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB4A8FF),
    onPrimary = Color(0xFF21165C),
    primaryContainer = Color(0xFF30286A),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = WispEmerald,
    onSecondary = Color.White,
    background = WispDarkBackground,
    onBackground = Color(0xFFF9FAFB),
    surface = WispDarkSurface,
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF222630),
    onSurfaceVariant = Color(0xFFB6BECC),
    outline = WispDarkLine,
    secondaryContainer = Color(0xFF30286A),
    onSecondaryContainer = Color(0xFFE5E1FF),
    surfaceContainerLowest = Color(0xFF121212),
    surfaceContainerLow = Color(0xFF1A1C22),
    surfaceContainer = Color(0xFF20232B),
    surfaceContainerHigh = Color(0xFF272B35),
    surfaceContainerHighest = Color(0xFF303541),
    tertiaryContainer = Color(0xFF30286A),
    onTertiaryContainer = Color(0xFFE5E1FF),
    outlineVariant = WispDarkLine,
    error = WispRose,
)

private val LightColorScheme = lightColorScheme(
    primary = WispElectric,
    onPrimary = Color.White,
    primaryContainer = WispElectric,
    onPrimaryContainer = Color.White,
    secondary = WispEmerald,
    onSecondary = Color.White,
    tertiary = WispAmber,
    background = WispWhite,
    onBackground = WispInk,
    surface = WispWhite,
    onSurface = WispInk,
    surfaceVariant = WispMuted,
    onSurfaceVariant = WispSecondary,
    outline = WispLine,
    secondaryContainer = Color(0xFFEDEAFF),
    onSecondaryContainer = Color(0xFF352AA6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F9FB),
    surfaceContainer = Color(0xFFF3F4F6),
    surfaceContainerHigh = Color(0xFFECEEF2),
    surfaceContainerHighest = Color(0xFFE5E7EB),
    tertiaryContainer = Color(0xFFEDEAFF),
    onTertiaryContainer = Color(0xFF352AA6),
    outlineVariant = WispLine,
    error = WispRose,
)

@Composable
fun WispFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
