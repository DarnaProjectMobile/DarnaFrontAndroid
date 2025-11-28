package com.sim.darna.reviews

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

interface ReviewsApi {

    @GET("reviews/me/feedbacks")
    suspend fun getMyFeedbacks(): List<CollectorReviewResponse>

    @GET("reviews/me/reputation")
    suspend fun getMyReputation(): CollectorReputationResponse
}

data class CollectorReviewResponse(
    @SerializedName("_id") val id: String?,
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

data class CollectorReputationResponse(
    val collectorId: String?,
    val reviewsCount: Int,
    val averageRating: Float,
    val averageCollectorRating: Float,
    val averageCleanlinessRating: Float,
    val averageLocationRating: Float,
    val averageConformityRating: Float
)