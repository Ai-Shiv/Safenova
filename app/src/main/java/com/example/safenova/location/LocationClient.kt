package com.example.safenova.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordinates(val latitude: Double, val longitude: Double) {
    fun distanceKmTo(otherLat: Double, otherLng: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(otherLat - latitude)
        val dLng = Math.toRadians(otherLng - longitude)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(latitude)) * cos(Math.toRadians(otherLat)) *
                sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun formatted(): String = String.format("%.4f, %.4f", latitude, longitude)
}

class LocationClient(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    companion object {
        // Default fallback matches the seeded Central Metro District coordinates in Supabase
        val DEFAULT_COORDINATES = Coordinates(28.6315, 77.2197)
    }

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
                    DEFAULT_COORDINATES
                }
            }
        } catch (e: Exception) {
            DEFAULT_COORDINATES
        }
    }
}
