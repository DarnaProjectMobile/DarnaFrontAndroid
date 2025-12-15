package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sim.darna.notifications.NotificationStore
import com.sim.darna.notifications.StoredNotification
import com.sim.darna.navigation.Routes
import com.sim.darna.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotificationsScreen(
    navController: androidx.navigation.NavController,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var notifications by remember { mutableStateOf(emptyList<StoredNotification>()) }

    fun refresh() {
        notifications = NotificationStore.getNotifications(context)
    }

    fun handleNotificationClick(notification: StoredNotification) {
        val payload = notification.data.orEmpty()
        val type = payload["type"]
        val annonceId = payload["annonceId"]

        when (type) {
            "BOOKING_REQUEST", "BOOKING_RESPONSE" -> {
                if (annonceId != null) {
                    navController.navigate("${Routes.PropertyDetail}/$annonceId")
                } else {
                    navController.navigate(Routes.MyVisits)
                }
            }

            "VISIT_ACCEPTED", "VISIT_REFUSED" -> {
                navController.navigate(Routes.MyVisits)
            }

            "VISIT_REQUEST" -> {
                navController.navigate(Routes.VisitRequests)
            }

            "NEW_MESSAGE" -> {
                val visitId = payload["visitId"]
                val housingTitle = payload["housingTitle"] ?: "Chat"
                if (visitId != null) {
                    navController.navigate("chat/$visitId/$housingTitle")
                } else {
                    navController.navigate(Routes.MyVisits)
                }
            }

            else -> {
                // Check if it's a reminder
                if (type?.startsWith("VISIT_REMINDER") == true) {
                    navController.navigate(Routes.MyVisits)
                } else {
                    Toast.makeText(context, "Notification non prise en charge", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Scaffold(
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(shadowElevation = 6.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        AppTheme.primary,
                                        AppTheme.primary.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color.White
                            )
                        }

                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Notifications",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (notifications.isNotEmpty())
                                    "Suivez vos demandes et réservations."
                                else
                                    "Vous n'avez pas encore reçu de notifications.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .height(24.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        val topPadding = padding.calculateTopPadding()

        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lottie animation
                val compositionResult = rememberLottieComposition(
                    LottieCompositionSpec.Asset("empty.json")
                )
                LottieAnimation(
                    composition = compositionResult.value,
                    modifier = Modifier.size(200.dp),
                    iterations = Int.MAX_VALUE // Loop infinitely
                )
                Text(
                    text = "Aucune notification pour le moment",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Button(onClick = { refresh() }, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Actualiser")
                }
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = {
                        NotificationStore.clearNotifications(context)
                        refresh()
                    }) {
                        Text("Effacer les notifications")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (notifications.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            NotificationStore.clearNotifications(context)
                            refresh()
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = topPadding + 8.dp),
                        shape = RoundedCornerShape(999.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Effacer",
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Effacer tout",
                                color = Color(0xFFEF5350),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications, key = { it.id }) { notification ->
                        val dismissState = rememberDismissState { value ->
                            if (value == DismissValue.DismissedToEnd || value == DismissValue.DismissedToStart) {
                                NotificationStore.removeNotification(context, notification.id)
                                refresh()
                                true
                            } else {
                                false
                            }
                        }

                        SwipeToDismiss(
                            state = dismissState,
                            background = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .background(Color(0xFFFFCDD2), RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Glissez pour supprimer",
                                        color = Color(0xFFB71C1C),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            dismissContent = {
                                NotificationCard(
                                    notification = notification,
                                    onClick = { handleNotificationClick(notification) },
                                )
                            },
                            directions = setOf(
                                DismissDirection.StartToEnd,
                                DismissDirection.EndToStart
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: StoredNotification,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AppTheme.primary.copy(alpha = 0.25f),
                                    AppTheme.primary.copy(alpha = 0.45f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.textPrimary
                    )
                    Text(
                        text = notification.formattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppTheme.primary.copy(alpha = 0.05f)
            ) {
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.textPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

