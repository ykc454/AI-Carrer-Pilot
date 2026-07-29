package com.nextgendevs.aicareerpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nextgendevs.aicareerpilot.presentation.theme.AICareerPilotTheme
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.nextgendevs.aicareerpilot.presentation.screens.MainScreen
import com.nextgendevs.aicareerpilot.presentation.viewmodel.ResumeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ResumeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        //Edge-to-edge layout (modern Android UI)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        //Status bar color = match your black UI
        enableEdgeToEdge()
        //White icons on dark background
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContent {
            AICareerPilotTheme {
                MainScreen(viewModel)
            }
        }
    }
}

