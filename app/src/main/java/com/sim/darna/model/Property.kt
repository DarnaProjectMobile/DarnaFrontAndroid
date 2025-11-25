package com.sim.darna.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import java.util.Date

// Data class to hold user info when populated
data class PropertyUser(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("email")
    val email: String? = null
)

// Custom deserializer to handle user as either String or Object
class UserDeserializer : JsonDeserializer<String?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String? {
        if (json == null || json.isJsonNull) return null
        
        return try {
            when {
                json.isJsonPrimitive -> {
                    val primitive = json.asJsonPrimitive
                    when {
                        primitive.isString -> primitive.asString
                        primitive.isNumber -> primitive.asString // Handle numeric IDs
                        else -> null
                    }
                }
                json.isJsonObject -> {
                    // User is an object, extract _id and store username
                    val obj = json.asJsonObject
                    val id = when {
                        obj.has("_id") -> {
                            val idElement = obj.get("_id")
                            when {
                                idElement.isJsonPrimitive && idElement.asJsonPrimitive.isString -> 
                                    idElement.asString
                                idElement.isJsonObject && idElement.asJsonObject.has("\$oid") ->
                                    idElement.asJsonObject.get("\$oid").asString
                                else -> idElement.toString().replace("\"", "")
                            }
                        }
                        obj.has("id") -> {
                            val idElement = obj.get("id")
                            if (idElement.isJsonPrimitive && idElement.asJsonPrimitive.isString) {
                                idElement.asString
                            } else {
                                idElement.toString().replace("\"", "")
                            }
                        }
                        else -> null
                    }
                    
                    id
                }
                else -> null
            }
        } catch (e: Exception) {
            // Fallback: try to get string representation
            json.toString().replace("\"", "")
        }
    }
}

// Custom deserializer for PropertyUser to extract username
class PropertyUserDeserializer : JsonDeserializer<PropertyUser?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PropertyUser? {
        if (json == null || json.isJsonNull) return null
        
        return try {
            when {
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    val id = when {
                        obj.has("_id") -> {
                            val idElement = obj.get("_id")
                            when {
                                idElement.isJsonPrimitive && idElement.asJsonPrimitive.isString -> 
                                    idElement.asString
                                else -> null
                            }
                        }
                        else -> null
                    }
                    PropertyUser(
                        id = id,
                        username = obj.get("username")?.asString,
                        email = obj.get("email")?.asString
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

data class Property(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("location")
    val location: String?,
    
    @SerializedName("type")
    val type: String?,
    
    @SerializedName("images")
    val images: List<String>?,
    
    @SerializedName("image")
    val image: String? = null, // For single image (first image from array)
    
    @SerializedName("nbrCollocateurMax")
    val nbrCollocateurMax: Int?,
    
    @SerializedName("nbrCollocateurActuel")
    val nbrCollocateurActuel: Int?,
    
    @SerializedName("startDate")
    val startDate: String?, // ISO date string
    
    @SerializedName("endDate")
    val endDate: String?, // ISO date string
    
    @SerializedName("user")
    val user: String?, // User ID (can be populated as object, but we extract ID via PropertyTypeAdapter)
    
    // Owner username (extracted from populated user object)
    val ownerUsername: String? = null,
    
    @SerializedName("bookings")
    val bookings: List<Booking>? = null,
    
    // Additional fields from iOS model
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("calmLevel")
    val calmLevel: String = "",
    
    @SerializedName("calmLevelDescription")
    val calmLevelDescription: String = "",
    
    @SerializedName("lifestyle")
    val lifestyle: String = "",
    
    @SerializedName("lifestyleDescription")
    val lifestyleDescription: String = "",
    
    @SerializedName("homeEnergy")
    val homeEnergy: String = "",
    
    @SerializedName("homeEnergyDescription")
    val homeEnergyDescription: String = "",
    
    @SerializedName("ownerName")
    val ownerName: String? = null
) {
    // Helper function to get the first image
    fun getFirstImage(): String? {
        return image ?: images?.firstOrNull()
    }
}

// Custom deserializer for BookingUser to handle both string ID and populated object
class BookingUserDeserializer : JsonDeserializer<BookingUser?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BookingUser? {
        if (json == null || json.isJsonNull) return null
        
        return try {
            when {
                json.isJsonPrimitive -> {
                    // User is a string (ObjectId), create BookingUser with just id
                    val id = if (json.asJsonPrimitive.isString) {
                        json.asString
                    } else {
                        json.asString
                    }
                    BookingUser(id = id)
                }
                json.isJsonObject -> {
                    // User is populated as object, deserialize normally
                    val obj = json.asJsonObject
                    BookingUser(
                        id = obj.get("_id")?.asString,
                        username = obj.get("username")?.asString,
                        email = obj.get("email")?.asString,
                        phone = obj.get("numTel")?.asString,
                        gender = obj.get("gender")?.asString,
                        dateOfBirth = obj.get("dateOfBirth")?.asString,
                        dateDeNaissance = obj.get("dateDeNaissance")?.asString
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

data class Booking(
    @SerializedName("user")
    @JsonAdapter(BookingUserDeserializer::class)
    val user: BookingUser? = null,
    
    @SerializedName("bookingStartDate")
    val bookingStartDate: String? = null // ISO date string
)

data class BookingUser(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("numTel")
    val phone: String? = null,
    
    @SerializedName("gender")
    val gender: String? = null,
    
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    
    @SerializedName("dateDeNaissance")
    val dateDeNaissance: String? = null
)

data class PropertyWithBookings(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("bookings")
    val bookings: List<Booking>
)

