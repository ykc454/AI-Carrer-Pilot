package com.example.aicareerpilot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val AppColorScheme = lightColorScheme(
    primary = White,
    onPrimary = Black,

    background = Black,
    onBackground = White,

    surface = DarkGray,
    onSurface = White,

    secondary = Gray,
    onSecondary = White,

    tertiary = LightGray
)

@Composable
fun AICareerPilotTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}