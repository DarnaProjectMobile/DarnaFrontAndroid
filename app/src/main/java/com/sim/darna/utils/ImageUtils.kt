package com.sim.darna.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for handling image-related operations
 */
object ImageUtils {

    /**
     * Converts an image URI to a MultipartBody.Part for upload
     *
     * @param context The application context
     * @param imageUri The URI of the image to convert
     * @param fieldName The field name to use in the multipart request (defaults to "image")
     * @return MultipartBody.Part ready for upload, or null if conversion fails
     */
    fun imageUriToMultipartPart(context: Context, imageUri: Uri, fieldName: String = "image"): MultipartBody.Part? {
        return try {
            val file = getFileFromUri(context, imageUri) ?: return null
            
            // Detect the actual MIME type of the file
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            
            // Determine file extension based on MIME type
            val extension = when {
                mimeType.contains("jpeg") || mimeType.contains("jpg") -> ".jpg"
                mimeType.contains("png") -> ".png"
                mimeType.contains("gif") -> ".gif"
                mimeType.contains("webp") -> ".webp"
                else -> ".jpg" // Default fallback
            }
            
            // Create the request body with proper MIME type
            val mediaType = mimeType.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()
            val requestFile = file.asRequestBody(mediaType)
            
            // Create the multipart part with the specified field name
            MultipartBody.Part.createFormData(fieldName, file.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Converts an image URI to a File object
     *
     * @param context The application context
     * @param uri The URI of the image to convert
     * @return File object containing the image data, or null if conversion fails
     */
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val extension = getExtensionFromUri(uri, context)
            val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}$extension")
            val outputStream = FileOutputStream(tempFile)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Gets the appropriate file extension from the URI or MIME type
     */
    private fun getExtensionFromUri(uri: Uri, context: Context): String {
        // Try to get MIME type from content resolver
        val mimeType = context.contentResolver.getType(uri)
        
        return when {
            mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> ".jpg"
            mimeType?.contains("png") == true -> ".png"
            mimeType?.contains("gif") == true -> ".gif"
            mimeType?.contains("webp") == true -> ".webp"
            else -> {
                // Fallback: extract extension from URI if MIME type isn't available
                val fileName = uri.lastPathSegment ?: "image"
                if (fileName.contains(".")) {
                    "." + fileName.substringAfterLast(".")
                } else {
                    ".jpg" // Default fallback
                }
            }
        }
    }

    /**
     * Checks if the given string represents a URL (starts with http or https)
     */
    fun isImageUrl(imageString: String?): Boolean {
        return imageString?.startsWith("http") == true
    }

    /**
     * Builds a complete image URL using the base URL if the image string is not already a full URL
     */
    fun buildFullImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrEmpty()) return null
        
        if (isImageUrl(imagePath)) {
            return imagePath
        }
        
        // Check if it's a relative path that needs to be appended to base URL
        return if (imagePath.startsWith("/")) {
            "${ApiConfig.BASE_URL}uploads/users$imagePath"
        } else {
            "${ApiConfig.BASE_URL}uploads/users/$imagePath"
        }
    }
}