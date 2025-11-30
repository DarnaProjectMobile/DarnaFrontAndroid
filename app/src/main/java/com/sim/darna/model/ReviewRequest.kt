package com.sim.darna.model

data class ReviewRequest(
    val rating: Int,
    val comment: String
)
data class UpdateReviewRequest(
    val rating: Int,
    val comment: String
)
