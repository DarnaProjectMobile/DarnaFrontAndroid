package com.sim.darna.data.api

import com.sim.darna.data.model.CreatePaymentIntentRequest
import com.sim.darna.data.model.PaymentIntentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApi {

    // Endpoint backend: POST /api/payments/create-intent
    // Le backend attend { amount: number } où amount est en euros
    // Note: Le backend multiplie par 100 pour convertir en centimes pour Stripe
    @POST("payments/create-intent")
    suspend fun createPaymentIntent(
        @Body request: CreatePaymentIntentRequest
    ): PaymentIntentResponse
}
