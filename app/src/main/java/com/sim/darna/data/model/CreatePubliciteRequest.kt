package com.sim.darna.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePubliciteRequest(
    val titre: String,
    val description: String,
    val image: String,
    val type: PubliciteType,
    val categorie: Categorie,
    val dateExpiration: String? = null,
    val detailReduction: DetailReduction? = null,
    val detailPromotion: DetailPromotion? = null,
    val detailJeu: DetailJeu? = null
)

@Serializable
data class UpdatePubliciteRequest(
    val titre: String,
    val description: String,
    val image: String,
    val type: PubliciteType,
    val categorie: Categorie,
    val dateExpiration: String? = null,
    val detailReduction: DetailReduction? = null,
    val detailPromotion: DetailPromotion? = null,
    val detailJeu: DetailJeu? = null
)

