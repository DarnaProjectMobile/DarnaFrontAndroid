package com.sim.darna.data.repository

import com.sim.darna.auth.UserSessionManager
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.remote.PubliciteApi
import retrofit2.Response
import javax.inject.Inject

class PubliciteRepository @Inject constructor(
    private val api: PubliciteApi
) {
    suspend fun getAll(): Response<List<Publicite>> = api.getAll()
    suspend fun getOne(id: String): Response<Publicite> = api.getOne(id)

    suspend fun create(payload: Map<String, Any>): Response<Publicite> {
        val token = UserSessionManager.currentToken ?: throw Exception("Non authentifié")
        return api.create("Bearer $token", payload)
    }

    suspend fun update(id: String, payload: Map<String, Any>): Response<Publicite> {
        val token = UserSessionManager.currentToken ?: throw Exception("Non authentifié")
        return api.update("Bearer $token", id, payload)
    }

    suspend fun delete(id: String): Response<Unit> {
        val token = UserSessionManager.currentToken ?: throw Exception("Non authentifié")
        return api.delete("Bearer $token", id)
    }
}
