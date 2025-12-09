package com.sim.darna.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.sim.darna.viewmodel.ReportViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Modern Color Palette
object FeedbackColors {
    val Primary = Color(0xFFFF4B6E)
    val Secondary = Color(0xFF4C6FFF)
    val Accent = Color(0xFFFFC857)
    val Background = Color(0xFFF7F7F7)
    val CardBackground = Color.White
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF6B7280)
    val Border = Color(0xFFE5E7EB)
    val Success = Color(0xFF10B981)
}

@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val username = prefs.getString("username", "User") ?: "User"
    val email = prefs.getString("email", "") ?: ""

    val vm: ReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    LaunchedEffect(Unit) { vm.init(context) }

    var selectedCategory by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler { onNavigateBack() }

    val categories = listOf(
        Triple("Bug Report", Icons.Outlined.BugReport, FeedbackColors.Primary),
        Triple("Feature Request", Icons.Outlined.Lightbulb, FeedbackColors.Secondary),
        Triple("General Feedback", Icons.Outlined.Reviews, FeedbackColors.Accent),
        Triple("Help & Support", Icons.Outlined.HelpOutline, Color(0xFF8B5CF6))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FeedbackColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            FeedbackHeader(
                username = username,
                onBackClick = onNavigateBack
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // User Info Card
                UserInfoCard(username = username, email = email)

                // Category Selection
                Text(
                    text = "Choisissez une catégorie",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FeedbackColors.TextPrimary
                )

                categories.forEach { (label, icon, color) ->
                    ModernCategoryCard(
                        label = label,
                        icon = icon,
                        color = color,
                        isSelected = selectedCategory == label,
                        onClick = { selectedCategory = label }
                    )
                }

                // Feedback Input
                Text(
                    text = "Votre message",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FeedbackColors.TextPrimary
                )

                FeedbackInputCard(
                    value = feedbackText,
                    onValueChange = { feedbackText = it }
                )

                // Submit Button
                val enabled = selectedCategory.isNotEmpty() && feedbackText.isNotBlank()

                SubmitButton(
                    enabled = enabled,
                    onClick = {
                        vm.sendReport(
                            reason = selectedCategory,
                            details = feedbackText
                        ) { success ->
                            scope.launch {
                                if (success) {
                                    showSuccessDialog = true
                                    delay(2000)
                                    showSuccessDialog = false
                                    onNavigateBack()
                                }
                            }
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        SuccessDialog()
    }
}

@Composable
fun FeedbackHeader(
    username: String,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FeedbackColors.CardBackground,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Back Button
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onBackClick() },
                    shape = CircleShape,
                    color = FeedbackColors.Background
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        tint = FeedbackColors.TextPrimary
                    )
                }

                // Title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Envoyer un avis",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = FeedbackColors.TextPrimary
                    )
                    Text(
                        text = "Aidez-nous à améliorer l'app",
                        fontSize = 14.sp,
                        color = FeedbackColors.TextSecondary
                    )
                }

                // Icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = FeedbackColors.Primary.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RateReview,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = FeedbackColors.Primary
                    )
                }
            }
        }
    }
}

@Composable
fun UserInfoCard(username: String, email: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = FeedbackColors.CardBackground,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Brush.linearGradient(
                    colors = listOf(
                        FeedbackColors.Primary,
                        FeedbackColors.Secondary
                    )
                ).let { FeedbackColors.Primary }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    FeedbackColors.Primary,
                                    FeedbackColors.Secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // User Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FeedbackColors.TextPrimary
                )
                if (email.isNotEmpty()) {
                    Text(
                        text = email,
                        fontSize = 14.sp,
                        color = FeedbackColors.TextSecondary
                    )
                }
            }

            // Verified Badge
            Surface(
                shape = CircleShape,
                color = FeedbackColors.Success.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(6.dp),
                    tint = FeedbackColors.Success
                )
            }
        }
    }
}

@Composable
fun ModernCategoryCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) color.copy(alpha = 0.1f) else FeedbackColors.CardBackground,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) color else FeedbackColors.Border
        ),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) color.copy(alpha = 0.2f) else color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    tint = color
                )
            }

            // Label
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) color else FeedbackColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )

            // Check Icon
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FeedbackInputCard(
    value: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = FeedbackColors.CardBackground,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = {
                    Text(
                        text = "Décrivez votre problème ou suggestion en détail...",
                        color = FeedbackColors.TextSecondary,
                        fontSize = 15.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FeedbackColors.CardBackground,
                    unfocusedContainerColor = FeedbackColors.CardBackground,
                    focusedBorderColor = FeedbackColors.Primary.copy(alpha = 0.3f),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = FeedbackColors.Primary
                ),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = FeedbackColors.TextPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SubmitButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) FeedbackColors.Primary else FeedbackColors.Border,
        shadowElevation = if (enabled) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                tint = if (enabled) Color.White else FeedbackColors.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Envoyer le feedback",
                color = if (enabled) Color.White else FeedbackColors.TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SuccessDialog() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(40.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = FeedbackColors.CardBackground,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success Icon
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = FeedbackColors.Success.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = FeedbackColors.Success
                        )
                    }
                }

                // Success Text
                Text(
                    text = "Merci !",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = FeedbackColors.TextPrimary
                )

                Text(
                    text = "Votre feedback a été envoyé avec succès",
                    fontSize = 15.sp,
                    color = FeedbackColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Success Animation Indicator
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = FeedbackColors.Success,
                    trackColor = FeedbackColors.Success.copy(alpha = 0.2f)
                )
            }
        }
    }
}