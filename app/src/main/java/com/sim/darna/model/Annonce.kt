package com.sim.darna.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

// Main Annonce data class
data class Annonce(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val image: String?,
    val type: String?,
    val location: String?,
    val startDate: String?,
    val endDate: String?,
    @JsonAdapter(UserAdapter::class)
    val user: AnnonceUser,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

// Nested user info
data class AnnonceUser(
    @SerializedName("_id")
    val id: String,
    val username: String? = null,
    val email: String? = null
)

// Adapter to handle user JSON deserialization
class UserAdapter : JsonDeserializer<AnnonceUser> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AnnonceUser {
        return when {
            json?.isJsonObject == true -> {
                val obj = json.asJsonObject
                AnnonceUser(
                    id = obj.get("_id")?.asString ?: obj.get("id")?.asString ?: "",
                    username = obj.get("username")?.asString,
                    email = obj.get("email")?.asString
                )
            }
            json?.isJsonPrimitive == true -> {
                AnnonceUser(id = json.asString)
            }
            else -> AnnonceUser(id = "")
        }
    }
}

// Request for creating an annonce
data class CreateAnnonceRequest(
    val title: String,
    val description: String,
    val price: Double,
    val image: String,
    val type: String,
    val location: String,
    val startDate: String,
    val endDate: String
)

// Request for updating an annonce
data class UpdateAnnonceRequest(
    val title: String,
    val description: String,
    val price: Double,
    val image: String,
    val type: String,
    val location: String,
    val startDate: String,
    val endDate: String
)

// Response for deleting an annonce
data class DeleteAnnonceResponse(
    val message: String
)
