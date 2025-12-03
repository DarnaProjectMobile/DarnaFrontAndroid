package com.sim.darna.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone

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
        viewModel.markAllAsRead(visiteId)
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
                                modifier = Modifier.fillMaxWidth()
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
}

@Composable
private fun MessageBubble(
    message: MessageResponse,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isCurrentUser) AppColors.primary else Color(0xFFE5E7EB)
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
            
            // Images
            message.images?.forEach { imageUrl ->
                if (imageUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val imageUrlFull = if (imageUrl.startsWith("http")) {
                        imageUrl
                    } else {
                        "http://10.0.2.2:3000$imageUrl" // Use 10.0.2.2 for Android emulator
                    }
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrlFull),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // Text content
            if (!message.content.isNullOrBlank()) {
                if (message.images?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    color = if (isCurrentUser) Color.White else AppColors.textPrimary,
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatMessageTime(message.createdAt),
                    fontSize = 11.sp,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else AppColors.textSecondary
                )
                if (isCurrentUser) {
                    if (message.read == true) {
                        Text(
                            text = "✓✓",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = "✓",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
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

