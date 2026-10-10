package com.example.safenova.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.data.models.IncidentReport
import com.example.safenova.data.models.SafePlace
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }

    var destinationQuery by remember { mutableStateOf("") }
    var selectedRoute by remember { mutableStateOf("SAFEST") } // "SAFEST" vs "FASTEST"
    var incidentsList by remember { mutableStateOf<List<IncidentReport>>(emptyList()) }
    var safePlacesList by remember { mutableStateOf<List<SafePlace>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            incidentsList = repo.fetchIncidentReports()
            safePlacesList = repo.fetchSafePlaces()
        } catch (_: Exception) {}
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Header & Route Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Safety-Aware Navigation", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = destinationQuery,
                        onValueChange = { destinationQuery = it },
                        placeholder = { Text("Enter destination address...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SafePurple) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RouteOptionCard(
                            title = "🛡️ Safest Route",
                            time = "18 mins",
                            badge = "Well-lit • Police Patrol",
                            selected = selectedRoute == "SAFEST",
                            onClick = { selectedRoute = "SAFEST" },
                            modifier = Modifier.weight(1f)
                        )

                        RouteOptionCard(
                            title = "⚡ Fastest Route",
                            time = "12 mins",
                            badge = "Caution: Low Lighting",
                            selected = selectedRoute == "FASTEST",
                            onClick = { selectedRoute = "FASTEST" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    "🧭 Starting ${if (selectedRoute == "SAFEST") "Safest" else "Fastest"} Navigation..."
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRoute == "SAFEST") SafeGreen else SafePurple
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "START SAFE NAVIGATION", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Simulated Visual Map View Overlay
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map Center",
                            tint = SafePurple,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Live GPS Heatmap Active",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${incidentsList.size} Incident Alerts • ${safePlacesList.size} Safe Refuges Plotted",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Live Incident Markers Feed
        item {
            Text(text = "Live Map Incident Markers (${incidentsList.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (incidentsList.isEmpty()) {
                    IncidentMarkerCard(
                        category = "Poor Lighting Alert",
                        description = "2 streetlights broken in alley corner",
                        distance = "0.3 km away",
                        isAlert = true
                    )
                    IncidentMarkerCard(
                        category = "Grace Refuge Shelter",
                        description = "Verified Women Safe Refuge • Open 24/7",
                        distance = "0.8 km away",
                        isAlert = false
                    )
                } else {
                    incidentsList.forEach { incident ->
                        IncidentMarkerCard(
                            category = incident.category,
                            description = incident.description ?: "Community safety report",
                            distance = "Nearby GPS",
                            isAlert = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteOptionCard(
    title: String,
    time: String,
    badge: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SafePurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = time, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = SafePurple)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = badge, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun IncidentMarkerCard(
    category: String,
    description: String,
    distance: String,
    isAlert: Boolean
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background((if (isAlert) AlertRed else SafeGreen).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAlert) Icons.Default.ReportProblem else Icons.Default.Shield,
                    contentDescription = category,
                    tint = if (isAlert) AlertRed else SafeGreen
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = category, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = (if (isAlert) AlertRed else SafeGreen).copy(alpha = 0.15f)
            ) {
                Text(
                    text = distance,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAlert) AlertRed else SafeGreen,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
