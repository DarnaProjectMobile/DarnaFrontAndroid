package com.sim.darna.model

data class UpdateReviewRequest(
    val rating: Int? = null,
    
    val comment: String? = null,
    
    val userName: String? = null,
    
    val propertyName: String? = null
)

