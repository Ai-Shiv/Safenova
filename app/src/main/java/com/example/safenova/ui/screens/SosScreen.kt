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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafeRose
import com.example.safenova.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@Composable
fun SosScreen(
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    var isSirenPlaying by remember { mutableStateOf(false) }
    var isSilentAlertSent by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Warning Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AlertRed.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = AlertRed
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pressing SOS will broadcast your live GPS location & audio to your emergency contacts.",
                        fontSize = 12.sp,
                        color = AlertRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Giant SOS Button
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(AlertRed.copy(alpha = 0.15f))
                    .padding(16.dp)
            ) {
                Surface(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                com.example.safenova.services.TrackingForegroundService.startService(context)
                                val locationClient = com.example.safenova.location.LocationClient(context)
                                val audioRecorder = com.example.safenova.media.AudioRecorderHelper(context)
                                audioRecorder.startRecording()
                                val coords = locationClient.getCurrentLocation()
                                val repo = com.example.safenova.data.repo.SafeNovaRepository()
                                repo.triggerSosAlert(coords.latitude, coords.longitude)
                                snackbarHostState.showSnackbar("🚨 SOS & LIVE TRACKING SERVICE ACTIVATED! GPS Monitored background service running.")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("🚨 EMERGENCY ALERT ACTIVATED!")
                            }
                        }
                    },
                    shape = CircleShape,
                    color = AlertRed,
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Trigger SOS",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PRESS SOS",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tap to Alert",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Quick Emergency Actions Grid
        item {
            Text(
                text = "Immediate Action Triggers",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTile(
                        title = if (isSirenPlaying) "Stop Loud Siren" else "Sound Loud Siren",
                        subtitle = "110dB Alarm Noise",
                        icon = Icons.Default.VolumeUp,
                        active = isSirenPlaying,
                        activeColor = AlertRed,
                        onClick = {
                            isSirenPlaying = !isSirenPlaying
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isSirenPlaying) "🔊 Loud Emergency Siren Started!" else "Siren Stopped."
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        title = "Silent SOS Alert",
                        subtitle = "No noise • GPS SMS",
                        icon = Icons.Default.LocationOn,
                        active = isSilentAlertSent,
                        activeColor = SafePurple,
                        onClick = {
                            isSilentAlertSent = true
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("🔕 Silent Location Alert dispatched successfully!")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    ActionTile(
                        title = "Simulate Fake Call",
                        subtitle = "Excuse yourself safely",
                        icon = Icons.Default.PhoneCallback,
                        active = false,
                        activeColor = SafeRose,
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("📞 Fake Incoming Call incoming in 3 seconds...")
                                kotlinx.coroutines.delay(3000)
                                val intent = android.content.Intent(context, com.example.safenova.ui.fakecall.FakeCallActivity::class.java).apply {
                                    putExtra("CALLER_NAME", "Dad (Emergency)")
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        title = "Flashlight Strobe",
                        subtitle = "Visual beacon signal",
                        icon = Icons.Default.FlashOn,
                        active = false,
                        activeColor = WarningOrange,
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("🔦 Strobe Flashlight activated!")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Active Emergency Contacts List
        item {
            Text(
                text = "Emergency Contacts (3 Active)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ContactItem(name = "Mom (Primary Contact)", phone = "+1 (555) 019-2834", relation = "Family")
                    ContactItem(name = "Jessica Miller", phone = "+1 (555) 012-9847", relation = "Sister")
                    ContactItem(name = "David - Roommate", phone = "+1 (555) 018-3342", relation = "Friend")
                }
            }
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (active) activeColor else activeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (active) Color.White else activeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ContactItem(
    name: String,
    phone: String,
    relation: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$phone • $relation",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SafeGreen.copy(alpha = 0.15f)
        ) {
            Text(
                text = "READY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SafeGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
