package com.sim.darna.repository

import android.content.Context
import android.graphics.Bitmap
import com.sim.darna.auth.PropertyApi
import com.sim.darna.auth.RetrofitClient
import com.sim.darna.model.Property
import com.sim.darna.model.PropertyWithBookings
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.io.File
import java.io.FileOutputStream

class PropertyRepository(private val context: Context) {
    
    private val api: PropertyApi = RetrofitClient.propertyApi(context)
    
    fun getAllProperties(): Call<List<Property>> {
        return api.getAllProperties()
    }
    
    fun getPropertyById(id: String): Call<Property> {
        return api.getPropertyById(id)
    }
    
    /**
     * Create property with actual file upload (enables backend image verification)
     */
    fun createPropertyWithImageFile(
        title: String,
        description: String,
        price: Double,
        location: String,
        type: String,
        imageBitmap: Bitmap,  // Pass the actual bitmap instead of base64
        nbrCollocateurMax: Int,
        nbrCollocateurActuel: Int,
        startDate: String,
        endDate: String
    ): Call<Property> {
        // Convert bitmap to temporary file
        val imageFile = bitmapToFile(imageBitmap, "property_${System.currentTimeMillis()}.jpg")
        
        // Create RequestBody instances
        val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val locationBody = location.toRequestBody("text/plain".toMediaTypeOrNull())
        val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
        val nbrMaxBody = nbrCollocateurMax.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val nbrActuelBody = nbrCollocateurActuel.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val startDateBody = startDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val endDateBody = endDate.toRequestBody("text/plain".toMediaTypeOrNull())
        
        // Create MultipartBody.Part for the image
        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("images", imageFile.name, requestFile)
        
        return api.createPropertyWithFiles(
            title = titleBody,
            description = descriptionBody,
            price = priceBody,
            location = locationBody,
            type = typeBody,
            nbrCollocateurMax = nbrMaxBody,
            nbrCollocateurActuel = nbrActuelBody,
            startDate = startDateBody,
            endDate = endDateBody,
            images = listOf(imagePart)
        )
    }

    /**
     * Update property with image file upload
     */
    fun updatePropertyWithImageFile(
        id: String,
        title: String? = null,
        description: String? = null,
        price: Double? = null,
        location: String? = null,
        type: String? = null,
        imageBitmap: Bitmap? = null,  // Pass the actual bitmap instead of base64
        nbrCollocateurMax: Int? = null,
        nbrCollocateurActuel: Int? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Call<Property> {
        // Convert bitmap to temporary file if provided
        val imagePart = if (imageBitmap != null) {
            val imageFile = bitmapToFile(imageBitmap, "property_${System.currentTimeMillis()}.jpg")
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images", imageFile.name, requestFile)
        } else {
            null
        }
        
        // Create RequestBody instances for provided fields only
        val titleBody = title?.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceBody = price?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val locationBody = location?.toRequestBody("text/plain".toMediaTypeOrNull())
        val typeBody = type?.toRequestBody("text/plain".toMediaTypeOrNull())
        val nbrMaxBody = nbrCollocateurMax?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val nbrActuelBody = nbrCollocateurActuel?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val startDateBody = startDate?.toRequestBody("text/plain".toMediaTypeOrNull())
        val endDateBody = endDate?.toRequestBody("text/plain".toMediaTypeOrNull())
        
        return api.updatePropertyWithFiles(
            id = id,
            title = titleBody,
            description = descriptionBody,
            price = priceBody,
            location = locationBody,
            type = typeBody,
            nbrCollocateurMax = nbrMaxBody,
            nbrCollocateurActuel = nbrActuelBody,
            startDate = startDateBody,
            endDate = endDateBody,
            images = if (imagePart != null) listOf(imagePart) else null
        )
    }
    
    /**
     * Helper function to convert Bitmap to File
     */
    private fun bitmapToFile(bitmap: Bitmap, filename: String): File {
        val file = File(context.cacheDir, filename)
        file.createNewFile()
        
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
        fos.flush()
        fos.close()
        
        return file
    }
    
    // Keep the old base64 method for backward compatibility
    fun createProperty(
        title: String,
        description: String,
        price: Double,
        location: String,
        type: String,
        images: List<String>,
        nbrCollocateurMax: Int,
        nbrCollocateurActuel: Int,
        startDate: String,
        endDate: String
    ): Call<Property> {
        val request = com.sim.darna.auth.CreatePropertyRequest(
            title = title,
            description = description,
            price = price,
            location = location,
            type = type,
            images = images,
            nbrCollocateurMax = nbrCollocateurMax,
            nbrCollocateurActuel = nbrCollocateurActuel,
            startDate = startDate,
            endDate = endDate
        )
        return api.createProperty(request)
    }
    
    fun updateProperty(
        id: String,
        title: String? = null,
        description: String? = null,
        price: Double? = null,
        location: String? = null,
        type: String? = null,
        images: List<String>? = null,
        nbrCollocateurMax: Int? = null,
        nbrCollocateurActuel: Int? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Call<Property> {
        val request = com.sim.darna.auth.UpdatePropertyRequest(
            title = title,
            description = description,
            price = price,
            location = location,
            type = type,
            images = images,
            nbrCollocateurMax = nbrCollocateurMax,
            nbrCollocateurActuel = nbrCollocateurActuel,
            startDate = startDate,
            endDate = endDate
        )
        return api.updateProperty(id, request)
    }
    
    fun deleteProperty(id: String): Call<Unit> {
        return api.deleteProperty(id)
    }
    
    fun bookProperty(id: String, bookingStartDate: String): Call<Property> {
        val request = com.sim.darna.auth.BookPropertyRequest(bookingStartDate)
        return api.bookProperty(id, request)
    }
    
    fun getPropertyWithBookings(id: String): Call<PropertyWithBookings> {
        return api.getPropertyWithBookings(id)
    }
    
    fun respondToBooking(
        annonceId: String,
        bookingId: String,
        accept: Boolean
    ): Call<Property> {
        return api.respondToBooking(annonceId, bookingId, accept)
    }
}