package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.notification.NotificationViewModel
import com.sim.darna.notification.NotificationUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: androidx.navigation.NavController,
    viewModel: NotificationViewModel
) {
    val uiState by viewModel.state.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

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
                    IconButton(
                        onClick = { 
                            viewModel.loadNotifications()
                            viewModel.loadUnreadCount()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = Color(0xFF0066FF)
                        )
                    }
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead() }
                        ) {
                            Text(
                                text = "Tout lire",
                                color = Color(0xFF0066FF),
                                fontSize = 14.sp
                            )
                        }
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
                // Filtrer les notifications cachées et afficher les notifications visibles
                val visibleNotifications = uiState.notifications.filter { notification ->
                    val isHidden = notification.hidden == true
                    if (isHidden) {
                        android.util.Log.d("NotificationsScreen", "Notification cachée ignorée: id=${notification.id}, type=${notification.type}")
                    }
                    !isHidden
                }
                android.util.Log.d("NotificationsScreen", "Affichage de ${visibleNotifications.size} notification(s) visible(s) sur ${uiState.notifications.size} total")
                NotificationList(
                    notifications = visibleNotifications,
                    onNotificationClick = { notification ->
                        try {
                            // Marquer comme lu si nécessaire
                            if (notification.read != true) {
                                viewModel.markAsRead(notification.id ?: "")
                            }
                            // Navigation vers les détails de la notification
                            notification.id?.let { id ->
                                if (id.isNotEmpty()) {
                                    try {
                                        navController.navigate("notification_detail/$id")
                                    } catch (e: Exception) {
                                        android.util.Log.e("NotificationsScreen", "Erreur lors de la navigation", e)
                                        // Essayer avec un encodage URL si nécessaire
                                        try {
                                            val encodedId = java.net.URLEncoder.encode(id, "UTF-8")
                                            navController.navigate("notification_detail/$encodedId")
                                        } catch (e2: Exception) {
                                            android.util.Log.e("NotificationsScreen", "Erreur lors de la navigation avec encodage", e2)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationsScreen", "Erreur lors du clic sur la notification", e)
                        }
                    },
                    onHideClick = { notification ->
                        viewModel.hideNotification(notification.id ?: "")
                    },
                    onDeleteClick = { notification ->
                        viewModel.deleteNotification(notification.id ?: "")
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun NotificationList(
    notifications: List<com.sim.darna.notification.NotificationResponse>,
    onNotificationClick: (com.sim.darna.notification.NotificationResponse) -> Unit,
    onHideClick: (com.sim.darna.notification.NotificationResponse) -> Unit,
    onDeleteClick: (com.sim.darna.notification.NotificationResponse) -> Unit,
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
            NotificationItem(
                notification = notification,
                onClick = { onNotificationClick(notification) },
                onHide = { onHideClick(notification) },
                onDelete = { onDeleteClick(notification) }
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: com.sim.darna.notification.NotificationResponse,
    onClick: () -> Unit,
    onHide: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isRead = notification.read ?: false
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
            Icons.Default.Info
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) Color.White else Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRead) 2.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icône avec badge si non lu
            Box {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = backgroundColor
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(12.dp),
                        tint = iconColor
                    )
                }
                if (!isRead) {
                    Surface(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd),
                        shape = CircleShape,
                        color = Color(0xFFFF3B30)
                    ) {}
                }
            }

            // Contenu
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title ?: "",
                    fontSize = 16.sp,
                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = notification.message ?: "",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (notification.logementTitle != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Text(
                            text = notification.logementTitle,
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                notification.createdAt?.let { dateString ->
                    Text(
                        text = formatDate(dateString),
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
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
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFF757575)
                                )
                                Text("Masquer")
                            }
                        },
                        onClick = {
                            onHide()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFFFF3B30)
                                )
                                Text("Supprimer", color = Color(0xFFFF3B30))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF9E9E9E)
            )
            Text(
                text = "Aucune notification",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "Vous n'avez pas encore de notifications",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            if (onRefresh != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0066FF)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualiser")
                }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFFF3B30)
            )
            Text(
                text = "Erreur",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0066FF)
                )
            ) {
                Text("Réessayer")
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


