// data/model/Publicite.kt
package com.sim.darna.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class Publicite(
    @Json(name = "_id") val _id: String? = null,
    val titre: String,
    val description: String,
    val type: String? = null,
    val pourcentageReduction: Int? = null,
    val imageUrl: String? = null,
    val dateDebut: Date? = null,
    val dateFin: Date? = null,
    val partenaire: Partenaire? = null,
    val codePromo: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
