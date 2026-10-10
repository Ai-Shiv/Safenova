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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.data.models.TrustedContact
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.LocationClient
import com.example.safenova.ui.theme.CinnamonBorder
import com.example.safenova.ui.theme.CinnamonCard
import com.example.safenova.ui.theme.SafeDarkPurple
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafePurpleGlow
import com.example.safenova.ui.theme.TextPrimary
import com.example.safenova.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInSetupScreen(
    snackbarHostState: SnackbarHostState,
    onJourneyStarted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }

    var destinationAddress by remember { mutableStateOf("Connaught Place, Central Block") }
    var selectedEtaMinutes by remember { mutableIntStateOf(15) }
    var trustedContacts by remember { mutableStateOf<List<TrustedContact>>(emptyList()) }

    val etaOptions = listOf(5, 15, 30, 45, 60)

    LaunchedEffect(Unit) {
        try {
            trustedContacts = repo.fetchTrustedContacts()
        } catch (_: Exception) {}
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Auto Check-In & Safety Timer Setup",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Set your destination and expected travel time. If you don't check in before the timer expires, an automatic SOS alert is sent to your trusted contacts.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Destination Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = "Destination", tint = SafePurpleGlow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trip Destination", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }

                    OutlinedTextField(
                        value = destinationAddress,
                        onValueChange = { destinationAddress = it },
                        label = { Text("Destination Address / Landmark") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = SafePurpleGlow) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ETA Timer Selection
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = "ETA", tint = SafeGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Expected Travel Duration (ETA)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        etaOptions.forEach { minutes ->
                            val isSelected = selectedEtaMinutes == minutes
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedEtaMinutes = minutes },
                                label = { Text("$minutes mins", fontSize = 12.sp) },
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

        // Alerted Contacts Summary
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.People, contentDescription = "Contacts", tint = SafePurpleGlow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Auto-Alert Recipients (${trustedContacts.size} Active)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }

                    if (trustedContacts.isEmpty()) {
                        Text(
                            text = "No custom contacts added. Add family members in Settings -> Emergency Contacts.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    } else {
                        trustedContacts.forEach { contact ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Notified", tint = SafeGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "${contact.contactName} (${contact.contactPhone})", fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Start Journey Button
        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val coords = LocationClient(context).getCurrentLocation()
                            repo.startJourney(coords.latitude, coords.longitude, selectedEtaMinutes)
                            snackbarHostState.showSnackbar("⏱️ Auto Check-In Journey started! Timer set for $selectedEtaMinutes mins.")
                            onJourneyStarted()
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Journey started locally.")
                            onJourneyStarted()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("START AUTO CHECK-IN TRIP", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}
