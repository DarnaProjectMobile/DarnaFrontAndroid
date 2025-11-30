package com.sim.darna.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sim.darna.firebase.FirebaseNotificationResponse
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    navController: NavController,
    notification: FirebaseNotificationResponse
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Détails de la notification",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Carte principale
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Type et icône
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    val (iconColor, backgroundColor, icon) = when (notification.type?.lowercase()) {
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
                        
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = backgroundColor
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(14.dp),
                                tint = iconColor
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notification.title ?: "Notification",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            if (notification.type != null) {
                                Text(
                                    text = formatNotificationType(notification.type),
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                    
                    Divider(color = Color(0xFFE0E0E0))
                    
                    // Message
                    Text(
                        text = notification.body ?: "",
                        fontSize = 16.sp,
                        color = Color(0xFF424242),
                        lineHeight = 24.sp
                    )
                    
                    // Date de création
                    notification.createdAt?.let { createdAt ->
                        Divider(color = Color(0xFFE0E0E0))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF757575)
                            )
                            Column {
                                Text(
                                    text = "Date de création",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = formatNotificationDate(createdAt),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1A1A1A)
                                )
                            }
                        }
                    }
                    
                    // Statut
                    Divider(color = Color(0xFFE0E0E0))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (notification.isRead == true) Icons.Default.Done else Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (notification.isRead == true) Color(0xFF00C853) else Color(0xFFFFC107)
                        )
                        Text(
                            text = if (notification.isRead == true) "Lu" else "Non lu",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (notification.isRead == true) Color(0xFF00C853) else Color(0xFFFFC107)
                        )
                    }
                }
            }
        }
    }
}

private fun formatNotificationType(type: String): String {
    return when (type.lowercase()) {
        "visite_accepted" -> "Visite acceptée"
        "visite_rejected" -> "Visite refusée"
        "visite_reserved" -> "Visite réservée"
        "visite_modified" -> "Visite modifiée"
        "visite_cancelled" -> "Visite annulée"
        "visite_completed" -> "Visite effectuée"
        "review_submitted" -> "Évaluation faite"
        "visite_reminder_1h" -> "Rappel - 1 heure"
        "visite_reminder_2h" -> "Rappel - 2 heures"
        "visite_reminder_1d" -> "Rappel - 1 jour"
        "visite_reminder_2d" -> "Rappel - 2 jours"
        else -> type
    }
}

private fun formatNotificationDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "-"
    return try {
        // Essayer plusieurs formats de date possibles
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        
        var date: Date? = null
        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault()).apply {
                    if (format.contains("Z")) {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                }
                date = inputFormat.parse(dateString)
                if (date != null) break
            } catch (e: Exception) {
                // Essayer le format suivant
                continue
            }
        }
        
        if (date != null) {
            val outputFormat = SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH)
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        // En cas d'erreur, retourner la date originale
        dateString
    }
}

