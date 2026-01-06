package com.sim.darna.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sim.darna.chat.ChatViewModel
import com.sim.darna.chat.MessageResponse
import com.sim.darna.factory.ChatVmFactory
import com.sim.darna.ui.components.AppColors
import com.sim.darna.ui.components.AppRadius
import com.sim.darna.ui.components.AppSpacing
import com.sim.darna.utils.ApiConfig
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    visiteId: String,
    title: String
) {
    val context = LocalContext.current
    val viewModel: ChatViewModel = viewModel(
        factory = ChatVmFactory(ApiConfig.BASE_URL, context)
    )
    val uiState by viewModel.state.collectAsState()
    val userId = com.sim.darna.auth.TokenStorage.getUserId(context) ?: ""

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Charger les messages au démarrage
    LaunchedEffect(visiteId) {
        viewModel.loadMessages(visiteId)
    }

    // Scroll automatique vers le bas quand de nouveaux messages arrivent
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Gestion des erreurs
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    // Sélecteur d'images
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.sendImages(visiteId, uris, context)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                title = title,
                isConnected = uiState.isConnected,
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = AppColors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Liste des messages
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading && uiState.messages.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.primary)
                    }
                } else if (uiState.messages.isEmpty()) {
                    EmptyChatState()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.messages, key = { it.id ?: UUID.randomUUID().toString() }) { message ->
                            MessageItem(
                                message = message,
                                isMe = message.senderId == userId,
                                baseUrl = viewModel.baseUrl,
                                onReaction = { emoji -> message.id?.let { viewModel.toggleReaction(it, emoji) } },
                                onDelete = { message.id?.let { viewModel.deleteMessage(it) } }
                            )
                        }
                    }
                }
            }

            // Input bar
            ChatInputBar(
                text = messageText,
                onTextChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(visiteId, messageText)
                        messageText = ""
                    }
                },
                onAttach = { imageLauncher.launch("image/*") },
                isSending = uiState.isSending
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    title: String,
    isConnected: Boolean,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "En ligne" else "Hors ligne",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.surface,
            titleContentColor = AppColors.textPrimary
        ),
        modifier = Modifier.shadow(4.dp)
    )
}

@Composable
fun EmptyChatState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = AppColors.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = AppColors.primary
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Commencez la discussion",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )
        Text(
            "Échangez des messages et des photos\npour organiser votre visite.",
            fontSize = 14.sp,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: MessageResponse,
    isMe: Boolean,
    baseUrl: String,
    onReaction: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        val bubbleColor = if (isMe) AppColors.primary else AppColors.surface
        val contentColor = if (isMe) Color.White else AppColors.textPrimary

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!isMe) {
                Avatar(name = message.senderName ?: "?")
                Spacer(Modifier.width(8.dp))
            }

            Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                // Image(s) si présentes
                message.images?.forEach { imgPath ->
                    val fullUrl = if (imgPath.startsWith("http")) imgPath else "${baseUrl.removeSuffix("/")}/$imgPath"
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = "Image message",
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .sizeIn(maxWidth = 240.dp, maxHeight = 320.dp)
                            .clip(RoundedCornerShape(AppRadius.md))
                            .border(1.dp, AppColors.divider, RoundedCornerShape(AppRadius.md)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Bulle de texte
                if (!message.content.isNullOrBlank()) {
                    Surface(
                        color = bubbleColor,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 0.dp,
                            bottomEnd = if (isMe) 0.dp else 16.dp
                        ),
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { if (isMe && message.isDeleted != true) showMenu = true }
                            )
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = message.content,
                                color = contentColor,
                                fontSize = 15.sp,
                                style = if (message.isDeleted == true) androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) else androidx.compose.ui.text.TextStyle.Default
                            )

                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatTime(message.createdAt),
                                    fontSize = 10.sp,
                                    color = contentColor.copy(alpha = 0.7f)
                                )
                                if (isMe) {
                                    Spacer(Modifier.width(4.dp))
                                    MessageStatusIcon(message.status ?: "sent", contentColor.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }

                // Réactions
                if (!message.reactions.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        message.reactions.forEach { (emoji, users) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AppColors.surface,
                                border = BorderStroke(1.dp, AppColors.divider),
                                modifier = Modifier.clickable { onReaction(emoji) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 12.sp)
                                    if (users.size > 1) {
                                        Spacer(Modifier.width(2.dp))
                                        Text(users.size.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Supprimer", color = AppColors.danger) },
                onClick = {
                    onDelete()
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AppColors.danger) }
            )
        }
    }
}

@Composable
fun Avatar(name: String) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = AppColors.primary.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.take(1).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.primary
            )
        }
    }
}

@Composable
fun MessageStatusIcon(status: String, tint: Color) {
    val icon = when (status) {
        "delivered" -> Icons.Default.DoneAll
        "read" -> Icons.Default.DoneAll
        else -> Icons.Default.Done
    }
    val finalTint = if (status == "read") Color(0xFF2196F3) else tint
    Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = finalTint)
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    isSending: Boolean
) {
    Surface(
        color = AppColors.surface,
        modifier = Modifier.shadow(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttach) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Attacher", tint = AppColors.primary)
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                placeholder = { Text("Écrivez un message...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    unfocusedBorderColor = AppColors.divider
                )
            )

            FloatingActionButton(
                onClick = onSend,
                modifier = Modifier
                    .size(48.dp)
                    .padding(start = 4.dp),
                shape = CircleShape,
                containerColor = if (text.isNotBlank()) AppColors.primary else AppColors.divider,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Envoyer")
                }
            }
        }
    }
}

private fun formatTime(isoDate: String?): String {
    if (isoDate == null) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoDate)
        val outSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        outSdf.format(date ?: Date())
    } catch (e: Exception) {
        ""
    }
}