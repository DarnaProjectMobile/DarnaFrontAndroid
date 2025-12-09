package com.sim.darna.auth

import com.sim.darna.model.Review
import com.sim.darna.model.ReviewRequest
import com.sim.darna.model.ReviewSummary
import com.sim.darna.model.UpdateReviewRequest
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {

    @GET("reviews")
    suspend fun getReviews(
        @Query("property") propertyId: String? = null,
        @Query("user") userId: String? = null
    ): Response<List<Review>>

    @POST("reviews")
    suspend fun createReview(@Body body: ReviewRequest): Response<Review>


    @PUT("reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: String,
        @Body body: UpdateReviewRequest
    ): Response<Review>


    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String): Response<Unit>

    @GET("reviews/summary/{propertyId}")
    suspend fun getReviewSummary(@Path("propertyId") propertyId: String): Response<ReviewSummary>
}
