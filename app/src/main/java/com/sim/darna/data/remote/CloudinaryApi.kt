package com.sim.darna.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CloudinaryApi {
    @Multipart
    @POST("cloudinary/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<CloudinaryResponse>
}

@JsonClass(generateAdapter = true)
data class CloudinaryResponse(
    @Json(name = "url")
    val url: String,
    
    @Json(name = "publicId")
    val publicId: String
)
