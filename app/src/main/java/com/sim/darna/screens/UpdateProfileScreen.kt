package com.sim.darna.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.viewmodel.UpdateProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UpdateProfileScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()
    
    // ViewModel initialization
    val viewModel: UpdateProfileViewModel = viewModel()
    LaunchedEffect(Unit) { viewModel.init(context) }

    // Load current user data
    var username by remember { mutableStateOf(prefs.getString("username", "") ?: "") }
    var email by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var phone by remember { mutableStateOf(prefs.getString("numTel", "") ?: "") }
    var birthday by remember { mutableStateOf(prefs.getString("dateDeNaissance", "") ?: "") }
    var gender by remember { mutableStateOf(prefs.getString("gender", "") ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Observe ViewModel states
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    // Handle success
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            showSuccessDialog = true
            delay(2000)
            showSuccessDialog = false
            viewModel.resetState()
            onNavigateBack()
        }
    }
    
    // Handle error
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showErrorDialog = true
        }
    }

    BackHandler { onNavigateBack() }

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
                                "Update Profile",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Edit your personal information",
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

                // Avatar Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1A73E8), Color(0xFF0D47A1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            username.firstOrNull()?.uppercase() ?: "U",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Username Field
                InputFieldCard(
                    label = "Username",
                    value = username,
                    onValueChange = { username = it },
                    icon = Icons.Default.Person
                )

                Spacer(Modifier.height(16.dp))

                // Email Field
                InputFieldCard(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Default.Email
                )

                Spacer(Modifier.height(16.dp))

                // Phone Field
                InputFieldCard(
                    label = "Phone Number",
                    value = phone,
                    onValueChange = { phone = it },
                    icon = Icons.Default.Phone
                )

                Spacer(Modifier.height(16.dp))

                // Birthday Field
                InputFieldCard(
                    label = "Birthday (YYYY-MM-DD)",
                    value = birthday,
                    onValueChange = { birthday = it },
                    icon = Icons.Default.Cake
                )

                Spacer(Modifier.height(16.dp))

                // Password Field (Optional)
                PasswordInputFieldCard(
                    label = "New Password (Optional)",
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    icon = Icons.Default.Lock,
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
                )

                Spacer(Modifier.height(16.dp))

                // Gender Selection
                Text("Gender", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenderCard(
                        label = "Male",
                        isSelected = gender.equals("Male", ignoreCase = true),
                        onClick = { gender = "Male" },
                        modifier = Modifier.weight(1f)
                    )
                    GenderCard(
                        label = "Female",
                        isSelected = gender.equals("Female", ignoreCase = true),
                        onClick = { gender = "Female" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(32.dp))

                // UPDATE BUTTON
                val enabled = username.isNotBlank() && email.isNotBlank()
                val buttonScale by animateFloatAsState(if (enabled) 1f else 0.95f)

                Button(
                    onClick = {
                        // Call ViewModel to update profile via API
                        viewModel.updateProfile(
                            username = username,
                            email = email,
                            password = newPassword.ifBlank { null },  // Only send if not empty
                            numTel = phone.ifBlank { null },
                            dateDeNaissance = birthday.ifBlank { null },
                            gender = gender.ifBlank { null }
                        )
                    },
                    enabled = enabled && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(buttonScale),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A73E8)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Update Profile", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
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
                    Text("Success!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Your profile has been updated",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
    
    // ERROR POPUP
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                viewModel.resetState()
            },
            confirmButton = {
                TextButton(onClick = { 
                    showErrorDialog = false
                    viewModel.resetState()
                }) {
                    Text("OK")
                }
            },
            title = { Text("❌ Error") },
            text = { Text(errorMessage ?: "Unknown error occurred") }
        )
    }
}

@Composable
fun InputFieldCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF1A73E8))
                }

                Spacer(Modifier.width(12.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter $label", fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    textStyle = TextStyle(fontSize = 15.sp),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun PasswordInputFieldCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit
) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            "Leave empty to keep current password",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF1A73E8))
                }

                Spacer(Modifier.width(12.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter new password", fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    textStyle = TextStyle(fontSize = 15.sp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(0xFF757575)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GenderCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        if (isSelected) 1.02f else 1f,
        animationSpec = spring()
    )

    Card(
        modifier = modifier
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF1A73E8) else Color.Black
            )
        }
    }
}
