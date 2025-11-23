package com.sim.darna.auth

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val token: String,
    val user: UserDto
)

data class UserDto(
    @SerializedName("_id")
    val id: String? = null,
    val username: String,
    val email: String,
    val role: String
)
