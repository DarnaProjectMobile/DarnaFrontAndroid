package com.sim.darna.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentIntentResponse(
    val clientSecret: String
)
