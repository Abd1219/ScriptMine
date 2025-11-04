package com.abdapps.scriptmine.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FuturisticColorScheme = darkColorScheme(
    primary = Primary,
    secondary = PrimaryVariant,
    tertiary = NeonGreen,
    background = FuturisticBackground,
    surface = FuturisticSurface,
    surfaceVariant = SurfaceElevated,
    onPrimary = FuturisticBackground,
    onSecondary = FuturisticBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary,
    outlineVariant = TextPlaceholder,
    error = ErrorColor,
    onError = FuturisticBackground
)

// Keep light scheme for compatibility but use futuristic as default
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = PrimaryVariant,
    tertiary = NeonGreen,
    background = FuturisticBackground,
    surface = FuturisticSurface,
    surfaceVariant = SurfaceElevated,
    onPrimary = FuturisticBackground,
    onSecondary = FuturisticBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary,
    outlineVariant = TextPlaceholder
)

@Composable
fun ScriptMineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> FuturisticColorScheme // Always use futuristic theme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FuturisticBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}