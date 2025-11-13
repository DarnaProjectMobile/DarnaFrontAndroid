package com.sim.darna.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PublicitesResponse(
    val data: List<Publicite>
)
