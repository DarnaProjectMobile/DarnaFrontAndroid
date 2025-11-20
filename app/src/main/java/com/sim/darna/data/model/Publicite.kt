package com.sim.darna.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Publicite(
    val id: String? = null,
    val titre: String,
    val description: String,
    val imageUrl: String? = null,
    val type: PubliciteType,
    val sponsorId: String = "",
    val sponsorName: String? = null,
    val sponsorLogo: String? = null,
    val categorie: Categorie? = null,
    val dateExpiration: String? = null,
    // Champs spécifiques selon le type
    val detailReduction: DetailReduction? = null,
    val detailPromotion: DetailPromotion? = null,
    val detailJeu: DetailJeu? = null,
    val qrCode: String? = null // QR Code généré côté backend pour les réductions
)

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
    val gains: List<String> // Liste des gains possibles pour la roulette
)

