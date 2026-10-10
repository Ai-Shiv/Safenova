package com.example.safenova.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "SafeNova Home", Icons.Default.Home)
    object Login : Screen("login", "Login", Icons.Default.Lock)
    object Sos : Screen("sos", "Emergency SOS", Icons.Default.Warning)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object CrimeReport : Screen("crime_report", "Crime Report", Icons.Default.Report)
    object AreaConditions : Screen("area_conditions", "Area Conditions", Icons.Default.Explore)
    object FindShelters : Screen("find_shelters", "Find Shelters", Icons.Default.NightShelter)
    object EscortRide : Screen("escort_ride", "Find Escort Ride", Icons.Default.DirectionsCar)
    object EmergencyPin : Screen("emergency_pin", "Emergency PIN", Icons.Default.Lock)
    object EmergencyProfile : Screen("emergency_profile", "Emergency Medical Profile", Icons.Default.Lock)
    object Map : Screen("map", "Safety Map & Routes", Icons.Default.Explore)
}
