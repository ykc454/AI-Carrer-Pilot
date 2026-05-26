package com.example.aicareerpilot.presentation.screens

import androidx.compose.foundation.layout.width
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.aicareerpilot.presentation.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black,
                        Color(0xFF111111),
                        Color(0xFF1C1C1C)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // Header Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "YC",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Yash Chaudhari",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Android Developer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "AI Career Pilot helps users analyze resumes against job descriptions using AI-powered ATS scoring and career insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Features Section
        ProfileSectionTitle(title = "App Features")

        Spacer(modifier = Modifier.height(12.dp))

        FeatureItem(
            icon = Icons.Default.Work,
            title = "Resume Analysis",
            subtitle = "Analyze resume against job descriptions"
        )

        FeatureItem(
            icon = Icons.Default.Star,
            title = "ATS Match Score",
            subtitle = "Get professional ATS compatibility score"
        )

        FeatureItem(
            icon = Icons.Default.Description,
            title = "Resume Feedback",
            subtitle = "Strengths and improvement suggestions"
        )

        FeatureItem(
            icon = Icons.Default.Code,
            title = "AI Powered",
            subtitle = "Smart AI analysis using modern AI models"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Actions Section
        ProfileSectionTitle(title = "More")

        Spacer(modifier = Modifier.height(12.dp))

        ActionItem(
            icon = Icons.Default.PrivacyTip,
            title = "Privacy Policy"
        ) {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://your-privacy-policy-link.com")
            )

            context.startActivity(intent)
        }

        ActionItem(
            icon = Icons.Default.Info,
            title = "About App"
        ) {

        }

        ActionItem(
            icon = Icons.Default.Email,
            title = "Contact Developer"
        ) {

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:yourmail@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "AI Career Pilot Feedback")
            }

            context.startActivity(intent)
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

            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share App"
                )
            )
        }
        ActionItem(
            icon = Icons.Default.Logout,
            title = "Logout"
        ) {
            showLogoutDialog = true
        }

        Spacer(modifier = Modifier.height(32.dp))

        Divider(color = Color.DarkGray)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Version 1.0.0",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "© 2026 Yash Chaudhari",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
    if (showLogoutDialog) {

        AlertDialog(

            onDismissRequest = {
                showLogoutDialog = false
            },

            containerColor = Color(0xFF1A1A1A),

            title = {

                Text(
                    text = "Logout",
                    color = Color.White
                )
            },

            text = {

                Text(
                    text = "Are you sure you want to logout?",
                    color = Color.LightGray
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
                        color = Color.Red
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
                        color = Color.White
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
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f)
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            Column {

                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}



