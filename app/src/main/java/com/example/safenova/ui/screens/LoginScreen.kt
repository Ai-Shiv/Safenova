package com.example.safenova.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.CinnamonBorder
import com.example.safenova.ui.theme.CinnamonCard
import com.example.safenova.ui.theme.CinnamonCardElevated
import com.example.safenova.ui.theme.CinnamonObsidian
import com.example.safenova.ui.theme.SAFENOVATheme
import com.example.safenova.ui.theme.SafeDarkPurple
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafePurpleGlow
import com.example.safenova.ui.theme.TextPrimary
import com.example.safenova.ui.theme.TextSecondary

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val badge: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
fun LoginScreen(vm: AuthViewModel) {
    var signup by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("ayushtiwari01012007@gmail.com") }
    var password by remember { mutableStateOf("safenova123") }
    var currentSlide by remember { mutableIntStateOf(0) }
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsGranted = result.values.any { it }
    }

    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "1. One-Tap SOS & Live GPS",
                subtitle = "Broadcasts your live GPS coordinates, timestamp & ID to Supabase and alerts your Trusted Circle + Responder Dashboard in real time.",
                badge = "TIER 1 • CORE",
                icon = Icons.Default.NotificationsActive,
                accent = AlertRed
            ),
            OnboardingSlide(
                title = "2. Safety Score Routes & Heatmap",
                subtitle = "Compare Safest, Fastest & Shortest routes with an explainable Safety Score Prototype powered by community reports and nearby help.",
                badge = "TIER 2 • SMART ROUTING",
                icon = Icons.Default.Explore,
                accent = SafePurpleGlow
            ),
            OnboardingSlide(
                title = "3. Escape Call, Escort & Safe Stays",
                subtitle = "Trigger a realistic Fake Call to exit uncomfortable situations, set Auto Check-In timers, or locate 24/7 Police, Hospitals & Shelters.",
                badge = "TIER 3 • DIFFERENTIATORS",
                icon = Icons.Default.People,
                accent = SafeGreen
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CinnamonObsidian
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 36.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Brand Header
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SafePurple, SafeDarkPurple)
                                )
                            )
                            .border(1.5.dp, SafePurpleGlow.copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "SafeNova Logo",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "SAFENOVA",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "Personal Safety, Smart Routing & Live SOS Command",
                        fontSize = 12.sp,
                        color = SafePurpleGlow
                    )
                }
            }

            // 3-Card Onboarding Carousel (Screen 1 requirement)
            item {
                val slide = slides[currentSlide]
                Card(
                    colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                    border = BorderStroke(1.dp, CinnamonBorder),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { currentSlide = (currentSlide + 1) % slides.size }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = slide.accent.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, slide.accent.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = slide.badge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = slide.accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                slides.indices.forEach { idx ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (idx == currentSlide) 18.dp else 7.dp, 7.dp)
                                            .clip(CircleShape)
                                            .background(if (idx == currentSlide) SafePurpleGlow else CinnamonBorder)
                                            .clickable { currentSlide = idx }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = slide.title,
                                tint = slide.accent,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = slide.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = slide.subtitle,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(Modifier.height(10.dp))

                        // Permission Request Row
                        Surface(
                            onClick = {
                                val perms = mutableListOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                permissionLauncher.launch(perms.toTypedArray())
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = CinnamonCardElevated,
                            border = BorderStroke(1.dp, if (permissionsGranted) SafeGreen else SafePurple.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (permissionsGranted) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                                        contentDescription = "Permissions",
                                        tint = if (permissionsGranted) SafeGreen else SafePurpleGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (permissionsGranted) "GPS & Notification Permissions Ready" else "Enable GPS & Notification Permissions",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (permissionsGranted) SafeGreen else TextPrimary
                                    )
                                }
                                Text(
                                    text = if (permissionsGranted) "GRANTED" else "ALLOW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (permissionsGranted) SafeGreen else SafePurpleGlow
                                )
                            }
                        }
                    }
                }
            }

            // Supabase Login / Sign Up Card (Screen 2 requirement)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                    border = BorderStroke(1.dp, CinnamonBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (signup) "Create SafeNova Account" else "Sign In with Supabase",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SafeGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "SUPABASE AUTH",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafeGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (signup) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number (Optional)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email or Phone ID") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (min 6 characters)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        vm.error?.let { errorText ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorText,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (signup) {
                                    vm.signUp(email.trim(), password, name.trim(), phone.trim())
                                } else {
                                    vm.signIn(email.trim(), password)
                                }
                            },
                            enabled = !vm.loading && email.isNotBlank() && password.length >= 6,
                            colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (vm.loading) "Authenticating with Supabase..." else if (signup) "CREATE SUPABASE ACCOUNT" else "SIGN IN TO SAFENOVA",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = {
                                    signup = !signup
                                    vm.clearError()
                                }
                            ) {
                                Text(
                                    text = if (signup) "Already registered? Sign In" else "New user? Create Account",
                                    color = SafePurpleGlow,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Instant Mentor Demo Mode Button
            item {
                OutlinedButton(
                    onClick = { vm.continueAsGuest() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SafePurpleGlow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "⚡ Launch Instant Live Demo (Connected to Supabase)",
                        color = SafePurpleGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SAFENOVATheme {
        LoginScreen(vm = AuthViewModel())
    }
}
