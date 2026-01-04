package com.sim.darna.data.repository

import android.content.Context
import android.util.Log
import com.sim.darna.auth.TokenStorage
import com.sim.darna.auth.UserSessionManager
import com.sim.darna.data.remote.StripeService
import retrofit2.Response
import javax.inject.Inject

class StripeRepository @Inject constructor(
    private val api: StripeService
) {
    suspend fun createPaymentIntent(context: Context, amount: Double): Response<com.sim.darna.data.remote.PaymentIntentResponse> {
        // Essayer d'abord UserSessionManager, puis TokenStorage
        val token = UserSessionManager.currentToken 
            ?: TokenStorage.getToken(context) 
            ?: throw Exception("Non authentifié")
        
        // Le backend attend le montant en euros directement (pas en centimes)
        // S'assurer que le montant est au minimum 0.5 (selon le DTO backend)
        val validAmount = amount.coerceAtLeast(0.5)
        val body = com.sim.darna.data.remote.CreatePaymentIntentRequest(
            amount = validAmount
        )
        Log.d("StripeRepository", "Creating payment intent with amount: $validAmount")
        return api.createPaymentIntent("Bearer $token", body)
    }
}

