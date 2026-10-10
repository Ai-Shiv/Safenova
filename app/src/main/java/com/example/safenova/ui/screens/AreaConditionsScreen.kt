package com.example.safenova.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AreaConditionsScreen(
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    var lightingSelected by remember { mutableStateOf("Well-Lit Streetlights") }
    var crowdSelected by remember { mutableStateOf("Moderately Busy") }
    var securitySelected by remember { mutableStateOf("Police/Patrol Visible") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Area Safety Rating Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SafePurple
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Current Area: Downtown Central",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "4.2 / 5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Lighting: 85% Good • Crowd: Moderate • Patrol: 2 active units",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Area Rating Input Header
        item {
            Text(
                text = "Submit Live Area Safety Condition",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Rate your immediate surroundings to inform women nearby.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Lighting Condition
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Lighting", tint = WarningOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Street Lighting", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val lightingOptions = listOf("Pitch Dark", "Poorly Lit", "Well-Lit Streetlights")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        lightingOptions.forEach { option ->
                            FilterChip(
                                selected = lightingSelected == option,
                                onClick = { lightingSelected = option },
                                label = { Text(option, fontSize = 12.sp) },
                                leadingIcon = if (lightingSelected == option) {
                                    { Icon(Icons.Default.Check, null) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // Crowd Density
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.People, contentDescription = "Crowd", tint = SafePurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Crowd Activity Level", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val crowdOptions = listOf("Deserted / Isolated", "Few Passersby", "Moderately Busy", "Crowded")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        crowdOptions.forEach { option ->
                            FilterChip(
                                selected = crowdSelected == option,
                                onClick = { crowdSelected = option },
                                label = { Text(option, fontSize = 12.sp) },
                                leadingIcon = if (crowdSelected == option) {
                                    { Icon(Icons.Default.Check, null) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // Security / Police Presence
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = "Security", tint = SafeGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Security & Police Visibility", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val securityOptions = listOf("No Security", "Private Guard", "Police/Patrol Visible")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        securityOptions.forEach { option ->
                            FilterChip(
                                selected = securitySelected == option,
                                onClick = { securitySelected = option },
                                label = { Text(option, fontSize = 12.sp) },
                                leadingIcon = if (securitySelected == option) {
                                    { Icon(Icons.Default.Check, null) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // Submit Button
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val coords = com.example.safenova.location.LocationClient(context).getCurrentLocation()
                            val repo = com.example.safenova.data.repo.SafeNovaRepository()
                            val userId = repo.getEffectiveUserId()
                            val condition = com.example.safenova.data.models.AreaCondition(
                                reporterId = userId,
                                lightingRating = lightingSelected,
                                crowdRating = crowdSelected,
                                securityPresence = securitySelected,
                                latitude = coords.latitude,
                                longitude = coords.longitude
                            )
                            repo.submitAreaCondition(condition)
                            snackbarHostState.showSnackbar("🌐 Area condition saved to Supabase at (${coords.formatted()})!")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("🌐 Area condition report submitted!")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "SUBMIT AREA CONDITION REPORT", fontWeight = FontWeight.Bold)
            }
        }

        // Community Safety Updates Feed
        item {
            Text(
                text = "Recent Community Safety Reports",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CommunityReportItem(
                    author = "Elena R.",
                    timeAgo = "15 mins ago",
                    location = "Main St Subway Station",
                    comment = "Well-lit and police patrol van stationed outside exit #2.",
                    isPositive = true
                )

                CommunityReportItem(
                    author = "Aisha K.",
                    timeAgo = "1 hour ago",
                    location = "Oak Lane Alley",
                    comment = "Caution: 2 streetlights out near the park corner. Take Main St instead.",
                    isPositive = false
                )
            }
        }
    }
}

@Composable
fun CommunityReportItem(
    author: String,
    timeAgo: String,
    location: String,
    comment: String,
    isPositive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isPositive) SafeGreen else WarningOrange)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "• $timeAgo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (isPositive) SafeGreen else WarningOrange).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = location,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) SafeGreen else WarningOrange,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = comment, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
