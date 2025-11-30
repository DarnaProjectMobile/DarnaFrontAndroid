package com.sim.darna.data.repository

import com.sim.darna.data.api.PaymentApi
import com.sim.darna.data.model.CreatePaymentIntentRequest
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val api: PaymentApi
) {
    suspend fun createPaymentIntent(
        amount: Int // montant en euros (le backend multiplie par 100 pour Stripe)
    ): String {
        val request = CreatePaymentIntentRequest(
            amount = amount
        )
        val response = api.createPaymentIntent(request)
        return response.clientSecret
    }
}
