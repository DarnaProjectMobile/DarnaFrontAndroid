package com.sim.darna.model

import com.google.gson.annotations.SerializedName

data class ReviewSummary(
    @SerializedName("summary")
    val summary: String,
    
    @SerializedName("pros")
    val pros: List<String>,
    
    @SerializedName("cons")
    val cons: List<String>,
    
    @SerializedName("sentimentScore")
    val sentimentScore: Double, // -1 to 1
    
    @SerializedName("commonThemes")
    val commonThemes: List<String>,
    
    @SerializedName("improvements")
    val improvements: List<String>
)
