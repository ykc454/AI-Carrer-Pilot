package com.nextgendevs.aicareerpilot.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


val Black = Color(0xFF000000)
val DarkGray = Color(0xFF121212)
val Gray = Color(0xFF2C2C2C)
val LightGray = Color(0xFFB0B0B0)
val White = Color(0xFFFFFFFF)

val primarycolor: Color = Color(0xFFFFFFFF)
val secondarycolor: Color = Color(0xFFFFFFFF)
val tertiarycolor: Color = Color(0xFF000000)

/**
 * App palette with a light and a dark variant.
 *
 * Dark: soft graphite surfaces, muted lavender accent.
 * Light: warm lavender-tinted paper, white cards, deep plum ink and accent.
 * Status colours are desaturated in both (no neon).
 */
@Immutable
data class AppPalette(
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val hairline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val good: Color,
    val goodSoft: Color,
    val warn: Color,
    val warnSoft: Color,
    val bad: Color,
    val badSoft: Color,
    val isDark: Boolean
)

/** Dark = the original AI Career Pilot look: pure black, white accent, grey text. */
val DarkPalette = AppPalette(
    background = Color(0xFF000000),
    surface = Color(0xFF111111),
    surfaceHigh = Color(0xFF1A1A1A),
    hairline = Color(0xFF2C2C2C),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF9E9E9E),
    textMuted = Color(0xFF616161),
    accent = Color(0xFFFFFFFF),
    accentSoft = Color(0x1AFFFFFF),
    onAccent = Color(0xFF000000),
    good = Color(0xFF7CC9A2),
    goodSoft = Color(0x1A7CC9A2),
    warn = Color(0xFFE6B566),
    warnSoft = Color(0x1AE6B566),
    bad = Color(0xFFE9837E),
    badSoft = Color(0x1AE9837E),
    isDark = true
)

val LightPalette = AppPalette(
    background = Color(0xFFF6F4F9),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFFFFFFF),
    hairline = Color(0xFFE8E3F0),
    textPrimary = Color(0xFF1C1631),
    textSecondary = Color(0xFF5E5772),
    textMuted = Color(0xFF9790A8),
    accent = Color(0xFF4A2477),          // deep plum
    accentSoft = Color(0xFFEFE8F8),
    onAccent = Color(0xFFFFFFFF),
    good = Color(0xFF2F7D5B),
    goodSoft = Color(0xFFE6F3EC),
    warn = Color(0xFFA86E12),
    warnSoft = Color(0xFFFBF0DC),
    bad = Color(0xFFB8463B),
    badSoft = Color(0xFFFBEAE7),
    isDark = false
)

val LocalAppPalette = staticCompositionLocalOf { DarkPalette }

/** Read the current palette from any composable: `AppColors.Background`, … */
object AppColors {
    val Background: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.background
    val Surface: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.surface
    val SurfaceHigh: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.surfaceHigh
    val Hairline: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.hairline
    val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.textPrimary
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.textSecondary
    val TextMuted: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.textMuted
    val Accent: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.accent
    val AccentSoft: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.accentSoft
    val OnAccent: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.onAccent
    val Good: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.good
    val GoodSoft: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.goodSoft
    val Warn: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.warn
    val WarnSoft: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.warnSoft
    val Bad: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.bad
    val BadSoft: Color @Composable @ReadOnlyComposable get() = LocalAppPalette.current.badSoft
    val IsDark: Boolean @Composable @ReadOnlyComposable get() = LocalAppPalette.current.isDark
}
