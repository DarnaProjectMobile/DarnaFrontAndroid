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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// Modern Color Palette
private val PrimaryColor = Color(0xFFFF4B6E)
private val SecondaryColor = Color(0xFF4C6FFF)
private val AccentColor = Color(0xFFFFC857)
private val BackgroundColor = Color(0xFFF7F7F7)
private val SurfaceColor = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()

    val viewModel: UpdateProfileViewModel = viewModel()
    LaunchedEffect(Unit) { viewModel.init(context) }

    var username by remember { mutableStateOf(prefs.getString("username", "") ?: "") }
    var email by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var phone by remember { mutableStateOf(prefs.getString("numTel", "") ?: "") }
    var birthday by remember { mutableStateOf(prefs.getString("dateDeNaissance", "") ?: "") }
    var gender by remember { mutableStateOf(prefs.getString("gender", "") ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            showSuccessDialog = true
            delay(2000)
            showSuccessDialog = false
            viewModel.resetState()
            onNavigateBack()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showErrorDialog = true
        }
    }

    BackHandler { onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Modifier le profil",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceColor,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryColor.copy(alpha = 0.2f),
                                        SecondaryColor.copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .border(3.dp, PrimaryColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            username.firstOrNull()?.uppercase() ?: "U",
                            color = PrimaryColor,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Modifiez vos informations personnelles",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Form Fields
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Username
                ModernInputField(
                    label = "Nom d'utilisateur",
                    value = username,
                    onValueChange = { username = it },
                    icon = Icons.Outlined.Person,
                    placeholder = "Votre nom d'utilisateur"
                )

                // Email
                ModernInputField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Outlined.Email,
                    placeholder = "votre@email.com"
                )

                // Phone
                ModernInputField(
                    label = "Téléphone",
                    value = phone,
                    onValueChange = { phone = it },
                    icon = Icons.Outlined.Phone,
                    placeholder = "+216 12 345 678"
                )

                // Birthday
                ModernInputField(
                    label = "Date de naissance",
                    value = birthday,
                    onValueChange = { birthday = it },
                    icon = Icons.Outlined.Cake,
                    placeholder = "AAAA-MM-JJ"
                )

                // Password
                ModernPasswordField(
                    label = "Nouveau mot de passe",
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    passwordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )

                // Gender Selection
                Column {
                    Text(
                        "Genre",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GenderOption(
                            label = "Homme",
                            icon = Icons.Outlined.Man,
                            isSelected = gender.equals("Male", ignoreCase = true),
                            onClick = { gender = "Male" },
                            modifier = Modifier.weight(1f)
                        )
                        GenderOption(
                            label = "Femme",
                            icon = Icons.Outlined.Woman,
                            isSelected = gender.equals("Female", ignoreCase = true),
                            onClick = { gender = "Female" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Update Button
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                val enabled = username.isNotBlank() && email.isNotBlank()

                Button(
                    onClick = {
                        viewModel.updateProfile(
                            username = username,
                            email = email,
                            password = newPassword.ifBlank { null },
                            numTel = phone.ifBlank { null },
                            dateDeNaissance = birthday.ifBlank { null },
                            gender = gender.ifBlank { null }
                        )
                    },
                    enabled = enabled && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        disabledContainerColor = PrimaryColor.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = SurfaceColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = SurfaceColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Mettre à jour",
                            color = SurfaceColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.6f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceColor,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(32.dp)
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        "Succès !",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Votre profil a été mis à jour",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.resetState()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        viewModel.resetState()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = PrimaryColor
                    )
                ) {
                    Text("OK", fontWeight = FontWeight.SemiBold)
                }
            },
            icon = {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Erreur",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    errorMessage ?: "Une erreur s'est produite",
                    color = TextSecondary
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceColor
        )
    }
}

@Composable
fun ModernInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String
) {
    Column {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSecondary, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = SecondaryColor,
                    modifier = Modifier.size(22.dp)
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedBorderColor = SecondaryColor,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(fontSize = 15.sp)
        )
    }
}

@Composable
fun ModernPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            "Laisser vide pour garder le mot de passe actuel",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nouveau mot de passe", color = TextSecondary, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = SecondaryColor,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedBorderColor = SecondaryColor,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(fontSize = 15.sp)
        )
    }
}

@Composable
fun GenderOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SecondaryColor.copy(alpha = 0.1f) else SurfaceColor,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) SecondaryColor else Color(0xFFE0E0E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) SecondaryColor else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) SecondaryColor else TextPrimary
            )
        }
    }
}