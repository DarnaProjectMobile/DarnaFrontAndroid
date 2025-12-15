package com.sim.darna.auth

data class ResetPasswordRequest(
    val code: String,
    val newPassword: String,
    val confirmPassword: String
)
