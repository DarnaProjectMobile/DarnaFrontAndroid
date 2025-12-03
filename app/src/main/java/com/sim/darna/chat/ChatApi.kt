package com.sim.darna.chat

import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.*

interface ChatApi {
    @Multipart
    @POST("chat/message")
    suspend fun sendMessage(
        @Part("visiteId") visiteId: RequestBody,
        @Part("content") content: RequestBody?,
        @Part images: List<MultipartBody.Part>?
    ): MessageResponse

    @GET("chat/visite/{visiteId}/messages")
    suspend fun getMessages(@Path("visiteId") visiteId: String): List<MessageResponse>

    @PATCH("chat/message/{messageId}/read")
    suspend fun markAsRead(@Path("messageId") messageId: String): MessageResponse

    @POST("chat/visite/{visiteId}/mark-all-read")
    suspend fun markAllAsRead(@Path("visiteId") visiteId: String): MarkReadResponse

    @GET("chat/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @Multipart
    @POST("chat/upload-images")
    suspend fun uploadImages(
        @Part images: List<MultipartBody.Part>
    ): UploadImagesResponse
}

data class SendMessageRequest(
    val visiteId: String,
    val content: String? = null,
    val images: List<String>? = null
)

data class MessageResponse(
    @SerializedName("_id")
    val id: String?,
    val visiteId: String?,
    val senderId: String?,
    val receiverId: String?,
    val content: String?,
    val images: List<String>? = null,
    val type: String? = "text",
    val read: Boolean?,
    val readAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val senderName: String? = null,
    val receiverName: String? = null
)

data class MarkReadResponse(
    val success: Boolean?,
    val message: String?
)

data class UnreadCountResponse(
    val unreadCount: Int?
)

data class UploadImagesResponse(
    val images: List<String>?
)





