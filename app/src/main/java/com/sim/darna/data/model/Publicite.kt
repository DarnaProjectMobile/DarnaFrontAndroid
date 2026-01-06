// data/model/Publicite.kt
package com.sim.darna.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

enum class PubliciteType {
    REDUCTION,
    PROMOTION,
    JEU
}

@JsonClass(generateAdapter = true)
data class DetailReduction(
    val pourcentage: Int? = null,
    val conditionsUtilisation: String? = null
)

@JsonClass(generateAdapter = true)
data class DetailPromotion(
    val offre: String? = null,
    val conditions: String? = null
)

@JsonClass(generateAdapter = true)
data class DetailJeu(
    val description: String? = null,
    val gains: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class Sponsor(
    @Json(name = "_id") val _id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val role: String? = null,
    val image: String? = null
)

@JsonClass(generateAdapter = true)
data class Publicite(
    @Json(name = "_id") val _id: String? = null,
    val titre: String,
    val description: String,
    val type: String? = null, // REDUCTION, PROMOTION, JEU
    val image: String? = null,
    val imageUrl: String? = null, // Alias pour compatibilité
    val details: String? = null,
    val coupon: String? = null,
    val qrCode: String? = null, // base64 image
    val statut: String? = null, // EN_ATTENTE, PUBLIEE, REJETEE
    val paymentDate: Date? = null,
    val categorie: String? = null,
    val dateExpiration: String? = null,
    val detailReduction: DetailReduction? = null,
    val detailPromotion: DetailPromotion? = null,
    val detailJeu: DetailJeu? = null,
    val sponsor: Sponsor? = null,
    val sponsorId: String? = null,
    val sponsorName: String? = null,
    val sponsorLogo: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
