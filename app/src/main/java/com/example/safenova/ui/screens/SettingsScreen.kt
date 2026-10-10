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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.data.SettingsDataStore
import com.example.safenova.ui.theme.AlertRed
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
fun SettingsScreen(
    snackbarHostState: SnackbarHostState,
    onSignOut: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToPin: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val settingsStore = remember { SettingsDataStore(context) }

    var shakeToSosEnabled by remember { mutableStateOf(true) }
    var shakeSensitivity by remember { mutableFloatStateOf(0.7f) }
    var powerButtonTriggerEnabled by remember { mutableStateOf(true) }
    var stealthModeEnabled by remember { mutableStateOf(false) }
    var backgroundAudioEnabled by remember { mutableStateOf(true) }
    var authorizedOnlyLocation by remember { mutableStateOf(true) }
    var sessionExpiryOption by remember { mutableStateOf("30 mins") }

    val expiryOptions = listOf("15 mins", "30 mins", "1 hour", "End of Trip")

    LaunchedEffect(Unit) {
        settingsStore.shakeSosEnabled.collect { shakeToSosEnabled = it }
    }
    LaunchedEffect(Unit) {
        settingsStore.shakeSensitivity.collect { shakeSensitivity = it }
    }
    LaunchedEffect(Unit) {
        settingsStore.powerButtonEnabled.collect { powerButtonTriggerEnabled = it }
    }
    LaunchedEffect(Unit) {
        settingsStore.stealthModeEnabled.collect { stealthModeEnabled = it }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Profile & Emergency Medical Info Quick Actions
        item {
            SettingsCategoryHeader("Profile, Medical Info & Contacts")
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
                    Text(
                        text = "Manage your emergency medical profile, trusted contacts circle, and secret duress PIN.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Button(
                        onClick = onNavigateToProfile,
                        colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Emergency Medical Profile (Supabase)", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToContacts,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SafePurpleGlow),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.People, contentDescription = null, tint = SafePurpleGlow)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trusted Contacts", color = SafePurpleGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToPin,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SafePurpleGlow),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SafePurpleGlow)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Duress PIN", color = SafePurpleGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Privacy Controls (Tier 1 Core Requirement: Authorized-only + Auto-Expiring Sessions)
        item {
            SettingsCategoryHeader("Privacy Controls & Auto-Expiring Location")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SwitchSettingRow(
                        title = "Share Location Only with Authorized Contacts",
                        subtitle = "Strict encrypted access: only active contacts & verified responders can view GPS",
                        icon = Icons.Default.Notifications,
                        checked = authorizedOnlyLocation,
                        onCheckedChange = { authorizedOnlyLocation = it }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = "Expiry", tint = SafeGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Auto-Expire Location Sharing Session After:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            expiryOptions.forEach { opt ->
                                FilterChip(
                                    selected = sessionExpiryOption == opt,
                                    onClick = {
                                        sessionExpiryOption = opt
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("🔒 Location session auto-expiry set to $opt.")
                                        }
                                    },
                                    label = { Text(opt, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SafeDarkPurple,
                                        selectedLabelColor = SafePurpleGlow
                                    )
                                )
                            }
                        }
                    }

                    SwitchSettingRow(
                        title = "Auto Audio Evidence on SOS",
                        subtitle = "Record 60s ambient audio clip during active emergency",
                        icon = Icons.Default.Mic,
                        checked = backgroundAudioEnabled,
                        onCheckedChange = { backgroundAudioEnabled = it }
                    )
                }
            }
        }

        // 3. Gesture & Physical Triggers
        item {
            SettingsCategoryHeader("Gesture & Physical SOS Triggers")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SwitchSettingRow(
                        title = "Shake Phone for SOS",
                        subtitle = "Rapidly shake phone 3 times to dispatch emergency alert",
                        icon = Icons.Default.PhonelinkRing,
                        checked = shakeToSosEnabled,
                        onCheckedChange = {
                            shakeToSosEnabled = it
                            coroutineScope.launch { settingsStore.saveShakeSosEnabled(it) }
                        }
                    )

                    if (shakeToSosEnabled) {
                        Column {
                            Text(
                                text = "Shake Sensitivity: ${(shakeSensitivity * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Slider(
                                value = shakeSensitivity,
                                onValueChange = {
                                    shakeSensitivity = it
                                    coroutineScope.launch { settingsStore.saveShakeSensitivity(it) }
                                },
                                valueRange = 0.2f..1.0f
                            )
                        }
                    }

                    SwitchSettingRow(
                        title = "Triple Power Button SOS",
                        subtitle = "Press lock button 3 times consecutively",
                        icon = Icons.Default.Security,
                        checked = powerButtonTriggerEnabled,
                        onCheckedChange = {
                            powerButtonTriggerEnabled = it
                            coroutineScope.launch { settingsStore.savePowerButtonEnabled(it) }
                        }
                    )

                    SwitchSettingRow(
                        title = "Calculator Disguise Mode",
                        subtitle = "Stealth mode for high-risk situations",
                        icon = Icons.Default.VisibilityOff,
                        checked = stealthModeEnabled,
                        onCheckedChange = {
                            stealthModeEnabled = it
                            coroutineScope.launch {
                                settingsStore.saveStealthModeEnabled(it)
                                snackbarHostState.showSnackbar(
                                    if (it) "Stealth Disguise Mode Enabled!" else "Normal App Mode Restored."
                                )
                            }
                        }
                    )
                }
            }
        }

        // 4. Account & Sign Out
        item {
            SettingsCategoryHeader("Account & Session")
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AlertRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "SIGN OUT OF SAFENOVA", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = SafePurpleGlow,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun SwitchSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = SafePurpleGlow,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SafePurple
            )
        )
    }
}
