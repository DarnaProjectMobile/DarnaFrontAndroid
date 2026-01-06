package com.sim.darna.auth

import com.sim.darna.model.Property
import com.sim.darna.model.PropertyWithBookings
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface PropertyApi {
    
    @GET("annonces")
    fun getAllProperties(): Call<List<Property>>
    
    @GET("annonces/{id}")
    fun getPropertyById(@Path("id") id: String): Call<Property>
    
    // Multipart support for file uploads with image verification
    @Multipart
    @POST("annonces")
    fun createPropertyWithFiles(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("location") location: RequestBody,
        @Part("type") type: RequestBody,
        @Part("nbrCollocateurMax") nbrCollocateurMax: RequestBody,
        @Part("nbrCollocateurActuel") nbrCollocateurActuel: RequestBody,
        @Part("startDate") startDate: RequestBody,
        @Part("endDate") endDate: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Call<Property>
    
    // Multipart support for updating property with file uploads
    @Multipart
    @PATCH("annonces/{id}")
    fun updatePropertyWithFiles(
        @Path("id") id: String,
        @Part("title") title: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("location") location: RequestBody? = null,
        @Part("type") type: RequestBody? = null,
        @Part("nbrCollocateurMax") nbrCollocateurMax: RequestBody? = null,
        @Part("nbrCollocateurActuel") nbrCollocateurActuel: RequestBody? = null,
        @Part("startDate") startDate: RequestBody? = null,
        @Part("endDate") endDate: RequestBody? = null,
        @Part images: List<MultipartBody.Part>? = null
    ): Call<Property>
    
    // Keep the old JSON-based endpoint for backward compatibility
    @POST("annonces")
    fun createProperty(@Body property: CreatePropertyRequest): Call<Property>
    
    @PATCH("annonces/{id}")
    fun updateProperty(
        @Path("id") id: String,
        @Body property: UpdatePropertyRequest
    ): Call<Property>
    
    @DELETE("annonces/{id}")
    fun deleteProperty(@Path("id") id: String): Call<Unit>
    
    @POST("annonces/{id}/book")
    fun bookProperty(
        @Path("id") id: String,
        @Body booking: BookPropertyRequest
    ): Call<Property>
    
    @GET("annonces/{id}")
    fun getPropertyWithBookings(@Path("id") id: String): Call<PropertyWithBookings>
    
    @POST("annonces/{annonceId}/booking/{bookingId}/respond")
    fun respondToBooking(
        @Path("annonceId") annonceId: String,
        @Path("bookingId") bookingId: String,
        @Query("accept") accept: Boolean
    ): Call<Property>
}

data class CreatePropertyRequest(
    val title: String,
    val description: String,
    val price: Double,
    val location: String,
    val type: String,
    val images: List<String>,
    val nbrCollocateurMax: Int,
    val nbrCollocateurActuel: Int,
    val startDate: String, // ISO date string
    val endDate: String // ISO date string
)

data class UpdatePropertyRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val location: String? = null,
    val type: String? = null,
    val images: List<String>? = null,
    val nbrCollocateurMax: Int? = null,
    val nbrCollocateurActuel: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

data class BookPropertyRequest(
    val bookingStartDate: String // ISO date string
)