package com.sim.darna.repository

import com.sim.darna.auth.ReviewApi
import com.sim.darna.model.Review
import android.content.Context
import com.sim.darna.model.ReviewRequest

class ReviewRepository(
    private val api: ReviewApi,
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
        return try {
            val res = api.createReview(ReviewRequest(rating, comment))
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
