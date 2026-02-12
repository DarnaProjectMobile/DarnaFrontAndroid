package com.sim.darna.model

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("propertyId")
    val propertyId: String,
    
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("comment")
    val comment: String,
    
    @SerializedName("userName")
    val userName: String,
    
    @SerializedName("propertyName")
    val propertyName: String,
    
    @SerializedName("date")
    val date: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    
    @SerializedName("visiteId")
    val visiteId: String? = null,
    
    @SerializedName("logementId")
    val logementId: String? = null,
    
    @SerializedName("collectorId")
    val collectorId: String? = null
)

