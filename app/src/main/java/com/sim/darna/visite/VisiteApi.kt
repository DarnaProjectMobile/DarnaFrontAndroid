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

    @POST("visite/{id}/cancel")
    suspend fun cancelVisite(@Path("id") id: String): VisiteResponse

    @DELETE("visite/{id}")
    suspend fun deleteVisite(@Path("id") id: String)

    @POST("visite/{id}/validate")
    suspend fun validateVisite(@Path("id") id: String): VisiteResponse

    @POST("visite/{id}/review")
    suspend fun createReview(
        @Path("id") id: String,
        @Body body: CreateReviewRequest
    ): ReviewResponse

    @GET("visite/{id}/reviews")
    suspend fun getVisiteReviews(@Path("id") id: String): List<ReviewResponse>
}

data class VisiteResponse(
    @SerializedName("_id")
    val id: String?,
    val logementId: String?,
    val userId: String?,
    val dateVisite: String?,
    val status: String?,
    val notes: String?,
    val contactPhone: String?,
    @SerializedName("clientUsername")
    val clientUsername: String? = null,
    @SerializedName("logementTitle")
    val logementTitle: String? = null,
    @SerializedName("validated")
    val validated: Boolean? = null,
    @SerializedName("reviewId")
    val reviewId: String? = null
)

data class CreateReviewRequest(
    val visiteId: String? = null, // Rempli automatiquement par le backend depuis l'URL, mais requis par le DTO
    val collectorRating: Int,
    val cleanlinessRating: Int,
    val locationRating: Int,
    val conformityRating: Int,
    @SerializedName("comment")
    val comment: String? = null
)

data class ReviewResponse(
    @SerializedName("_id")
    val id: String?,
    val visiteId: String?,
    val userId: String?,
    val logementId: String?,
    val collectorId: String?,
    val rating: Float?,
    val collectorRating: Float?,
    val cleanlinessRating: Float?,
    val locationRating: Float?,
    val conformityRating: Float?,
    val comment: String?,
    val createdAt: String?
)