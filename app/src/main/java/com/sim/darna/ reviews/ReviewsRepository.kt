package com.sim.darna.reviews

class ReviewsRepository(
    private val api: ReviewsApi
) {
    suspend fun getMyFeedbacks(): List<CollectorReviewResponse> = api.getMyFeedbacks()

    suspend fun getMyReputation(): CollectorReputationResponse = api.getMyReputation()
}
