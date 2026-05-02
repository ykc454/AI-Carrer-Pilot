package com.example.aicareerpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.aicareerpilot.ui.theme.AICareerPilotTheme
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import android.graphics.Color
import androidx.core.view.WindowInsetsControllerCompat
import com.example.aicareerpilot.ui.screen.HomeScreen
import com.example.aicareerpilot.ui.screen.MainScreen
import com.example.aicareerpilot.ui.viewmodel.ResumeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ResumeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Edge-to-edge layout (modern Android UI)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ✅ Status bar color = match your black UI
        window.statusBarColor = Color.BLACK

        // ✅ White icons on dark background
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContent {
            AICareerPilotTheme {
                MainScreen(viewModel)
            }
        }
    }
}

