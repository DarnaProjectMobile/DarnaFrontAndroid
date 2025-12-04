package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.firebase.FirebaseNotificationViewModel
import com.sim.darna.firebase.FirebaseNotificationUiState
import com.sim.darna.firebase.FirebaseNotificationResponse
import com.sim.darna.ui.components.AppColors
import com.sim.darna.ui.components.AppRadius
import com.sim.darna.ui.components.AppSpacing
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: androidx.navigation.NavController,
    viewModel: FirebaseNotificationViewModel,
    parentNavController: androidx.navigation.NavController? = null
) {
    val uiState by viewModel.state.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    var showUnreadOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        android.util.Log.d("NotificationsScreen", "Chargement des notifications...")
        viewModel.loadNotifications()
        viewModel.loadUnreadCount()
    }
    
    LaunchedEffect(navController.currentBackStackEntry?.id) {
        android.util.Log.d("NotificationsScreen", "Rafraîchissement des notifications...")
        viewModel.loadNotifications()
        viewModel.loadUnreadCount()
    }

    val filteredNotifications = remember(uiState.notifications, showUnreadOnly) {
        if (showUnreadOnly) {
            uiState.notifications.filter { it.isRead != true }
        } else {
            uiState.notifications
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.background,
                        AppColors.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header moderne
            ModernNotificationHeader(
                unreadCount = unreadCount,
                onBackClick = { navController.popBackStack() }
            )

            when {
                uiState.isLoading -> {
                    ModernLoadingState()
                }
                uiState.error != null -> {
                    uiState.error?.let { errorMessage ->
                        ModernErrorState(
                            message = errorMessage,
                            onRetry = { viewModel.loadNotifications() }
                        )
                    }
                }
                filteredNotifications.isEmpty() -> {
                    ModernEmptyState(
                        showUnreadOnly = showUnreadOnly,
                        onRefresh = { viewModel.loadNotifications() }
                    )
                }
                else -> {
                    android.util.Log.d("NotificationsScreen", "Affichage de ${filteredNotifications.size} notification(s)")
                    ModernNotificationList(
                        notifications = filteredNotifications,
                        onNotificationClick = { notification ->
                            try {
                                android.util.Log.d("NotificationsScreen", "Clic sur notification: ${notification.id}")
                                
                                if (notification.isRead != true) {
                                    notification.id?.let { id ->
                                        viewModel.markAsRead(id)
                                    }
                                }
                                
                                if (notification.type == "NEW_MESSAGE" && !notification.visitId.isNullOrBlank()) {
                                    try {
                                        notification.id?.let { id ->
                                            viewModel.markAsRead(id)
                                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                                kotlinx.coroutines.delay(500)
                                                viewModel.deleteNotification(id)
                                            }
                                        }
                                        val visiteId = notification.visitId
                                        val visiteTitle = notification.title?.replace("Nouveau message de ", "")?.takeIf { it.isNotBlank() } 
                                            ?: "Chat"
                                        val encodedTitle = java.net.URLEncoder.encode(visiteTitle, "UTF-8")
                                        android.util.Log.d("NotificationsScreen", "Navigation vers chat/$visiteId/$encodedTitle")
                                        val targetNavController = parentNavController ?: navController
                                        targetNavController.navigate("chat/$visiteId/$encodedTitle") {
                                            launchSingleTop = true
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("NotificationsScreen", "Erreur navigation chat", e)
                                    }
                                } else {
                                    notification.id?.let { id ->
                                        if (id.isNotEmpty()) {
                                            try {
                                                android.util.Log.d("NotificationsScreen", "Navigation vers notification_detail/$id")
                                                navController.navigate("notification_detail/$id") {
                                                    launchSingleTop = true
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("NotificationsScreen", "Erreur navigation", e)
                                                try {
                                                    val encodedId = java.net.URLEncoder.encode(id, "UTF-8")
                                                    navController.navigate("notification_detail/$encodedId") {
                                                        launchSingleTop = true
                                                    }
                                                } catch (e2: Exception) {
                                                    android.util.Log.e("NotificationsScreen", "Erreur encodage", e2)
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("NotificationsScreen", "Erreur clic notification", e)
                            }
                        },
                        onMarkAsRead = { notification ->
                            notification.id?.let { id ->
                                viewModel.markAsRead(id)
                            }
                        },
                        onDelete = { notification ->
                            notification.id?.let { id ->
                                viewModel.deleteNotification(id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernNotificationHeader(
    unreadCount: Int,
    onBackClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.md)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .alpha(alpha)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.primary.copy(alpha = 0.1f),
                            AppColors.secondary.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(AppRadius.lg)
                )
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onBackClick),
                shape = CircleShape,
                color = AppColors.surface
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = AppColors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = "Notifications",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.textPrimary
                )
                if (unreadCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.danger,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Text(
                            text = "$unreadCount non lue${if (unreadCount > 1) "s" else ""}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            CircularProgressIndicator(
                color = AppColors.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Chargement...",
                fontSize = 14.sp,
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
private fun ModernEmptyState(
    showUnreadOnly: Boolean,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            modifier = Modifier.padding(AppSpacing.xl)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_animation"
            )
            
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                shape = CircleShape,
                color = AppColors.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = AppColors.primary
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Text(
                    text = if (showUnreadOnly) "Aucune notification non lue" else "Aucune notification",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Text(
                    text = if (showUnreadOnly) 
                        "Toutes vos notifications ont été lues" 
                    else 
                        "Vous n'avez pas encore de notifications",
                    fontSize = 14.sp,
                    color = AppColors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ModernErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            modifier = Modifier.padding(AppSpacing.xl)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = AppColors.danger.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = AppColors.danger
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Text(
                    text = "Erreur",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = AppColors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary
                ),
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(AppSpacing.xs))
                Text("Réessayer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ModernNotificationList(
    notifications: List<FirebaseNotificationResponse>,
    onNotificationClick: (FirebaseNotificationResponse) -> Unit,
    onMarkAsRead: (FirebaseNotificationResponse) -> Unit,
    onDelete: (FirebaseNotificationResponse) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        itemsIndexed(
            items = notifications,
            key = { _, notification -> notification.id ?: "" }
        ) { index, notification ->
            AnimatedNotificationCard(
                notification = notification,
                index = index,
                onClick = { onNotificationClick(notification) },
                onMarkAsRead = { onMarkAsRead(notification) },
                onDelete = { onDelete(notification) }
            )
        }
    }
}

@Composable
private fun AnimatedNotificationCard(
    notification: FirebaseNotificationResponse,
    index: Int,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay((index * 50).toLong())
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(400)) + 
                slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + 
                scaleIn(initialScale = 0.9f, animationSpec = tween(400)),
        exit = fadeOut() + slideOutVertically() + scaleOut()
    ) {
        SwipeableNotificationCard(
            notification = notification,
            onClick = onClick,
            onMarkAsRead = onMarkAsRead,
            onDelete = onDelete
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNotificationCard(
    notification: FirebaseNotificationResponse,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isRemoved by remember { mutableStateOf(false) }
    
    // État du swipe (Nouvelle API Material 3)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe vers la droite -> Marquer comme lu
                    if (notification.isRead != true) {
                        onMarkAsRead()
                    }
                    false // On ne supprime pas, on revient juste à l'état initial
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe vers la gauche -> Supprimer
                    isRemoved = true
                    onDelete()
                    true
                }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.25f }
    )

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 300),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val color by animateColorAsState(
                    when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF34C759) // Vert iOS (Marquer lu)
                        SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF3B30) // Rouge iOS (Supprimer)
                    }, label = "swipe_color"
                )
                
                val alignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                }
                
                val icon = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                    SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                    else -> Icons.Default.Check
                }
                
                val scale by animateFloatAsState(
                    if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                    label = "icon_scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(AppRadius.lg))
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.scale(scale),
                        tint = Color.White
                    )
                }
            },
            content = {
                ModernNotificationCard(
                    notification = notification,
                    onClick = onClick
                )
            }
        )
    }
}

@Composable
private fun ModernNotificationCard(
    notification: FirebaseNotificationResponse,
    onClick: () -> Unit
) {
    val isRead = notification.isRead ?: false
    val type = notification.type ?: "info"
    val title = notification.title?.lowercase() ?: ""
    
    // Détection INTELLIGENTE du style (Type OU Titre)
    val (iconColor, backgroundColor, icon) = when {
        // Messages
        type.equals("new_message", ignoreCase = true) || title.contains("message") -> Triple(
            Color(0xFF007AFF), // Bleu iOS
            Color(0xFF007AFF).copy(alpha = 0.1f), 
            Icons.Default.Message
        )
        // Succès / Accepté
        type.equals("success", ignoreCase = true) || 
        type.contains("accepted", ignoreCase = true) || 
        type.contains("completed", ignoreCase = true) ||
        title.contains("acceptée") || title.contains("validée") -> Triple(
            Color(0xFF34C759), // Vert iOS
            Color(0xFF34C759).copy(alpha = 0.1f), 
            Icons.Default.CheckCircle
        )
        // Erreur / Refus / Suppression
        type.equals("error", ignoreCase = true) || 
        type.contains("rejected", ignoreCase = true) || 
        type.contains("cancelled", ignoreCase = true) ||
        title.contains("refusée") || title.contains("annulée") || title.contains("supprimée") -> Triple(
            Color(0xFFFF3B30), // Rouge iOS
            Color(0xFFFF3B30).copy(alpha = 0.1f), 
            Icons.Default.Error
        )
        // Modification
        type.contains("modified", ignoreCase = true) || title.contains("modifiée") -> Triple(
            Color(0xFF5AC8FA), // Bleu clair iOS
            Color(0xFF5AC8FA).copy(alpha = 0.1f), 
            Icons.Default.Edit
        )
        // Évaluation
        type.contains("review", ignoreCase = true) || title.contains("évaluation") || title.contains("avis") -> Triple(
            Color(0xFF5856D6), // Violet iOS
            Color(0xFF5856D6).copy(alpha = 0.1f), 
            Icons.Default.Star
        )
        // Avertissement / Rappel
        type.contains("warning", ignoreCase = true) || 
        type.contains("reminder", ignoreCase = true) ||
        title.contains("rappel") || title.contains("attention") -> Triple(
            Color(0xFFFF9500), // Orange iOS
            Color(0xFFFF9500).copy(alpha = 0.1f), 
            Icons.Default.Warning
        )
        // Par défaut
        else -> Triple(
            Color(0xFF007AFF), // Bleu iOS par défaut
            Color(0xFF007AFF).copy(alpha = 0.1f), 
            Icons.Default.Notifications
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isRead) 1.dp else 4.dp,
                shape = RoundedCornerShape(AppRadius.lg),
                spotColor = if (!isRead) iconColor.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(AppRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) Color.White else backgroundColor.copy(alpha = 0.15f)
        ),
        border = if (!isRead) androidx.compose.foundation.BorderStroke(
            1.5.dp,
            iconColor.copy(alpha = 0.5f)
        ) else androidx.compose.foundation.BorderStroke(
            0.5.dp,
            Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icône
            Box {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = iconColor
                        )
                    }
                }
                if (!isRead) {
                    Surface(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp),
                        shape = CircleShape,
                        color = Color(0xFFFF3B30),
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
                    ) {}
                }
            }

            // Contenu Texte
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title ?: "",
                    fontSize = 16.sp,
                    fontWeight = if (isRead) FontWeight.SemiBold else FontWeight.Bold,
                    color = Color(0xFF1C1C1E), // Noir iOS
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.body ?: "",
                    fontSize = 14.sp,
                    color = Color(0xFF3A3A3C), // Gris foncé iOS
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                notification.createdAt?.let { dateString ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF8E8E93) // Gris clair iOS
                        )
                        Text(
                            text = formatDate(dateString),
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString)
        val now = Calendar.getInstance()
        val notifDate = Calendar.getInstance()
        notifDate.time = date ?: return dateString
        
        val diffMillis = now.timeInMillis - notifDate.timeInMillis
        val diffMinutes = diffMillis / (1000 * 60)
        val diffHours = diffMillis / (1000 * 60 * 60)
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)
        
        when {
            diffMinutes < 1 -> "À l'instant"
            diffMinutes < 60 -> "Il y a ${diffMinutes}min"
            diffHours < 24 -> "Il y a ${diffHours}h"
            diffDays < 7 -> "Il y a ${diffDays}j"
            else -> {
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
                formatter.format(date)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}
