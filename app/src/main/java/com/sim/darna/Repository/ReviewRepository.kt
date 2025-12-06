package com.sim.darna.repository

import com.sim.darna.auth.ReviewApi
import com.sim.darna.model.Review
import android.content.Context
import com.sim.darna.model.ReviewRequest
import com.sim.darna.model.UpdateReviewRequest

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
    // GET REVIEWS FOR PROPERTY
    // -------------------------
    suspend fun getReviewsForProperty(propertyId: String): List<Review> {
        val res = api.getReviews(propertyId = propertyId)
        return if (res.isSuccessful) {
            res.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    // -------------------------
    // CREATE REVIEW
    // -------------------------
    suspend fun createReview(rating: Int, comment: String, propertyId: String? = null, userName: String? = null, propertyName: String? = null): Review? {
        return try {
            // Check if required property info is provided
            val propId = propertyId ?: ""
            if (propId.isEmpty()) {
                throw IllegalArgumentException("Property ID is required to create a review")
            }
            
            val request = ReviewRequest(
                rating = rating,
                comment = comment,
                propertyId = propId,  // This maps to "property" in JSON due to @SerializedName
                userName = userName ?: "",
                propertyName = propertyName ?: ""
            )
            
            val res = api.createReview(request)
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // -------------------------
    // UPDATE REVIEW
    // -------------------------
    suspend fun updateReview(id: String, rating: Int, comment: String, userName: String? = null, propertyName: String? = null): Review? {
        return try {
            // If user/property names are not provided, use empty values
            // Backend should ideally handle this or these should be fetched from the existing review
            val body = UpdateReviewRequest(rating, comment, userName ?: "", propertyName ?: "")

            val res = api.updateReview(id, body)
            if (res.isSuccessful) res.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // -------------------------
    // DELETE REVIEW
    // -------------------------
    suspend fun deleteReview(id: String): Boolean {
        val res = api.deleteReview(id)
        return res.isSuccessful
    }
}