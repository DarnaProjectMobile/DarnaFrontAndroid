package com.sim.darna.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.auth.RetrofitClient
import com.sim.darna.model.Review
import com.sim.darna.model.ReviewSummary
import com.sim.darna.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {

    private var repo: ReviewRepository? = null

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _reviewSummary = MutableStateFlow<ReviewSummary?>(null)
    val reviewSummary: StateFlow<ReviewSummary?> = _reviewSummary

    // ------------------------------------------------------
    // INIT: MUST be called before any repo usage
    // ------------------------------------------------------
    fun init(context: Context) {
        if (repo == null) {
            val api = RetrofitClient.reviewApi(context)
            repo = ReviewRepository(api)
        }
    }

    // Helper to avoid null repository
    private fun getRepo(): ReviewRepository {
        return repo ?: throw IllegalStateException(
            "ReviewViewModel.init(context) was NOT called!"
        )
    }

    // ------------------------------------------------------
    // LOAD ALL REVIEWS
    // ------------------------------------------------------
    fun loadReviews() {
        viewModelScope.launch {
            try {
                _reviews.value = getRepo().getReviews()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // ------------------------------------------------------
    // LOAD REVIEWS FOR SPECIFIC PROPERTY
    // ------------------------------------------------------
    fun loadReviewsForProperty(propertyId: String) {
        viewModelScope.launch {
            try {
                _reviews.value = getRepo().getReviewsForProperty(propertyId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // ------------------------------------------------------
    // LOAD REVIEWS FOR SPECIFIC USER
    // ------------------------------------------------------
    fun loadReviewsForUser(userId: String) {
        viewModelScope.launch {
            try {
                _reviews.value = getRepo().getReviewsForUser(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // ------------------------------------------------------
    // LOAD REVIEWS FOR SPECIFIC USER AND PROPERTY
    // ------------------------------------------------------
    fun loadReviewsForUserAndProperty(userId: String, propertyId: String) {
        viewModelScope.launch {
            try {
                _reviews.value = getRepo().getReviewsForUserAndProperty(userId, propertyId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ------------------------------------------------------
    // ADD REVIEW
    // ------------------------------------------------------
    fun addReview(rating: Int, comment: String, propertyId: String? = null, userName: String? = null, propertyName: String? = null) {
        viewModelScope.launch {
            try {
                val created = getRepo().createReview(rating, comment, propertyId, userName, propertyName)
                if (created != null) {
                    _reviews.value = _reviews.value + created
                } else {
                    // Handle case where review creation failed
                    println("Failed to create review - server returned null")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error creating review: ${e.message}")
            }
        }
    }

    // ------------------------------------------------------
    // UPDATE REVIEW
    // ------------------------------------------------------
    fun updateReview(id: String, rating: Int, comment: String, userName: String? = null, propertyName: String? = null) {
        viewModelScope.launch {
            try {
                val updated = getRepo().updateReview(id, rating, comment, userName, propertyName)
                if (updated != null) {
                    // Update the local list directly for immediate feedback
                    _reviews.value = _reviews.value.map { review ->
                        if (review.id == id) updated else review
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ------------------------------------------------------
    // DELETE REVIEW
    // ------------------------------------------------------
    fun deleteReview(id: String) {
        viewModelScope.launch {
            try {
                if (getRepo().deleteReview(id)) {
                    // Remove the review from the local list directly for immediate feedback
                    _reviews.value = _reviews.value.filter { review ->
                        review.id != id
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ------------------------------------------------------
    // LOAD REVIEW SUMMARY (AI-POWERED)
    // ------------------------------------------------------
    fun loadReviewSummary(propertyId: String) {
        viewModelScope.launch {
            try {
                _reviewSummary.value = getRepo().getReviewSummary(propertyId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
