package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = ForestGreenDark,
    primaryContainer = ForestGreenPrimary,
    onPrimaryContainer = EmeraldContainer,
    secondary = EmeraldLight,
    onSecondary = ForestGreenDark,
    secondaryContainer = SlateSecondary,
    onSecondaryContainer = Color.White,
    tertiary = GoldenAlert,
    background = SurfaceDark,
    surface = SurfaceVariantDark,
    onBackground = Color(0xFFE2E9E4),
    onSurface = Color(0xFFE2E9E4),
    error = CriticalRed,
    errorContainer = CriticalRedContainer
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = SlateSecondary,
    onSecondary = Color.White,
    secondaryContainer = SlateSecondaryContainer,
    onSecondaryContainer = ForestGreenDark,
    tertiary = GoldenAlert,
    background = SurfaceLight,
    surface = Color.White,
    onBackground = Color(0xFF131F19),
    onSurface = Color(0xFF131F19),
    error = CriticalRed,
    errorContainer = CriticalRedContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding for official inspection
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
