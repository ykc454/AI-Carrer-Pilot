package com.nextgendevs.aicareerpilot

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.IntentCompat
import com.nextgendevs.aicareerpilot.util.getFileName
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
        //Edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        //Status bar color = match your black UI
        enableEdgeToEdge()
        //White icons on dark background
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false

        // Only on first launch. After rotation the ViewModel already has it.
        if (savedInstanceState == null) {
            handleShareIntent(intent)
        }

        setContent {
            AICareerPilotTheme {
                MainScreen(viewModel)
            }
        }
    }

    // Called when the app is already open and the user shares another file
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return

        // The shared file comes as a Uri inside EXTRA_STREAM
        val uri = IntentCompat.getParcelableExtra(
            intent, Intent.EXTRA_STREAM, Uri::class.java
        ) ?: return

        viewModel.onResumeShared(uri, getFileName(this, uri))
    }
}



