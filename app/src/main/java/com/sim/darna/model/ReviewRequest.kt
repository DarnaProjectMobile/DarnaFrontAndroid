package com.sim.darna.model

import com.google.gson.annotations.SerializedName

data class ReviewRequest(
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("comment")
    val comment: String,
    
    @SerializedName("property")
    val propertyId: String,
    
    @SerializedName("userName")
    val userName: String,
    
    @SerializedName("propertyName")
    val propertyName: String
)

data class UpdateReviewRequest(
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("comment")
    val comment: String,
    
    @SerializedName("userName")
    val userName: String,
    
    @SerializedName("propertyName")
    val propertyName: String
)