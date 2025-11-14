package com.sim.darna.repository

import com.sim.darna.api.RetrofitClient

class AnnonceRepository {
    suspend fun getAnnonces() = RetrofitClient.api.getAllAnnonces()
}
