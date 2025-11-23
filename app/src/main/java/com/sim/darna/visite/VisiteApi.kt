package com.sim.darna.visite

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VisiteApi {
    @POST("visite")
    suspend fun createVisite(@Body body: CreateVisiteRequest): VisiteResponse

    @GET("visite/my-visites")
    suspend fun getMyVisites(): List<VisiteResponse>

    @GET("visite/my-logements-visites")
    suspend fun getMyLogementsVisites(): List<VisiteResponse>

    @PATCH("visite/{id}")
    suspend fun updateVisite(@Path("id") id: String, @Body body: UpdateVisiteRequest): VisiteResponse

    @PUT("visite/{id}/status")
    suspend fun updateStatus(@Path("id") id: String, @Body body: UpdateStatusRequest): VisiteResponse

    @POST("visite/{id}/accept")
    suspend fun acceptVisite(@Path("id") id: String): VisiteResponse

    @POST("visite/{id}/reject")
    suspend fun rejectVisite(@Path("id") id: String): VisiteResponse

    @DELETE("visite/{id}")
    suspend fun deleteVisite(@Path("id") id: String)
}

data class VisiteResponse(
    @SerializedName("_id")
    val id: String?,
    val logementId: String?,
    val userId: String?,
    val dateVisite: String?,
    val status: String?,
    val notes: String?,
    val contactPhone: String?
)
