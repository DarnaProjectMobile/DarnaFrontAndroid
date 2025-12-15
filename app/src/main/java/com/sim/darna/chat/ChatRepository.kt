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

    // Nouvelles méthodes pour suppression, modification et statuts
    
    // Supprimer un message (soft delete)
    suspend fun deleteMessage(messageId: String): MessageResponse =
        api.deleteMessage(messageId)

    // Modifier le contenu d'un message
    suspend fun updateMessage(messageId: String, newContent: String): MessageResponse =
        api.updateMessage(messageId, UpdateMessageRequest(newContent))

    // Mettre à jour le statut d'un message
    suspend fun updateMessageStatus(messageId: String, status: String): MessageResponse =
        api.updateMessageStatus(messageId, UpdateStatusRequest(status))

    // Ajouter ou retirer une réaction à un message
    suspend fun toggleReaction(messageId: String, emoji: String): MessageResponse =
        api.toggleReaction(messageId, ReactionRequest(emoji))
}






