package com.example.safenova.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("emergency_pin") val emergencyPin: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @Transient val bloodType: String? = null,
    @Transient val allergies: String? = null,
    @Transient val medicalConditions: String? = null,
    @Transient val emergencyNotes: String? = null
) {
    fun unpackMedicalMetadata(): UserProfile {
        val raw = emergencyPin ?: return copy(
            bloodType = bloodType ?: "O+",
            allergies = allergies ?: "None",
            medicalConditions = medicalConditions ?: "None",
            emergencyNotes = emergencyNotes ?: "Contact trusted circle & responder unit immediately."
        )
        val parts = raw.split("|")
        return if (parts.size >= 5) {
            copy(
                emergencyPin = parts[0].ifBlank { "2580" },
                bloodType = parts[1].ifBlank { "O+" },
                allergies = parts[2].ifBlank { "None" },
                medicalConditions = parts[3].ifBlank { "None" },
                emergencyNotes = parts.subList(4, parts.size).joinToString("|")
            )
        } else {
            copy(
                emergencyPin = raw.take(4).ifBlank { "2580" },
                bloodType = bloodType ?: "O+",
                allergies = allergies ?: "None",
                medicalConditions = medicalConditions ?: "None",
                emergencyNotes = emergencyNotes ?: "Contact trusted circle & responder unit immediately."
            )
        }
    }

    fun packForDatabase(): UserProfile {
        val cleanPin = (emergencyPin ?: "2580").split("|").firstOrNull()?.ifBlank { "2580" } ?: "2580"
        val packed = listOf(
            cleanPin,
            bloodType ?: "O+",
            allergies ?: "None",
            medicalConditions ?: "None",
            emergencyNotes ?: ""
        ).joinToString("|")
        return UserProfile(
            id = id,
            fullName = fullName,
            phoneNumber = phoneNumber,
            emergencyPin = packed
        )
    }
}
