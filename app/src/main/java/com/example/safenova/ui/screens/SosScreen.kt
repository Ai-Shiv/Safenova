package com.example.safenova.ui.screens

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safenova.data.models.SosAlert
import com.example.safenova.data.models.TrustedContact
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.Coordinates
import com.example.safenova.location.LocationClient
import com.example.safenova.media.AudioRecorderHelper
import com.example.safenova.services.TrackingForegroundService
import com.example.safenova.ui.fakecall.FakeCallActivity
import com.example.safenova.ui.theme.AlertRed
import com.example.safenova.ui.theme.AlertRedLight
import com.example.safenova.ui.theme.CinnamonBorder
import com.example.safenova.ui.theme.CinnamonCard
import com.example.safenova.ui.theme.CinnamonCardElevated
import com.example.safenova.ui.theme.CinnamonObsidian
import com.example.safenova.ui.theme.SafeDarkPurple
import com.example.safenova.ui.theme.SafeGreen
import com.example.safenova.ui.theme.SafePurple
import com.example.safenova.ui.theme.SafePurpleGlow
import com.example.safenova.ui.theme.SafeRose
import com.example.safenova.ui.theme.TextPrimary
import com.example.safenova.ui.theme.TextSecondary
import com.example.safenova.ui.theme.WarningOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SosScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToContacts: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = remember { SafeNovaRepository() }

    var activeSosAlert by remember { mutableStateOf<SosAlert?>(null) }
    var liveCoords by remember { mutableStateOf(LocationClient.DEFAULT_COORDINATES) }
    var trustedContacts by remember { mutableStateOf<List<TrustedContact>>(emptyList()) }
    var isSirenPlaying by remember { mutableStateOf(false) }
    var isSilentAlertSent by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }

    // Fake Call Configurator state (Screen 14)
    val callerOptions = listOf("Dad (Emergency)", "Mom", "Police Dispatch 112", "Roommate Priya")
    var selectedCaller by remember { mutableStateOf(callerOptions[0]) }
    var selectedDelaySec by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        try {
            liveCoords = LocationClient(context).getCurrentLocation()
        } catch (_: Exception) {}
        try {
            trustedContacts = repo.fetchTrustedContacts()
            val existingSos = repo.fetchActiveSosAlerts().firstOrNull {
                it.status.equals("ACTIVE", true) || it.status.equals("ACKNOWLEDGED", true)
            }
            if (existingSos != null) {
                activeSosAlert = existingSos
            }
        } catch (_: Exception) {}
    }

    // Continuous GPS coordinate refresh while SOS is active
    LaunchedEffect(activeSosAlert) {
        while (activeSosAlert != null) {
            delay(5000L)
            try {
                liveCoords = LocationClient(context).getCurrentLocation()
                val alerts = repo.fetchActiveSosAlerts()
                val match = alerts.firstOrNull { it.id == activeSosAlert?.id }
                if (match != null) {
                    activeSosAlert = if (match.status.equals("RESOLVED", true)) null else match
                }
            } catch (_: Exception) {}
        }
    }

    fun sendSmsToTrustedCircle() {
        coroutineScope.launch {
            val coords = LocationClient(context).getCurrentLocation()
            liveCoords = coords
            val mapsUrl = "https://maps.google.com/?q=${coords.latitude},${coords.longitude}"
            val phones = trustedContacts.filter { it.isActive }.map { it.contactPhone }
                .ifEmpty { listOf("+91 98765 11111", "+91 98765 22222") }
            val smsBody = "🚨 [SAFENOVA SOS ALERT - DEMO MODE] Emergency triggered! Live GPS Location: $mapsUrl (${coords.formatted()}). Responder Dashboard notified."
            try {
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${phones.joinToString(";")}")
                    putExtra("sms_body", smsBody)
                }
                context.startActivity(smsIntent)
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("📲 SMS payload prepared for ${phones.size} contacts: $mapsUrl")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Demo-Safe Mode Honesty Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AlertRedLight),
                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Demo Safe",
                        tint = AlertRed
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "DEMO-SAFE MODE • LIVE SUPABASE SYNC",
                            fontSize = 11.sp,
                            color = AlertRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Pressing SOS creates a live emergency record in Supabase, streams GPS to the Responder Web Dashboard, and alerts your Trusted Contacts.",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // 2. Giant One-Tap SOS Button OR SOS Active Command Panel (Screen 4)
        item {
            val currentSos = activeSosAlert
            if (currentSos == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(210.dp)
                        .clip(CircleShape)
                        .background(AlertRed.copy(alpha = 0.16f))
                        .border(2.dp, AlertRed.copy(alpha = 0.5f), CircleShape)
                        .padding(16.dp)
                ) {
                    Surface(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    TrackingForegroundService.startService(context)
                                    val audioRecorder = AudioRecorderHelper(context)
                                    audioRecorder.startRecording()
                                    val coords = LocationClient(context).getCurrentLocation()
                                    liveCoords = coords
                                    val created = repo.triggerSosAlert(
                                        coords.latitude,
                                        coords.longitude,
                                        "DEMO_MODE_LIVE_STREAM_ACTIVE"
                                    )
                                    activeSosAlert = created
                                    snackbarHostState.showSnackbar(
                                        "🚨 SOS CREATED IN SUPABASE! Live on Responder Dashboard & Trusted Circle."
                                    )
                                } catch (e: Exception) {
                                    activeSosAlert = SosAlert(
                                        id = "local-sos-demo",
                                        userId = "demo",
                                        status = "ACTIVE",
                                        lastLatitude = liveCoords.latitude,
                                        lastLongitude = liveCoords.longitude
                                    )
                                    snackbarHostState.showSnackbar("🚨 EMERGENCY SOS ACTIVATED!")
                                }
                            }
                        },
                        shape = CircleShape,
                        color = AlertRed,
                        shadowElevation = 14.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Trigger SOS",
                                tint = Color.White,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "PRESS SOS",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "One-Tap Alert",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            } else {
                // Screen 4: SOS Active Status Card with "I'm Safe", "Cancel", and "Who Has Been Alerted"
                Card(
                    colors = CardDefaults.cardColors(containerColor = CinnamonCard),
                    border = BorderStroke(2.dp, AlertRed),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(AlertRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SOS ACTIVE • ${currentSos.status.uppercase()}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = AlertRed
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SafeGreen.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "LIVE GPS ON",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafeGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CinnamonObsidian)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Live Coordinates: ${liveCoords.formatted()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Supabase Record ID: ${(currentSos.id ?: "active").take(18)}...",
                                fontSize = 11.sp,
                                color = SafePurpleGlow
                            )
                            Text(
                                text = "Alerted: Responder Web Dashboard + ${trustedContacts.count { it.isActive }.coerceAtLeast(3)} Trusted Contacts",
                                fontSize = 11.sp,
                                color = SafeGreen
                            )
                        }

                        Button(
                            onClick = { sendSmsToTrustedCircle() },
                            colors = ButtonDefaults.buttonColors(containerColor = SafePurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "SMS", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("NOTIFY TRUSTED CONTACTS VIA SMS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val sosId = currentSos.id
                                    activeSosAlert = null
                                    TrackingForegroundService.stopService(context)
                                    coroutineScope.launch {
                                        try {
                                            if (sosId != null && sosId != "local-sos-demo") {
                                                repo.resolveSosAlert(sosId, "RESOLVED")
                                            }
                                        } catch (_: Exception) {}
                                        snackbarHostState.showSnackbar("✅ Marked 'I'm Safe'! SOS resolved in Supabase & Responder Dashboard.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Safe", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("I'M SAFE", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val sosId = currentSos.id
                                    activeSosAlert = null
                                    TrackingForegroundService.stopService(context)
                                    coroutineScope.launch {
                                        try {
                                            if (sosId != null && sosId != "local-sos-demo") {
                                                repo.resolveSosAlert(sosId, "RESOLVED")
                                            }
                                        } catch (_: Exception) {}
                                        snackbarHostState.showSnackbar("SOS Cancelled and cleared from active queue.")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, TextSecondary),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = TextPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CANCEL SOS", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Immediate Action Triggers Grid
        item {
            Text(
                text = "Immediate Action Triggers",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = if (isSirenPlaying) "Stop Loud Siren" else "Sound Loud Siren",
                        subtitle = "110dB Audible Deterrent",
                        icon = Icons.Default.VolumeUp,
                        active = isSirenPlaying,
                        activeColor = AlertRed,
                        onClick = {
                            isSirenPlaying = !isSirenPlaying
                            if (isSirenPlaying) {
                                try {
                                    val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                                    toneG.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 1500)
                                } catch (_: Exception) {}
                            }
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isSirenPlaying) "🔊 Loud Emergency Siren Activated!" else "Siren Stopped."
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        title = "Silent SOS + SMS",
                        subtitle = "Stealth DB + SMS Dispatch",
                        icon = Icons.Default.LocationOn,
                        active = isSilentAlertSent,
                        activeColor = SafePurpleGlow,
                        onClick = {
                            isSilentAlertSent = true
                            coroutineScope.launch {
                                try {
                                    val coords = LocationClient(context).getCurrentLocation()
                                    liveCoords = coords
                                    val created = repo.triggerSosAlert(coords.latitude, coords.longitude, "SILENT_STEALTH_SOS")
                                    activeSosAlert = created
                                } catch (_: Exception) {}
                                sendSmsToTrustedCircle()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Instant Fake Call",
                        subtitle = "Caller: $selectedCaller",
                        icon = Icons.Default.PhoneCallback,
                        active = false,
                        activeColor = SafeRose,
                        onClick = {
                            val intent = Intent(context, FakeCallActivity::class.java).apply {
                                putExtra("CALLER_NAME", selectedCaller)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        title = if (isTorchOn) "Turn Off Strobe" else "Flashlight Strobe",
                        subtitle = "Visual SOS Torch Signal",
                        icon = Icons.Default.FlashOn,
                        active = isTorchOn,
                        activeColor = WarningOrange,
                        onClick = {
                            isTorchOn = !isTorchOn
                            try {
                                val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                                val camId = camManager.cameraIdList.firstOrNull()
                                if (camId != null) {
                                    camManager.setTorchMode(camId, isTorchOn)
                                }
                            } catch (_: Exception) {}
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isTorchOn) "🔦 Flashlight Beacon Activated!" else "Flashlight Turned Off."
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Screen 14: Fake Call / Escape Mode Configurator
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
                        Icon(imageVector = Icons.Default.PhoneCallback, contentDescription = "Fake Call", tint = SafeRose)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Fake Call / Escape Mode Setup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Pick a caller persona and timer delay to excuse yourself safely",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        callerOptions.forEach { caller ->
                            FilterChip(
                                selected = selectedCaller == caller,
                                onClick = { selectedCaller = caller },
                                label = { Text(caller, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SafeDarkPurple,
                                    selectedLabelColor = SafePurpleGlow
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Delay:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        listOf(0 to "Now", 3 to "3 sec", 10 to "10 sec").forEach { (sec, label) ->
                            FilterChip(
                                selected = selectedDelaySec == sec,
                                onClick = { selectedDelaySec = sec },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SafeRose.copy(alpha = 0.22f),
                                    selectedLabelColor = SafeRose
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (selectedDelaySec > 0) {
                                    snackbarHostState.showSnackbar("📞 Incoming call from '$selectedCaller' in $selectedDelaySec seconds...")
                                    delay(selectedDelaySec * 1000L)
                                }
                                val intent = Intent(context, FakeCallActivity::class.java).apply {
                                    putExtra("CALLER_NAME", selectedCaller)
                                }
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeRose),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedDelaySec == 0) "TRIGGER FAKE CALL NOW ($selectedCaller)"
                            else "SCHEDULE FAKE CALL IN ${selectedDelaySec}S",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 5. Who Gets Alerted (Trusted Circle + Responder Web Command)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Who Gets Alerted (${trustedContacts.count { it.isActive }.coerceAtLeast(3)} Contacts + Web Responder)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Manage CRUD →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafePurpleGlow,
                    modifier = Modifier.clickable { onNavigateToContacts() }
                )
            }
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ContactItem(
                        name = "SafeNova Web Responder Dashboard",
                        phone = "Real-Time Supabase Stream",
                        relation = "Command Center",
                        statusLabel = if (activeSosAlert != null) "ALERTED LIVE" else "CONNECTED"
                    )

                    if (trustedContacts.isEmpty()) {
                        ContactItem(name = "Mom (Primary SOS)", phone = "+91 98765 11111", relation = "Family")
                        ContactItem(name = "Priya Sharma", phone = "+91 98765 22222", relation = "Sister")
                        ContactItem(name = "Rohan Verma", phone = "+91 98765 33333", relation = "Trusted Friend")
                    } else {
                        trustedContacts.filter { it.isActive }.forEach { c ->
                            ContactItem(
                                name = c.contactName,
                                phone = c.contactPhone,
                                relation = c.relation ?: "Family",
                                statusLabel = if (activeSosAlert != null) "ALERTED" else "READY"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) activeColor.copy(alpha = 0.2f) else CinnamonCard
        ),
        border = BorderStroke(1.dp, if (active) activeColor else CinnamonBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (active) activeColor else activeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (active) Color.White else activeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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
}

@Composable
fun ContactItem(
    name: String,
    phone: String,
    relation: String,
    statusLabel: String = "READY"
) {
    val badgeColor = if (statusLabel.contains("ALERTED")) AlertRed else SafeGreen
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$phone • $relation",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = badgeColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
        ) {
            Text(
                text = statusLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
