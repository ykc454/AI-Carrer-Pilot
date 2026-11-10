package com.nextgendevs.aicareerpilot.util


import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

enum class DeviceType {
    PHONE, TABLET
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun getDeviceType(): DeviceType {
    val configuration = LocalConfiguration.current

    return when {
        configuration.screenWidthDp < 600 -> DeviceType.PHONE
        else -> DeviceType.TABLET
    }
}