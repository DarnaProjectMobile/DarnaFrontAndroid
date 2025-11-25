package com.sim.darna.repository

import android.content.Context
import com.sim.darna.auth.PropertyApi
import com.sim.darna.auth.RetrofitClient
import com.sim.darna.model.Property
import com.sim.darna.model.PropertyWithBookings
import retrofit2.Call

class PropertyRepository(private val context: Context) {
    
    private val api: PropertyApi = RetrofitClient.propertyApi(context)
    
    fun getAllProperties(): Call<List<Property>> {
        return api.getAllProperties()
    }
    
    fun getPropertyById(id: String): Call<Property> {
        return api.getPropertyById(id)
    }
    
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
}

