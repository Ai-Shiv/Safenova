package com.example.safenova.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ui.theme.SafePurple
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    var shakeToSosEnabled by remember { mutableStateOf(true) }
    var shakeSensitivity by remember { mutableFloatStateOf(0.7f) }
    var powerButtonTriggerEnabled by remember { mutableStateOf(true) }
    var stealthModeEnabled by remember { mutableStateOf(false) }
    var backgroundAudioEnabled by remember { mutableStateOf(true) }
    var locationSharingEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Quick Triggers
        item {
            SettingsCategoryHeader("Gesture & Physical Triggers")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SwitchSettingRow(
                        title = "Shake Phone for SOS",
                        subtitle = "Rapidly shake phone 3 times to send emergency alert",
                        icon = Icons.Default.PhonelinkRing,
                        checked = shakeToSosEnabled,
                        onCheckedChange = { shakeToSosEnabled = it }
                    )

                    if (shakeToSosEnabled) {
                        Column {
                            Text(
                                text = "Shake Sensitivity: ${(shakeSensitivity * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = shakeSensitivity,
                                onValueChange = { shakeSensitivity = it },
                                valueRange = 0.2f..1.0f
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    SwitchSettingRow(
                        title = "Triple Power Button SOS",
                        subtitle = "Press lock button 3 times consecutively",
                        icon = Icons.Default.Security,
                        checked = powerButtonTriggerEnabled,
                        onCheckedChange = { powerButtonTriggerEnabled = it }
                    )
                }
            }
        }

        // Section 2: Stealth & Disguise
        item {
            SettingsCategoryHeader("Stealth & Disguise Mode")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SwitchSettingRow(
                        title = "Calculator Disguise Icon",
                        subtitle = "Disguise SafeNova app icon as a standard calculator",
                        icon = Icons.Default.VisibilityOff,
                        checked = stealthModeEnabled,
                        onCheckedChange = {
                            stealthModeEnabled = it
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (it) "Stealth Calculator Disguise Enabled!" else "Normal App Icon Restored."
                                )
                            }
                        }
                    )
                }
            }
        }

        // Section 3: Privacy & Media
        item {
            SettingsCategoryHeader("Privacy & Auto Recording")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SwitchSettingRow(
                        title = "Continuous GPS Background Sharing",
                        subtitle = "Allow trusted contacts to query live location anytime",
                        icon = Icons.Default.Notifications,
                        checked = locationSharingEnabled,
                        onCheckedChange = { locationSharingEnabled = it }
                    )

                    SwitchSettingRow(
                        title = "Auto Audio Recording on SOS",
                        subtitle = "Automatically record 60s ambient audio during panic alert",
                        icon = Icons.Default.Mic,
                        checked = backgroundAudioEnabled,
                        onCheckedChange = { backgroundAudioEnabled = it }
                    )
                }
            }
        }

        // Section 4: Emergency Contacts Management
        item {
            SettingsCategoryHeader("Emergency Contacts Management")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3 Trusted Emergency Contacts saved.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Opening Phone Contact Picker...")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Contact")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Emergency Contact")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
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
                tint = SafePurple,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = SafePurple)
        )
    }
}
