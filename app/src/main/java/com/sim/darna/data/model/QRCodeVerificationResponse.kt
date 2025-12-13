package com.sim.darna.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QRCodeVerificationResponse(
    val valid: Boolean,
    val message: String?,
    val reduction: Int? = null, // Pourcentage de réduction
    val publiciteId: String? = null
)

