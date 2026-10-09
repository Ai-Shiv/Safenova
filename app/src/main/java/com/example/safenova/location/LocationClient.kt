package com.example.safenova.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

data class Coordinates(val latitude: Double, val longitude: Double)

class LocationClient(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinates {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                Coordinates(location.latitude, location.longitude)
            } else {
                val lastLocation = fusedLocationClient.lastLocation.await()
                if (lastLocation != null) {
                    Coordinates(lastLocation.latitude, lastLocation.longitude)
                } else {
                    Coordinates(37.7749, -122.4194) // Default fallback
                }
            }
        } catch (e: Exception) {
            Coordinates(37.7749, -122.4194) // Fallback coordinates if permission denied or GPS unavailable
        }
    }
}
