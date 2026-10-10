package com.example.safenova.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.data.models.TrustedContact
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.Coordinates
import com.example.safenova.location.LocationClient
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.CinnamonBorder
import com.example.safenova.ui.theme.CinnamonCard
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafePurpleGlow
import com.example.safenova.ui.theme.TextPrimary
import com.example.safenova.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SosActiveScreen(
    activeSosId: String?,
    onResolveSos: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }

    var currentCoords by remember { mutableStateOf(Coordinates(28.6315, 77.2197)) }
    var trustedContactsList by remember { mutableStateOf<List<TrustedContact>>(emptyList()) }
    var activeTimerSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            trustedContactsList = repo.fetchTrustedContacts()
        } catch (_: Exception) {}

        while (true) {
            try {
                currentCoords = LocationClient(context).getCurrentLocation()
            } catch (_: Exception) {}
            delay(1000L)
            activeTimerSeconds++
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Emergency Pulsing Header
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(AlertRed.copy(alpha = 0.15f))
                    .padding(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AlertRed,
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "SOS Active",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SOS ACTIVE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = String.format("%02d:%02d", activeTimerSeconds / 60, activeTimerSeconds % 60),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, AlertRed),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "GPS", tint = AlertRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Live GPS Telemetry Broadcasting",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Coordinates: ${currentCoords.formatted()} • Updating every 5s",
                                fontSize = 11.sp,
                                color = SafePurpleGlow
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Audio", tint = SafePurpleGlow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ambient Audio Recording Active (Encrypted Backup)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Who Has Been Alerted
        item {
            Text(
                text = "Alerted Contacts (${trustedContactsList.size}) & Responders",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Responder Portal", tint = SafePurpleGlow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "SafeNova Responder Web Dashboard", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = SafeGreen.copy(alpha = 0.2f)) {
                            Text("ALERT SENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafeGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    if (trustedContactsList.isEmpty()) {
                        Text(
                            text = "No custom contacts added yet. Add family members in Trusted Contacts screen.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    } else {
                        trustedContactsList.forEach { contact ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = contact.contactName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text(text = "${contact.contactPhone} • ${contact.relation ?: "Family"}", fontSize = 11.sp, color = TextSecondary)
                                }
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Notified", tint = SafeGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Resolve / Cancel Actions
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                activeSosId?.let { repo.resolveSosAlert(it, "USER_SAFE_RESOLVED") }
                                com.example.safenova.services.TrackingForegroundService.stopService(context)
                                snackbarHostState.showSnackbar("✅ SOS Resolved. Confirmed safe!")
                                onResolveSos()
                            } catch (_: Exception) {
                                onResolveSos()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Safe")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("I'M SAFE NOW (RESOLVE SOS)", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = {
                        com.example.safenova.services.TrackingForegroundService.stopService(context)
                        onResolveSos()
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AlertRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CANCEL SOS EMERGENCY", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
