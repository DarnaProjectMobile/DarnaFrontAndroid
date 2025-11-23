package com.sim.darna.visite

class VisiteRepository(private val api: VisiteApi) {
    suspend fun createVisite(request: CreateVisiteRequest): VisiteResponse = api.createVisite(request)

    suspend fun getMyVisites(): List<VisiteResponse> = api.getMyVisites()

    suspend fun getMyLogementsVisites(): List<VisiteResponse> = api.getMyLogementsVisites()

    suspend fun updateVisite(id: String, body: UpdateVisiteRequest): VisiteResponse =
        api.updateVisite(id, body)

    suspend fun updateStatus(id: String, status: String): VisiteResponse =
        api.updateStatus(id, UpdateStatusRequest(status))

    suspend fun acceptVisite(id: String): VisiteResponse = api.acceptVisite(id)

    suspend fun rejectVisite(id: String): VisiteResponse = api.rejectVisite(id)

    suspend fun deleteVisite(id: String) {
        api.deleteVisite(id)
    }
}
