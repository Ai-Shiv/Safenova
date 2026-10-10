package com.example.safenova.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Map : Screen("map", "Map & Routes", Icons.Default.Map)
    object Sos : Screen("sos", "SOS Panel", Icons.Default.Warning)
    object TrustedContacts : Screen("trusted_contacts", "Contacts", Icons.Default.People)
    object FindShelters : Screen("find_shelters", "Nearby Help", Icons.Default.LocalHospital)
    object Settings : Screen("settings", "Profile & Settings", Icons.Default.Settings)
    object Login : Screen("login", "Login", Icons.Default.Lock)
    object CrimeReport : Screen("crime_report", "Report Incident", Icons.Default.Report)
    object AreaConditions : Screen("area_conditions", "Area Conditions", Icons.Default.Explore)
    object EscortRide : Screen("escort_ride", "Escort & Campus Mode", Icons.Default.DirectionsCar)
    object EmergencyPin : Screen("emergency_pin", "Duress & App PIN", Icons.Default.Lock)
    object EmergencyProfile : Screen("emergency_profile", "Emergency Medical Info", Icons.Default.Person)
}
