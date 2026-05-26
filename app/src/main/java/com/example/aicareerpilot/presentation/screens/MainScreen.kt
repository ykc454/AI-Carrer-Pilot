package com.example.aicareerpilot.presentation.screens

import SignInScreen
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.aicareerpilot.presentation.viewmodel.AuthViewModel
import com.example.aicareerpilot.presentation.viewmodel.ResumeViewModel
import com.example.aicareerpilot.util.DeviceType
import com.example.aicareerpilot.util.getDeviceType

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object History : Screen("history", Icons.Default.History, "History")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
    object SignIn : Screen(
        "sign_in",
        Icons.Default.Login,
        "Sign In"
    )
    object HistoryDetail : Screen(
        route = "history_detail/{recordId}",
        icon = Icons.Default.Description,
        label = "Detail"
    ) {
        fun createRoute(recordId: Int): String {
            return "history_detail/$recordId"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ResumeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val screens = listOf(Screen.Home, Screen.History, Screen.Profile)
    val deviceType = getDeviceType()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val authViewModel: AuthViewModel = hiltViewModel()

    val showBars = currentRoute != Screen.SignIn.route

    val startDestination =
        if (authViewModel.isLoggedIn())
            Screen.Home.route
        else
            Screen.SignIn.route
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
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                if (showBars) {
                LargeTopAppBar(

                    title = {
                        Column {
                            Text(
                                text = "AI Career Pilot",
                                style = if (getDeviceType() == DeviceType.TABLET)
                                    MaterialTheme.typography.displayMedium
                                else
                                    MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "Analyze your resume against any job description",
                                Modifier.padding(bottom = 11.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Black,
                        scrolledContainerColor = Color.Black
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
            },
            bottomBar = {
                if (showBars && deviceType == DeviceType.PHONE) {
                    NavigationBar(
                        containerColor = Color.Black,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .height(58.dp)
                    ) {
                        screens.forEach { screen ->
                            val isSelected = currentRoute == screen.route


                            // 2. Scale Animation: The icon grows slightly
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.25f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "scale"
                            )

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    handleNavigation(navController, screen.route)
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
                startDestination = startDestination,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onRecordClick = { record ->
                            navController.navigate(
                                Screen.HistoryDetail.createRoute(record.id)
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.SignIn.route) {
                    SignInScreen(
                        navController
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel,
                        onRecordClick = { record ->
                            navController.navigate(
                                Screen.HistoryDetail.createRoute(record.id)
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(navController)
                }

                composable("history_detail/{recordId}") { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getString("recordId")?.toIntOrNull()

                    // Fast path: Just pass the ID. The NavHost layout stays completely lightweight.
                    HistoryDetailScreen(
                        recordId = recordId,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// Clean helper for navigation logic
private fun handleNavigation(
    navController: NavHostController,
    route: String
) {

    navController.navigate(route) {

        // Remove detail screens
        popUpTo(navController.graph.startDestinationId)

        launchSingleTop = true
        restoreState = true
    }
}