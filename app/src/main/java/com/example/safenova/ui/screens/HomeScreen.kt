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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.navigation.Screen
import com.example.safenova.ui.components.FeatureCard
import com.example.safenova.ui.components.ProtectionStatusBadge
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafeRose
import com.example.safenova.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome & Protection Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SafeNova Security",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Welcome, Sarah",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        ProtectionStatusBadge(isProtected = true)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "GPS Tracking Active • 3 Emergency Contacts Ready",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // AI Risk Prediction Card
        item {
            val aiEngine = androidx.compose.runtime.remember { com.example.safenova.ai.AiSafetyEngine() }
            val risk = androidx.compose.runtime.remember {
                aiEngine.calculateRiskScore(
                    latitude = 37.7749,
                    longitude = -122.4194,
                    recentIncidents = emptyList<com.example.safenova.data.models.IncidentReport>(),
                    areaConditions = emptyList<com.example.safenova.data.models.AreaCondition>(),
                    nearbySafePlaces = emptyList<com.example.safenova.data.models.SafePlace>()
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SafePurple.copy(alpha = 0.12f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "AI Risk", tint = SafePurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "AI Route & Safety Score", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SafeGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${risk.riskLevel} (${risk.safetyScore}/100)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = risk.summary,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Emergency SOS Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.Sos) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AlertRed
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    AlertRed,
                                    Color(0xFFB71C1C)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "SOS",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "EMERGENCY SOS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Tap to view immediate SOS panic triggers & siren",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onNavigate(Screen.Sos) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = AlertRed
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Open SOS Emergency Panel",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Helplines Header
        item {
            Text(
                text = "Emergency Helplines",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Helplines Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HelplineChip(
                    title = "Police 112",
                    color = SafePurple,
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Dialing Emergency Police Helpline 112...")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                HelplineChip(
                    title = "Women 1091",
                    color = SafeRose,
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Dialing Women Helpline 1091...")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                HelplineChip(
                    title = "Medical 102",
                    color = SafeGreen,
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Dialing Medical Emergency 102...")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Safety Features Title
        item {
            Text(
                text = "Safety Services & Reports",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Links to all requested feature pages
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. SOS Page
                FeatureCard(
                    title = "SOS Emergency Trigger",
                    description = "Instant siren, silent panic alert & fake incoming call",
                    icon = Icons.Default.Warning,
                    accentColor = AlertRed,
                    badgeText = "URGENT",
                    onClick = { onNavigate(Screen.Sos) }
                )

                // 2. Settings Page
                FeatureCard(
                    title = "Safety Settings",
                    description = "Configure emergency contacts, shake triggers & stealth mode",
                    icon = Icons.Default.Settings,
                    accentColor = SafePurple,
                    onClick = { onNavigate(Screen.Settings) }
                )

                // 3. Crime Report Page
                FeatureCard(
                    title = "Crime Report",
                    description = "File anonymous reports on unsafe incidents or harassment",
                    icon = Icons.Default.Report,
                    accentColor = WarningOrange,
                    onClick = { onNavigate(Screen.CrimeReport) }
                )

                // 4. Area Conditions Report Page
                FeatureCard(
                    title = "Area Conditions Report",
                    description = "Check lighting, crowds & submit local safety ratings",
                    icon = Icons.Default.Explore,
                    accentColor = SafeGreen,
                    badgeText = "LIVE",
                    onClick = { onNavigate(Screen.AreaConditions) }
                )

                // 5. Find Shelters Page
                FeatureCard(
                    title = "Find Shelters",
                    description = "Locate verified nearby women's shelters & emergency refuges",
                    icon = Icons.Default.NightShelter,
                    accentColor = SafeRose,
                    onClick = { onNavigate(Screen.FindShelters) }
                )

                // 6. Find an Escort Ride Page
                FeatureCard(
                    title = "Find an Escort Ride",
                    description = "Request verified female drivers or campus security escort",
                    icon = Icons.Default.DirectionsCar,
                    accentColor = SafePurple,
                    onClick = { onNavigate(Screen.EscortRide) }
                )

                // 7. Setup Emergency Pin Page
                FeatureCard(
                    title = "Setup Emergency PIN",
                    description = "Create duress PIN for silent alarm & decoy app launch",
                    icon = Icons.Default.Lock,
                    accentColor = Color(0xFF00838F),
                    onClick = { onNavigate(Screen.EmergencyPin) }
                )
            }
        }
    }
}

@Composable
fun HelplineChip(
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
