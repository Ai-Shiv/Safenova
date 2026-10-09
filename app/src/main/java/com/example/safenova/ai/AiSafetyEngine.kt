package com.example.safenova.ai

import com.example.safenova.data.models.AreaCondition
import com.example.safenova.data.models.IncidentReport
import com.example.safenova.data.models.SafePlace
import java.util.Calendar

data class SafetyRiskAssessment(
    val riskLevel: String, // "LOW", "MEDIUM", "HIGH"
    val safetyScore: Int,   // 0 to 100
    val summary: String,
    val recommendedAction: String
)

class AiSafetyEngine {

    fun calculateRiskScore(
        latitude: Double,
        longitude: Double,
        recentIncidents: List<IncidentReport>,
        areaConditions: List<AreaCondition>,
        nearbySafePlaces: List<SafePlace>
    ): SafetyRiskAssessment {
        var baseScore = 85 // Out of 100

        // 1. Time Factor (Night penalty)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNightTime = currentHour in 22..23 || currentHour in 0..5
        if (isNightTime) {
            baseScore -= 15
        }

        // 2. Proximity Incidents
        val incidentCount = recentIncidents.size
        baseScore -= (incidentCount * 8).coerceAtMost(30)

        // 3. Lighting & Crowd Conditions
        val poorLightingCount = areaConditions.count { it.lightingRating?.contains("Dark", ignoreCase = true) == true }
        baseScore -= (poorLightingCount * 10).coerceAtMost(20)

        val desertedCount = areaConditions.count { it.crowdRating?.contains("Deserted", ignoreCase = true) == true }
        baseScore -= (desertedCount * 10).coerceAtMost(15)

        // 4. Nearby Safe Places Bonus
        val safePlaceBonus = (nearbySafePlaces.size * 5).coerceAtMost(15)
        baseScore += safePlaceBonus

        val finalScore = baseScore.coerceIn(10, 100)

        val (level, summary, action) = when {
            finalScore >= 75 -> Triple(
                "LOW RISK",
                "Area is well-lit with active crowd & safe refuges nearby.",
                "Safe to travel. Standard location sharing active."
            )
            finalScore >= 50 -> Triple(
                "MEDIUM RISK",
                "Moderate lighting or recent reports in area.",
                "Stay on main streets. Keep phone accessible."
            )
            else -> Triple(
                "HIGH RISK",
                "Low lighting, low crowd, or recent incident alerts.",
                "Recommend requesting Safe Escort Ride or taking alternative main route."
            )
        }

        return SafetyRiskAssessment(
            riskLevel = level,
            safetyScore = finalScore,
            summary = summary,
            recommendedAction = action
        )
    }
}
