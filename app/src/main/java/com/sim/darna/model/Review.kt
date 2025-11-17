package com.sim.darna.model


data class Review(
    val _id: String,
    val rating: Int,
    val comment: String,
    val user: User?,
    val createdAt: String
)

data class User(
    val _id: String,
    val username: String,
    val email: String
)
