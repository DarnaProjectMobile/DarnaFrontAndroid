package com.sim.darna.ui.theme

import android.app.Activity
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
    primary = BluePrimary,
    secondary = BlueAccent,
    tertiary = BlueLight,
    background = BackgroundDark,
    surface = Color(0xFF263238),
    surfaceVariant = Color(0xFF37474F),
    onPrimary = TextOnPrimary,
    onSecondary = TextOnPrimary,
    onTertiary = TextPrimary,
    onBackground = TextOnPrimary,
    onSurface = TextOnPrimary,
    error = Error,
    onError = TextOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueAccent,
    tertiary = BlueLight,
    background = BackgroundLight,
    surface = SurfaceWhite,
    surfaceVariant = SurfaceGray,
    onPrimary = TextOnPrimary,
    onSecondary = TextOnPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Error,
    onError = TextOnPrimary
)

@Composable
fun DarnaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Désactivé pour utiliser notre thème personnalisé
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