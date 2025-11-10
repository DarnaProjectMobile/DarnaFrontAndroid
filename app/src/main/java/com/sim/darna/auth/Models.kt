package com.sim.darna.auth

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: String? = null,
    val username: String,
    val email: String,
    val role: String
)
