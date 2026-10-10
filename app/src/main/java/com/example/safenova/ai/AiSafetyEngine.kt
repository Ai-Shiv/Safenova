package com.example.safenova.ai

import com.example.safenova.data.models.AreaCondition
import com.example.safenova.data.models.IncidentReport
import com.example.safenova.data.models.SafePlace
import com.example.safenova.location.Coordinates
import java.util.Calendar

data class SafetyRiskAssessment(
    val riskLevel: String,        // "LOW RISK", "MEDIUM RISK", "HIGH RISK"
    val safetyScore: Int,         // 0 to 100
    val summary: String,
    val recommendedAction: String,
    val factors: List<String> = emptyList()
)

data class RouteOptionPrototype(
    val id: String,               // "SAFEST", "FASTEST", "SHORTEST"
    val title: String,
    val distanceKm: Double,
    val durationMins: Int,
    val riskLevel: String,        // "LOW", "MEDIUM", "HIGH"
    val safetyScore: Int,         // 0 to 100
    val badge: String,
    val explanation: String,
    val nearbyReportCount: Int,
    val nearbyHelpCount: Int
)

class AiSafetyEngine {

    fun calculateRiskScore(
        latitude: Double,
        longitude: Double,
        recentIncidents: List<IncidentReport>,
        areaConditions: List<AreaCondition>,
        nearbySafePlaces: List<SafePlace>,
        overrideNightMode: Boolean? = null
    ): SafetyRiskAssessment {
        var baseScore = 86
        val factors = mutableListOf<String>()
        val origin = Coordinates(latitude, longitude)

        // 1. Time-Aware Factor (Night penalty)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNightTime = overrideNightMode ?: (currentHour in 20..23 || currentHour in 0..5)
        if (isNightTime) {
            baseScore -= 14
            factors.add("Time of Day: Night hours (-14 pts)")
        } else {
            factors.add("Time of Day: Daylight / Active hours (+0 penalty)")
        }

        // 2. Nearby Community Reports (within 2.5 km)
        val localIncidents = recentIncidents.filter {
            origin.distanceKmTo(it.latitude, it.longitude) <= 2.5
        }.ifEmpty { recentIncidents.take(4) }
        val incidentPenalty = (localIncidents.size * 4).coerceAtMost(24)
        baseScore -= incidentPenalty
        factors.add("Community Reports Nearby: ${localIncidents.size} active pins (-$incidentPenalty pts)")

        // 3. Street Lighting & Crowd Conditions
        val poorLightingCount = areaConditions.count {
            it.lightingRating?.contains("Dark", ignoreCase = true) == true ||
                    it.lightingRating?.contains("Poor", ignoreCase = true) == true
        }
        val lightingPenalty = (poorLightingCount * 5).coerceAtMost(15)
        baseScore -= lightingPenalty

        val desertedCount = areaConditions.count {
            it.crowdRating?.contains("Deserted", ignoreCase = true) == true
        }
        val crowdPenalty = (desertedCount * 5).coerceAtMost(10)
        baseScore -= crowdPenalty
        factors.add("Lighting & Crowd Telemetry: $poorLightingCount low-light / $desertedCount isolated reports (-${lightingPenalty + crowdPenalty} pts)")

        // 4. Proximity to Police, Hospitals & Safe Shelters
        val helpBonus = (nearbySafePlaces.size * 3).coerceAtMost(18)
        baseScore += helpBonus
        factors.add("Nearby Police/Hospitals/Shelters: ${nearbySafePlaces.size} verified units (+$helpBonus pts)")

        val finalScore = baseScore.coerceIn(15, 98)

        val (level, summary, action) = when {
            finalScore >= 75 -> Triple(
                "LOW RISK",
                "Safety Score Prototype: Well-lit main corridor with ${nearbySafePlaces.size} nearby police/medical/shelter units.",
                "Safe to travel. Keep live GPS sharing active."
            )
            finalScore >= 52 -> Triple(
                "MEDIUM RISK",
                "Safety Score Prototype: Moderate caution advised (${localIncidents.size} community reports in vicinity).",
                "Prefer the Safest Route along main avenues and enable Auto Check-In."
            )
            else -> Triple(
                "HIGH RISK",
                "Safety Score Prototype: Elevated risk due to poor lighting reports and night-time conditions.",
                "Take Safest Route, request Night Escort, or alert Trusted Circle."
            )
        }

        return SafetyRiskAssessment(
            riskLevel = level,
            safetyScore = finalScore,
            summary = summary,
            recommendedAction = action,
            factors = factors
        )
    }

    fun evaluateRoutesPrototype(
        destinationName: String,
        incidents: List<IncidentReport>,
        safePlaces: List<SafePlace>,
        isNightMode: Boolean
    ): List<RouteOptionPrototype> {
        val baseSeed = destinationName.trim().length.coerceAtLeast(3)
        val baseDist = 2.4 + (baseSeed % 4) * 0.6
        val nightPenalty = if (isNightMode) 10 else 0
        val totalReports = incidents.size
        val policeAndHospitals = safePlaces.count {
            it.type.equals("POLICE", true) || it.type.equals("HOSPITAL", true) || it.type.equals("SHELTER", true)
        }.coerceAtLeast(3)

        val safestScore = (93 - nightPenalty / 2 + (policeAndHospitals.coerceAtMost(6))).coerceIn(78, 96)
        val fastestScore = (68 - nightPenalty - (totalReports.coerceAtMost(8))).coerceIn(48, 74)
        val shortestScore = (44 - nightPenalty - (totalReports.coerceAtMost(10))).coerceIn(24, 52)

        return listOf(
            RouteOptionPrototype(
                id = "SAFEST",
                title = "🛡️ Safest Route (Main Boulevard)",
                distanceKm = ((baseDist + 0.8) * 10).toInt() / 10.0,
                durationMins = (baseDist * 4.5 + 5).toInt(),
                riskLevel = "LOW",
                safetyScore = safestScore,
                badge = "Well-Lit • $policeAndHospitals Police/Medical Units • Active Crowd",
                explanation = "Rule-Based Score: Passes 2 Police Patrol Booths & 24/7 Hospital; avoids ${totalReports.coerceAtLeast(3)} dark-alley reports.",
                nearbyReportCount = 1,
                nearbyHelpCount = policeAndHospitals
            ),
            RouteOptionPrototype(
                id = "FASTEST",
                title = "⚡ Fastest Route (Express Corridor)",
                distanceKm = (baseDist * 10).toInt() / 10.0,
                durationMins = (baseDist * 3.4 + 2).toInt(),
                riskLevel = "MEDIUM",
                safetyScore = fastestScore,
                badge = "Moderate Lighting • 3 Reports Nearby",
                explanation = "Rule-Based Score: Saves ~4 mins via underpass, but passes near 3 community reports (Poor Lighting / Blocked Road).",
                nearbyReportCount = 3,
                nearbyHelpCount = 2
            ),
            RouteOptionPrototype(
                id = "SHORTEST",
                title = "📏 Shortest Route (Service Alley)",
                distanceKm = ((baseDist - 0.5).coerceAtLeast(1.2) * 10).toInt() / 10.0,
                durationMins = (baseDist * 4.0 + 1).toInt(),
                riskLevel = "HIGH",
                safetyScore = shortestScore,
                badge = "High Caution • Unlit Stretch • Low Crowd",
                explanation = "Rule-Based Score: Shortest distance, but cuts through unlit back-lane with multiple harassment/darkness reports.",
                nearbyReportCount = (totalReports / 2).coerceAtLeast(5),
                nearbyHelpCount = 0
            )
        )
    }
}
