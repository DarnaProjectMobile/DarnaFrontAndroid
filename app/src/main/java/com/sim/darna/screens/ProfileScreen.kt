package com.sim.darna.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sim.darna.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(navController: NavHostController) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)

    // State to trigger reload when coming back from UpdateProfile
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // Reload data whenever screen appears or refreshTrigger changes
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    
    // Load data from SharedPreferences
    LaunchedEffect(refreshTrigger) {
        username = prefs.getString("username", "") ?: ""
        email = prefs.getString("email", "") ?: ""
        role = prefs.getString("role", "") ?: ""
        birthday = prefs.getString("dateDeNaissance", "") ?: ""
        phone = prefs.getString("numTel", "") ?: ""
        gender = prefs.getString("gender", "") ?: ""
        createdAt = prefs.getString("createdAt", "") ?: ""
        userId = prefs.getString("user_id", "") ?: ""
    }
    
    // Refresh data when navigating back
    DisposableEffect(Unit) {
        onDispose {
            refreshTrigger++
        }
    }

    // Animation states
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    // Infinite rotation for avatar
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsing effect
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F9FF), Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            // Avatar
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800)) + scaleIn(
                    initialScale = 0.3f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2196F3), Color(0xFF00B8D4))
                            )
                        )
                        .rotate(rotation),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(130.dp)
                            .rotate(-rotation),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Username
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, 200)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(800, 200)
                )
            ) {
                Text(
                    text = username.ifEmpty { "User" },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            Spacer(Modifier.height(4.dp))

            // Role badge
            if (role.isNotEmpty()) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, 300)) + scaleIn()
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = role.uppercase(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Contact Card
            AnimatedCard(visible, 400) {
                InfoCard("Informations de contact") {
                    ProfileRow(Icons.Default.Email, "Email", email)
                    DividerSpacer()
                    ProfileRow(Icons.Default.Phone, "Phone", phone)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Personal Card
            AnimatedCard(visible, 500) {
                InfoCard("Informations personnelles") {
                    ProfileRow(Icons.Default.Person, "Username", username)
                    DividerSpacer()
                    ProfileRow(Icons.Default.Cake, "Birthday", birthday)
                    DividerSpacer()
                    ProfileRow(Icons.Default.Wc, "Gender", gender)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Account Card
            AnimatedCard(visible, 600) {
                InfoCard("Détails du compte") {
                    ProfileRow(Icons.Default.CalendarToday, "Created At", createdAt)
                    DividerSpacer()
                    ProfileRow(Icons.Default.Badge, "User ID", userId)
                }
            }

            Spacer(Modifier.height(32.dp))

            // LOGOUT BUTTON (FIXED)
            AnimatedCard(visible, 700) {
                GradientButton(
                    text = "Logout",
                    icon = Icons.Default.Logout,
                    colors = listOf(Color(0xFFEF5350), Color(0xFFE53935))
                ) {
                    prefs.edit().clear().apply()

                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Main) { inclusive = true }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // UPDATE PROFILE BUTTON
            AnimatedCard(visible, 800) {
                GradientButton(
                    text = "Update Profile",
                    icon = Icons.Default.Edit,
                    colors = listOf(Color(0xFF4CAF50), Color(0xFF388E3C))
                ) {
                    navController.navigate(Routes.UpdateProfile)
                }
            }

            Spacer(Modifier.height(16.dp))

            // FEEDBACK BUTTON (COMPATIBLE)
            AnimatedCard(visible, 900) {
                GradientButton(
                    text = "Send Feedback",
                    icon = Icons.Default.Feedback,
                    colors = listOf(Color(0xFF1A73E8), Color(0xFF0D47A1))
                ) {
                    navController.navigate("feedback")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun AnimatedCard(visible: Boolean, delay: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600, delayMillis = delay)) +
                slideInVertically(
                    initialOffsetY = { 60 },
                    animationSpec = tween(600, delayMillis = delay)
                )
    ) { content() }
}

@Composable
fun DividerSpacer() {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider(color = Color(0xFFE0E0E0))
    Spacer(Modifier.height(16.dp))
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun ProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2196F3))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color(0xFF757575))
            Spacer(Modifier.height(4.dp))
            Text(value.ifEmpty { "Non renseigné" }, fontSize = 15.sp)
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(colors)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
