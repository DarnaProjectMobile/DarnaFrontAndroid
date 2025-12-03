package com.sim.darna.firebase

class FirebaseNotificationRepository(private val api: FirebaseNotificationApi) {

    suspend fun registerToken(request: RegisterTokenRequest): RegisterTokenResponse =
        api.registerToken(request)

    suspend fun getMyNotifications(): List<FirebaseNotificationResponse> =
        api.getMyNotifications()

    suspend fun markAsRead(id: String): MarkAsReadResponse =
        api.markAsRead(id)

    suspend fun deleteNotification(id: String): MarkAsReadResponse =
        api.deleteNotification(id)
}















