package com.example.aicareerpilot.presentation.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.aicareerpilot.R
import com.example.aicareerpilot.presentation.viewmodel.AuthUiState
import com.example.aicareerpilot.presentation.viewmodel.AuthViewModel
import com.example.aicareerpilot.presentation.viewmodel.ResumeViewModel
import kotlin.math.PI
import kotlin.math.sin

@SuppressLint("ContextCastToActivity")
@Composable
fun SignInScreen(resumeViewModel: ResumeViewModel,
    navController: NavHostController
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading

    // 1. Core Animation Setup
    val infiniteTransition = rememberInfiniteTransition(label = "SignInAnimations")
    val activity = LocalContext.current as Activity

    DisposableEffect(Unit) {

        // Lock portrait while SignInScreen is visible
        activity.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        onDispose {

            // Restore normal rotation after leaving screen
            activity.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Smooth scanning loop (2.5 seconds per loop)
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 280f, // Adjusted to cover full vertical padding bounds
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScanOffset"
    )

    // Ambient background pulsing pulse
    val bgGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = SineCrossingEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BgGlow"
    )

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                resumeViewModel.refreshRemainingAttempts()
                Toast.makeText(context, "Google Login Success", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.SignIn.route) { inclusive = true }
                }
                authViewModel.resetState()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
                authViewModel.resetState()
            }
            else -> Unit
        }
    }

    // Main App Container (Dark, rich background with subtle glow)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background blob
        Box(
            modifier = Modifier
                .size(400.dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = bgGlowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Typography updates for premium tech presentation
            Text(
                text = "AI Career Pilot",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Smart AI Resume Analysis",
                color = Color.Gray.copy(alpha = 0.8f),
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 2. Animated Resume Preview Card
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF141414))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            ) {
                // Layout Mockup Lines Inside Card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) { index ->
                        // Calculate approximate Y coordinate block to compare against scanner position
                        val lineYPos = (index * 36) + 20
                        val isHighlighted = scanOffset > lineYPos - 25 && scanOffset < lineYPos + 25

                        // Animate line color dynamically based on laser tracking
                        val lineColor by animateColorAsState(
                            targetValue = if (isHighlighted) Color(0xFF00E5FF) else Color(0xFF2C2C2C),
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "LineColor-$index"
                        )

                        Box(
                            modifier = Modifier
                                .height(10.dp)
                                .fillMaxWidth(fraction = if (index % 3 == 0) 0.6f else if (index % 2 == 0) 0.9f else 0.75f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(lineColor)
                        )
                    }
                }

                // 3. Upgraded Laser & Glow Stack
                Box(
                    modifier = Modifier
                        .offset(y = scanOffset.dp)
                        .fillMaxWidth()
                ) {
                    // Soft laser back-glow trailing behind line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .offset(y = (-12).dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF00E5FF).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Razor crisp core glowing line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.2f),
                                        Color(0xFF00E5FF),
                                        Color(0xFF00E5FF).copy(alpha = 0.2f)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(54.dp))

            // Action Button
            OutlinedButton(
                onClick = {
                    if (!isLoading) {
                        authViewModel.signInWithGoogle(context)
                    }
                },

                enabled = !isLoading,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isLoading)
                        Color(0xFF111111)
                    else
                        Color.Black,

                    disabledContainerColor = Color(0xFF111111),
                    disabledContentColor = Color.White
                ),

                border = BorderStroke(
                    1.dp,
                    if (isLoading)
                        Color(0xFFFFFFFF)
                    else
                        Color.White.copy(alpha = 0.5f)
                )
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    if (isLoading) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFFFFFFF)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Signing in...",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )

                    } else {

                        Image(
                            painter = painterResource(R.drawable.google_logo_new),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Custom Easing Helper to simulate organic scanning transitions
val SineCrossingEasing = Easing { fraction ->
            ((sin((fraction * PI) - (PI / 2)) + 1) / 2).toFloat()
}