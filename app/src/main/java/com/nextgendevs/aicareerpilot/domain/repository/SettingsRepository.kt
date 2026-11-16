package com.nextgendevs.aicareerpilot.domain.repository

import com.nextgendevs.aicareerpilot.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

/** Local, per-device app preferences. */
interface SettingsRepository {
    val themeMode: StateFlow<ThemeMode>
    fun setThemeMode(mode: ThemeMode)
}
