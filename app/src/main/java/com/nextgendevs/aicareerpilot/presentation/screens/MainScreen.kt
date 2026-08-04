package com.nextgendevs.aicareerpilot.presentation.screens

import DeveloperTrendsScreen
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.nextgendevs.aicareerpilot.presentation.viewmodel.AuthViewModel
import com.nextgendevs.aicareerpilot.presentation.viewmodel.ResumeViewModel
import com.nextgendevs.aicareerpilot.util.DeviceType
import com.nextgendevs.aicareerpilot.util.getDeviceType
sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object History : Screen("history", Icons.Default.History, "History")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")

    object DeveloperTrends : Screen(
        "developer_trends",
        Icons.AutoMirrored.Filled.TrendingUp,
        "Trends"
    )
    object SignIn : Screen(
        "sign_in",
        Icons.AutoMirrored.Filled.Login,
        "Sign In"
    )
    object SignUp : Screen(
        "sign_up",
        Icons.AutoMirrored.Filled.Login,
        "Sign Up"
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
fun MainScreen(resumeViewModel: ResumeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    val remainingAttempts by
    resumeViewModel.remainingAttempts.collectAsState()
    var showCreditDialog by remember {
        mutableStateOf(false)
    }
    val screenSubtitle = when (currentRoute) {

        Screen.Home.route ->
            "Analyze your resume against any job description"

        Screen.History.route ->
            "Track your previous AI resume analyses"

        Screen.DeveloperTrends.route ->
            "Explore trending technologies and developer discussions"

        Screen.Profile.route ->
            "Manage your account and app preferences"

        Screen.SignIn.route ->
            "Secure AI-powered career assistance"

        else ->
            "AI-powered career growth platform"
    }
    val screens = listOf(
        Screen.Home,
        Screen.History,
        Screen.DeveloperTrends,
        Screen.Profile
    )
    val deviceType = getDeviceType()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn = authViewModel.isLoggedIn.collectAsState()
    val showBars = currentRoute != Screen.SignIn.route &&
            currentRoute != Screen.SignUp.route

    val startDestination =
        if (isLoggedIn.value)
            Screen.Home.route
        else
            Screen.SignIn.route

    Row(modifier = Modifier.fillMaxSize()) {

        // 1. TABLET SIDE: Navigation Rail
        if (showBars && deviceType == DeviceType.TABLET) {
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "AI Career Pilot",
                                    style = if (deviceType == DeviceType.TABLET)
                                        MaterialTheme.typography.displayMedium
                                    else
                                        MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Spacer(Modifier.width(28.dp))

                                Surface(
                                    onClick = {
                                        showCreditDialog = true
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.08f),
                                    border = BorderStroke(
                                        1.dp,
                                        Color.White.copy(alpha = 0.15f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.size(30.dp),

                                        ) {
                                            CircularProgressIndicator(
                                                progress = { remainingAttempts / 3f },
                                                modifier = Modifier.fillMaxSize(),
                                                strokeWidth = 2.dp,
                                                color = Color.White,
                                                trackColor = Color.White.copy(alpha = 0.15f),
                                            )

                                            Text(
                                                text = remainingAttempts.toString(),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }


                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = screenSubtitle,
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
                        resumeViewModel = resumeViewModel,
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
                        resumeViewModel = resumeViewModel,
                        navController = navController
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        resumeViewModel,
                        onRecordClick = { record ->
                            navController.navigate(
                                Screen.HistoryDetail.createRoute(record.id)
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.DeveloperTrends.route) {
                    DeveloperTrendsScreen(resumeViewModel)
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(resumeViewModel,navController)
                }

                composable(Screen.SignUp.route) {
                    SignUpScreen(authViewModel = authViewModel, navController = navController)
                }

                composable("history_detail/{recordId}") { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getString("recordId")?.toIntOrNull()

                    // Fast path: Just pass the ID. The NavHost layout stays completely lightweight.
                    HistoryDetailScreen(
                        recordId = recordId,
                        resumeViewModel = resumeViewModel
                    )
                }
            }
        }
    }
    if (showCreditDialog) {
        CreditInfoDialog(
            remainingAttempts = remainingAttempts,
            onDismiss = { showCreditDialog = false }
        )
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