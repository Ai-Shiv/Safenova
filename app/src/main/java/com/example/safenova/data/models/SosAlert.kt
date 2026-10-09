package com.example.safenova.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SosAlert(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    val status: String = "ACTIVE",
    @SerialName("last_latitude") val lastLatitude: Double,
    @SerialName("last_longitude") val lastLongitude: Double,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null
)
