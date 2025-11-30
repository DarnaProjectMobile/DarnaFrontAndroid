package com.sim.darna.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class Publicite(
    @SerialName("_id")
    private val _id: JsonElement? = null,

    val titre: String = "",
    val description: String = "",

    @SerialName("image")
    val imageUrl: String? = null,

    val type: PubliciteType = PubliciteType.PROMOTION,

    @SerialName("sponsor")
    private val _sponsor: JsonElement? = null,

    val sponsorName: String? = null,
    val sponsorLogo: String? = null,
    val categorie: Categorie? = null,
    val dateExpiration: String? = null,

    // Champs spécifiques selon le type
    val detailReduction: DetailReduction? = null,
    val detailPromotion: DetailPromotion? = null,
    val detailJeu: DetailJeu? = null,
    val qrCode: String? = null
) {
    // Extraire l'ID proprement
    val id: String?
        get() = try {
            when {
                _id == null -> null
                _id.toString().contains("\$oid") -> {
                    _id.jsonObject["\$oid"]?.jsonPrimitive?.content
                }
                else -> _id.jsonPrimitive.content
            }
        } catch (e: Exception) {
            null
        }

    // Extraire le sponsorId proprement
    val sponsorId: String
        get() = try {
            when {
                _sponsor == null -> ""
                _sponsor.toString().contains("\$oid") -> {
                    _sponsor.jsonObject["\$oid"]?.jsonPrimitive?.content ?: ""
                }
                else -> _sponsor.jsonPrimitive.content
            }
        } catch (e: Exception) {
            ""
        }
}

@Serializable(with = PubliciteTypeSerializer::class)
enum class PubliciteType {
    REDUCTION,
    PROMOTION,
    JEU
}

object PubliciteTypeSerializer : KSerializer<PubliciteType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("PubliciteType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PubliciteType {
        return when (decoder.decodeString().uppercase()) {
            "REDUCTION", "RÉDUCTION" -> PubliciteType.REDUCTION
            "PROMOTION", "PROMO", "BON PLAN", "BONPLAN", "OFFRE", "GOOD DEAL" -> PubliciteType.PROMOTION
            "JEU", "GAME" -> PubliciteType.JEU
            else -> PubliciteType.PROMOTION
        }
    }

    override fun serialize(encoder: Encoder, value: PubliciteType) {
        val stringValue = when (value) {
            PubliciteType.REDUCTION -> "reduction"
            PubliciteType.PROMOTION -> "promotion"
            PubliciteType.JEU -> "jeu"
        }
        encoder.encodeString(stringValue)
    }
}

@Serializable
enum class Categorie {
    TOUT,
    NOURRITURE,
    TECH,
    LOISIRS,
    VOYAGE,
    MODE,
    AUTRE
}

@Serializable
data class DetailReduction(
    val pourcentage: Int,
    val conditionsUtilisation: String
)

@Serializable
data class DetailPromotion(
    val offre: String,
    val conditions: String
)

@Serializable
data class DetailJeu(
    val description: String,
    val gains: List<String>
)