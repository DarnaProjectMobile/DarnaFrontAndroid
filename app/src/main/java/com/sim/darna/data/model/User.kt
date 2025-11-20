package com.sim.darna.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: String, // "student" ou "sponsor"
    val sponsorId: String? = null // Si role = "sponsor", contient l'ID du sponsor
)

