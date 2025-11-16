package com.sim.darna.repository

import com.sim.darna.api.RetrofitClient
import com.sim.darna.model.Annonce
import com.sim.darna.model.CreateAnnonceRequest
import com.sim.darna.model.UpdateAnnonceRequest
import com.sim.darna.model.DeleteAnnonceResponse
import retrofit2.Response

class AnnonceRepository {

    private val api = RetrofitClient.api

    suspend fun getAnnonces(): List<Annonce> = api.getAllAnnonces()
    suspend fun getAnnonceById(id: String): Annonce = api.getAnnonceById(id)
    suspend fun createAnnonce(request: CreateAnnonceRequest): Annonce = api.createAnnonce(request)
    suspend fun updateAnnonce(id: String, request: UpdateAnnonceRequest): Annonce =
        api.updateAnnonce(id, request)

    suspend fun deleteAnnonce(id: String): DeleteAnnonceResponse = api.deleteAnnonce(id)
}
