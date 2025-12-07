package com.sim.darna.data.repository

import android.net.Uri
import com.sim.darna.auth.TokenStorage
import com.sim.darna.data.remote.ImageUploadResponse
import com.sim.darna.data.remote.PubliciteUploadService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class PubliciteUploadRepository @Inject constructor(
    private val api: PubliciteUploadService
) {
    suspend fun uploadImage(context: android.content.Context, imageUri: Uri): Response<ImageUploadResponse> {
        val token = TokenStorage.getToken(context) ?: throw Exception("Non authentifié")
        
        // Convertir Uri en File
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
        
        return api.uploadImage("Bearer $token", imagePart)
    }
}

