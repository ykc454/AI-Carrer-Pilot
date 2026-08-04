package com.nextgendevs.aicareerpilot.presentation.screens


import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.nextgendevs.aicareerpilot.presentation.theme.primarycolor
import com.nextgendevs.aicareerpilot.presentation.theme.secondarycolor
import com.nextgendevs.aicareerpilot.presentation.theme.tertiarycolor
import com.nextgendevs.aicareerpilot.presentation.viewmodel.AuthUiState
import com.nextgendevs.aicareerpilot.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("ContextCastToActivity")
@Composable
fun SignUpScreen(navController: NavHostController,authViewModel: AuthViewModel) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState by authViewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading
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

                Toast.makeText(
                    context,
                    "Account created successfully",
                    Toast.LENGTH_SHORT
                ).show()

                navController.navigate(Screen.SignIn.route) {
                    popUpTo(0) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }

                authViewModel.resetState()
            }

            is AuthUiState.Error -> {

                Toast.makeText(
                    context,
                    (uiState as AuthUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()

                authViewModel.resetState()
            }

            else -> Unit
        }
    }


    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(32.dp))
                Text("Create Guest Account", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it
                        scope.launch {
                            delay(30)
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }},
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = secondarycolor,
                        focusedLabelColor = primarycolor,
                        focusedContainerColor = tertiarycolor,
                        unfocusedContainerColor = tertiarycolor,
                        unfocusedLabelColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),

                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = secondarycolor,
                        focusedLabelColor = primarycolor,
                        focusedContainerColor = tertiarycolor,
                        unfocusedContainerColor = tertiarycolor,
                        unfocusedLabelColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))



                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.SignUp.route) {
                                inclusive = true
                            }
                        }
                        authViewModel.registerWithEmail(email, password)

                    },
                    modifier = Modifier
                        .fillMaxWidth().padding(horizontal = 20.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(primarycolor)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("Sign Up", fontSize = 18.sp)
                    }

                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = {
                    navController.popBackStack()
                }
                ) {
                    Text("Already have an account? Login",color = Color.DarkGray)
                }
            }
        }
    }
}