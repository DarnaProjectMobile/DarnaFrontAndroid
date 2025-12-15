package com.sim.darna.data.repository

import android.net.Uri
import android.webkit.MimeTypeMap
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
        
        // Détecter le type MIME réel de l'image
        val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
        
        // Déterminer l'extension du fichier basée sur le type MIME
        val extension = when {
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
            mimeType.contains("png") -> ".png"
            mimeType.contains("gif") -> ".gif"
            mimeType.contains("webp") -> ".webp"
            else -> ".jpg" // Par défaut
        }
        
        // Convertir Uri en File avec la bonne extension
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}$extension")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        // Utiliser le type MIME réel au lieu de "image/*"
        val mediaType = mimeType.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()
        val requestFile = file.asRequestBody(mediaType)
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
        
        return api.uploadImage("Bearer $token", imagePart)
    }
}

