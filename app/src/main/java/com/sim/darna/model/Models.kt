package com.sim.darna.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token") val token: String,
    val user: UserDto
)

data class UserDto(

    @SerializedName("_id")
    val id: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("image")
    val image: String? = null,

    @SerializedName("dateDeNaissance")
    val dateDeNaissance: String? = null,

    @SerializedName("numTel")
    val numTel: String? = null,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("credits")
    val credits: Int? = null,

    @SerializedName("ratingAvg")
    val ratingAvg: Double? = null,

    @SerializedName("isVerified")
    val isVerified: Boolean? = false,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
