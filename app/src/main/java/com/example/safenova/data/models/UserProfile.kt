package com.example.safenova.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("emergency_pin") val emergencyPin: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
