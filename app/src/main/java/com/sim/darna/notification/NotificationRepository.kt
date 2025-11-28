package com.sim.darna.notification

class NotificationRepository(private val api: NotificationApi) {

    suspend fun getMyNotifications(): List<NotificationResponse> = api.getMyNotifications()

    suspend fun getNotificationById(id: String): NotificationResponse = api.getNotificationById(id)

    suspend fun createNotification(request: CreateNotificationRequest): NotificationResponse =
        api.createNotification(request)

    suspend fun markAsRead(id: String): NotificationResponse = api.markAsRead(id)

    suspend fun markAllAsRead(): List<NotificationResponse> = api.markAllAsRead()

    suspend fun hideNotification(id: String): NotificationResponse = api.hideNotification(id)

    suspend fun deleteNotification(id: String) {
        api.deleteNotification(id)
    }

    suspend fun getUnreadCount(): Int = api.getUnreadCount().count
}


