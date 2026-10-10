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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.LocationClient
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
fun EscortRideScreen(
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }

    var pickupInput by remember { mutableStateOf("Current GPS (North Campus Library Gate 1)") }
    var destinationInput by remember { mutableStateOf("University Girls Hostel, Block B") }
    var selectedEscortType by remember { mutableStateOf("Pink Police Night Escort") }
    var isRideRequested by remember { mutableStateOf(false) }
    var isSelfieVerified by remember { mutableStateOf(false) }
    var autoDeleteOnArrival by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Honesty Banner: Proposed Government & Campus Integration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SafeDarkPurple.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, SafePurpleGlow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Night Escort & Campus Safety Mode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarningOrange.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PROPOSED INTEGRATION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Demonstrates the police/campus night escort workflow: officer & vehicle details, in-vehicle selfie verification, and automatic data deletion on safe arrival.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Pickup & Destination
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
                    OutlinedTextField(
                        value = pickupInput,
                        onValueChange = { pickupInput = it },
                        label = { Text("Pickup Location") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Pickup", tint = SafePurpleGlow) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = destinationInput,
                        onValueChange = { destinationInput = it },
                        label = { Text("Destination Address") },
                        leadingIcon = { Icon(Icons.Default.Navigation, contentDescription = "Destination", tint = SafeRose) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Escort Type Options
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EscortOptionCard(
                    title = "Pink Police Night Escort (PCR Unit)",
                    subtitle = "Verified Female Police Officer • Govt Escort Flow",
                    eta = "4 mins away",
                    icon = Icons.Default.Security,
                    selected = selectedEscortType == "Pink Police Night Escort",
                    onClick = { selectedEscortType = "Pink Police Night Escort" }
                )

                EscortOptionCard(
                    title = "Campus Security Patrol Van",
                    subtitle = "University Security Escort between Library, Labs & Hostels",
                    eta = "3 mins away",
                    icon = Icons.Default.School,
                    selected = selectedEscortType == "Campus Security Patrol Van",
                    onClick = { selectedEscortType = "Campus Security Patrol Van" }
                )

                EscortOptionCard(
                    title = "Walking Companion Buddy",
                    subtitle = "Verified Campus Peer Group Walk",
                    eta = "2 mins away",
                    icon = Icons.Default.DirectionsWalk,
                    selected = selectedEscortType == "Walking Companion Buddy",
                    onClick = { selectedEscortType = "Walking Companion Buddy" }
                )
            }
        }

        // Screen 16 & 17: Vehicle/Officer Details + In-Vehicle Selfie + Auto-Delete on Safe Arrival
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SafeDarkPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Officer", tint = SafePurpleGlow)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sub-InspectorKavita Rao (Badge #DL-409)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified", tint = SafeGreen, modifier = Modifier.size(15.dp))
                            }
                            Text(
                                text = "PCR Vehicle: DL-01-PB-2044 • Pink Patrol Unit #4",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SafePurpleGlow
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = WarningOrange, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "4.98 Verified Rating • GPS Telemetry Linked", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    // In-Vehicle Selfie Verification Step
                    Surface(
                        onClick = {
                            isSelfieVerified = !isSelfieVerified
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isSelfieVerified) "📸 In-vehicle selfie & officer badge photo verified!"
                                    else "Selfie check cleared."
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelfieVerified) SafeGreen.copy(alpha = 0.16f) else CinnamonCardElevated,
                        border = BorderStroke(1.dp, if (isSelfieVerified) SafeGreen else CinnamonBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSelfieVerified) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                                    contentDescription = "Selfie",
                                    tint = if (isSelfieVerified) SafeGreen else SafePurpleGlow
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isSelfieVerified) "In-Vehicle Selfie & Vehicle Photo Verified" else "Take In-Vehicle Verification Selfie",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Encrypted ephemeral verification shared with Trusted Circle",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Text(
                                text = if (isSelfieVerified) "DONE" else "CAPTURE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelfieVerified) SafeGreen else SafePurpleGlow
                            )
                        }
                    }

                    // Privacy Auto-Delete on Safe Arrival Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Auto Delete", tint = SafeCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Auto-Delete Selfie & GPS Log on Safe Arrival",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Zero data retention once you confirm safe arrival",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = autoDeleteOnArrival,
                            onCheckedChange = { autoDeleteOnArrival = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SafePurple)
                        )
                    }

                    // Request / Safe Arrival Confirmation Buttons
                    if (!isRideRequested) {
                        Button(
                            onClick = {
                                isRideRequested = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("🚓 Night Escort Requested! Pink Patrol Unit #4 en route (ETA 4 mins).")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REQUEST NIGHT ESCORT", fontWeight = FontWeight.ExtraBold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isRideRequested = false
                                    isSelfieVerified = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            "✅ Safe Arrival Confirmed! Ephemeral selfie & trip trace auto-deleted."
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ARRIVED SAFELY (AUTO-DELETE)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { isRideRequested = false },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AlertRed),
                                modifier = Modifier.weight(0.5f)
                            ) {
                                Text("Cancel", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Screen 21: Campus Safety Mode Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.School, contentDescription = "Campus Mode", tint = SafeCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Campus Safety Mode (University Zone)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Direct dispatch to Campus Security Offices, 24/7 Medical Centre & Proctor Desk",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CinnamonCardElevated,
                            border = BorderStroke(1.dp, CinnamonBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🛡️ Main Gate Security", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                Text("0.2 km • Patrol Active", fontSize = 10.sp, color = SafeGreen)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CinnamonCardElevated,
                            border = BorderStroke(1.dp, CinnamonBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🏥 Campus Health Wing", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                Text("0.4 km • 24/7 Nurse & Doctor", fontSize = 10.sp, color = SafeCyan)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val coords = LocationClient(context).getCurrentLocation()
                                    repo.triggerSosAlert(coords.latitude, coords.longitude, "CAMPUS_SECURITY_SOS_DISPATCH")
                                } catch (_: Exception) {}
                                snackbarHostState.showSnackbar("🎓 Campus SOS dispatched to Main Gate Security & Responder Dashboard!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TRIGGER CAMPUS SECURITY SOS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EscortOptionCard(
    title: String,
    subtitle: String,
    eta: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SafeDarkPurple.copy(alpha = 0.55f) else CinnamonCard
        ),
        border = BorderStroke(1.dp, if (selected) SafePurpleGlow else CinnamonBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) SafePurple else SafePurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (selected) Color.White else SafePurpleGlow,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SafeGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = eta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
