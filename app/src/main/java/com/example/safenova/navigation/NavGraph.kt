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
import com.example.safenova.ui.screens.EmergencyProfileScreen
import com.example.safenova.ui.screens.EscortRideScreen
import com.example.safenova.ui.screens.FindSheltersScreen
import com.example.safenova.ui.screens.HomeScreen
import com.example.safenova.ui.screens.LoginScreen
import com.example.safenova.ui.screens.MapScreen
import com.example.safenova.ui.screens.SettingsScreen
import com.example.safenova.ui.screens.SosScreen
import com.example.safenova.ui.screens.TrustedContactsScreen

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
        Screen.Home.route -> "SAFENOVA"
        Screen.Map.route -> Screen.Map.title
        Screen.Sos.route -> Screen.Sos.title
        Screen.TrustedContacts.route -> "Trusted Contacts"
        Screen.FindShelters.route -> "Nearby Help & Safe Stays"
        Screen.Settings.route -> Screen.Settings.title
        Screen.CrimeReport.route -> Screen.CrimeReport.title
        Screen.AreaConditions.route -> Screen.AreaConditions.title
        Screen.EscortRide.route -> Screen.EscortRide.title
        Screen.EmergencyPin.route -> Screen.EmergencyPin.title
        Screen.EmergencyProfile.route -> Screen.EmergencyProfile.title
        else -> "SAFENOVA"
    }

    val rootRoutes = setOf(
        Screen.Home.route,
        Screen.Map.route,
        Screen.Sos.route,
        Screen.TrustedContacts.route,
        Screen.FindShelters.route
    )
    val canNavigateBack = currentRoute !in rootRoutes

    Scaffold(
        topBar = {
            SafeNovaTopBar(
                title = currentScreenTitle,
                canNavigateBack = canNavigateBack,
                onNavigateBack = { navController.popBackStack() },
                onQuickSos = {
                    if (currentRoute != Screen.Sos.route) {
                        navController.navigate(Screen.Sos.route) {
                            launchSingleTop = true
                        }
                    }
                },
                onOpenSettings = {
                    if (currentRoute != Screen.Settings.route) {
                        navController.navigate(Screen.Settings.route) {
                            launchSingleTop = true
                        }
                    }
                }
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

            composable(Screen.Map.route) {
                MapScreen(
                    snackbarHostState = snackbarHostState,
                    onNavigateToReport = { navController.navigate(Screen.CrimeReport.route) },
                    onNavigateToSos = { navController.navigate(Screen.Sos.route) }
                )
            }

            composable(Screen.Sos.route) {
                SosScreen(
                    snackbarHostState = snackbarHostState,
                    onNavigateToContacts = { navController.navigate(Screen.TrustedContacts.route) }
                )
            }

            composable(Screen.TrustedContacts.route) {
                TrustedContactsScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.FindShelters.route) {
                FindSheltersScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    snackbarHostState = snackbarHostState,
                    onSignOut = { authViewModel.signOut() },
                    onNavigateToProfile = { navController.navigate(Screen.EmergencyProfile.route) },
                    onNavigateToContacts = { navController.navigate(Screen.TrustedContacts.route) },
                    onNavigateToPin = { navController.navigate(Screen.EmergencyPin.route) }
                )
            }

            composable(Screen.EmergencyProfile.route) {
                EmergencyProfileScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.CrimeReport.route) {
                CrimeReportScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.AreaConditions.route) {
                AreaConditionsScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.EscortRide.route) {
                EscortRideScreen(snackbarHostState = snackbarHostState)
            }

            composable(Screen.EmergencyPin.route) {
                EmergencyPinScreen(snackbarHostState = snackbarHostState)
            }
        }
    }
}
