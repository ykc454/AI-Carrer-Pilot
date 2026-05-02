package com.example.aicareerpilot.util


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

enum class DeviceType {
    PHONE, TABLET
}

@Composable
fun getDeviceType(): DeviceType {
    val configuration = LocalConfiguration.current

    return when {
        configuration.screenWidthDp < 600 -> DeviceType.PHONE
        else -> DeviceType.TABLET
    }
}