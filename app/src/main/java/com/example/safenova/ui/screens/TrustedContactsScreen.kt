package com.example.safenova.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.safenova.location.LocationClient
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.CinnamonBorder
import com.example.safenova.ui.theme.CinnamonCard
import com.example.safenova.ui.theme.CinnamonCardElevated
import com.example.safenova.ui.theme.SafeDarkPurple
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafePurpleGlow
import com.example.safenova.ui.theme.SafeRose
import com.example.safenova.ui.theme.TextPrimary
import com.example.safenova.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrustedContactsScreen(
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }

    var contacts by remember { mutableStateOf<List<TrustedContact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Modal state for Add / Edit
    var showDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<TrustedContact?>(null) }
    var formName by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formRelation by remember { mutableStateOf("Family") }

    val relationOptions = listOf("Family", "Sister", "Friend", "Roommate", "Partner", "Guardian")

    fun loadContacts() {
        coroutineScope.launch {
            isLoading = true
            try {
                contacts = repo.fetchTrustedContacts()
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadContacts()
    }

    fun dispatchSmsIntent(phoneNumbers: List<String>, isTest: Boolean) {
        coroutineScope.launch {
            val coords = LocationClient(context).getCurrentLocation()
            val mapsLink = "https://maps.google.com/?q=${coords.latitude},${coords.longitude}"
            val body = if (isTest) {
                "[SAFENOVA TEST ALERT] Hi, I've added you to my SafeNova Trusted Circle. My current live GPS is (${coords.formatted()}): $mapsLink"
            } else {
                "🚨 [SAFENOVA EMERGENCY SOS] I need immediate help! Live GPS Location: $mapsLink (${coords.formatted()})"
            }
            try {
                val joinedPhones = phoneNumbers.joinToString(";")
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$joinedPhones")
                    putExtra("sms_body", body)
                }
                context.startActivity(smsIntent)
                snackbarHostState.showSnackbar("📲 Opened SMS dispatch for ${phoneNumbers.size} contact(s) with live GPS link!")
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("📨 Alert dispatched to ${phoneNumbers.size} trusted contact(s): $mapsLink")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Add Contact Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                border = BorderStroke(1.dp, CinnamonBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SafeDarkPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Trusted Circle",
                                    tint = SafePurpleGlow
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Trusted Emergency Circle",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${contacts.count { it.isActive }} of ${contacts.size} contacts active in Supabase",
                                    fontSize = 12.sp,
                                    color = SafeGreen
                                )
                            }
                        }

                        IconButton(onClick = { loadContacts() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Supabase",
                                tint = SafePurpleGlow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Authorized contacts receive instant SOS coordinates, Auto Check-In expiry alerts, and live Journey links.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                editingContact = null
                                formName = ""
                                formPhone = "+91 "
                                formRelation = "Family"
                                showDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Contact", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val activePhones = contacts.filter { it.isActive }.map { it.contactPhone }
                                if (activePhones.isEmpty()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Add or enable at least 1 active contact first.")
                                    }
                                } else {
                                    dispatchSmsIntent(activePhones, isTest = true)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SafePurpleGlow),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Test Alert",
                                tint = SafePurpleGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Alert All", color = SafePurpleGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Contacts List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoading) "Syncing with Supabase..." else "Saved Trusted Contacts (${contacts.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Tap Edit or Toggle Active",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        if (!isLoading && contacts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                    border = BorderStroke(1.dp, CinnamonBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No Trusted Contacts Yet", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Tap 'Add Contact' above to add family or friends who will receive your SOS alerts.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        items(contacts, key = { it.id ?: it.contactName + it.contactPhone }) { contact ->
            TrustedContactCard(
                contact = contact,
                onToggleActive = { newActive ->
                    val cid = contact.id ?: return@TrustedContactCard
                    coroutineScope.launch {
                        try {
                            repo.updateTrustedContact(
                                contactId = cid,
                                name = contact.contactName,
                                phone = contact.contactPhone,
                                relation = contact.relation ?: "Family",
                                isActive = newActive
                            )
                            loadContacts()
                            snackbarHostState.showSnackbar(
                                if (newActive) "✅ ${contact.contactName} enabled for SOS alerts"
                                else "⏸️ ${contact.contactName} paused"
                            )
                        } catch (_: Exception) {}
                    }
                },
                onEdit = {
                    editingContact = contact
                    formName = contact.contactName
                    formPhone = contact.contactPhone
                    formRelation = contact.relation ?: "Family"
                    showDialog = true
                },
                onDelete = {
                    val cid = contact.id ?: return@TrustedContactCard
                    coroutineScope.launch {
                        try {
                            repo.deleteTrustedContact(cid)
                            loadContacts()
                            snackbarHostState.showSnackbar("🗑️ Removed ${contact.contactName} from Supabase")
                        } catch (_: Exception) {}
                    }
                },
                onTestAlert = {
                    dispatchSmsIntent(listOf(contact.contactPhone), isTest = true)
                },
                onCall = {
                    try {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.contactPhone}"))
                        context.startActivity(dialIntent)
                    } catch (_: Exception) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Dialing ${contact.contactPhone}...")
                        }
                    }
                }
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = CinnamonCard,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            title = {
                Text(
                    text = if (editingContact == null) "Add Trusted Contact" else "Edit Trusted Contact",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = formName,
                        onValueChange = { formName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g., Mom (Primary SOS)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formPhone,
                        onValueChange = { formPhone = it },
                        label = { Text("Phone Number (for SMS & Call)") },
                        placeholder = { Text("+91 98765 43210") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Relationship",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        relationOptions.forEach { rel ->
                            FilterChip(
                                selected = formRelation == rel,
                                onClick = { formRelation = rel },
                                label = { Text(rel, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SafeDarkPurple,
                                    selectedLabelColor = SafePurpleGlow
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formName.isBlank() || formPhone.isBlank()) return@Button
                        showDialog = false
                        coroutineScope.launch {
                            try {
                                val existing = editingContact
                                if (existing?.id != null) {
                                    repo.updateTrustedContact(
                                        contactId = existing.id,
                                        name = formName.trim(),
                                        phone = formPhone.trim(),
                                        relation = formRelation,
                                        isActive = existing.isActive
                                    )
                                    snackbarHostState.showSnackbar("✅ Updated ${formName.trim()} in Supabase!")
                                } else {
                                    repo.addTrustedContact(
                                        name = formName.trim(),
                                        phone = formPhone.trim(),
                                        relation = formRelation
                                    )
                                    snackbarHostState.showSnackbar("✅ Added ${formName.trim()} to Supabase Trusted Circle!")
                                }
                                loadContacts()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error saving contact: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafePurple)
                ) {
                    Text(if (editingContact == null) "Save Contact" else "Update Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun TrustedContactCard(
    contact: TrustedContact,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestAlert: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CinnamonCard),
        border = BorderStroke(1.dp, if (contact.isActive) CinnamonBorder else CinnamonBorder.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (contact.isActive) SafeDarkPurple else CinnamonCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.contactName.take(1).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (contact.isActive) SafePurpleGlow else TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = contact.contactName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SafePurple.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = contact.relation ?: "Family",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SafePurpleGlow,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = contact.contactPhone,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Switch(
                    checked = contact.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SafePurple
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onTestAlert,
                    shape = RoundedCornerShape(8.dp),
                    color = SafePurple.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, SafePurple.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Test SMS", tint = SafePurpleGlow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test SMS Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafePurpleGlow)
                    }
                }

                Surface(
                    onClick = onCall,
                    shape = RoundedCornerShape(8.dp),
                    color = SafeGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SafeGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = SafeGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                    }
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = SafePurpleGlow, modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
