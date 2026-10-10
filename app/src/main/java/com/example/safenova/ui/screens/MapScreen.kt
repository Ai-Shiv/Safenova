package com.example.safenova.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ai.AiSafetyEngine
import com.example.safenova.ai.RouteOptionPrototype
import com.example.safenova.data.models.IncidentReport
import com.example.safenova.data.models.SafePlace
import com.example.safenova.data.models.TrustedContact
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.Coordinates
import com.example.safenova.location.LocationClient
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.CinnamonBorder
import com.example.safenova.ui.theme.CinnamonCard
import com.example.safenova.ui.theme.CinnamonCardElevated
import com.example.safenova.ui.theme.CinnamonObsidian
import com.example.safenova.ui.theme.SafeCyan
import com.example.safenova.ui.theme.SafeDarkPurple
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafePurpleGlow
import com.example.safenova.ui.theme.SafeRose
import com.example.safenova.ui.theme.TextPrimary
import com.example.safenova.ui.theme.TextSecondary
import com.example.safenova.ui.theme.WarningOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToReport: () -> Unit = {},
    onNavigateToSos: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }
    val aiEngine = remember { AiSafetyEngine() }

    var currentCoords by remember { mutableStateOf(LocationClient.DEFAULT_COORDINATES) }
    var destinationQuery by remember { mutableStateOf("North Campus Dorms, Block B") }
    var selectedRouteId by remember { mutableStateOf("SAFEST") }
    var showHeatmap by remember { mutableStateOf(true) }
    var showPins by remember { mutableStateOf(true) }
    var simulateNightMode by remember { mutableStateOf(true) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    var incidentsList by remember { mutableStateOf<List<IncidentReport>>(emptyList()) }
    var safePlacesList by remember { mutableStateOf<List<SafePlace>>(emptyList()) }
    var contactsList by remember { mutableStateOf<List<TrustedContact>>(emptyList()) }

    // Journey Mode & Auto Check-In state
    var selectedEtaMinutes by remember { mutableIntStateOf(15) }
    var activeJourneyId by remember { mutableStateOf<String?>(null) }
    var isJourneyActive by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(0) }

    val destinationPresets = listOf(
        "North Campus Dorms, Block B",
        "Connaught Place Metro Gate 3",
        "City General Trauma Hospital",
        "Shakti Sadan Safe Night Shelter"
    )

    val reportFilters = listOf(
        "All",
        "Poor Lighting",
        "Crowd Level",
        "Blocked Road",
        "Harassment",
        "Suspicious Activity"
    )

    fun refreshMapData() {
        coroutineScope.launch {
            try {
                currentCoords = LocationClient(context).getCurrentLocation()
            } catch (_: Exception) {}
            try {
                incidentsList = repo.fetchIncidentReports()
                safePlacesList = repo.fetchSafePlaces()
                contactsList = repo.fetchTrustedContacts()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        refreshMapData()
    }

    val routes: List<RouteOptionPrototype> = remember(destinationQuery, incidentsList, safePlacesList, simulateNightMode) {
        aiEngine.evaluateRoutesPrototype(
            destinationName = destinationQuery,
            incidents = incidentsList,
            safePlaces = safePlacesList,
            isNightMode = simulateNightMode
        )
    }

    val filteredIncidents = remember(incidentsList, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") {
            incidentsList
        } else {
            incidentsList.filter { it.category.contains(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    // Auto Check-In Countdown Timer
    LaunchedEffect(isJourneyActive, remainingSeconds) {
        if (isJourneyActive && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                // Timer expired without user check-in -> auto-alert trusted contacts & Supabase SOS!
                isJourneyActive = false
                try {
                    activeJourneyId?.let { repo.completeJourney(it, "MISSED_CHECKIN_SOS") }
                    repo.triggerSosAlert(
                        currentCoords.latitude,
                        currentCoords.longitude,
                        "AUTO_CHECKIN_EXPIRED_FOR_${destinationQuery.take(24)}"
                    )
                } catch (_: Exception) {}
                snackbarHostState.showSnackbar("🚨 AUTO CHECK-IN EXPIRED! SOS created in Supabase & Trusted Contacts alerted!")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Visual Interactive Dark Map + Heatmap + Route Polylines Canvas
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Live GPS Map & Safety Heatmap",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "GPS: ${currentCoords.formatted()} • ${filteredIncidents.size} Pins • ${safePlacesList.size} Safe Places",
                                fontSize = 11.sp,
                                color = SafePurpleGlow
                            )
                        }

                        Surface(
                            onClick = onNavigateToReport,
                            shape = RoundedCornerShape(8.dp),
                            color = WarningOrange.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, WarningOrange)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAlert,
                                    contentDescription = "Report",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+ Report Pin",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningOrange
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Map Layer Toggle Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MapTogglePill(
                            label = if (showHeatmap) "🔥 Heatmap ON" else "🔥 Heatmap OFF",
                            active = showHeatmap,
                            activeColor = SafeRose,
                            onClick = { showHeatmap = !showHeatmap },
                            modifier = Modifier.weight(1f)
                        )
                        MapTogglePill(
                            label = if (showPins) "📍 Pins (${filteredIncidents.size})" else "📍 Pins Hidden",
                            active = showPins,
                            activeColor = SafePurpleGlow,
                            onClick = { showPins = !showPins },
                            modifier = Modifier.weight(1f)
                        )
                        MapTogglePill(
                            label = if (simulateNightMode) "🌙 Night Score" else "☀️ Day Score",
                            active = simulateNightMode,
                            activeColor = SafeCyan,
                            onClick = { simulateNightMode = !simulateNightMode },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom Vector Map Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(235.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CinnamonObsidian)
                            .border(1.dp, CinnamonBorder, RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // 1. Subtle Cinnamon Dark Street Grid
                            val gridCols = 8
                            val gridRows = 6
                            for (i in 1 until gridCols) {
                                val x = w * (i.toFloat() / gridCols)
                                drawLine(
                                    color = Color(0xFF1E1A2D),
                                    start = Offset(x, 0f),
                                    end = Offset(x, h),
                                    strokeWidth = if (i % 2 == 0) 2.5f else 1f
                                )
                            }
                            for (j in 1 until gridRows) {
                                val y = h * (j.toFloat() / gridRows)
                                drawLine(
                                    color = Color(0xFF1E1A2D),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = if (j % 2 == 0) 2.5f else 1f
                                )
                            }

                            val startPt = Offset(w * 0.16f, h * 0.78f)
                            val endPt = Offset(w * 0.84f, h * 0.22f)

                            // 2. Heatmap Layer (only shown when >= 3 reports exist in area)
                            if (showHeatmap && filteredIncidents.size >= 3) {
                                val hotspots = listOf(
                                    Offset(w * 0.52f, h * 0.50f) to AlertRed,
                                    Offset(w * 0.64f, h * 0.62f) to WarningOrange,
                                    Offset(w * 0.42f, h * 0.36f) to SafeRose
                                )
                                hotspots.forEach { (center, heatColor) ->
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                heatColor.copy(alpha = 0.42f),
                                                heatColor.copy(alpha = 0.15f),
                                                Color.Transparent
                                            ),
                                            center = center,
                                            radius = 110f
                                        ),
                                        radius = 110f,
                                        center = center
                                    )
                                }
                            }

                            // 3. Draw 3 Route Polylines (Safest, Fastest, Shortest)
                            val safestPath = Path().apply {
                                moveTo(startPt.x, startPt.y)
                                quadraticTo(w * 0.22f, h * 0.20f, endPt.x, endPt.y)
                            }
                            val fastestPath = Path().apply {
                                moveTo(startPt.x, startPt.y)
                                quadraticTo(w * 0.50f, h * 0.50f, endPt.x, endPt.y)
                            }
                            val shortestPath = Path().apply {
                                moveTo(startPt.x, startPt.y)
                                quadraticTo(w * 0.72f, h * 0.78f, endPt.x, endPt.y)
                            }

                            drawPath(
                                path = shortestPath,
                                color = if (selectedRouteId == "SHORTEST") AlertRed else AlertRed.copy(alpha = 0.32f),
                                style = Stroke(
                                    width = if (selectedRouteId == "SHORTEST") 8f else 4f,
                                    cap = StrokeCap.Round,
                                    pathEffect = if (selectedRouteId == "SHORTEST") null else PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                                )
                            )
                            drawPath(
                                path = fastestPath,
                                color = if (selectedRouteId == "FASTEST") WarningOrange else WarningOrange.copy(alpha = 0.35f),
                                style = Stroke(
                                    width = if (selectedRouteId == "FASTEST") 8f else 4f,
                                    cap = StrokeCap.Round
                                )
                            )
                            drawPath(
                                path = safestPath,
                                color = if (selectedRouteId == "SAFEST") SafeGreen else SafeGreen.copy(alpha = 0.35f),
                                style = Stroke(
                                    width = if (selectedRouteId == "SAFEST") 9f else 4.5f,
                                    cap = StrokeCap.Round
                                )
                            )

                            // 4. Community Incident Pins & Safe Places Pins
                            if (showPins) {
                                // Safe Places (Police / Hospitals / Shelters) along upper safest corridor
                                val safeOffsets = listOf(
                                    Offset(w * 0.28f, h * 0.35f),
                                    Offset(w * 0.48f, h * 0.25f),
                                    Offset(w * 0.68f, h * 0.24f)
                                )
                                safeOffsets.forEach { pt ->
                                    drawCircle(color = SafeCyan.copy(alpha = 0.3f), radius = 16f, center = pt)
                                    drawCircle(color = SafeCyan, radius = 7f, center = pt)
                                }

                                // Incident Pins along middle/lower corridors
                                val pinCount = filteredIncidents.size.coerceAtMost(8)
                                for (idx in 0 until pinCount) {
                                    val px = w * (0.36f + (idx * 0.06f) % 0.42f)
                                    val py = h * (0.44f + ((idx * 13) % 30) / 100f)
                                    val cat = filteredIncidents[idx].category
                                    val pinColor = when {
                                        cat.contains("Harassment", true) -> AlertRed
                                        cat.contains("Suspicious", true) -> SafeRose
                                        else -> WarningOrange
                                    }
                                    drawCircle(color = pinColor.copy(alpha = 0.3f), radius = 15f, center = Offset(px, py))
                                    drawCircle(color = pinColor, radius = 6.5f, center = Offset(px, py))
                                }
                            }

                            // 5. Start (Live User GPS) & Destination Beacons
                            drawCircle(color = SafePurpleGlow.copy(alpha = 0.3f), radius = 24f, center = startPt)
                            drawCircle(color = SafePurple, radius = 10f, center = startPt)
                            drawCircle(color = Color.White, radius = 4f, center = startPt)

                            drawCircle(color = SafeGreen.copy(alpha = 0.3f), radius = 22f, center = endPt)
                            drawCircle(color = SafeGreen, radius = 9f, center = endPt)
                        }

                        // Map Legend Overlay (Top-Left & Bottom-Right)
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CinnamonCard.copy(alpha = 0.88f))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("🟣 You (Live GPS)   🟢 Destination", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("🔵 Police/Hospital  🟠 Community Pin", fontSize = 10.sp, color = TextSecondary)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CinnamonCard.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, CinnamonBorder),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (filteredIncidents.size >= 3) "Heatmap Active (${filteredIncidents.size} aggregated reports)"
                                else "Min 3 reports required for Heatmap",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafePurpleGlow,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Active Journey & Auto Check-In Banner (if active) or Setup Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isJourneyActive) SafeDarkPurple else CinnamonCard
                ),
                border = BorderStroke(1.dp, if (isJourneyActive) SafePurpleGlow else CinnamonBorder),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Auto Check-In",
                                tint = if (isJourneyActive) SafeGreen else SafePurpleGlow
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isJourneyActive) "ACTIVE JOURNEY • AUTO CHECK-IN RUNNING" else "Auto Check-In & Journey Mode",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isJourneyActive)
                                        "Sharing live trip with ${contactsList.count { it.isActive }} contacts • Confirm arrival before timer ends"
                                    else
                                        "Set destination & ETA. If you don't confirm arrival, trusted contacts are alerted.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isJourneyActive) {
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CinnamonObsidian)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Destination: $destinationQuery",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Route: $selectedRouteId • Supabase Journey Synced",
                                    fontSize = 11.sp,
                                    color = SafeGreen
                                )
                            }
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = SafePurpleGlow
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isJourneyActive = false
                                    coroutineScope.launch {
                                        try {
                                            activeJourneyId?.let { repo.completeJourney(it, "SAFE_ARRIVAL") }
                                        } catch (_: Exception) {}
                                        snackbarHostState.showSnackbar("✅ Checked In Safely! Journey marked complete in Supabase.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Safe", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("I Arrived Safely", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    remainingSeconds = 1 // Trigger expiry in 1 second for live mentor demo!
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AlertRed),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Text("Demo Expiry Alert", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Check-In ETA:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            listOf(1 to "1m (Demo)", 10 to "10m", 20 to "20m", 30 to "30m").forEach { (mins, label) ->
                                FilterChip(
                                    selected = selectedEtaMinutes == mins,
                                    onClick = { selectedEtaMinutes = mins },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SafeDarkPurple,
                                        selectedLabelColor = SafePurpleGlow
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Destination Search & 3 Routes with Safety Score Prototype (Screens 8 & 9)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Destination & 3-Route Safety Score",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SafePurple.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "PROTOTYPE SCORE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafePurpleGlow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = destinationQuery,
                        onValueChange = { destinationQuery = it },
                        label = { Text("Destination Address") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SafePurpleGlow) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Destination Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        destinationPresets.forEach { preset ->
                            Surface(
                                onClick = { destinationQuery = preset },
                                shape = RoundedCornerShape(8.dp),
                                color = if (destinationQuery == preset) SafeDarkPurple else CinnamonCardElevated,
                                border = BorderStroke(1.dp, if (destinationQuery == preset) SafePurpleGlow else CinnamonBorder)
                            ) {
                                Text(
                                    text = preset.substringBefore(","),
                                    fontSize = 11.sp,
                                    color = if (destinationQuery == preset) SafePurpleGlow else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // 3 Route Option Cards (Safest, Fastest, Shortest)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        routes.forEach { route ->
                            RoutePrototypeCard(
                                route = route,
                                selected = selectedRouteId == route.id,
                                onClick = { selectedRouteId = route.id }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val chosen = routes.find { it.id == selectedRouteId } ?: routes.first()
                            remainingSeconds = selectedEtaMinutes * 60
                            isJourneyActive = true
                            coroutineScope.launch {
                                try {
                                    val created = repo.startJourney(
                                        destLat = currentCoords.latitude + 0.015,
                                        destLng = currentCoords.longitude + 0.012,
                                        etaMinutes = selectedEtaMinutes
                                    )
                                    activeJourneyId = created?.id
                                } catch (_: Exception) {}
                                snackbarHostState.showSnackbar(
                                    "🧭 Started ${chosen.title} (${chosen.distanceKm} km) • Auto Check-In set for $selectedEtaMinutes min!"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START JOURNEY & AUTO CHECK-IN ($selectedEtaMinutes MIN)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 4. Community Reports Map Filter & Live Feed (Screen 13)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Community Map Markers (${filteredIncidents.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Filter by Report Type",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reportFilters.forEach { filter ->
                        FilterChip(
                            selected = selectedCategoryFilter == filter,
                            onClick = { selectedCategoryFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SafeDarkPurple,
                                selectedLabelColor = SafePurpleGlow
                            )
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredIncidents.forEach { incident ->
                    val distKm = currentCoords.distanceKmTo(incident.latitude, incident.longitude)
                    IncidentMarkerCard(
                        category = incident.category,
                        description = incident.description ?: "Community safety report",
                        distance = String.format("%.1f km away", distKm.coerceAtMost(9.9)),
                        isAnonymous = incident.isAnonymous,
                        isAlert = true
                    )
                }
            }
        }
    }
}

@Composable
private fun MapTogglePill(
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (active) activeColor.copy(alpha = 0.18f) else CinnamonCardElevated,
        border = BorderStroke(1.dp, if (active) activeColor else CinnamonBorder),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) activeColor else TextSecondary
            )
        }
    }
}

@Composable
private fun RoutePrototypeCard(
    route: RouteOptionPrototype,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scoreColor = when (route.riskLevel) {
        "LOW" -> SafeGreen
        "MEDIUM" -> WarningOrange
        else -> AlertRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SafeDarkPurple.copy(alpha = 0.55f) else CinnamonCardElevated
        ),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) scoreColor else CinnamonBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${route.distanceKm} km • ${route.durationMins} mins • ${route.badge}",
                        fontSize = 11.sp,
                        color = SafePurpleGlow
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = scoreColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${route.riskLevel} • ${route.safetyScore}/100",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scoreColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = route.explanation,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun IncidentMarkerCard(
    category: String,
    description: String,
    distance: String,
    isAnonymous: Boolean = true,
    isAlert: Boolean = true
) {
    val badgeColor = when {
        category.contains("Harassment", true) -> AlertRed
        category.contains("Suspicious", true) -> SafeRose
        category.contains("Lighting", true) -> WarningOrange
        category.contains("Blocked", true) -> SafeCyan
        else -> SafePurpleGlow
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CinnamonCard),
        border = BorderStroke(1.dp, CinnamonBorder),
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
                    .background(badgeColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAlert) Icons.Default.ReportProblem else Icons.Default.Shield,
                    contentDescription = category,
                    tint = badgeColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAnonymous) "• Anonymous" else "• Verified User",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = distance,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
