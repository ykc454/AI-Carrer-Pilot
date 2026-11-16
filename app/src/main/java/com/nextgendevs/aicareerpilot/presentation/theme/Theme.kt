package com.nextgendevs.aicareerpilot.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.nextgendevs.aicareerpilot.domain.model.ThemeMode

private fun schemeFor(p: AppPalette) =
    if (p.isDark) darkColorScheme(
        primary = p.accent, onPrimary = p.onAccent,
        background = p.background, onBackground = p.textPrimary,
        surface = p.surface, onSurface = p.textPrimary,
        surfaceVariant = p.surfaceHigh, onSurfaceVariant = p.textSecondary,
        secondary = p.surfaceHigh, onSecondary = p.textPrimary,
        tertiary = p.textSecondary, outline = p.hairline,
        error = p.bad
    ) else lightColorScheme(
        primary = p.accent, onPrimary = p.onAccent,
        background = p.background, onBackground = p.textPrimary,
        surface = p.surface, onSurface = p.textPrimary,
        surfaceVariant = p.accentSoft, onSurfaceVariant = p.textSecondary,
        secondary = p.accentSoft, onSecondary = p.accent,
        tertiary = p.textSecondary, outline = p.hairline,
        error = p.bad
    )

@Composable
fun AICareerPilotTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = if (dark) DarkPalette else LightPalette

    // Status / navigation bar icons follow the chosen theme, not the system one
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(
            colorScheme = schemeFor(palette),
            typography = Typography,
            content = content
        )
    }
}
