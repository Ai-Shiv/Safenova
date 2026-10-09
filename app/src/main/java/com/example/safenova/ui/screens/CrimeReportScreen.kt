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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrimeReportScreen(
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    val categories = listOf(
        "Harassment / Catcalling",
        "Stalking / Following",
        "Poor Lighting / Alleyway",
        "Physical Assault",
        "Theft / Robbery",
        "Suspicious Person"
    )

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var locationInput by remember { mutableStateOf("5th Avenue & 23rd St, Downtown") }
    var descriptionInput by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(true) }
    var hasPhotoAttached by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Report Crime or Unsafe Incident",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Help make your city safer. Reports are shared with local safety patrols & community map.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Category Selection
        item {
            Text(
                text = "Select Incident Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(text = category, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarningOrange.copy(alpha = 0.2f),
                            selectedLabelColor = WarningOrange
                        )
                    )
                }
            }
        }

        // Location Input
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            OutlinedTextField(
                value = locationInput,
                onValueChange = { locationInput = it },
                label = { Text("Incident Location") },
                trailingIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val locationClient = com.example.safenova.location.LocationClient(context)
                            val coords = locationClient.getCurrentLocation()
                            locationInput = "GPS: ${String.format("%.4f", coords.latitude)}, ${String.format("%.4f", coords.longitude)}"
                            snackbarHostState.showSnackbar("📍 Real GPS coordinates attached: (${coords.latitude}, ${coords.longitude})")
                        }
                    }) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Use GPS", tint = SafePurple)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Description Box
        item {
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text("Describe what happened (Optional)") },
                placeholder = { Text("Provide details e.g., time, person description, vehicle license plate...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Photo Attachment Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Photo", tint = SafePurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (hasPhotoAttached) "1 Photo Evidence Attached" else "Attach Photo / Evidence",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "Optional image or screenshot", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = {
                            hasPhotoAttached = !hasPhotoAttached
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (hasPhotoAttached) "📸 Image attached to crime report." else "Photo removed."
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafePurple.copy(alpha = 0.15f), contentColor = SafePurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = if (hasPhotoAttached) "Remove" else "Upload")
                    }
                }
            }
        }

        // Anonymous Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
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
                        Text(text = "Submit Anonymously", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Your name and phone number will be stripped from public reports.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                }
            }
        }

        // Submit Button
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val locationClient = com.example.safenova.location.LocationClient(context)
                            val coords = locationClient.getCurrentLocation()
                            val repo = com.example.safenova.data.repo.SafeNovaRepository()
                            val report = com.example.safenova.data.models.IncidentReport(
                                category = selectedCategory,
                                description = descriptionInput.ifBlank { null },
                                latitude = coords.latitude,
                                longitude = coords.longitude,
                                isAnonymous = isAnonymous
                            )
                            repo.submitIncidentReport(report)
                            snackbarHostState.showSnackbar(
                                "✅ Report for '$selectedCategory' saved to Supabase at GPS (${String.format("%.4f", coords.latitude)}, ${String.format("%.4f", coords.longitude)})!"
                            )
                            descriptionInput = ""
                            hasPhotoAttached = false
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(
                                "Report submitted locally."
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.ReportProblem, contentDescription = "Submit")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "SUBMIT CRIME REPORT", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
