package com.sim.darna.model

import com.google.gson.annotations.SerializedName

data class ReviewRequest(
    @SerializedName("property")
    val propertyId: String,
    
    val rating: Int,
    
    val comment: String,
    
    val userName: String? = null,
    
    val propertyName: String? = null,
    
    val visiteId: String? = null,
    
    val logementId: String? = null,
    
    val collectorId: String? = null
)

