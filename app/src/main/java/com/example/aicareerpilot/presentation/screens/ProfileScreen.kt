package com.example.aicareerpilot.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.aicareerpilot.presentation.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

private val BackgroundColor = Color(0xFF000000)
private val CardColor = Color(0xFF000000)
private val BorderColor = Color(0xFFFFFFFF)
private val PrimaryWhite = Color(0xFFF5F5F5)
private val SecondaryText = Color(0xFF9E9E9E)

@Composable
fun ProfileScreen(navController: NavHostController) {

    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    val currentUser = FirebaseAuth.getInstance().currentUser

    val userName = currentUser?.displayName ?: "Guest User"
    val userEmail = currentUser?.email ?: "No Email"

    val initials = userName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {

        // HEADER CARD

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = 1.dp,
                color = Color(0xFF5E5E5E)
            ),
            colors = CardDefaults.cardColors(
                containerColor = CardColor
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Avatar

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = initials,
                        color = Color.Black,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = userName,
                    color = PrimaryWhite,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = userEmail,
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(22.dp))

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color.White.copy(alpha = 0.06f)
                ) {

                    Text(
                        text = "AI Career Pilot User",
                        modifier = Modifier.padding(
                            horizontal = 18.dp,
                            vertical = 10.dp
                        ),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Premium AI-powered resume analysis and ATS optimization platform.",
                    color = SecondaryText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))


        // ACTIONS

        ProfileSectionTitle(title = "Legal & Information")

        Spacer(modifier = Modifier.height(16.dp))

        ActionItem(
            icon = Icons.Default.PrivacyTip,
            title = "Privacy Policy"
        ) {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://ykc454.github.io/ai-career-pilot-privacy/")
            )

            runCatching {
                context.startActivity(intent)
            }
        }

        ActionItem(
            icon = Icons.Default.Info,
            title = "About App"
        ) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://ykc454.github.io/ai_career_pilot_about_app/")
            )

            runCatching {
                context.startActivity(intent)
            }

        }

        ActionItem(
            icon = Icons.Default.Email,
            title = "Contact Developer"
        ) {

            val intent = Intent(Intent.ACTION_SENDTO).apply {

                data = Uri.parse("mailto:crazyfintak@gmail.com")

                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "AI Career Pilot Feedback"
                )
            }

            runCatching {
                context.startActivity(intent)
            }
        }

        ActionItem(
            icon = Icons.Default.Share,
            title = "Share App"
        ) {

            val shareIntent = Intent(Intent.ACTION_SEND).apply {

                type = "text/plain"

                putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out AI Career Pilot app!"
                )
            }
            runCatching {
                context.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share App"
                    )
                )
            }

        }

        ActionItem(
            Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
            isLogout = true
        ) {

            showLogoutDialog = true
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = BorderColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Version 1.0.0",
            color = SecondaryText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "© 2026 Yash Chaudhari",
            color = SecondaryText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    // LOGOUT DIALOG

    if (showLogoutDialog) {

        AlertDialog(

            onDismissRequest = {
                showLogoutDialog = false
            },

            containerColor = CardColor,

            title = {

                Text(
                    text = "Logout",
                    color = PrimaryWhite,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text = "Are you sure you want to logout?",
                    color = SecondaryText
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        showLogoutDialog = false

                        authViewModel.logout()

                        navController.navigate(Screen.SignIn.route) {

                            popUpTo(0) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }
                ) {

                    Text(
                        text = "Logout",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {
                        showLogoutDialog = false
                    }
                ) {

                    Text(
                        text = "Cancel",
                        color = SecondaryText
                    )
                }
            }
        )
    }
}

@Composable
fun ProfileSectionTitle(
    title: String
) {

    Text(
        text = title,
        color = PrimaryWhite,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ActionItem(
    icon: ImageVector,
    title: String,
    isLogout: Boolean = false,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                color = if (isLogout) Color.White else PrimaryWhite,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}