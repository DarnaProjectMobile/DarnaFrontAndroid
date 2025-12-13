package com.sim.darna.data.repository

import android.content.Context
import com.sim.darna.auth.TokenStorage
import com.sim.darna.auth.UserSessionManager
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.model.QRCodeVerificationResponse
import com.sim.darna.data.remote.PubliciteApi
import retrofit2.Response
import javax.inject.Inject

class PubliciteRepository @Inject constructor(
    private val api: PubliciteApi
) {
    suspend fun getAll(): Response<List<Publicite>> = api.getAll()
    suspend fun getOne(id: String): Response<Publicite> = api.getOne(id)

    suspend fun create(context: Context, payload: Map<String, Any>): Response<Publicite> {
        // Essayer d'abord UserSessionManager, puis TokenStorage
        val token = UserSessionManager.currentToken 
            ?: TokenStorage.getToken(context) 
            ?: throw Exception("Non authentifié")
        return api.create("Bearer $token", payload)
    }

    suspend fun update(context: Context, id: String, payload: Map<String, Any>): Response<Publicite> {
        // Essayer d'abord UserSessionManager, puis TokenStorage
        val token = UserSessionManager.currentToken 
            ?: TokenStorage.getToken(context) 
            ?: throw Exception("Non authentifié")
        return api.update("Bearer $token", id, payload)
    }

    suspend fun delete(context: Context, id: String): Response<Unit> {
        // Essayer d'abord UserSessionManager, puis TokenStorage
        val token = UserSessionManager.currentToken 
            ?: TokenStorage.getToken(context) 
            ?: throw Exception("Non authentifié")
        return api.delete("Bearer $token", id)
    }
    
    suspend fun verifyQRCode(context: Context, qrData: String): Response<QRCodeVerificationResponse> {
        val token = UserSessionManager.currentToken 
            ?: TokenStorage.getToken(context) 
            ?: throw Exception("Non authentifié")
        return api.verifyQRCode("Bearer $token", mapOf("qrData" to qrData))
    }
}
