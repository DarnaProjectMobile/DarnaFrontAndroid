package com.sim.darna.api

import com.sim.darna.model.Annonce
import retrofit2.http.GET

interface ApiService {

    @GET("annonces")
    suspend fun getAllAnnonces(): List<Annonce>
}
