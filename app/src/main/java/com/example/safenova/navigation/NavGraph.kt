package com.example.safenova.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.safenova.ui.components.SafeNovaBottomBar
import com.example.safenova.ui.components.SafeNovaTopBar
import com.example.safenova.ui.screens.AreaConditionsScreen
import com.example.safenova.ui.screens.AuthViewModel
import com.example.safenova.ui.screens.CrimeReportScreen
import com.example.safenova.ui.screens.EmergencyPinScreen
import com.example.safenova.ui.screens.EscortRideScreen
import com.example.safenova.ui.screens.FindSheltersScreen
import com.example.safenova.ui.screens.HomeScreen
import com.example.safenova.ui.screens.LoginScreen
import com.example.safenova.ui.screens.SettingsScreen
import com.example.safenova.ui.screens.SosScreen

@Composable
fun SafeNovaNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    if (!authViewModel.isAuthenticated) {
        LoginScreen(vm = authViewModel)
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val currentScreenTitle = when (currentRoute) {
        Screen.Home.route -> Screen.Home.title
        Screen.Sos.route -> Screen.Sos.title
        Screen.Settings.route -> Screen.Settings.title
        Screen.CrimeReport.route -> Screen.CrimeReport.title
        Screen.AreaConditions.route -> Screen.AreaConditions.title
        Screen.FindShelters.route -> Screen.FindShelters.title
        Screen.EscortRide.route -> Screen.EscortRide.title
        Screen.EmergencyPin.route -> Screen.EmergencyPin.title
        Screen.EmergencyProfile.route -> Screen.EmergencyProfile.title
        Screen.Map.route -> Screen.Map.title
        else -> "SafeNova"
    }

    val canNavigateBack = currentRoute != Screen.Home.route

    Scaffold(
        topBar = {
            SafeNovaTopBar(
                title = currentScreenTitle,
                canNavigateBack = canNavigateBack,
                onNavigateBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            SafeNovaBottomBar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigate = { screen -> navController.navigate(screen.route) },
                    snackbarHostState = snackbarHostState
                )
            }

            composable(Screen.Sos.route) {
                SosScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    snackbarHostState = snackbarHostState,
                    onSignOut = { authViewModel.signOut() },
                    onNavigateToProfile = { navController.navigate(Screen.EmergencyProfile.route) }
                )
            }

            composable(Screen.EmergencyProfile.route) {
                com.example.safenova.ui.screens.EmergencyProfileScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.CrimeReport.route) {
                CrimeReportScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.AreaConditions.route) {
                AreaConditionsScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.FindShelters.route) {
                FindSheltersScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.EscortRide.route) {
                EscortRideScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.EmergencyPin.route) {
                EmergencyPinScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.Map.route) {
                com.example.safenova.ui.screens.MapScreen(snackbarHostState = snackbarHostState)
            }
        }
    }
}
