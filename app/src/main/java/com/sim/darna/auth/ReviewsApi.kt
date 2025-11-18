package com.sim.darna.auth

import com.sim.darna.model.Review
import com.sim.darna.model.ReviewRequest
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {

    @GET("reviews")
    suspend fun getReviews(): Response<List<Review>>

    @POST("reviews")
    suspend fun createReview(@Body body: ReviewRequest): Response<Review>


    @PUT("reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: String,
        @Body body: Map<String, Any>
    ): Response<Review>

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String): Response<Unit>
}
