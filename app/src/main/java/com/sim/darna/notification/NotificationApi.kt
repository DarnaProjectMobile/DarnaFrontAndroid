package com.sim.darna.notification

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface NotificationApi {
    @GET("notification/my-notifications")
    suspend fun getMyNotifications(): List<NotificationResponse>

    @GET("notification/{id}")
    suspend fun getNotificationById(@Path("id") id: String): NotificationResponse

    @POST("notification")
    suspend fun createNotification(@Body body: CreateNotificationRequest): NotificationResponse

    @PUT("notification/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): NotificationResponse

    @PUT("notification/read-all")
    suspend fun markAllAsRead(): List<NotificationResponse>

    @PUT("notification/{id}/hide")
    suspend fun hideNotification(@Path("id") id: String): NotificationResponse

    @DELETE("notification/{id}")
    suspend fun deleteNotification(@Path("id") id: String)

    @GET("notification/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse
}

data class NotificationResponse(
    @SerializedName("_id")
    val id: String?,
    val userId: String?,
    val type: String?, // "visite_accepted", "visite_rejected", "visite_reserved", "visite_reminder"
    val title: String?,
    val message: String?,
    val visiteId: String?,
    val logementId: String?,
    val logementTitle: String?,
    val read: Boolean?,
    val hidden: Boolean? = false, // Pour masquer les notifications
    val createdAt: String?,
    val scheduledFor: String? // Pour les rappels programmés
)

data class CreateNotificationRequest(
    val userId: String,
    val type: String,
    val title: String,
    val message: String,
    val visiteId: String? = null,
    val logementId: String? = null,
    val logementTitle: String? = null,
    val scheduledFor: String? = null // ISO date string pour les rappels
)

data class UnreadCountResponse(
    val count: Int
)


