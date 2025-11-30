package com.sim.darna.logement

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface LogementApi {
    @GET("logement")
    suspend fun getAllLogements(): List<LogementResponse>
    
    @GET("logement/my-logements")
    suspend fun getMyLogements(): List<LogementResponse>
    
    @GET("logement/{id}")
    suspend fun getLogementById(@Path("id") id: String): LogementResponse
    
    @GET("logement/annonce/{annonceId}")
    suspend fun getLogementByAnnonceId(@Path("annonceId") annonceId: String): LogementResponse
    
    @POST("logement")
    suspend fun createLogement(@Body body: CreateLogementRequest): LogementResponse
    
    @PATCH("logement/{id}")
    suspend fun updateLogement(@Path("id") id: String, @Body body: UpdateLogementRequest): LogementResponse
    
    @DELETE("logement/{id}")
    suspend fun deleteLogement(@Path("id") id: String)
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

data class CreateLogementRequest(
    val annonceId: String?,
    val title: String,
    val description: String?,
    val address: String,
    val price: Double,
    val images: List<String>? = null,
    val rooms: Int? = null,
    val surface: Double? = null,
    val available: Boolean? = true,
    val location: Location? = null
)

data class UpdateLogementRequest(
    val title: String?,
    val description: String?,
    val address: String?,
    val price: Double?,
    val images: List<String>? = null,
    val rooms: Int? = null,
    val surface: Double? = null,
    val available: Boolean? = null,
    val location: Location? = null
)

