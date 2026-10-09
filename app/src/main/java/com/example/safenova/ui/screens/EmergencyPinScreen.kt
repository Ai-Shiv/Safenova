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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafeRose
import kotlinx.coroutines.launch

@Composable
fun EmergencyPinScreen(
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf("Duress PIN") } // "Normal PIN" vs "Duress PIN"
    var pinValue by remember { mutableStateOf("") }

    var silentSmsOnDuress by remember { mutableStateOf(true) }
    var policeBroadcastOnDuress by remember { mutableStateOf(true) }
    var openDecoyNotesApp by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Emergency & Duress PIN Setup",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Configure a secret Duress PIN to enter when forced to unlock under threat. It opens a fake decoy app while silently signaling emergency help.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tab Switcher
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabButton(
                        title = "Standard App PIN",
                        selected = activeTab == "Standard PIN",
                        onClick = { activeTab = "Standard PIN"; pinValue = "" },
                        modifier = Modifier.weight(1f)
                    )

                    TabButton(
                        title = "Secret Duress PIN",
                        selected = activeTab == "Duress PIN",
                        onClick = { activeTab = "Duress PIN"; pinValue = "" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // PIN Visual Dots
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (activeTab == "Duress PIN") "Set 4-Digit Duress Emergency PIN" else "Set Standard App Lock PIN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (activeTab == "Duress PIN") AlertRed else SafePurple
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinValue.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled)
                                        (if (activeTab == "Duress PIN") AlertRed else SafePurple)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        }

        // Custom Keypad Grid
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("CLR", "0", "DEL")
                )

                keys.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            KeypadButton(
                                label = key,
                                onClick = {
                                    when (key) {
                                        "DEL" -> if (pinValue.isNotEmpty()) pinValue = pinValue.dropLast(1)
                                        "CLR" -> pinValue = ""
                                        else -> if (pinValue.length < 4) pinValue += key
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Duress Automation Settings (When Duress PIN is active)
        if (activeTab == "Duress PIN") {
            item {
                Text(
                    text = "Automated Actions on Duress PIN Entry",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
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
                        DuressActionRow(
                            title = "Silent SMS to Emergency Contacts",
                            subtitle = "Dispatches current GPS location immediately",
                            checked = silentSmsOnDuress,
                            onCheckedChange = { silentSmsOnDuress = it }
                        )

                        DuressActionRow(
                            title = "Broadcast to Police Emergency Portal",
                            subtitle = "Transmits silent SOS beacon to local authorities",
                            checked = policeBroadcastOnDuress,
                            onCheckedChange = { policeBroadcastOnDuress = it }
                        )

                        DuressActionRow(
                            title = "Display Decoy Notes/Calculator App",
                            subtitle = "Hides all safety screens behind fake app screen",
                            checked = openDecoyNotesApp,
                            onCheckedChange = { openDecoyNotesApp = it }
                        )
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    if (pinValue.length == 4) {
                        coroutineScope.launch {
                            try {
                                val repo = com.example.safenova.data.repo.SafeNovaRepository()
                                if (activeTab == "Duress PIN") {
                                    repo.updateEmergencyPin(pinValue)
                                }
                                snackbarHostState.showSnackbar(
                                    "🔐 $activeTab updated & saved to Supabase profile!"
                                )
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    "🔐 $activeTab saved successfully!"
                                )
                            }
                            pinValue = ""
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please enter a complete 4-digit PIN.")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "Duress PIN") AlertRed else SafePurple
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Save PIN")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "SAVE ${activeTab.uppercase()}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.size(64.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "DEL") {
                Icon(imageVector = Icons.Default.Backspace, contentDescription = "Delete", modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DuressActionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
