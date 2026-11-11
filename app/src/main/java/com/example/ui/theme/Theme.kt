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
    primary = YasinGreen,
    onPrimary = Color.White,
    primaryContainer = YasinNavy,
    onPrimaryContainer = Color.White,
    secondary = YasinBlue,
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = YasinGreen,
    onPrimary = Color.White,
    primaryContainer = YasinGreenLight,
    onPrimaryContainer = YasinGreen,
    secondary = YasinNavy,
    onSecondary = Color.White,
    secondaryContainer = YasinBlueLight,
    onSecondaryContainer = YasinNavy,
    background = BackgroundLight,
    surface = SurfaceCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun YasinTelecomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent brand colors for telecom shop
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
