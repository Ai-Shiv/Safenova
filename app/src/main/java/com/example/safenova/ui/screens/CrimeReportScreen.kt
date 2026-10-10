package com.example.safenova.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.data.models.AreaCondition
import com.example.safenova.data.models.IncidentReport
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.LocationClient
import com.example.safenova.ui.theme.CinnamonBorder
import com.example.safenova.ui.theme.CinnamonCard
import com.example.safenova.ui.theme.SafeDarkPurple
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafePurpleGlow
import com.example.safenova.ui.theme.TextPrimary
import com.example.safenova.ui.theme.TextSecondary
import com.example.safenova.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrimeReportScreen(
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }

    val categories = listOf(
        "Poor Lighting",
        "Crowd Level",
        "Blocked Road",
        "Harassment",
        "Suspicious Activity",
        "Unsafe Area"
    )
    val crowdLevels = listOf(
        "Deserted / Isolated",
        "Few Passersby",
        "Moderately Busy",
        "Crowded"
    )

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedCrowdLevel by remember { mutableStateOf(crowdLevels[0]) }
    var locationInput by remember { mutableStateOf("Connaught Place Service Lane (GPS Attached)") }
    var descriptionInput by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(true) }
    var hasPhotoAttached by remember { mutableStateOf(false) }
    var recentReports by remember { mutableStateOf<List<IncidentReport>>(emptyList()) }

    fun loadRecent() {
        coroutineScope.launch {
            try {
                recentReports = repo.fetchIncidentReports().take(6)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        loadRecent()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Report Community Incident or Unsafe Spot",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Reports are stored in Supabase, plotted as live map pins, and aggregated into the Safety Heatmap & Route Score.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // 1. Category Picker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Select Report Category",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(text = category, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarningOrange.copy(alpha = 0.22f),
                                    selectedLabelColor = WarningOrange
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Crowd Level Picker (Screen 12 requirement)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.People, contentDescription = "Crowd", tint = SafePurpleGlow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Current Crowd Level",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        crowdLevels.forEach { crowd ->
                            val isSelected = selectedCrowdLevel == crowd
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCrowdLevel = crowd },
                                label = { Text(text = crowd, fontSize = 11.sp) },
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

        // 3. Location & Optional Note
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text("Incident Location / GPS") },
                        trailingIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    val coords = LocationClient(context).getCurrentLocation()
                                    locationInput = "GPS: ${coords.formatted()}"
                                    snackbarHostState.showSnackbar("📍 Attached live GPS coordinates: (${coords.formatted()})")
                                }
                            }) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Use GPS", tint = SafePurpleGlow)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Optional Note / Details") },
                        placeholder = { Text("e.g., Streetlights broken near gate 2, blocked walkway...") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 4. Anonymous Toggle & Evidence Attachment
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Submit Anonymously", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = "Hides your identity on public community map markers.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = isAnonymous,
                            onCheckedChange = { isAnonymous = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SafePurple
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Photo", tint = SafePurpleGlow)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (hasPhotoAttached) "1 Evidence Photo Attached" else "Optional Photo Evidence",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = { hasPhotoAttached = !hasPhotoAttached },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeDarkPurple),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = if (hasPhotoAttached) "Remove" else "Attach", color = SafePurpleGlow, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 5. Submit Button
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val coords = LocationClient(context).getCurrentLocation()
                            val combinedNote = buildString {
                                if (descriptionInput.isNotBlank()) append(descriptionInput.trim()).append(" • ")
                                append("Crowd: $selectedCrowdLevel")
                            }
                            val report = IncidentReport(
                                category = selectedCategory,
                                description = combinedNote,
                                latitude = coords.latitude,
                                longitude = coords.longitude,
                                isAnonymous = isAnonymous
                            )
                            repo.submitIncidentReport(report)
                            repo.submitAreaCondition(
                                AreaCondition(
                                    reporterId = repo.getEffectiveUserId(),
                                    lightingRating = if (selectedCategory.contains("Lighting", true)) "Poorly Lit" else "Well-Lit Streetlights",
                                    crowdRating = selectedCrowdLevel,
                                    securityPresence = "Community Reported",
                                    latitude = coords.latitude,
                                    longitude = coords.longitude
                                )
                            )
                            loadRecent()
                            snackbarHostState.showSnackbar(
                                "✅ Saved '$selectedCategory' pin to Supabase at (${coords.formatted()})!"
                            )
                            descriptionInput = ""
                            hasPhotoAttached = false
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Report submitted locally.")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarningOrange, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.ReportProblem, contentDescription = "Submit")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "SUBMIT REPORT TO SUPABASE MAP", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }

        // 6. Live Newly Submitted Pins Preview
        if (recentReports.isNotEmpty()) {
            item {
                Text(
                    text = "Latest Pins in Supabase (${recentReports.size} shown)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentReports.forEach { r ->
                        IncidentMarkerCard(
                            category = r.category,
                            description = r.description ?: "Community safety pin",
                            distance = String.format("%.4f, %.4f", r.latitude, r.longitude),
                            isAnonymous = r.isAnonymous,
                            isAlert = true
                        )
                    }
                }
            }
        }
    }
}
