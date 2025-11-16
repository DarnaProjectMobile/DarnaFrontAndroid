package com.sim.darna.model

import com.google.gson.annotations.SerializedName

data class Annonce(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val user: AnnonceUser,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

data class AnnonceUser(
    @SerializedName("_id")
    val id: String,
    val username: String,
    val email: String
)
