package com.sim.darna.auth

import com.sim.darna.model.Property
import com.sim.darna.model.PropertyWithBookings
import retrofit2.Call
import retrofit2.http.*

interface PropertyApi {
    
    @GET("annonces")
    fun getAllProperties(): Call<List<Property>>
    
    @GET("annonces/{id}")
    fun getPropertyById(@Path("id") id: String): Call<Property>
    
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

