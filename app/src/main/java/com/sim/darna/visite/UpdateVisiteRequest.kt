package com.sim.darna.visite

data class UpdateVisiteRequest(
    val logementId: String? = null,
    val dateVisite: String? = null,
    val notes: String? = null,
    val contactPhone: String? = null
)



