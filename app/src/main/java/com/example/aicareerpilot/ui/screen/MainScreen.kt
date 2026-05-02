package com.example.aicareerpilot.ui.screen

import android.net.http.SslCertificate.restoreState
import android.net.http.SslCertificate.saveState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.aicareerpilot.ui.viewmodel.ResumeViewModel
import com.example.aicareerpilot.util.DeviceType
import com.example.aicareerpilot.util.getDeviceType

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object History : Screen("history", Icons.Default.History, "History")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
}

@Composable
fun MainScreen(viewModel: ResumeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val screens = listOf(Screen.Home, Screen.History, Screen.Profile)
    val deviceType = getDeviceType()

    // YOUR ROW IS BACK HERE
    Row(modifier = Modifier.fillMaxSize()) {

        // 1. TABLET SIDE: Navigation Rail
        if (deviceType == DeviceType.TABLET) {
            NavigationRail(
                containerColor = Color.Black,
                header = { /* Optional: Add logo here */ }
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = { handleNavigation(navController, screen.route) },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color.White,
                            indicatorColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        // 2. MAIN CONTENT AREA (Scaffold)
        Scaffold(
            bottomBar = {
                if (deviceType == DeviceType.PHONE) {
                    NavigationBar(
                        containerColor = Color.Black,
                        tonalElevation = 0.dp,
                        // Removes extra system padding
                        modifier = Modifier.height(70.dp) // Set your desired height here (standard is 80dp)
                    ) {
                        screens.forEach { screen ->
                            val isSelected = currentRoute == screen.route

                            // 1. "Blink" Animation: Soft pulse when active
                            val infiniteTransition = rememberInfiniteTransition(label = "blink")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "glowAlpha"
                            )

                            // 2. Scale Animation: The icon grows slightly
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.25f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "scale"
                            )

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ){
                                        Box(contentAlignment = Alignment.Center) {
                                        // The "Spotlight" Glow


                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = null,
                                            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                                            tint = if (isSelected) Color.White else Color.DarkGray
                                        )

                                    }
                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = screen.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else Color.DarkGray,
                                            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                                        )
                                    }

                                },

                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent // We use our own glow
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Home.route) { HomeScreen(viewModel) }
                composable(Screen.History.route) { HistoryScreen(viewModel) }
                composable(Screen.Profile.route) { /* ProfileScreen() */ }
            }
        }
    }
}

// Clean helper for navigation logic
private fun handleNavigation(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}