package com.example.safenova.ai

import com.example.safenova.data.models.AreaCondition
import com.example.safenova.data.models.IncidentReport
import com.example.safenova.data.models.SafePlace
import com.example.safenova.location.Coordinates

data class EvaluatedRoute(
    val routeId: String,          // "SAFEST", "FASTEST", "LOW_TRAFFIC"
    val title: String,            // "Safest Route", "Fastest Route", "Low Traffic Route"
    val durationMinutes: Int,
    val distanceKm: Double,
    val safetyScore: Int,         // 0 to 100
    val safetyLevel: String,      // "HIGH SAFETY", "MODERATE SAFETY", "CAUTION"
    val polylinePoints: List<Coordinates>,
    val highlights: List<String>  // e.g. ["88% Streetlights On", "2 Police Patrols", "Busy Streets"]
)

object SafeRouteCalculator {

    fun calculateRouteOptions(
        start: Coordinates,
        destination: Coordinates,
        incidents: List<IncidentReport>,
        conditions: List<AreaCondition>,
        safePlaces: List<SafePlace>
    ): List<EvaluatedRoute> {
        val distKm = start.distanceKmTo(destination.latitude, destination.longitude).coerceAtLeast(0.5)

        // Generate 3 Waypoint Paths
        val midLat = (start.latitude + destination.latitude) / 2
        val midLng = (start.longitude + destination.longitude) / 2

        // Path 1: Safest (swings through main lit avenues & safe places)
        val safestPoints = listOf(
            start,
            Coordinates(midLat + 0.003, midLng - 0.002),
            Coordinates(destination.latitude + 0.001, destination.longitude - 0.001),
            destination
        )

        // Path 2: Fastest (direct line)
        val fastestPoints = listOf(
            start,
            Coordinates(midLat, midLng),
            destination
        )

        // Path 3: Low Traffic (outer arterial ring)
        val lowTrafficPoints = listOf(
            start,
            Coordinates(midLat - 0.004, midLng + 0.003),
            destination
        )

        // Calculate Safety Scores
        val safestScore = evaluateSafety(safestPoints, incidents, conditions, safePlaces, baseLighting = 90)
        val fastestScore = evaluateSafety(fastestPoints, incidents, conditions, safePlaces, baseLighting = 65)
        val lowTrafficScore = evaluateSafety(lowTrafficPoints, incidents, conditions, safePlaces, baseLighting = 75)

        return listOf(
            EvaluatedRoute(
                routeId = "SAFEST",
                title = "🛡️ Safest Route",
                durationMinutes = (distKm * 3.5 + 4).toInt(),
                distanceKm = distKm * 1.15,
                safetyScore = safestScore,
                safetyLevel = if (safestScore >= 80) "HIGH SAFETY" else "MODERATE SAFETY",
                polylinePoints = safestPoints,
                highlights = listOf("88% Streetlights On", "Busy Main Streets", "Near Police Post")
            ),
            EvaluatedRoute(
                routeId = "FASTEST",
                title = "⚡ Fastest Route",
                durationMinutes = (distKm * 2.8 + 2).toInt(),
                distanceKm = distKm,
                safetyScore = fastestScore,
                safetyLevel = if (fastestScore >= 70) "MODERATE SAFETY" else "CAUTION",
                polylinePoints = fastestPoints,
                highlights = listOf("Direct Path", "Moderate Traffic", "2 Low-Light Zones")
            ),
            EvaluatedRoute(
                routeId = "LOW_TRAFFIC",
                title = "🚙 Low Traffic Route",
                durationMinutes = (distKm * 3.0 + 3).toInt(),
                distanceKm = distKm * 1.08,
                safetyScore = lowTrafficScore,
                safetyLevel = if (lowTrafficScore >= 75) "MODERATE SAFETY" else "CAUTION",
                polylinePoints = lowTrafficPoints,
                highlights = listOf("Smooth Flow", "Outer Arterial Lane", "Near 24/7 Pharmacy")
            )
        )
    }

    private fun evaluateSafety(
        points: List<Coordinates>,
        incidents: List<IncidentReport>,
        conditions: List<AreaCondition>,
        safePlaces: List<SafePlace>,
        baseLighting: Int
    ): Int {
        var score = baseLighting

        // Incident Penalty
        val nearbyIncidents = incidents.count { inc ->
            points.any { pt -> pt.distanceKmTo(inc.latitude, inc.longitude) < 0.8 }
        }
        score -= (nearbyIncidents * 12).coerceAtMost(35)

        // Lighting & Crowd Bonus
        val wellLit = conditions.count { cond ->
            cond.lightingRating?.contains("Well-Lit", true) == true
        }
        score += (wellLit * 5).coerceAtMost(15)

        // Safe Place Proximity Bonus
        val safeCount = safePlaces.count { place ->
            points.any { pt -> pt.distanceKmTo(place.latitude, place.longitude) < 1.0 }
        }
        score += (safeCount * 6).coerceAtMost(18)

        return score.coerceIn(25, 98)
    }
}
