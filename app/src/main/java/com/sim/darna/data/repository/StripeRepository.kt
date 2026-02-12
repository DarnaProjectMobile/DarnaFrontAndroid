package com.sim.darna.data.repository

import com.sim.darna.auth.UserSessionManager
import com.sim.darna.data.remote.StripeService
import retrofit2.Response
import javax.inject.Inject

class StripeRepository @Inject constructor(
    private val api: StripeService
) {
    suspend fun createPaymentIntent(amount: Double): Response<com.sim.darna.data.remote.PaymentIntentResponse> {
        val token = UserSessionManager.currentToken ?: throw Exception("Non authentifié")
        // Le backend attend le montant en euros directement (pas en centimes)
        val body = mapOf(
            "amount" to amount
        )
        return api.createPaymentIntent("Bearer $token", body)
    }
}

