package com.sim.darna.repository

import com.sim.darna.auth.ReviewApi
import com.sim.darna.model.Review
import android.content.Context
import com.sim.darna.model.ReviewRequest
import com.sim.darna.model.ReviewSummary
import com.sim.darna.model.UpdateReviewRequest
import android.util.Log

class ReviewRepository(
    private val api: ReviewApi,
) {
    private val TAG = "ReviewRepository"

    // -------------------------
    // GET ALL REVIEWS
    // -------------------------
    suspend fun getReviews(): List<Review> {
        return try {
            Log.d(TAG, "Fetching all reviews")
            val res = api.getReviews()
            if (res.isSuccessful) {
                val reviews = res.body() ?: emptyList()
                Log.d(TAG, "Successfully fetched ${reviews.size} reviews")
                reviews
            } else {
                val errorBody = res.errorBody()?.string()
                Log.e(TAG, "Failed to fetch reviews: ${res.code()} - ${res.message()}")
                Log.e(TAG, "Error body: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching reviews", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    // -------------------------
    // GET REVIEWS FOR PROPERTY
    // -------------------------
    suspend fun getReviewsForProperty(propertyId: String): List<Review> {
        return try {
            Log.d(TAG, "Fetching reviews for property: $propertyId")
            val res = api.getReviews(propertyId = propertyId)
            if (res.isSuccessful) {
                val reviews = res.body() ?: emptyList()
                Log.d(TAG, "Successfully fetched ${reviews.size} reviews for property $propertyId")
                // Log first review details for debugging
                if (reviews.isNotEmpty()) {
                    val firstReview = reviews.first()
                    Log.d(TAG, "First review - id: ${firstReview.id}, propertyId: ${firstReview.propertyId}, userId: ${firstReview.userId}")
                }
                reviews
            } else {
                val errorBody = res.errorBody()?.string()
                Log.e(TAG, "Failed to fetch reviews for property $propertyId: ${res.code()} - ${res.message()}")
                Log.e(TAG, "Error body: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching reviews for property $propertyId", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    // -------------------------
    // GET REVIEWS FOR USER
    // -------------------------
    suspend fun getReviewsForUser(userId: String): List<Review> {
        return try {
            Log.d(TAG, "Fetching reviews for user: $userId")
            val res = api.getReviews(userId = userId)
            if (res.isSuccessful) {
                val reviews = res.body() ?: emptyList()
                Log.d(TAG, "Successfully fetched ${reviews.size} reviews for user $userId")
                reviews
            } else {
                val errorBody = res.errorBody()?.string()
                Log.e(TAG, "Failed to fetch reviews for user $userId: ${res.code()} - ${res.message()}")
                Log.e(TAG, "Error body: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching reviews for user $userId", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    // -------------------------
    // GET REVIEWS FOR USER AND PROPERTY
    // -------------------------
    suspend fun getReviewsForUserAndProperty(userId: String, propertyId: String): List<Review> {
        return try {
            Log.d(TAG, "Fetching reviews for user: $userId and property: $propertyId")
            val res = api.getReviews(propertyId = propertyId, userId = userId)
            if (res.isSuccessful) {
                val reviews = res.body() ?: emptyList()
                Log.d(TAG, "Successfully fetched ${reviews.size} reviews for user $userId and property $propertyId")
                reviews
            } else {
                val errorBody = res.errorBody()?.string()
                Log.e(TAG, "Failed to fetch reviews for user $userId and property $propertyId: ${res.code()} - ${res.message()}")
                Log.e(TAG, "Error body: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching reviews for user $userId and property $propertyId", e)
            e.printStackTrace()
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
            if (res.isSuccessful) {
                val review = res.body()
                Log.d(TAG, "Successfully created review for property $propId")
                review
            } else {
                Log.e(TAG, "Failed to create review: ${res.code()} - ${res.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while creating review", e)
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
            val body = UpdateReviewRequest(
                rating = rating,
                comment = comment,
                userName = userName,
                propertyName = propertyName
            )

            val res = api.updateReview(id, body)
            if (res.isSuccessful) {
                val review = res.body()
                Log.d(TAG, "Successfully updated review $id")
                review
            } else {
                Log.e(TAG, "Failed to update review $id: ${res.code()} - ${res.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating review $id", e)
            null
        }
    }

    // -------------------------
    // DELETE REVIEW
    // -------------------------
    suspend fun deleteReview(id: String): Boolean {
        return try {
            val res = api.deleteReview(id)
            if (res.isSuccessful) {
                Log.d(TAG, "Successfully deleted review $id")
                true
            } else {
                Log.e(TAG, "Failed to delete review $id: ${res.code()} - ${res.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while deleting review $id", e)
            false
        }
    }

    // -------------------------
    // GET REVIEW SUMMARY (AI-POWERED)
    // -------------------------
    suspend fun getReviewSummary(propertyId: String): ReviewSummary? {
        return try {
            Log.d(TAG, "Fetching AI summary for property: $propertyId")
            val res = api.getReviewSummary(propertyId)
            if (res.isSuccessful) {
                val summary = res.body()
                Log.d(TAG, "Successfully fetched summary for property $propertyId")
                summary
            } else {
                val errorBody = res.errorBody()?.string()
                Log.e(TAG, "Failed to fetch summary for property $propertyId: ${res.code()} - ${res.message()}")
                Log.e(TAG, "Error body: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching summary for property $propertyId", e)
            e.printStackTrace()
            null
        }
    }
}