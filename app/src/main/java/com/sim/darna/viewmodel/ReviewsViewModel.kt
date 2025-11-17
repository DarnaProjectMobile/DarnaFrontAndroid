package com.sim.darna.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.auth.RetrofitClient
import com.sim.darna.repository.ReviewRepository
import com.sim.darna.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ReviewViewModel : ViewModel() {

    private var repo: ReviewRepository? = null

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    fun init(context: Context) {
        if (repo == null) {
            val api = RetrofitClient.reviewApi(context) // ✅ correct function
            repo = ReviewRepository(api, context)
        }
    }

    private fun checkRepo(): ReviewRepository {
        return repo ?: throw IllegalStateException("ReviewViewModel: init(context) was not called!")
    }

    fun loadReviews() {
        viewModelScope.launch {
            _reviews.value = checkRepo().getReviews()
        }
    }

    fun addReview(rating: Int, comment: String) {
        viewModelScope.launch {
            val newReview = checkRepo().createReview(rating, comment)
            if (newReview != null) loadReviews()
        }
    }

    fun updateReview(id: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val updated = checkRepo().updateReview(id, rating, comment)
            if (updated != null) loadReviews()
        }
    }

    fun deleteReview(id: String) {
        viewModelScope.launch {
            if (checkRepo().deleteReview(id)) loadReviews()
        }
    }
}

