package com.sim.darna.chat

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ChatRepository(private val api: ChatApi) {

    suspend fun sendMessage(visiteId: String, content: String?, images: List<String>? = null): MessageResponse {
        val visiteIdBody = visiteId.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentBody = content?.toRequestBody("text/plain".toMediaTypeOrNull())
        return api.sendMessage(visiteIdBody, contentBody, null)
    }
    
    suspend fun sendMessageWithImages(
        visiteId: String, 
        content: String?, 
        imageParts: List<MultipartBody.Part>
    ): MessageResponse {
        val visiteIdBody = visiteId.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentBody = content?.toRequestBody("text/plain".toMediaTypeOrNull())
        return api.sendMessage(visiteIdBody, contentBody, imageParts)
    }

    suspend fun getMessages(visiteId: String): List<MessageResponse> =
        api.getMessages(visiteId)

    suspend fun markAsRead(messageId: String): MessageResponse =
        api.markAsRead(messageId)

    suspend fun markAllAsRead(visiteId: String): MarkReadResponse =
        api.markAllAsRead(visiteId)

    suspend fun getUnreadCount(): Int =
        api.getUnreadCount().unreadCount ?: 0

    suspend fun uploadImages(images: List<MultipartBody.Part>): List<String> =
        api.uploadImages(images).images ?: emptyList()
}





