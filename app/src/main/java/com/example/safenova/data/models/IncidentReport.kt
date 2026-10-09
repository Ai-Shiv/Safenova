package com.example.safenova.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IncidentReport(
    val id: String? = null,
    @SerialName("reporter_id") val reporterId: String? = null,
    val category: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("is_anonymous") val isAnonymous: Boolean = true,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
