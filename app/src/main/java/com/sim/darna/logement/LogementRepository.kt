package com.sim.darna.logement

class LogementRepository(private val api: LogementApi) {
    suspend fun getAllLogements(): List<LogementResponse> = api.getAllLogements()
    
    suspend fun getMyLogements(): List<LogementResponse> = api.getMyLogements()
    
    suspend fun getLogementById(id: String): LogementResponse = api.getLogementById(id)
    
    suspend fun getLogementByAnnonceId(annonceId: String): LogementResponse = api.getLogementByAnnonceId(annonceId)
    
    suspend fun createLogement(request: CreateLogementRequest): LogementResponse = api.createLogement(request)
    
    suspend fun updateLogement(id: String, request: UpdateLogementRequest): LogementResponse = api.updateLogement(id, request)
    
    suspend fun deleteLogement(id: String) = api.deleteLogement(id)
}






