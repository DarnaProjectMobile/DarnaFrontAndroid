package com.sim.darna.model

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("comment")
    val comment: String,
    
    @SerializedName("user")
    val userId: String,
    
    @SerializedName("property")
    val propertyId: String,
    
    @SerializedName("userName")
    val userName: String,
    
    @SerializedName("propertyName")
    val propertyName: String,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?,

    @SerializedName("visiteId")
    val visiteId: String?,

    @SerializedName("collectorRating")
    val collectorRating: Int?,

    @SerializedName("cleanlinessRating")
    val cleanlinessRating: Int?,

    @SerializedName("locationRating")
    val locationRating: Int?,

    @SerializedName("conformityRating")
    val conformityRating: Int?
)