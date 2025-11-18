package com.sim.darna.model

data class Report(
    val _id: String,
    val reason: String,
    val details: String,
    val user: String?,          // or User? if you already have a User model
    val createdAt: String? = null,
    val updatedAt: String? = null
)
