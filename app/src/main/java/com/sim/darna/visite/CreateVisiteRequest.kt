package com.sim.darna.visite

data class CreateVisiteRequest(
    val logementId: String,
    val dateVisite: String,
    val notes: String? = null,
    val contactPhone: String? = null
)
