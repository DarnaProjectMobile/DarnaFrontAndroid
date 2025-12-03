package com.sim.darna.firebase

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface FirebaseNotificationApi {
    @POST("notifications-firebase/register-token")
    suspend fun registerToken(@Body body: RegisterTokenRequest): RegisterTokenResponse

    @GET("notifications-firebase")
    suspend fun getMyNotifications(): List<FirebaseNotificationResponse>

    @PATCH("notifications-firebase/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): MarkAsReadResponse

    @DELETE("notifications-firebase/{id}")
    suspend fun deleteNotification(@Path("id") id: String): MarkAsReadResponse
}

data class RegisterTokenRequest(
    val fcmToken: String,
    val platform: String // "ANDROID", "IOS", "WEB"
)

data class RegisterTokenResponse(
    val success: Boolean
)

data class FirebaseNotificationResponse(
    @SerializedName("id")
    val id: String?,
    @SerializedName("userId")
    val userId: String?,
    @SerializedName("type")
    val type: String?, // NotificationType enum
    @SerializedName("title")
    val title: String?,
    @SerializedName("body")
    val body: String?,
    @SerializedName("visitId")
    val visitId: String?,
    @SerializedName("housingId")
    val housingId: String?,
    @SerializedName("role")
    val role: String?, // "CLIENT" or "COLLECTOR"
    @SerializedName("isRead")
    val isRead: Boolean?,
    @SerializedName("sentBy")
    val sentBy: String?, // "CLIENT" or "COLLECTOR"
    @SerializedName("createdAt")
    val createdAt: String? // ISO date string
)

data class MarkAsReadResponse(
    val success: Boolean
)















