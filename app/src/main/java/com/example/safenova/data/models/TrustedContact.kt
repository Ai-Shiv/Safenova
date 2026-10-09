package com.example.safenova.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrustedContact(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("contact_name") val contactName: String,
    @SerialName("contact_phone") val contactPhone: String,
    val relation: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)
