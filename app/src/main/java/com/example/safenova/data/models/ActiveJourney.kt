package com.example.safenova.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActiveJourney(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("destination_lat") val destinationLat: Double,
    @SerialName("destination_lng") val destinationLng: Double,
    @SerialName("expected_arrival_time") val expectedArrivalTime: String,
    val status: String = "ACTIVE",
    @SerialName("created_at") val createdAt: String? = null
)
