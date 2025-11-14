package com.sim.darna.model

data class Annonce(
    val id: Int,
    val titre: String,
    val description: String,
    val prix: Double,
    val imageUrl: String
)
