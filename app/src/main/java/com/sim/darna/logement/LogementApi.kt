package com.sim.darna.logement

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

interface LogementApi {
    @GET("logement")
    suspend fun getAllLogements(): List<LogementResponse>
}

data class LogementResponse(
    @SerializedName("_id")
    val id: String?,
    @SerializedName("annonceId")
    val annonceId: String?,
    @SerializedName("ownerId")
    val ownerId: String?,
    val title: String?,
    val description: String?,
    val address: String?,
    val price: Double?,
    val images: List<String>? = null,
    val rooms: Int? = null,
    val surface: Double? = null,
    val available: Boolean? = true,
    val location: Location? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

data class Location(
    val latitude: Double?,
    val longitude: Double?
)

