package com.sim.darna.data.remote

import retrofit2.Response
import retrofit2.http.*

interface StripeService {
    @POST("payments/create-intent")
    suspend fun createPaymentIntent(
        @Header("Authorization") token: String,
        @Body body: Map<String, Any>
    ): Response<PaymentIntentResponse>
}

data class PaymentIntentResponse(
    val clientSecret: String? = null,
    val paymentIntentId: String? = null
)

