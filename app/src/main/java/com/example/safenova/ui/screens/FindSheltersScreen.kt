package com.example.safenova.ui.screens

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightShelter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafeRose
import kotlinx.coroutines.launch

data class ShelterInfo(
    val name: String,
    val address: String,
    val distance: String,
    val openHours: String,
    val availableBeds: String,
    val isVerified: Boolean,
    val phone: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FindSheltersScreen(
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Shelters") }

    val filters = listOf("All Shelters", "24/7 Open", "Verified Only", "Emergency Beds")

    var sheltersState by remember {
        mutableStateOf(
            listOf(
                ShelterInfo(
                    name = "Grace Women's Refuge Center",
                    address = "142 Maple St, Downtown",
                    distance = "0.8 km",
                    openHours = "Open 24/7",
                    availableBeds = "12 Emergency Beds Free",
                    isVerified = true,
                    phone = "+1 (555) 321-9876"
                ),
                ShelterInfo(
                    name = "St. Mary Family & Women Sanctuary",
                    address = "89 Oak Avenue, Westside",
                    distance = "1.6 km",
                    openHours = "Open 24/7",
                    availableBeds = "8 Beds Free",
                    isVerified = true,
                    phone = "+1 (555) 888-2345"
                ),
                ShelterInfo(
                    name = "Hope Haven Emergency Shelter",
                    address = "305 Pine Boulevard, North District",
                    distance = "2.9 km",
                    openHours = "6:00 PM - 8:00 AM",
                    availableBeds = "15 Beds Free",
                    isVerified = true,
                    phone = "+1 (555) 777-5432"
                )
            )
        )
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            val repo = com.example.safenova.data.repo.SafeNovaRepository()
            val remotePlaces = repo.fetchSafePlaces()
            if (remotePlaces.isNotEmpty()) {
                sheltersState = remotePlaces.map { place ->
                    ShelterInfo(
                        name = place.name,
                        address = place.address,
                        distance = "0.5 km",
                        openHours = place.openHours ?: "Open 24/7",
                        availableBeds = "Verified Safe Place",
                        isVerified = place.isVerified,
                        phone = place.phone ?: "+1 (800) 555-SAFE"
                    )
                }
            }
        } catch (_: Exception) {}
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Find Women's Shelters & Safe Houses",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Locate verified emergency shelters, night refuges, and safe spaces nearby.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search city, neighborhood, or shelter name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SafePurple) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Filter Chips
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) }
                    )
                }
            }
        }

        // 24/7 Hotline Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SafeRose),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "24/7 Night Shelter Dispatch",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Need immediate transport to a safe house right now?",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Dialing 24/7 Shelter Helpline +1 (800) 555-SAFE...")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SafeRose),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call Hotline", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Call Hotline", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Shelters List Header
        item {
            Text(
                text = "Nearby Shelters (${sheltersState.size} Found)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Shelter Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                sheltersState.forEach { shelter ->
                    ShelterCard(
                        shelter = shelter,
                        onCallClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Calling ${shelter.name} at ${shelter.phone}...")
                            }
                        },
                        onDirectionsClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Opening Map Directions to ${shelter.address}...")
                            }
                        }
                    )
                }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (shelter.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = SafePurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${shelter.address} • ${shelter.distance} away",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SafeGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = shelter.openHours,
                        fontSize = 11.sp,
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SafePurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Shelter", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onDirectionsClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = "Directions", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Directions", fontSize = 12.sp)
                }
            }
        }
    }
}
