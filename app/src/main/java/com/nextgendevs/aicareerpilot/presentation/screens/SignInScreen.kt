package com.nextgendevs.aicareerpilot.presentation.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.nextgendevs.aicareerpilot.presentation.viewmodel.AuthUiState
import com.nextgendevs.aicareerpilot.presentation.viewmodel.AuthViewModel
import com.nextgendevs.aicareerpilot.presentation.viewmodel.ResumeViewModel
import com.nextgendevs.aicareerpilot.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.nextgendevs.aicareerpilot.presentation.theme.primarycolor
import com.nextgendevs.aicareerpilot.presentation.theme.secondarycolor
import com.nextgendevs.aicareerpilot.presentation.theme.tertiarycolor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ContextCastToActivity")
@Composable
fun SignInScreen(resumeViewModel: ResumeViewModel,
    navController: NavHostController
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val activity = LocalContext.current as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }



    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                resumeViewModel.refreshRemainingAttempts()
                Toast.makeText(context, "Google Login Success", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) {
                        inclusive = true
                    }
                    launchSingleTop = true
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
            Spacer(modifier = Modifier.height(90.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it
                    scope.launch {
                        delay(30)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }},
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = secondarycolor,
                    unfocusedLabelColor = Color.Gray,
                    focusedLabelColor = primarycolor,
                    focusedContainerColor = tertiarycolor,
                    unfocusedContainerColor = tertiarycolor,
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),

                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = secondarycolor,
                    focusedLabelColor = primarycolor,
                    focusedContainerColor = tertiarycolor,
                    unfocusedContainerColor = tertiarycolor,
                    unfocusedLabelColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = {

                    authViewModel.loginWithEmail(
                        email.trim(),
                        password
                    )

                },
                enabled = !isLoading,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(50.dp),

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

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )

                } else {

                    Text("Login")

                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "OR",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    .padding(horizontal = 8.dp)
                    .height(50.dp),

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
                            painter = painterResource(R.drawable.google_logo),
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
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(

                onClick = {

                    navController.navigate(Screen.SignUp.route)

                }

            ) {

                Text(
                    "Create Account",
                    color = Color.White
                )

            }
        }
    }
}
