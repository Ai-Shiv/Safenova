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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafeRose
import kotlinx.coroutines.launch

@Composable
fun EscortRideScreen(
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    var pickupInput by remember { mutableStateOf("Current GPS Location (Main Library)") }
    var destinationInput by remember { mutableStateOf("University Dorms, Block B") }
    var selectedEscortType by remember { mutableStateOf("Verified Female Driver") }
    var isRideRequested by remember { mutableStateOf(false) }

    var shareLiveTrip by remember { mutableStateOf(true) }
    var recordAudioDuringRide by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Find an Escort Ride",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Request verified female drivers or campus night safety escorts to accompany you safely.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Pickup & Destination Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = pickupInput,
                        onValueChange = { pickupInput = it },
                        label = { Text("Pickup Location") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Pickup", tint = SafePurple) },
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

        // Escort Type Options Header
        item {
            Text(
                text = "Select Companion Service",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EscortOptionCard(
                    title = "Verified Female Driver",
                    subtitle = "100% Background Checked Women Drivers",
                    eta = "3 mins away",
                    icon = Icons.Default.DirectionsCar,
                    selected = selectedEscortType == "Verified Female Driver",
                    onClick = { selectedEscortType = "Verified Female Driver" }
                )

                EscortOptionCard(
                    title = "Campus Security Patrol Escort",
                    subtitle = "Uniformed campus safety officer escort",
                    eta = "5 mins away",
                    icon = Icons.Default.Security,
                    selected = selectedEscortType == "Campus Security Patrol Escort",
                    onClick = { selectedEscortType = "Campus Security Patrol Escort" }
                )

                EscortOptionCard(
                    title = "Walking Companion Buddy",
                    subtitle = "Group safety walk for short distances",
                    eta = "2 mins away",
                    icon = Icons.Default.DirectionsWalk,
                    selected = selectedEscortType == "Walking Companion Buddy",
                    onClick = { selectedEscortType = "Walking Companion Buddy" }
                )
            }
        }

        // Escort Driver Preview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SafePurple.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SafePurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Escort Driver", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Officer Amanda Hayes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified", tint = SafePurple, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "4.95 Rating • 420+ Safe Rides Completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(text = "Vehicle: White Toyota Prius • Plate: 7XYZ89", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SafePurple)
                    }
                }
            }
        }

        // In-Ride Safety Features
        item {
            Text(
                text = "In-Ride Safety Protections",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = SafePurple)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Share Live Trip Link", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Auto-send live map link to emergency contacts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = shareLiveTrip, onCheckedChange = { shareLiveTrip = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = "Audio", tint = SafeRose)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Record Ambient Ride Audio", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Encrypted in-cabin audio recording for safety", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = recordAudioDuringRide, onCheckedChange = { recordAudioDuringRide = it })
                    }
                }
            }
        }

        // Request Escort Ride Button
        item {
            Button(
                onClick = {
                    isRideRequested = !isRideRequested
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (isRideRequested)
                                "🚕 Safe Escort Requested! Officer Amanda Hayes is en route (ETA 3 mins)."
                            else
                                "Ride cancelled."
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRideRequested) SafeRose else SafePurple
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = if (isRideRequested) Icons.Default.Lock else Icons.Default.DirectionsCar,
                    contentDescription = "Request"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRideRequested) "CANCEL SAFE ESCORT REQUEST" else "REQUEST SAFE ESCORT RIDE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
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
            containerColor = if (selected) SafePurple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        )
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
                    .background(if (selected) SafePurple else SafePurple.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (selected) Color.White else SafePurple,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SafeGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = eta,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
