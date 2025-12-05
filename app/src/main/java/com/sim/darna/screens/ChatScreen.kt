package com.sim.darna.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImage
import com.sim.darna.chat.ChatViewModel
import com.sim.darna.chat.MessageResponse
import com.sim.darna.ui.components.AppColors
import com.sim.darna.ui.components.AppSpacing
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

// État de chargement des images
sealed class ImageLoadState {
    object Loading : ImageLoadState()
    data class Error(val throwable: Throwable?) : ImageLoadState()
    object Success : ImageLoadState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    visiteId: String,
    visiteTitle: String,
    currentUserId: String,
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.state.collectAsState().value
    var messageText by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // États pour la modification de message
    var messageToEdit by remember { mutableStateOf<Pair<String, String>?>(null) } // (messageId, content)
    var editDialogText by remember { mutableStateOf("") }
    var showReactionPicker by remember { mutableStateOf<String?>(null) } // messageId pour lequel on ajoute une réaction

    // Launcher pour sélectionner des images depuis la galerie
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImages = uris
        }
    }

    // Permission launcher pour accéder aux images (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            Toast.makeText(context, "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(visiteId) {
        viewModel.loadMessages(visiteId)
        
        // Marquer tous les messages reçus comme "delivered" si ce n'est pas déjà fait
        uiState.messages.filter { 
            it.receiverId == currentUserId && it.status == "sent" 
        }.forEach { message ->
            message.id?.let { viewModel.updateMessageStatus(it, "delivered") }
        }
        
        // Marquer tous les messages comme lus
        viewModel.markAllAsRead(visiteId)
        
        // Mettre à jour le statut à "read" pour tous les messages reçus
        uiState.messages.filter { 
            it.receiverId == currentUserId && it.status != "read" 
        }.forEach { message ->
            message.id?.let { viewModel.updateMessageStatus(it, "read") }
        }
    }

    // Nettoyer quand on quitte l'écran
    DisposableEffect(visiteId) {
        onDispose {
            viewModel.leaveVisite(visiteId)
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = visiteTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Chat de visite",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.primary
                )
            )
        },
        containerColor = AppColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(AppColors.background)
            ) {
                if (uiState.isLoading && uiState.messages.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AppColors.primary
                    )
                } else if (uiState.messages.isEmpty()) {
                    EmptyChatState()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        items(uiState.messages) { message ->
                            MessageBubble(
                                message = message,
                                isCurrentUser = message.senderId == currentUserId,
                                baseUrl = viewModel.baseUrl,
                                modifier = Modifier.fillMaxWidth(),
                                onEditMessage = { messageId, content ->
                                    // Ouvrir le dialog de modification
                                    messageToEdit = Pair(messageId, content)
                                    editDialogText = content
                                },
                                onDeleteMessage = { messageId ->
                                    // Supprimer le message
                                    viewModel.deleteMessage(messageId)
                                },
                                onReactionClick = { messageId, emoji ->
                                    // Si l'emoji est vide, c'est qu'on veut ouvrir le picker
                                    // Sinon c'est un toggle direct (clic sur une réaction existante)
                                    if (emoji.isEmpty()) {
                                        showReactionPicker = messageId
                                    } else {
                                        viewModel.toggleReaction(messageId, emoji)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Selected Images Preview
            if (selectedImages.isNotEmpty()) {
                SelectedImagesPreview(
                    images = selectedImages,
                    onRemoveImage = { index ->
                        selectedImages = selectedImages.filterIndexed { i, _ -> i != index }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Input Area
            MessageInputArea(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank() || selectedImages.isNotEmpty()) {
                        scope.launch {
                            try {
                                // Send message with text and/or images
                                if (selectedImages.isNotEmpty()) {
                                    val imageParts = selectedImages.mapIndexed { index, uri ->
                                        val file = uriToFile(context, uri, "chat_image_$index.jpg")
                                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                        MultipartBody.Part.createFormData("images", file.name, requestFile)
                                    }
                                    viewModel.sendMessageWithImages(
                                        visiteId,
                                        messageText.takeIf { it.isNotBlank() },
                                        imageParts
                                    )
                                } else {
                                    viewModel.sendMessage(
                                        visiteId, 
                                        messageText.takeIf { it.isNotBlank() },
                                        null
                                    )
                                }
                                messageText = ""
                                selectedImages = emptyList()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                onImageClick = {
                    // Try to launch image picker directly (works on Android 13+)
                    try {
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } catch (e: Exception) {
                        // Fallback: request permission first (for older Android versions)
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                },
                isSending = uiState.isSending,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Dialog de modification de message
    messageToEdit?.let { (messageId, _) ->
        MessageEditDialog(
            currentText = editDialogText,
            onTextChange = { editDialogText = it },
            onDismiss = {
                messageToEdit = null
                editDialogText = ""
            },
            onConfirm = {
                if (editDialogText.isNotBlank()) {
                    viewModel.editMessage(messageId, editDialogText)
                    messageToEdit = null
                    editDialogText = ""
                }
            }
        )
    }

    // Sélecteur de réactions
    showReactionPicker?.let { messageId ->
        ReactionPicker(
            onReactionSelected = { emoji ->
                viewModel.toggleReaction(messageId, emoji)
            },
            onDismiss = { showReactionPicker = null }
        )
    }
}

// Dialog pour modifier un message
@Composable
private fun MessageEditDialog(
    currentText: String,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Modifier le message",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = currentText,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tapez votre message...") },
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = currentText.isNotBlank()
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: MessageResponse,
    isCurrentUser: Boolean,
    baseUrl: String,
    modifier: Modifier = Modifier,
    onEditMessage: ((String, String) -> Unit)? = null,
    onDeleteMessage: ((String) -> Unit)? = null,
    onReactionClick: ((String, String) -> Unit)? = null
) {
    // État pour afficher le menu contextuel
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier,
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Box {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        // Afficher en gris si le message est supprimé
                        if (message.isDeleted == true) {
                            Color(0xFFBDBDBD)
                        } else if (isCurrentUser) {
                            AppColors.primary
                        } else {
                            Color(0xFFE5E7EB)
                        }
                    )
                    // Appui long pour afficher le menu contextuel (seulement pour les messages de l'utilisateur)
                    .then(
                        // Appui long pour afficher le menu contextuel (pour tous les messages non supprimés)
                        if (message.isDeleted != true) {
                            Modifier.combinedClickable(
                                onClick = { },
                                onLongClick = { showMenu = true }
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
            ) {
                if (!isCurrentUser && message.senderName != null) {
                    Text(
                        text = message.senderName ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentUser) Color.White else AppColors.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                // Afficher les images seulement si le message n'est pas supprimé
                if (message.isDeleted != true && message.images != null && message.images.isNotEmpty()) {
                    Log.d("ChatScreen", "📸 Message has ${message.images.size} image(s)")
                    message.images.forEachIndexed { index, imageUrl ->
                        if (imageUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Construire l'URL complète en utilisant le baseUrl passé en paramètre
                            val imageUrlFull = if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                                // Nettoyer l'URL pour enlever tous les espaces et caractères invalides
                                imageUrl.trim()
                                    .replace(" ", "")
                                    .replace(Regex("\\s+"), "")
                                    .replace("\n", "")
                                    .replace("\r", "")
                            } else {
                                // Nettoyer l'imageUrl pour enlever tous les espaces
                                val cleanImageUrl = imageUrl.trim()
                                    .replace(" ", "")
                                    .replace(Regex("\\s+"), "")
                                    .replace("chat /", "chat/")
                                    .replace("chat  /", "chat/")
                                    .replace("\n", "")
                                    .replace("\r", "")
                                // Utiliser le baseUrl et enlever le trailing slash s'il existe
                                val normalizedBaseUrl = baseUrl.removeSuffix("/")
                                // S'assurer que cleanImageUrl commence par /
                                val path = if (cleanImageUrl.startsWith("/")) cleanImageUrl else "/$cleanImageUrl"
                                val fullUrl = "$normalizedBaseUrl$path"
                                Log.d("ChatScreen", "🔗 Constructed URL: $fullUrl")
                                Log.d("ChatScreen", "   - baseUrl: $baseUrl")
                                Log.d("ChatScreen", "   - imageUrl original: $imageUrl")
                                Log.d("ChatScreen", "   - imageUrl cleaned: $cleanImageUrl")
                                fullUrl
                            }
                            
                            // Debug log
                            Log.d("ChatScreen", "🖼️ Loading image $index from URL: $imageUrlFull")
                            
                            // Utiliser Box pour gérer l'état de chargement avec fallback
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                var imageLoadState by remember { mutableStateOf<ImageLoadState>(ImageLoadState.Loading) }
                                
                                LaunchedEffect(imageUrlFull) {
                                    // Reset state when URL changes
                                    imageLoadState = ImageLoadState.Loading
                                }
                                
                                when (imageLoadState) {
                                    is ImageLoadState.Loading -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = AppColors.primary,
                                            strokeWidth = 3.dp
                                        )
                                    }
                                    is ImageLoadState.Error -> {
                                        // Afficher un placeholder en cas d'erreur avec possibilité de réessayer
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = "Image non disponible",
                                                modifier = Modifier.size(48.dp),
                                                tint = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Image non disponible",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            TextButton(
                                                onClick = { imageLoadState = ImageLoadState.Loading }
                                            ) {
                                                Text(
                                                    text = "Réessayer",
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                    is ImageLoadState.Success -> {
                                        // Image chargée avec succès - sera géré par AsyncImage
                                    }
                                }
                                
                                // Toujours essayer de charger l'image
                                AsyncImage(
                                    model = imageUrlFull,
                                    contentDescription = "Image du message",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onError = { error ->
                                        imageLoadState = ImageLoadState.Error(error.result.throwable)
                                        Log.e("ChatScreen", "❌ Error loading image: $imageUrlFull")
                                        Log.e("ChatScreen", "Error: ${error.result.throwable?.message}")
                                        Log.e("ChatScreen", "Throwable: ${error.result.throwable}")
                                    },
                                    onSuccess = {
                                        imageLoadState = ImageLoadState.Success
                                        Log.d("ChatScreen", "✅ Image loaded successfully: $imageUrlFull")
                                    },
                                    onLoading = {
                                        imageLoadState = ImageLoadState.Loading
                                    }
                                )
                            }
                        } else {
                            Log.w("ChatScreen", "⚠️ Empty image URL at index $index")
                        }
                    }
                } else if (message.isDeleted != true) {
                    Log.d("ChatScreen", "📸 Message has no images (images: ${message.images})")
                }
                
                // Text content
                if (!message.content.isNullOrBlank()) {
                    if (message.images?.isNotEmpty() == true && message.isDeleted != true) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = message.content,
                            fontSize = 15.sp,
                            color = if (message.isDeleted == true) {
                                Color.White.copy(alpha = 0.8f)
                            } else if (isCurrentUser) {
                                Color.White
                            } else {
                                AppColors.textPrimary
                            },
                            lineHeight = 20.sp,
                            fontStyle = if (message.isDeleted == true) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                        )
                        
                        // Afficher l'indicateur "(modifié)" si le message a été édité
                        if (message.isEdited == true && message.isDeleted != true) {
                            Text(
                                text = "(modifié)",
                                fontSize = 10.sp,
                                color = if (isCurrentUser) Color.White.copy(alpha = 0.6f) else AppColors.textSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Heure et indicateurs de statut
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatMessageTime(message.createdAt).toString(),
                        fontSize = 11.sp,
                        color = if (message.isDeleted == true) {
                            Color.White.copy(alpha = 0.6f)
                        } else if (isCurrentUser) {
                            Color.White.copy(alpha = 0.7f)
                        } else {
                            AppColors.textSecondary
                        }
                    )
                    
                    // Indicateurs de statut pour les messages de l'utilisateur
                    if (isCurrentUser && message.isDeleted != true) {
                        when (message.status ?: "sent") {
                            "read" -> {
                                // Message lu - double coche bleue
                                Text(
                                    text = "✓✓",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4FC3F7) // Bleu clair
                                )
                            }
                            "delivered" -> {
                                // Message reçu - double coche grise
                                Text(
                                    text = "✓✓",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            else -> {
                                // Message envoyé - simple coche
                                Text(
                                    text = "✓",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                

            }

            // Afficher les réactions Style Messenger (en dehors de la bulle, chevauchement en bas)
            if (!message.reactions.isNullOrEmpty()) {
                MessageReactions(
                    reactions = message.reactions,
                    currentUserId = if (isCurrentUser) message.senderId ?: "" else message.receiverId ?: "",
                    onReactionClick = { emoji ->
                        onReactionClick?.invoke(message.id ?: "", emoji)
                    },
                    modifier = Modifier
                        .align(if (isCurrentUser) Alignment.BottomEnd else Alignment.BottomStart)
                        .offset(y = 10.dp, x = if (isCurrentUser) (-10).dp else 10.dp) // Chevauchement
                        .zIndex(1f) // Au premier plan
                )
            }
            
            // Menu contextuel (autorisé pour tous les messages non supprimés)
            if (showMenu && message.isDeleted != true) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // Option "Réagir"
                    if (onReactionClick != null) {
                        DropdownMenuItem(
                            text = { Text("Réagir") },
                            onClick = {
                                showMenu = false
                                message.id?.let { onReactionClick(it, "") }
                            },
                            leadingIcon = {
                                Text("😀", fontSize = 18.sp)
                            }
                        )
                    }

                    // Option "Modifier" seulement pour les messages texte sans images de l'utilisateur courant
                    if (isCurrentUser && message.type == "text" && message.images.isNullOrEmpty() && onEditMessage != null) {
                        DropdownMenuItem(
                            text = { Text("Modifier") },
                            onClick = {
                                showMenu = false
                                message.id?.let { id ->
                                    message.content?.let { content ->
                                        onEditMessage(id, content)
                                    }
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = "Modifier")
                            }
                        )
                    }
                    
                    // Option "Supprimer" seulement pour l'utilisateur courant
                    if (isCurrentUser && onDeleteMessage != null) {
                        DropdownMenuItem(
                            text = { Text("Supprimer") },
                            onClick = {
                                showMenu = false
                                message.id?.let { onDeleteMessage(it) }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                            }
                        )
                    }
                }
            }
        }
        
        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}


@Composable
private fun MessageInputArea(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageClick: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image button
            IconButton(
                onClick = onImageClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Ajouter une image",
                    tint = AppColors.primary
                )
            }
            
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Tapez un message...",
                        color = AppColors.textSecondary
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider
                ),
                maxLines = 4,
                enabled = !isSending
            )
            
            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier.size(56.dp),
                containerColor = AppColors.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Envoyer",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "💬",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(
            text = "Aucun message",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = "Commencez la conversation",
            fontSize = 14.sp,
            color = AppColors.textSecondary
        )
    }
}

@Composable
private fun SelectedImagesPreview(
    images: List<Uri>,
    onRemoveImage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            images.forEachIndexed { index, uri ->
                Box(
                    modifier = Modifier.size(80.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = "Image sélectionnée",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { onRemoveImage(index) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Retirer",
                            tint = Color.White,
                            modifier = Modifier
                                .background(Color.Red, CircleShape)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun uriToFile(context: android.content.Context, uri: Uri, fileName: String): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, fileName)
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return file
}

private fun formatMessageTime(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(dateString) ?: return ""
        
        val now = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { time = date }
        
        val diffInMillis = now.timeInMillis - messageDate.timeInMillis
        val diffInMinutes = diffInMillis / (1000 * 60)
        
        when {
            diffInMinutes < 1 -> "À l'instant"
            diffInMinutes < 60 -> "Il y a ${diffInMinutes}min"
            diffInMinutes < 1440 -> {
                val hours = diffInMinutes / 60
                "Il y a ${hours}h"
            }
            else -> {
                val displayFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                displayFormat.format(date)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessageReactions(
    reactions: Map<String, List<String>>?,
    currentUserId: String,
    onReactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (reactions.isNullOrEmpty()) return
    
    FlowRow(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.forEach { (emoji, userIds) ->
            val hasReacted = userIds.contains(currentUserId)
            val count = userIds.size
            
            Surface(
                onClick = { onReactionClick(emoji) },
                shape = RoundedCornerShape(12.dp),
                // Couleurs plus visibles
                color = if (hasReacted) AppColors.primary.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, if (hasReacted) AppColors.primary else Color.Gray.copy(alpha = 0.3f)),
                modifier = Modifier.height(28.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = emoji,
                        fontSize = 14.sp,
                        color = Color.Black // Force la couleur noire pour l'emoji (parfois transparent par défaut)
                    )
                    if (count > 1) {
                        Text(
                            text = count.toString(),
                            fontSize = 11.sp,
                            color = if (hasReacted) AppColors.primary else Color.Gray,
                            fontWeight = if (hasReacted) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReactionPicker(
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val commonEmojis = listOf(
        "👍", "❤️", "😂", "😮", "😢", "🙏",
        "🎉", "🔥", "👏", "✨", "💯", "🚀"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ajouter une réaction",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(commonEmojis) { emoji ->
                    Surface(
                        onClick = {
                            onReactionSelected(emoji)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Gray.copy(alpha = 0.1f),
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}


