package com.example.safenova.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ai.AiSafetyEngine
import com.example.safenova.ai.SafetyRiskAssessment
import com.example.safenova.data.models.AreaCondition
import com.example.safenova.data.models.IncidentReport
import com.example.safenova.data.models.SafePlace
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.Coordinates
import com.example.safenova.location.LocationClient
import com.example.safenova.navigation.Screen
import com.example.safenova.ui.components.FeatureCard
import com.example.safenova.ui.components.ProtectionStatusBadge
import com.example.safenova.ui.fakecall.FakeCallActivity
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.CinnamonBorder
import com.example.safenova.ui.theme.CinnamonCard
import com.example.safenova.ui.theme.CinnamonCardElevated
import com.example.safenova.ui.theme.SafeCyan
import com.example.safenova.ui.theme.SafeDarkPurple
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafePurpleGlow
import com.example.safenova.ui.theme.SafeRose
import com.example.safenova.ui.theme.TextPrimary
import com.example.safenova.ui.theme.TextSecondary
import com.example.safenova.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }
    val aiEngine = remember { AiSafetyEngine() }

    var userName by remember { mutableStateOf("Ayush Tiwari") }
    var currentCoords by remember { mutableStateOf(LocationClient.DEFAULT_COORDINATES) }
    var activeContactsCount by remember { mutableIntStateOf(3) }
    var incidents by remember { mutableStateOf<List<IncidentReport>>(emptyList()) }
    var conditions by remember { mutableStateOf<List<AreaCondition>>(emptyList()) }
    var safePlaces by remember { mutableStateOf<List<SafePlace>>(emptyList()) }
    var showScoreFactors by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            currentCoords = LocationClient(context).getCurrentLocation()
        } catch (_: Exception) {}
        try {
            repo.fetchUserProfile()?.let { profile ->
                if (profile.fullName.isNotBlank()) userName = profile.fullName
            }
            val contacts = repo.fetchTrustedContacts()
            if (contacts.isNotEmpty()) {
                activeContactsCount = contacts.count { it.isActive }
            }
            incidents = repo.fetchIncidentReports()
            conditions = repo.fetchAreaConditions()
            safePlaces = repo.fetchSafePlaces()
        } catch (_: Exception) {}
    }

    val risk: SafetyRiskAssessment = remember(currentCoords, incidents, conditions, safePlaces) {
        aiEngine.calculateRiskScore(
            latitude = currentCoords.latitude,
            longitude = currentCoords.longitude,
            recentIncidents = incidents,
            areaConditions = conditions,
            nearbySafePlaces = safePlaces
        )
    }

    fun dialNumber(number: String, label: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            context.startActivity(intent)
        } catch (_: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Dialing $label ($number)...")
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
        // 1. Welcome, Live GPS Telemetry & Protection Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SAFENOVA COMMAND • DEMO-SAFE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafePurpleGlow
                            )
                            Text(
                                text = "Hello, $userName",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        ProtectionStatusBadge(isProtected = true)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live GPS Pill + Destination Search Trigger
                    Surface(
                        onClick = { onNavigate(Screen.Map) },
                        shape = RoundedCornerShape(12.dp),
                        color = CinnamonCardElevated,
                        border = BorderStroke(1.dp, CinnamonBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Live GPS",
                                    tint = SafeGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Live GPS: ${currentCoords.formatted()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$activeContactsCount Trusted Contacts • ${incidents.size} Map Reports • ${safePlaces.size} Safe Places",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Route",
                                tint = SafePurpleGlow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Search Bar to Route Navigation
                    Surface(
                        onClick = { onNavigate(Screen.Map) },
                        shape = RoundedCornerShape(12.dp),
                        color = SafeDarkPurple.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, SafePurple.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Route Search",
                                    tint = SafePurpleGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Search Destination • Compare 3 Safe Routes...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "GO →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SafePurpleGlow
                            )
                        }
                    }
                }
            }
        }

        // 2. Safety Score Prototype Card (Rule-Based Explainable Score)
        item {
            val scoreColor = when {
                risk.safetyScore >= 75 -> SafeGreen
                risk.safetyScore >= 52 -> WarningOrange
                else -> AlertRed
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showScoreFactors = !showScoreFactors },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Safety Score", tint = SafePurpleGlow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Safety Score Prototype",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Rule-Based: Time + Reports + Nearby Help (Tap for factors)",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = scoreColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${risk.riskLevel} • ${risk.safetyScore}/100",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = scoreColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = risk.summary,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )

                    if (showScoreFactors && risk.factors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CinnamonCardElevated)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            risk.factors.forEach { factor ->
                                Text(
                                    text = "• $factor",
                                    fontSize = 11.sp,
                                    color = SafePurpleGlow
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. One-Tap Emergency SOS Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.Sos) },
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, AlertRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF991B1B),
                                    Color(0xFF450A0A)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(AlertRed.copy(alpha = 0.3f))
                                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "SOS",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "ONE-TAP EMERGENCY SOS",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Alerts Trusted Contacts + Live Web Responder Dashboard",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onNavigate(Screen.Sos) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AlertRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("OPEN SOS COMMAND", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(context, FakeCallActivity::class.java).apply {
                                        putExtra("CALLER_NAME", "Dad (Emergency)")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CinnamonCardElevated,
                                    contentColor = SafePurpleGlow
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.85f)
                            ) {
                                Icon(imageVector = Icons.Default.PhoneCallback, contentDescription = "Fake Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("FAKE CALL", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 4. Tap-to-Call Emergency Helplines
        item {
            Text(
                text = "Tap-to-Call Emergency Helplines",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HelplineChip(
                    title = "Police 112",
                    color = SafeCyan,
                    onClick = { dialNumber("112", "Police Control 112") },
                    modifier = Modifier.weight(1f)
                )
                HelplineChip(
                    title = "Women 1091",
                    color = SafeRose,
                    onClick = { dialNumber("1091", "Women Helpline 1091") },
                    modifier = Modifier.weight(1f)
                )
                HelplineChip(
                    title = "Medical 102",
                    color = SafeGreen,
                    onClick = { dialNumber("102", "Ambulance 102") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Core & Differentiator Feature Modules
        item {
            Text(
                text = "SAFENOVA Modules (MVP & Differentiators)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCard(
                    title = "Live Map, 3 Routes & Auto Check-In",
                    description = "Compare Safest/Fastest/Shortest routes, Safety Score Prototype, Heatmap & Check-In timer",
                    icon = Icons.Default.Map,
                    accentColor = SafePurpleGlow,
                    badgeText = "CORE DEMO",
                    onClick = { onNavigate(Screen.Map) }
                )

                FeatureCard(
                    title = "Trusted Contacts (Full CRUD)",
                    description = "Add, edit, remove & send test SMS GPS alerts to your emergency circle",
                    icon = Icons.Default.People,
                    accentColor = SafeGreen,
                    badgeText = "$activeContactsCount ACTIVE",
                    onClick = { onNavigate(Screen.TrustedContacts) }
                )

                FeatureCard(
                    title = "Report Unsafe Spot (Community & Anonymous)",
                    description = "Submit poor lighting, crowd level, blocked road or harassment pins to Supabase",
                    icon = Icons.Default.Report,
                    accentColor = WarningOrange,
                    badgeText = "${incidents.size} PINS",
                    onClick = { onNavigate(Screen.CrimeReport) }
                )

                FeatureCard(
                    title = "Nearby Help & Safe Night Shelters",
                    description = "Police, hospitals, 24/7 pharmacies & government shelters with tap-to-call & ratings",
                    icon = Icons.Default.LocalHospital,
                    accentColor = SafeRose,
                    badgeText = "${safePlaces.size} VERIFIED",
                    onClick = { onNavigate(Screen.FindShelters) }
                )

                FeatureCard(
                    title = "Area Conditions & Crowd Telemetry",
                    description = "Rate street lighting, crowd density & patrol presence in real time",
                    icon = Icons.Default.Explore,
                    accentColor = SafeCyan,
                    onClick = { onNavigate(Screen.AreaConditions) }
                )

                FeatureCard(
                    title = "Night Escort Request & Campus Mode",
                    description = "Police/Female driver escort flow with selfie check, auto-delete on arrival & Campus SOS",
                    icon = Icons.Default.DirectionsCar,
                    accentColor = SafePurple,
                    badgeText = "TIER 3",
                    onClick = { onNavigate(Screen.EscortRide) }
                )

                FeatureCard(
                    title = "Emergency Medical Profile",
                    description = "Blood group, allergies, medical conditions & responder instructions stored in Supabase",
                    icon = Icons.Default.Person,
                    accentColor = SafeRose,
                    onClick = { onNavigate(Screen.EmergencyProfile) }
                )

                FeatureCard(
                    title = "Duress PIN & Privacy Controls",
                    description = "Secret duress PIN, auto-expiring location sessions & shake-to-SOS settings",
                    icon = Icons.Default.Lock,
                    accentColor = WarningOrange,
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
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
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
                modifier = Modifier.size(15.dp)
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
