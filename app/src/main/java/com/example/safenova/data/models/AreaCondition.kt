package com.example.safenova.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AreaCondition(
    val id: String? = null,
    @SerialName("reporter_id") val reporterId: String,
    @SerialName("lighting_rating") val lightingRating: String? = null,
    @SerialName("crowd_rating") val crowdRating: String? = null,
    @SerialName("security_presence") val securityPresence: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("created_at") val createdAt: String? = null
)
