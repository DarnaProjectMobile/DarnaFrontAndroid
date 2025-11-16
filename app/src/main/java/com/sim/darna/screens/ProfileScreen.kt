package com.sim.darna.screens

import android.content.Context
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen() {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)

    // Load ALL user fields
    val username = prefs.getString("username", "") ?: ""
    val email = prefs.getString("email", "") ?: ""
    val role = prefs.getString("role", "") ?: ""
    val birthday = prefs.getString("dateDeNaissance", "") ?: ""
    val phone = prefs.getString("numTel", "") ?: ""
    val gender = prefs.getString("gender", "") ?: ""
    val createdAt = prefs.getString("createdAt", "") ?: ""
    val userId = prefs.getString("user_id", "") ?: ""

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

            // Avatar with gradient background
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF00B8D4))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(130.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            // Username as title
            Text(
                text = username.ifEmpty { "User" },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(Modifier.height(4.dp))

            // Role badge
            if (role.isNotEmpty()) {
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

            Spacer(Modifier.height(32.dp))

            // Information Cards Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Informations de contact",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(Modifier.height(20.dp))

                    // Email
                    ProfileRow(Icons.Default.Email, "Email", email)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                    Spacer(Modifier.height(16.dp))

                    // Phone
                    ProfileRow(Icons.Default.Phone, "Phone", phone)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Personal Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Informations personnelles",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(Modifier.height(20.dp))

                    // Username
                    ProfileRow(Icons.Default.Person, "Username", username)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                    Spacer(Modifier.height(16.dp))

                    // Birthday
                    ProfileLabel("Birthday", birthday, Icons.Default.Cake)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                    Spacer(Modifier.height(16.dp))

                    // Gender
                    ProfileLabel("Gender", gender, Icons.Default.Wc)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Account Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Détails du compte",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    Spacer(Modifier.height(20.dp))

                    // Created At
                    ProfileLabel("Created At", createdAt, Icons.Default.CalendarToday)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                    Spacer(Modifier.height(16.dp))

                    // User ID
                    ProfileLabel("User ID", userId, Icons.Default.Badge)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = { prefs.edit().clear().apply() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFEF5350), Color(0xFFE53935))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Logout",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF757575),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value.ifEmpty { "Non renseigné" },
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun ProfileLabel(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                label,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF757575),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value.ifEmpty { "Non renseigné" },
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Normal
            )
        }
    }
}