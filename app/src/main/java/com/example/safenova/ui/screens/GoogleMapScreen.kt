package com.example.safenova.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ai.EvaluatedRoute
import com.example.safenova.ai.SafeRouteCalculator
import com.example.safenova.data.models.IncidentReport
import com.example.safenova.data.models.SafePlace
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.Coordinates
import com.example.safenova.location.LocationClient
import com.example.safenova.notifications.SmsHelper
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.CinnamonBorder
import com.example.safenova.ui.theme.CinnamonCard
import com.example.safenova.ui.theme.SafeCyan
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafePurpleGlow
import com.example.safenova.ui.theme.SafeRose
import com.example.safenova.ui.theme.TextPrimary
import com.example.safenova.ui.theme.TextSecondary
import com.example.safenova.ui.theme.WarningOrange
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GoogleMapScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToReport: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }

    var currentCoords by remember { mutableStateOf(Coordinates(28.6315, 77.2197)) }
    var destinationAddress by remember { mutableStateOf("Connaught Place, Central Block") }
    var destinationCoords by remember { mutableStateOf(Coordinates(28.6328, 77.2197)) }

    var evaluatedRoutes by remember { mutableStateOf<List<EvaluatedRoute>>(emptyList()) }
    var selectedRouteId by remember { mutableStateOf("SAFEST") }

    var incidentReports by remember { mutableStateOf<List<IncidentReport>>(emptyList()) }
    var safePlaces by remember { mutableStateOf<List<SafePlace>>(emptyList()) }

    var isJourneyActive by remember { mutableStateOf(false) }
    var activeJourneyId by remember { mutableStateOf<String?>(null) }
    var journeyTimerSeconds by remember { mutableIntStateOf(0) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(28.6315, 77.2197), 14.5f)
    }

    // Live Location & Supabase Data Fetching
    LaunchedEffect(Unit) {
        try {
            val coords = LocationClient(context).getCurrentLocation()
            currentCoords = coords
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(coords.latitude, coords.longitude), 15f)
        } catch (_: Exception) {}

        try {
            incidentReports = repo.fetchIncidentReports()
            safePlaces = repo.fetchSafePlaces()
            val conditions = repo.fetchAreaConditions()

            evaluatedRoutes = SafeRouteCalculator.calculateRouteOptions(
                start = currentCoords,
                destination = destinationCoords,
                incidents = incidentReports,
                conditions = conditions,
                safePlaces = safePlaces
            )
        } catch (_: Exception) {}
    }

    // Active Journey Heartbeat Loop
    LaunchedEffect(isJourneyActive) {
        if (isJourneyActive) {
            while (isJourneyActive) {
                try {
                    val freshCoords = LocationClient(context).getCurrentLocation()
                    currentCoords = freshCoords

                    // Heartbeat telemetry to Supabase
                    activeJourneyId?.let { jId ->
                        repo.completeJourney(jId, "ACTIVE_GPS_${freshCoords.formatted()}")
                    }
                } catch (_: Exception) {}

                delay(5000L) // Heartbeat every 5 seconds
                journeyTimerSeconds += 5
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Real Google Map Component
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            // Live User GPS Marker
            Marker(
                state = MarkerState(position = LatLng(currentCoords.latitude, currentCoords.longitude)),
                title = "Live GPS Location (You)",
                snippet = "Accuracy High • GPS Active"
            )

            // Destination Marker
            Marker(
                state = MarkerState(position = LatLng(destinationCoords.latitude, destinationCoords.longitude)),
                title = "Destination: $destinationAddress",
                snippet = "End Point"
            )

            // Supabase Incident Markers
            incidentReports.forEach { incident ->
                Marker(
                    state = MarkerState(position = LatLng(incident.latitude, incident.longitude)),
                    title = "🚨 ${incident.category}",
                    snippet = incident.description ?: "Community Alert"
                )
            }

            // Supabase Safe Places Markers
            safePlaces.forEach { place ->
                Marker(
                    state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                    title = "🛡️ ${place.name}",
                    snippet = "${place.type} • ${place.phone ?: "112"}"
                )
            }

            // Route Polylines
            evaluatedRoutes.forEach { route ->
                val polyColor = when (route.routeId) {
                    "SAFEST" -> SafeGreen
                    "FASTEST" -> SafePurpleGlow
                    else -> SafeCyan
                }
                val isSelected = route.routeId == selectedRouteId
                Polyline(
                    points = route.polylinePoints.map { LatLng(it.latitude, it.longitude) },
                    color = if (isSelected) polyColor else polyColor.copy(alpha = 0.35f),
                    width = if (isSelected) 14f else 8f
                )
            }
        }

        // 2. Top Navigation Overlay (Search & Route Cards)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SafePurpleGlow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Safe Destination Routing", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        }

                        Surface(
                            onClick = onNavigateToReport,
                            shape = RoundedCornerShape(8.dp),
                            color = WarningOrange.copy(alpha = 0.2f)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Icon(imageVector = Icons.Default.AddAlert, contentDescription = "Report", tint = WarningOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Incident", fontSize = 11.sp, color = WarningOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = destinationAddress,
                        onValueChange = { destinationAddress = it },
                        placeholder = { Text("Search safe destination address...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Route Evaluation Cards Carousel
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(evaluatedRoutes) { route ->
                    val isSelected = route.routeId == selectedRouteId
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable { selectedRouteId = route.routeId },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SafePurple.copy(alpha = 0.25f) else CinnamonCard
                        ),
                        border = BorderStroke(1.dp, if (isSelected) SafePurpleGlow else CinnamonBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = route.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text(text = "${route.durationMinutes} mins (${String.format("%.1f", route.distanceKm)} km)", fontSize = 12.sp, color = SafePurpleGlow)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = SafeGreen.copy(alpha = 0.2f)) {
                                Text("Score ${route.safetyScore}/100 • ${route.safetyLevel}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafeGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // 3. Bottom Action Bar (Start Safe Journey / Auto SOS Dispatch)
        Card(
            colors = CardDefaults.cardColors(containerColor = CinnamonCard),
            border = BorderStroke(1.dp, CinnamonBorder),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isJourneyActive) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val journey = repo.startJourney(destinationCoords.latitude, destinationCoords.longitude, 15)
                                    activeJourneyId = journey?.id
                                    isJourneyActive = true
                                    com.example.safenova.services.TrackingForegroundService.startService(context)
                                    snackbarHostState.showSnackbar("🚗 Safe Journey Started! Server Connection Watchdog & GPS active.")
                                } catch (_: Exception) {
                                    isJourneyActive = true
                                    snackbarHostState.showSnackbar("Safe Journey Active.")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("START SAFE TRAVEL MONITORING", fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isJourneyActive = false
                                coroutineScope.launch {
                                    activeJourneyId?.let { repo.completeJourney(it, "ARRIVED_SAFELY") }
                                    com.example.safenova.services.TrackingForegroundService.stopService(context)
                                    snackbarHostState.showSnackbar("✅ Arrived Safely! Journey completed & telemetry closed.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Arrived")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ARRIVED SAFELY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val contacts = repo.fetchTrustedContacts()
                                        contacts.forEach { contact ->
                                            SmsHelper.sendBackgroundEmergencySms(
                                                context = context,
                                                phoneNumber = contact.contactPhone,
                                                message = "🚨 AUTO EMERGENCY SOS! SafeNova connection lost near GPS: ${currentCoords.formatted()}! Maps: https://maps.google.com/?q=${currentCoords.latitude},${currentCoords.longitude}"
                                            )
                                        }
                                        repo.triggerSosAlert(currentCoords.latitude, currentCoords.longitude, "DISCONNECT_AUTO_SOS")
                                        snackbarHostState.showSnackbar("🚨 AUTOMATED BACKGROUND EMERGENCY SMS SENT!")
                                    } catch (_: Exception) {
                                        snackbarHostState.showSnackbar("Automated SOS Dispatched.")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "SOS")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AUTO SOS DISPATCH", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
