package com.sim.darna.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PubliciteUploadService {
    @Multipart
    @POST("publicites/upload-image")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>
}

data class ImageUploadResponse(
    val message: String? = null,
    val imageUrl: String? = null,
    val filename: String? = null,
    val error: String? = null
)

