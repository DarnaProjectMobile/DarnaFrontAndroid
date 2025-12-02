package com.sim.darna.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Partenaire(
    @Json(name = "_id") val _id: String? = null,
    val username: String,
    val email: String,
    val role: String? = null
)