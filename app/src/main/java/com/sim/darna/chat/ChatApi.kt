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

    // Nouveaux endpoints pour suppression, modification et statuts
    
    // Supprimer un message (soft delete - marqué comme supprimé)
    @DELETE("chat/message/{messageId}")
    suspend fun deleteMessage(@Path("messageId") messageId: String): MessageResponse

    // Modifier le contenu d'un message
    @PATCH("chat/message/{messageId}")
    suspend fun updateMessage(
        @Path("messageId") messageId: String,
        @Body request: UpdateMessageRequest
    ): MessageResponse

    // Mettre à jour le statut d'un message (sent, delivered, read)
    @PATCH("chat/message/{messageId}/status")
    suspend fun updateMessageStatus(
        @Path("messageId") messageId: String,
        @Body request: UpdateStatusRequest
    ): MessageResponse

    // Ajouter ou retirer une réaction à un message
    @POST("chat/message/{messageId}/reaction")
    suspend fun toggleReaction(
        @Path("messageId") messageId: String,
        @Body request: ReactionRequest
    ): MessageResponse
}

data class SendMessageRequest(
    val visiteId: String,
    val content: String? = null,
    val images: List<String>? = null
)

// Modèle de réponse pour un message
// Nouveaux champs ajoutés pour les fonctionnalités avancées :
// - isDeleted : indique si le message a été supprimé
// - isEdited : indique si le message a été modifié
// - editedAt : date de dernière modification
// - status : statut du message ("sent", "delivered", "read")
// - deliveredAt : date de réception par le destinataire
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
    val receiverName: String? = null,
    // Nouveaux champs pour suppression/modification/statuts
    val isDeleted: Boolean? = false,
    val isEdited: Boolean? = false,
    val editedAt: String? = null,
    val status: String? = "sent", // "sent", "delivered", "read"
    val deliveredAt: String? = null,
    // Réactions aux messages (emoji -> liste d'IDs utilisateurs)
    val reactions: Map<String, List<String>>? = null
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

// Requête pour modifier un message
data class UpdateMessageRequest(
    val content: String
)

// Requête pour mettre à jour le statut d'un message
data class UpdateStatusRequest(
    val status: String // "sent", "delivered", "read"
)

// Requête pour ajouter/retirer une réaction
data class ReactionRequest(
    val emoji: String // L'emoji de la réaction (ex: "👍", "❤️", "😂")
)






