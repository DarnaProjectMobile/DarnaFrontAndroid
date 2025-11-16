package com.sim.darna.api

import com.sim.darna.model.Annonce
import com.sim.darna.model.CreateAnnonceRequest
import com.sim.darna.model.UpdateAnnonceRequest
import com.sim.darna.model.DeleteAnnonceResponse
import retrofit2.http.*

interface ApiService {

    @GET("annonces")
    suspend fun getAllAnnonces(): List<Annonce>

    @GET("annonces/{id}")
    suspend fun getAnnonceById(@Path("id") id: String): Annonce

    @POST("annonces")
    suspend fun createAnnonce(@Body request: CreateAnnonceRequest): Annonce

    @PUT("annonces/{id}")
    suspend fun updateAnnonce(@Path("id") id: String, @Body request: UpdateAnnonceRequest): Annonce

    @DELETE("annonces/{id}")
    suspend fun deleteAnnonce(@Path("id") id: String): DeleteAnnonceResponse
}
