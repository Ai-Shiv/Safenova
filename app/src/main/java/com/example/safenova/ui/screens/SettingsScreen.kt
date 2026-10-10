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
    snackbarHostState: SnackbarHostState,
    onSignOut: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsStore = remember { com.example.safenova.data.SettingsDataStore(context) }
    val repo = remember { com.example.safenova.data.repo.SafeNovaRepository() }

    var shakeToSosEnabled by remember { mutableStateOf(true) }
    var shakeSensitivity by remember { mutableFloatStateOf(0.7f) }
    var powerButtonTriggerEnabled by remember { mutableStateOf(true) }
    var stealthModeEnabled by remember { mutableStateOf(false) }
    var backgroundAudioEnabled by remember { mutableStateOf(true) }
    var locationSharingEnabled by remember { mutableStateOf(true) }

    var contactsList by remember { mutableStateOf<List<com.example.safenova.data.models.TrustedContact>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }
    var newContactRelation by remember { mutableStateOf("Family") }

    fun refreshContacts() {
        coroutineScope.launch {
            try {
                contactsList = repo.fetchTrustedContacts()
            } catch (_: Exception) {}
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        refreshContacts()
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        settingsStore.shakeSosEnabled.collect { shakeToSosEnabled = it }
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        settingsStore.shakeSensitivity.collect { shakeSensitivity = it }
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        settingsStore.powerButtonEnabled.collect { powerButtonTriggerEnabled = it }
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        settingsStore.stealthModeEnabled.collect { stealthModeEnabled = it }
    }

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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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

                    Spacer(modifier = Modifier.height(4.dp))

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

        // Section 4: Emergency Profile & Medical Info
        item {
            SettingsCategoryHeader("Emergency Profile & Medical Info")
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Set up your medical conditions, blood group, and responder notes.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onNavigateToProfile,
                        colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Emergency Profile & Medical Info")
                    }
                }
            }
        }

        // Section 5: Emergency Contacts Management
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
                        text = "${contactsList.size} Trusted Emergency Contacts saved in Supabase.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    contactsList.forEach { contact ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = contact.contactName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "${contact.contactPhone} • ${contact.relation ?: "Family"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            contact.id?.let { contactId ->
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                repo.deleteTrustedContact(contactId)
                                                snackbarHostState.showSnackbar("Contact deleted.")
                                                refreshContacts()
                                            } catch (_: Exception) {}
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showAddDialog = true },
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

        // Section 5: Account & Sign Out
        item {
            SettingsCategoryHeader("Account & Session")
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "SIGN OUT OF SAFENOVA", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Emergency Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("Contact Name") },
                        singleLine = true
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = newContactRelation,
                        onValueChange = { newContactRelation = it },
                        label = { Text("Relation (e.g., Mom, Friend)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newContactName.isNotBlank() && newContactPhone.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    repo.addTrustedContact(newContactName, newContactPhone, newContactRelation)
                                    snackbarHostState.showSnackbar("✅ Contact '$newContactName' saved to Supabase!")
                                    newContactName = ""
                                    newContactPhone = ""
                                    showAddDialog = false
                                    refreshContacts()
                                } catch (_: Exception) {
                                    snackbarHostState.showSnackbar("Failed to add contact.")
                                }
                            }
                        }
                    }
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
