package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import kotlinx.coroutines.launch
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

    // Charger les notifications au démarrage et quand on revient sur l'écran
    LaunchedEffect(Unit) {
        android.util.Log.d("NotificationsScreen", "Chargement des notifications...")
        viewModel.loadNotifications()
        viewModel.loadUnreadCount()
    }
    
    // Rafraîchir les notifications quand on revient sur l'écran
    LaunchedEffect(navController.currentBackStackEntry?.id) {
        android.util.Log.d("NotificationsScreen", "Rafraîchissement des notifications...")
        viewModel.loadNotifications()
        viewModel.loadUnreadCount()
    }

    // Filtrer les notifications selon le filtre
    val filteredNotifications = remember(uiState.notifications, showUnreadOnly) {
        if (showUnreadOnly) {
            uiState.notifications.filter { it.isRead != true }
        } else {
            uiState.notifications
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Notifications",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color(0xFFFF3B30),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                },
                actions = {
                    // Filtre pour masquer/afficher les non lues
                    IconButton(
                        onClick = { showUnreadOnly = !showUnreadOnly }
                    ) {
                        Icon(
                            imageVector = if (showUnreadOnly) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showUnreadOnly) "Afficher toutes" else "Masquer les lues",
                            tint = if (showUnreadOnly) Color(0xFF0066FF) else Color(0xFF9E9E9E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF0066FF))
                }
            }
            uiState.error != null -> {
                uiState.error?.let { errorMessage ->
                    ErrorState(
                        message = errorMessage,
                        onRetry = { viewModel.loadNotifications() },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            uiState.notifications.isEmpty() && !uiState.isLoading -> {
                EmptyState(
                    modifier = Modifier.padding(paddingValues),
                    onRefresh = { viewModel.loadNotifications() }
                )
            }
            else -> {
                // Afficher les notifications filtrées
                android.util.Log.d("NotificationsScreen", "Affichage de ${filteredNotifications.size} notification(s)")
                NotificationList(
                    notifications = filteredNotifications,
                    onNotificationClick = { notification ->
                        try {
                            android.util.Log.d("NotificationsScreen", "Clic sur notification: ${notification.id}, titre: ${notification.title}, type: ${notification.type}")
                            
                            // Marquer comme lu si nécessaire
                            if (notification.isRead != true) {
                                notification.id?.let { id ->
                                    viewModel.markAsRead(id)
                                }
                            }
                            
                            // Si c'est une notification de message, ouvrir directement le chat, marquer comme lu et supprimer
                            if (notification.type == "NEW_MESSAGE" && !notification.visitId.isNullOrBlank()) {
                                try {
                                    notification.id?.let { id ->
                                        // Marquer comme lu immédiatement
                                        viewModel.markAsRead(id)
                                        // Supprimer la notification après un court délai pour permettre la navigation
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
                                    // Utiliser parentNavController si disponible, sinon navController
                                    val targetNavController = parentNavController ?: navController
                                    targetNavController.navigate("chat/$visiteId/$encodedTitle") {
                                        launchSingleTop = true
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("NotificationsScreen", "Erreur lors de la navigation vers le chat", e)
                                }
                            } else {
                                // Navigation vers les détails de la notification pour les autres types
                                notification.id?.let { id ->
                                    if (id.isNotEmpty()) {
                                        try {
                                            android.util.Log.d("NotificationsScreen", "Navigation vers notification_detail/$id")
                                            navController.navigate("notification_detail/$id") {
                                                // Options de navigation pour une meilleure expérience
                                                launchSingleTop = true
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("NotificationsScreen", "Erreur lors de la navigation", e)
                                            // Essayer avec un encodage URL si nécessaire
                                            try {
                                                val encodedId = java.net.URLEncoder.encode(id, "UTF-8")
                                                android.util.Log.d("NotificationsScreen", "Tentative avec encodage: notification_detail/$encodedId")
                                                navController.navigate("notification_detail/$encodedId") {
                                                    launchSingleTop = true
                                                }
                                            } catch (e2: Exception) {
                                                android.util.Log.e("NotificationsScreen", "Erreur lors de la navigation avec encodage", e2)
                                            }
                                        }
                                    } else {
                                        android.util.Log.w("NotificationsScreen", "ID de notification vide, impossible de naviguer")
                                    }
                                } ?: run {
                                    android.util.Log.w("NotificationsScreen", "ID de notification null, impossible de naviguer")
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationsScreen", "Erreur lors du clic sur la notification", e)
                        }
                    },
                    onHideClick = { notification ->
                        // Marquer comme lu
                        notification.id?.let { id ->
                            viewModel.markAsRead(id)
                        }
                    },
                    onDeleteClick = { notification ->
                        // Supprimer la notification
                        notification.id?.let { id ->
                            viewModel.deleteNotification(id)
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun NotificationList(
    notifications: List<FirebaseNotificationResponse>,
    onNotificationClick: (FirebaseNotificationResponse) -> Unit,
    onHideClick: (FirebaseNotificationResponse) -> Unit,
    onDeleteClick: (FirebaseNotificationResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = notifications,
            key = { it.id ?: "" }
        ) { notification ->
            SwipeableNotificationItem(
                notification = notification,
                onClick = { onNotificationClick(notification) },
                onMarkAsRead = { onHideClick(notification) },
                onMarkAsUnread = { 
                    // Pour l'instant, on ne peut pas marquer comme non lu via l'API
                    // On peut juste recharger les notifications
                },
                onDelete = { onDeleteClick(notification) }
            )
        }
    }
}

@Composable
fun SwipeableNotificationItem(
    notification: FirebaseNotificationResponse,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit,
    onMarkAsUnread: () -> Unit = {},
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    var cardWidth by remember { mutableStateOf(0) }
    var offsetX by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Calculer la largeur de swipe (120dp pour les actions)
    val swipeThreshold = with(density) { 120.dp.toPx() }
    val maxSwipe = with(density) { 160.dp.toPx() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Actions de swipe (en arrière-plan) - Design amélioré
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterEnd)
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action: Marquer comme lu
            if (notification.isRead != true) {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { 
                            scope.launch {
                                offsetX = 0f
                            }
                            onMarkAsRead()
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF00C853),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Marquer comme lu",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            // Action: Supprimer
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clickable { 
                        scope.launch {
                            offsetX = 0f
                        }
                        onDelete()
                    },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFF3B30),
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }
            }
        }
        
        // Carte de notification (au premier plan)
        NotificationItem(
            notification = notification,
            onClick = onClick,
            onHide = onMarkAsRead,
            onDelete = onDelete,
            onMarkAsUnread = onMarkAsUnread,
            offsetX = offsetX,
            isDragging = isDragging,
            onOffsetChange = { newOffset ->
                offsetX = newOffset.coerceIn(-maxSwipe, 0f)
            },
            onDragStart = {
                isDragging = true
            },
            onSwipeEnd = {
                scope.launch {
                    isDragging = false
                    if (offsetX < -swipeThreshold / 2) {
                        // Swipe suffisant, déclencher l'action
                        if (notification.isRead != true && offsetX < -swipeThreshold) {
                            onMarkAsRead()
                        } else {
                            onDelete()
                        }
                    }
                    // Réinitialiser la position avec animation
                    offsetX = 0f
                }
            },
            onWidthMeasured = { width ->
                cardWidth = width
            }
        )
    }
}

@Composable
fun NotificationItem(
    notification: FirebaseNotificationResponse,
    onClick: () -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsUnread: () -> Unit = {},
    offsetX: Float = 0f,
    isDragging: Boolean = false,
    onOffsetChange: (Float) -> Unit = {},
    onDragStart: () -> Unit = {},
    onSwipeEnd: () -> Unit = {},
    onWidthMeasured: (Int) -> Unit = {}
) {
    val density = LocalDensity.current
    var showMenu by remember { mutableStateOf(false) }
    val isRead = notification.isRead ?: false
    val type = notification.type ?: "info"
    
    // Animation d'entrée
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Couleurs selon le type
    val (iconColor, backgroundColor, icon) = when (type.lowercase()) {
        "new_message" -> Triple(
            Color(0xFF0066FF),
            Color(0xFF0066FF).copy(alpha = 0.1f),
            Icons.Default.Message
        )
        "success", "visite_accepted", "visite_completed" -> Triple(
            Color(0xFF00C853),
            Color(0xFF00C853).copy(alpha = 0.1f),
            Icons.Default.CheckCircle
        )
        "error", "visite_rejected", "visite_cancelled" -> Triple(
            Color(0xFFFF3B30),
            Color(0xFFFF3B30).copy(alpha = 0.1f),
            Icons.Default.Error
        )
        "visite_modified" -> Triple(
            Color(0xFF2196F3),
            Color(0xFF2196F3).copy(alpha = 0.1f),
            Icons.Default.Edit
        )
        "review_submitted" -> Triple(
            Color(0xFF9C27B0),
            Color(0xFF9C27B0).copy(alpha = 0.1f),
            Icons.Default.Star
        )
        "warning", "visite_reminder_1h", "visite_reminder_2h", 
        "visite_reminder_1d", "visite_reminder_2d" -> Triple(
            Color(0xFFFFC107),
            Color(0xFFFFC107).copy(alpha = 0.1f),
            Icons.Default.Warning
        )
        else -> Triple(
            Color(0xFF0066FF),
            Color(0xFF0066FF).copy(alpha = 0.1f),
            Icons.Default.Notifications
        )
    }

    // Animation fluide pour l'offset
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset"
    )
    val animatedOffsetDp = with(density) { animatedOffset.toDp() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = animatedOffsetDp)
            .scale(scale)
            .animateContentSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { 
                        onDragStart()
                    },
                    onDragEnd = { 
                        onSwipeEnd() 
                    }
                ) { change, dragAmount ->
                    val newOffset = offsetX + dragAmount
                    onOffsetChange(newOffset)
                }
            }
            .onGloballyPositioned { coordinates ->
                onWidthMeasured(coordinates.size.width)
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) Color.White else Color(0xFFF0F7FF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRead) 1.dp else 6.dp
        ),
        border = if (!isRead) androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = Color(0xFF0066FF).copy(alpha = 0.3f)
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !isDragging && kotlin.math.abs(offsetX) < 10f,
                    onClick = {
                        // Ne déclencher le clic que si on n'est pas en train de swiper
                        if (!isDragging && kotlin.math.abs(offsetX) < 10f) {
                            onClick()
                        }
                    }
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icône avec badge si non lu - Design amélioré
            Box {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = backgroundColor,
                    shadowElevation = if (!isRead) 4.dp else 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = iconColor
                        )
                    }
                }
                if (!isRead) {
                    Surface(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp),
                        shape = CircleShape,
                        color = Color(0xFFFF3B30),
                        shadowElevation = 2.dp
                    ) {}
                }
            }

            // Contenu - Design amélioré
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = notification.title ?: "",
                        fontSize = 17.sp,
                        fontWeight = if (isRead) FontWeight.SemiBold else FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isRead) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = Color(0xFF0066FF)
                        ) {}
                    }
                }
                Text(
                    text = notification.body ?: "",
                    fontSize = 14.sp,
                    color = Color(0xFF424242),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                notification.createdAt?.let { dateString ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Text(
                            text = formatDate(dateString),
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Menu d'actions
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Actions",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF9E9E9E)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!isRead) {
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFF00C853)
                                    )
                                    Text(
                                        "Marquer comme lu",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
                            onClick = {
                                onHide()
                                showMenu = false
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFFFFC107)
                                    )
                                    Text(
                                        "Marquer comme non lu",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
                            onClick = {
                                onMarkAsUnread()
                                showMenu = false
                            }
                        )
                    }
                    Divider()
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFFFF3B30)
                                )
                                Text(
                                    "Supprimer",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFF3B30)
                                )
                            }
                        },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, onRefresh: (() -> Unit)? = null) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color(0xFF0066FF).copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF0066FF)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Aucune notification",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "Vous n'avez pas encore de notifications",
                    fontSize = 15.sp,
                    color = Color(0xFF757575),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color(0xFFFF3B30).copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFFFF3B30)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Erreur",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = message,
                    fontSize = 15.sp,
                    color = Color(0xFF757575),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0066FF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Réessayer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}


