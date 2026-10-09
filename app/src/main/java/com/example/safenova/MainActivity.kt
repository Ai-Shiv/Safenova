package com.example.safenova

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.safenova.data.repo.SafeNovaRepository
import com.example.safenova.location.LocationClient
import com.example.safenova.navigation.SafeNovaNavGraph
import com.example.safenova.sensors.ShakeDetector
import com.example.safenova.ui.theme.SAFENOVATheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Shake Sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector {
            Toast.makeText(this, "🚨 SHAKE SOS DETECTED! Broadcasting Emergency Location...", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                try {
                    val coords = LocationClient(this@MainActivity).getCurrentLocation()
                    SafeNovaRepository().triggerSosAlert(coords.latitude, coords.longitude)
                } catch (_: Exception) {}
            }
        }

        setContent {
            SAFENOVATheme {
                SafeNovaNavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        sensorManager.unregisterListener(shakeDetector)
        super.onPause()
    }
}

@Preview(showBackground = true)
@Composable
fun SafeNovaAppPreview() {
    SAFENOVATheme {
        SafeNovaNavGraph()
    }
}
