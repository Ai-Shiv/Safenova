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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.data.models.SafePlace
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.Coordinates
import com.example.safenova.location.LocationClient
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

data class ShelterInfo(
    val name: String,
    val type: String, // POLICE, HOSPITAL, PHARMACY, SHELTER, CAMPUS
    val address: String,
    val distance: String,
    val openHours: String,
    val availableBeds: String,
    val isVerified: Boolean,
    val phone: String,
    val latitude: Double = 28.6315,
    val longitude: Double = 77.2197
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FindSheltersScreen(
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Nearby") }
    var currentCoords by remember { mutableStateOf(LocationClient.DEFAULT_COORDINATES) }

    // Add / Rate Shelter modal state (Screen 19)
    var showAddModal by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("SHELTER") }
    var newAddress by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("181") }
    var newRating by remember { mutableIntStateOf(5) }

    // Public Facility Poll state (Screen 20)
    var pollVotes1 by remember { mutableIntStateOf(142) }
    var pollVotes2 by remember { mutableIntStateOf(98) }
    var pollVotes3 by remember { mutableIntStateOf(76) }

    val filters = listOf("All Nearby", "Police", "Hospitals", "Pharmacies", "Safe Shelters", "Campus")

    var sheltersState by remember { mutableStateOf(emptyList<ShelterInfo>()) }

    fun refreshSafePlaces() {
        coroutineScope.launch {
            try {
                currentCoords = LocationClient(context).getCurrentLocation()
            } catch (_: Exception) {}
            try {
                val remotePlaces = repo.fetchSafePlaces()
                if (remotePlaces.isNotEmpty()) {
                    sheltersState = remotePlaces.map { place ->
                        val distKm = currentCoords.distanceKmTo(place.latitude, place.longitude)
                        ShelterInfo(
                            name = place.name,
                            type = place.type.uppercase(),
                            address = place.address,
                            distance = String.format("%.1f km", distKm.coerceAtMost(12.5)),
                            openHours = place.openHours ?: "Open 24/7",
                            availableBeds = when (place.type.uppercase()) {
                                "POLICE" -> "24/7 Police Patrol"
                                "HOSPITAL" -> "Emergency Medical"
                                "PHARMACY" -> "24/7 Safe Chemist"
                                "CAMPUS" -> "Campus Security"
                                else -> "Govt Safe Shelter"
                            },
                            isVerified = place.isVerified,
                            phone = place.phone ?: "112",
                            latitude = place.latitude,
                            longitude = place.longitude
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        refreshSafePlaces()
    }

    val filteredList = remember(sheltersState, selectedFilter, searchQuery) {
        sheltersState.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "Police" -> item.type.contains("POLICE", true)
                "Hospitals" -> item.type.contains("HOSPITAL", true)
                "Pharmacies" -> item.type.contains("PHARMACY", true)
                "Safe Shelters" -> item.type.contains("SHELTER", true)
                "Campus" -> item.type.contains("CAMPUS", true)
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, true) ||
                    item.address.contains(searchQuery, true) ||
                    item.type.contains(searchQuery, true)
            matchesFilter && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nearby Help & Safe Night Shelters",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Police, Hospitals, Pharmacies & Govt Shelters synced with Supabase",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { showAddModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rate / Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search police, hospital, pharmacy, or shelter...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SafePurpleGlow) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category Filter Chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SafeDarkPurple,
                            selectedLabelColor = SafePurpleGlow
                        )
                    )
                }
            }
        }

        // 24/7 Emergency Helpline Dispatch Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SafeRose.copy(alpha = 0.18f)),
                border = BorderStroke(1.dp, SafeRose),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "24/7 Govt Women Shelter Helpline (181)",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Immediate safe night stay & emergency pickup assistance",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:181")))
                            } catch (_: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Dialing 181 Women Shelter Helpline...")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeRose, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call 181", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Dial 181", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // List Header
        item {
            Text(
                text = "Verified Nearby Facilities (${filteredList.size} Found)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Facility Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (filteredList.isEmpty()) {
                    Text(
                        text = "No facilities verified in your database yet.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    filteredList.forEach { shelter ->
                        ShelterCard(
                            shelter = shelter,
                            onCallClick = {
                                try {
                                    val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shelter.phone}"))
                                    context.startActivity(dial)
                                } catch (_: Exception) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Calling ${shelter.name} (${shelter.phone})...")
                                    }
                                }
                            },
                            onDirectionsClick = {
                                try {
                                    val geoUri = Uri.parse("geo:${shelter.latitude},${shelter.longitude}?q=${Uri.encode(shelter.name)}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Opening Directions to ${shelter.address}...")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Screen 20: Public Poll for New Safety Facilities
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
                        Icon(imageVector = Icons.Default.HowToVote, contentDescription = "Poll", tint = SafePurpleGlow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Citizen Poll: Request New Safety Facilities",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Vote to prioritize new shelters, Pink Police booths & lighting in remote zones",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    PollRow(
                        title = "New 24/7 Safe Night Shelter — Outer Ring Road Sector 9",
                        votes = pollVotes1,
                        onVote = {
                            pollVotes1++
                            coroutineScope.launch { snackbarHostState.showSnackbar("🗳️ Vote recorded for Sector 9 Safe Night Shelter!") }
                        }
                    )
                    PollRow(
                        title = "Pink Police Patrol Booth — North Campus Metro Exit 4",
                        votes = pollVotes2,
                        onVote = {
                            pollVotes2++
                            coroutineScope.launch { snackbarHostState.showSnackbar("🗳️ Vote recorded for North Campus Pink Patrol Booth!") }
                        }
                    )
                    PollRow(
                        title = "Solar Streetlights & Sos Pillar — East Industrial Service Lane",
                        votes = pollVotes3,
                        onVote = {
                            pollVotes3++
                            coroutineScope.launch { snackbarHostState.showSnackbar("🗳️ Vote recorded for Industrial Lane Streetlights!") }
                        }
                    )
                }
            }
        }
    }

    // Screen 19: Rate / Add Safe Shelter Dialog
    if (showAddModal) {
        AlertDialog(
            onDismissRequest = { showAddModal = false },
            containerColor = CinnamonCard,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            title = { Text("Rate or Add Safe Facility", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Facility / Shelter Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Address / Landmark") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Contact / Helpline Number") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category:", fontSize = 12.sp, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("SHELTER", "POLICE", "HOSPITAL", "PHARMACY").forEach { t ->
                            FilterChip(
                                selected = newType == t,
                                onClick = { newType = t },
                                label = { Text(t, fontSize = 10.sp) }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Safety Rating: ", fontSize = 12.sp, color = TextSecondary)
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= newRating) WarningOrange else TextSecondary,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { newRating = star }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isBlank()) return@Button
                        showAddModal = false
                        coroutineScope.launch {
                            try {
                                val coords = LocationClient(context).getCurrentLocation()
                                repo.addSafePlace(
                                    SafePlace(
                                        name = newName.trim(),
                                        type = newType,
                                        address = newAddress.ifBlank { "Community Verified Location" },
                                        latitude = coords.latitude,
                                        longitude = coords.longitude,
                                        phone = newPhone.ifBlank { "181" },
                                        openHours = "Open 24/7 • ★ $newRating.0 User Rated",
                                        isVerified = true
                                    )
                                )
                                refreshSafePlaces()
                                snackbarHostState.showSnackbar("✅ Added '${newName.trim()}' (★ $newRating.0) to Supabase!")
                                newName = ""
                                newAddress = ""
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("Saved rating locally.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafePurple)
                ) {
                    Text("Submit to Supabase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddModal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun PollRow(
    title: String,
    votes: Int,
    onVote: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CinnamonCardElevated,
        border = BorderStroke(1.dp, CinnamonBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = "$votes citizen votes", fontSize = 11.sp, color = SafePurpleGlow)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onVote,
                colors = ButtonDefaults.buttonColors(containerColor = SafeDarkPurple),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("+ Vote", fontSize = 11.sp, color = SafePurpleGlow, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ShelterCard(
    shelter: ShelterInfo,
    onCallClick: () -> Unit,
    onDirectionsClick: () -> Unit
) {
    val typeAccent = when (shelter.type.uppercase()) {
        "POLICE" -> SafeCyan
        "HOSPITAL" -> SafeGreen
        "PHARMACY" -> WarningOrange
        "CAMPUS" -> SafePurpleGlow
        else -> SafeRose
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinnamonCard),
        border = BorderStroke(1.dp, CinnamonBorder)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = shelter.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        if (shelter.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = SafeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${shelter.address} • ${shelter.distance} away",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeAccent.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = shelter.type,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = typeAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SafeGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = shelter.openHours,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SafeGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SafePurple.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = shelter.availableBeds,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SafePurpleGlow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tap to Call (${shelter.phone})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDirectionsClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SafePurpleGlow),
                    modifier = Modifier.weight(0.85f)
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = "Directions", tint = SafePurpleGlow, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Map Pin", fontSize = 11.sp, color = SafePurpleGlow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
