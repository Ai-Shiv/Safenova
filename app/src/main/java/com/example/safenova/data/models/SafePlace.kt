package com.example.safenova.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SafePlace(
    val id: String? = null,
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String? = null,
    @SerialName("open_hours") val openHours: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false
)
