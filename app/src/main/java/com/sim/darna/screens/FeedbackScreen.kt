package com.sim.darna.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.sim.darna.viewmodel.ReportViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    // Load saved username + email
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val username = prefs.getString("username", "User") ?: "User"
    val email = prefs.getString("email", "") ?: ""

    // ViewModel initialization
    val vm: ReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    LaunchedEffect(Unit) { vm.init(context) }

    // UI States
    var selectedCategory by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler { onNavigateBack() }

    val categories = listOf(
        "🐛 Bug Report" to "Bug Report",
        "💡 Feature Request" to "Feature Request",
        "⭐ General Feedback" to "General Feedback",
        "❓ Help & Support" to "Help & Support"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column {

            // HEADER
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1A73E8), Color(0xFF0D47A1))
                            )
                        )
                        .padding(20.dp)
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        IconButton(onClick = onNavigateBack) {
                            Text("←", color = Color.White, fontSize = 28.sp)
                        }

                        Spacer(Modifier.width(8.dp))

                        Column {
                            Text(
                                "Send Feedback",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Help us improve the app",
                                color = Color.White.copy(0.9f)
                            )
                        }
                    }
                }
            }

            // CONTENT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {

                // USER INFO CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1A73E8), Color(0xFF0D47A1))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                username.first().uppercase(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(username, fontWeight = FontWeight.SemiBold)
                            if (email.isNotEmpty())
                                Text(email, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // CATEGORY SELECTION
                Text("Select a category", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                categories.forEach { (label, realValue) ->
                    CategoryCard(
                        label = label,
                        isSelected = selectedCategory == realValue,
                        onClick = { selectedCategory = realValue }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(24.dp))

                // FEEDBACK TEXT
                Text("Your message", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.White)
                ) {
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = { Text("Describe your issue or suggestion...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 15.sp)
                    )
                }

                Spacer(Modifier.height(32.dp))

                // SUBMIT BUTTON
                val enabled = selectedCategory.isNotEmpty() && feedbackText.isNotBlank()
                val buttonScale by animateFloatAsState(if (enabled) 1f else 0.95f)

                Button(
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
                    },
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(buttonScale),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Submit Feedback")
                    }
                }
            }
        }
    }

    // SUCCESS POPUP
    if (showSuccessDialog) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(Color.White),
                modifier = Modifier.animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", fontSize = 48.sp, color = Color(0xFF4CAF50))
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Thank You!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Your feedback has been submitted successfully",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


@Composable
fun CategoryCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        if (isSelected) 1.02f else 1f,
        animationSpec = spring()
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            if (isSelected) Color(0xFFE8F0FE) else Color.White
        ),
        border = if (isSelected)
            BorderStroke(2.dp, Color(0xFF1A73E8))
        else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                fontSize = 15.sp,
                color = if (isSelected) Color(0xFF1A73E8) else Color.Black
            )
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF1A73E8)
                )
            }
        }
    }
}
