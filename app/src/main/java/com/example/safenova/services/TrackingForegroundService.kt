package com.example.safenova.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.safenova.R
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.LocationClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackingForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var trackingJob: Job? = null
    private var activeSosId: String? = null

    companion object {
        const val CHANNEL_ID = "safenova_tracking_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_SOS_ID = "EXTRA_SOS_ID"

        fun startService(context: Context, sosId: String? = null) {
            try {
                val intent = Intent(context, TrackingForegroundService::class.java).apply {
                    if (sosId != null) putExtra(EXTRA_SOS_ID, sosId)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, TrackingForegroundService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(EXTRA_SOS_ID)?.let { activeSosId = it }
        val notification = createNotification()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {}

        startLiveTracking()

        return START_STICKY
    }

    private fun startLiveTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            val locationClient = LocationClient(applicationContext)
            val repo = SafeNovaRepository()
            while (true) {
                try {
                    val coords = locationClient.getCurrentLocation()
                    val currentId = activeSosId
                    if (currentId != null) {
                        repo.updateSosLocation(currentId, coords.latitude, coords.longitude)
                    } else {
                        val created = repo.triggerSosAlert(coords.latitude, coords.longitude, "FOREGROUND_GPS_STREAM")
                        activeSosId = created.id
                    }
                } catch (_: Exception) {}
                delay(10000)
            }
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SafeNova Live SOS & GPS Telemetry")
            .setContentText("Broadcasting live coordinates to Trusted Circle & Responder Dashboard...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeNova Live Location Tracking",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        trackingJob?.cancel()
        super.onDestroy()
    }
}
