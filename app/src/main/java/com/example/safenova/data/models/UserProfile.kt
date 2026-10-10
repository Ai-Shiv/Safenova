package com.example.safenova.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("emergency_pin") val emergencyPin: String? = null,
    @SerialName("blood_type") val bloodType: String? = null,
    val allergies: String? = null,
    @SerialName("medical_conditions") val medicalConditions: String? = null,
    @SerialName("emergency_notes") val emergencyNotes: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
