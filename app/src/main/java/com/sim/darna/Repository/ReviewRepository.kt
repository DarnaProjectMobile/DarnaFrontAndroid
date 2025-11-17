package com.sim.darna.repository

import com.sim.darna.auth.ReviewApi
import com.sim.darna.model.Review
import android.content.Context

class ReviewRepository(
    private val api: ReviewApi,
    private val context: Context
) {

    // -------------------------
    // GET ALL REVIEWS
    // -------------------------
    suspend fun getReviews(): List<Review> {
        val res = api.getReviews()
        return if (res.isSuccessful) {
            res.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    // -------------------------
    // CREATE REVIEW (NO USER ID SENT)
    // -------------------------
    suspend fun createReview(rating: Int, comment: String): Review? {
        val body = mapOf(
            "rating" to rating,
            "comment" to comment
        )

        val res = api.createReview(body)
        return if (res.isSuccessful) res.body() else null
    }

    // -------------------------
    // UPDATE REVIEW
    // -------------------------
    suspend fun updateReview(id: String, rating: Int, comment: String): Review? {
        val body = mapOf(
            "rating" to rating,
            "comment" to comment
        )

        val res = api.updateReview(id, body)
        return if (res.isSuccessful) res.body() else null
    }

    // -------------------------
    // DELETE REVIEW
    // -------------------------
    suspend fun deleteReview(id: String): Boolean {
        val res = api.deleteReview(id)
        return res.isSuccessful
    }
}
