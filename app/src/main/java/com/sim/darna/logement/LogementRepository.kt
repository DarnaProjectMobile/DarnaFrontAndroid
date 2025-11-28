package com.sim.darna.logement

class LogementRepository(private val api: LogementApi) {
    suspend fun getAllLogements(): List<LogementResponse> = api.getAllLogements()
}






