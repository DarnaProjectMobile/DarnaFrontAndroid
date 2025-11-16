package com.sim.darna.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

data class Annonce(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    @JsonAdapter(UserAdapter::class)
    val user: AnnonceUser,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

data class AnnonceUser(
    @SerializedName("_id")
    val id: String,
    val username: String? = null,
    val email: String? = null
)

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

data class CreateAnnonceRequest(
    val title: String,
    val description: String,
    val price: Double
)

data class UpdateAnnonceRequest(
    val title: String,
    val description: String,
    val price: Double
)

data class DeleteAnnonceResponse(
    val message: String
)
