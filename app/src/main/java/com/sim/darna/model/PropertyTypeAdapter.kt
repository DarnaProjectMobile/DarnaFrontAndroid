package com.sim.darna.model

import com.google.gson.*
import java.lang.reflect.Type

class PropertyTypeAdapter : JsonDeserializer<Property> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Property? {
        if (json == null || !json.isJsonObject) return null
        
        val obj = json.asJsonObject
        val gson = Gson()
        
        // Extract user info
        var userId: String? = null
        var ownerUsername: String? = null
        
        val userElement = obj.get("user")
        if (userElement != null && !userElement.isJsonNull) {
            when {
                userElement.isJsonPrimitive -> {
                    userId = userElement.asString
                }
                userElement.isJsonObject -> {
                    val userObj = userElement.asJsonObject
                    userId = userObj.get("_id")?.asString
                    ownerUsername = userObj.get("username")?.asString
                }
            }
        }
        
        // Deserialize bookings
        val bookingsList = obj.get("bookings")?.asJsonArray?.mapNotNull { bookingElement ->
            try {
                gson.fromJson(bookingElement, Booking::class.java)
            } catch (e: Exception) {
                null
            }
        }
        
        // Deserialize images
        val imagesList = obj.get("images")?.asJsonArray?.mapNotNull { 
            if (it.isJsonPrimitive) it.asString else null
        }
        
        // Deserialize tags
        val tagsList = obj.get("tags")?.asJsonArray?.mapNotNull { 
            if (it.isJsonPrimitive) it.asString else null
        } ?: emptyList()
        
        // Manually construct Property with all required fields
        // Fields with default values will use their defaults if not provided
        return Property(
            id = obj.get("_id")?.asString ?: "",
            title = obj.get("title")?.asString ?: "",
            description = obj.get("description")?.asString,
            price = obj.get("price")?.asDouble ?: 0.0,
            location = obj.get("location")?.asString,
            type = obj.get("type")?.asString,
            images = imagesList,
            image = obj.get("image")?.asString,
            nbrCollocateurMax = obj.get("nbrCollocateurMax")?.asInt,
            nbrCollocateurActuel = obj.get("nbrCollocateurActuel")?.asInt,
            startDate = obj.get("startDate")?.asString,
            endDate = obj.get("endDate")?.asString,
            user = userId,
            ownerUsername = ownerUsername,
            bookings = bookingsList
        )
    }
}

