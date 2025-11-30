package com.sim.darna.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentIntentRequest(
    val amount: Int // montant en euros (le backend multiplie par 100 pour convertir en centimes pour Stripe)
)

