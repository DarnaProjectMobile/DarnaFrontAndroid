package com.sim.darna.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.sim.darna.auth.TokenStorage
import com.sim.darna.navigation.Routes
import com.sim.darna.notifications.FirebaseTokenRegistrar
import com.sim.darna.utils.FingerprintManager
import java.util.concurrent.Executor

// Modern Color Palette
object ProfileColors {
    val Primary = Color(0xFFFF4B6E)
    val Secondary = Color(0xFF4C6FFF)
    val Accent = Color(0xFFFFC857)
    val Background = Color(0xFFF7F7F7)
    val CardBackground = Color.White
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF6B7280)
    val Border = Color(0xFFE5E7EB)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)

    var refreshTrigger by remember { mutableStateOf(0) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf("") }

    LaunchedEffect(refreshTrigger) {
        username = prefs.getString("username", "") ?: ""
        email = prefs.getString("email", "") ?: ""
        role = prefs.getString("role", "") ?: ""
        birthday = prefs.getString("dateDeNaissance", "") ?: ""
        phone = prefs.getString("numTel", "") ?: ""
        gender = prefs.getString("gender", "") ?: ""
        createdAt = prefs.getString("createdAt", "") ?: ""
    }

    DisposableEffect(Unit) {
        onDispose { refreshTrigger++ }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            ProfileHeader(username, email, role)

            // Content Section
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Actions
                QuickActionsGrid(
                    onEditProfile = { navController.navigate(Routes.UpdateProfile) },
                    onFavorites = { navController.navigate(Routes.Favorites) },
                    onFeedback = { navController.navigate("feedback") },
                    onReservations = { navController.navigate(Routes.Reservations) }
                )

                // Contact Information
                ModernInfoCard(
                    title = "Contact",
                    icon = Icons.Outlined.ContactMail,
                    iconColor = ProfileColors.Secondary
                ) {
                    if (email.isNotEmpty()) {
                        InfoRow(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = email,
                            iconColor = ProfileColors.Secondary
                        )
                    }
                    if (phone.isNotEmpty()) {
                        if (email.isNotEmpty()) Spacer(Modifier.height(12.dp))
                        InfoRow(
                            icon = Icons.Outlined.Phone,
                            label = "Téléphone",
                            value = phone,
                            iconColor = ProfileColors.Secondary
                        )
                    }
                }

                // Personal Information
                ModernInfoCard(
                    title = "Informations personnelles",
                    icon = Icons.Outlined.Person,
                    iconColor = ProfileColors.Primary
                ) {
                    if (birthday.isNotEmpty()) {
                        InfoRow(
                            icon = Icons.Outlined.Cake,
                            label = "Date de naissance",
                            value = birthday,
                            iconColor = ProfileColors.Primary
                        )
                    }
                    if (gender.isNotEmpty()) {
                        if (birthday.isNotEmpty()) Spacer(Modifier.height(12.dp))
                        InfoRow(
                            icon = Icons.Outlined.Wc,
                            label = "Genre",
                            value = gender,
                            iconColor = ProfileColors.Primary
                        )
                    }
                    if (createdAt.isNotEmpty()) {
                        if (birthday.isNotEmpty() || gender.isNotEmpty()) Spacer(Modifier.height(12.dp))
                        InfoRow(
                            icon = Icons.Outlined.CalendarToday,
                            label = "Membre depuis",
                            value = createdAt,
                            iconColor = ProfileColors.Primary
                        )
                    }
                }

                // Reservation Management
                ReservationManagementCard(
                    onPendingClick = { navController.navigate(Routes.Reservations) },
                    onAcceptedClick = { navController.navigate(Routes.AcceptedClients) }
                )

                // Fingerprint Settings
                FingerprintSettingsCard(context = context)

                Spacer(Modifier.height(8.dp))

                // Logout Button
                ModernButton(
                    text = "Se déconnecter",
                    icon = Icons.Outlined.Logout,
                    backgroundColor = ProfileColors.CardBackground,
                    textColor = ProfileColors.Primary,
                    borderColor = ProfileColors.Primary
                ) {
                    FirebaseTokenRegistrar.unregisterCurrentToken(context)
                    TokenStorage.clearAuth(context)
                    prefs.edit().clear().apply()
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Main) { inclusive = true }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(username: String, email: String, role: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ProfileColors.Primary,
                        ProfileColors.Primary.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Username
            Text(
                text = username.ifEmpty { "Utilisateur" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(6.dp))

            // Email
            if (email.isNotEmpty()) {
                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(12.dp))
            }

            // Role Badge
            if (role.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = role.uppercase(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onEditProfile: () -> Unit,
    onFavorites: () -> Unit,
    onFeedback: () -> Unit,
    onReservations: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            icon = Icons.Outlined.Edit,
            label = "Modifier",
            color = ProfileColors.Secondary,
            modifier = Modifier.weight(1f),
            onClick = onEditProfile
        )
        QuickActionCard(
            icon = Icons.Outlined.FavoriteBorder,
            label = "Favoris",
            color = ProfileColors.Primary,
            modifier = Modifier.weight(1f),
            onClick = onFavorites
        )
        QuickActionCard(
            icon = Icons.Outlined.RateReview,
            label = "Avis",
            color = ProfileColors.Accent,
            modifier = Modifier.weight(1f),
            onClick = onFeedback
        )
        QuickActionCard(
            icon = Icons.Outlined.EventNote,
            label = "Demandes",
            color = ProfileColors.Success,
            modifier = Modifier.weight(1f),
            onClick = onReservations
        )
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = ProfileColors.CardBackground,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    tint = color
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ProfileColors.TextPrimary
            )
        }
    }
}

@Composable
fun ModernInfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ProfileColors.CardBackground,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = iconColor
                    )
                }
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileColors.TextPrimary
                )
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            color = iconColor.copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                tint = iconColor
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = ProfileColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value.ifEmpty { "Non renseigné" },
                fontSize = 15.sp,
                color = ProfileColors.TextPrimary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun ReservationManagementCard(
    onPendingClick: () -> Unit,
    onAcceptedClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ProfileColors.CardBackground,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = ProfileColors.Warning.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = ProfileColors.Warning
                    )
                }
                Text(
                    text = "Gestion des réservations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileColors.TextPrimary
                )
            }

            Spacer(Modifier.height(20.dp))

            ReservationOptionRow(
                icon = Icons.Outlined.Schedule,
                iconColor = ProfileColors.Warning,
                title = "Demandes en attente",
                subtitle = "Examiner les nouvelles demandes",
                onClick = onPendingClick
            )

            Spacer(Modifier.height(12.dp))
            Divider(color = ProfileColors.Border)
            Spacer(Modifier.height(12.dp))

            ReservationOptionRow(
                icon = Icons.Outlined.CheckCircle,
                iconColor = ProfileColors.Success,
                title = "Clients acceptés",
                subtitle = "Colocataires confirmés",
                onClick = onAcceptedClick
            )
        }
    }
}

@Composable
fun ReservationOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = iconColor.copy(alpha = 0.15f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                tint = iconColor
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProfileColors.TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = ProfileColors.TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = ProfileColors.TextSecondary
        )
    }
}

@Composable
fun ModernButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = borderColor?.let { androidx.compose.foundation.BorderStroke(2.dp, it) },
        shadowElevation = if (borderColor != null) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FingerprintSettingsCard(context: Context) {
    var isEnabled by remember { 
        mutableStateOf(FingerprintManager.isFingerprintEnabled(context)) 
    }
    var isRegistered by remember { 
        mutableStateOf(FingerprintManager.isFingerprintRegistered(context)) 
    }
    
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> false
    }

    fun registerFingerprint() {
        if (!canAuthenticate) {
            Toast.makeText(context, "L'authentification biométrique n'est pas disponible", Toast.LENGTH_SHORT).show()
            return
        }

        if (context !is FragmentActivity) {
            Toast.makeText(context, "Erreur: Activity non supportée", Toast.LENGTH_SHORT).show()
            return
        }

        val executor: Executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            context,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    FingerprintManager.setFingerprintRegistered(context, true)
                    FingerprintManager.setFingerprintEnabled(context, true)
                    isRegistered = true
                    isEnabled = true
                    Toast.makeText(context, "Empreinte enregistrée avec succès!", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            // User canceled
                        }
                        else -> {
                            Toast.makeText(context, "Erreur: $errString", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "Authentification échouée", Toast.LENGTH_SHORT).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enregistrer l'empreinte digitale")
            .setSubtitle("Placez votre doigt sur le capteur")
            .setNegativeButtonText("Annuler")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ProfileColors.CardBackground,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = ProfileColors.Secondary.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            tint = ProfileColors.Secondary
                        )
                    }
                    Column {
                        Text(
                            text = "Empreinte digitale",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ProfileColors.TextPrimary
                        )
                        Text(
                            text = if (isEnabled && isRegistered) "Activée" else if (!isRegistered) "Non enregistrée" else "Désactivée",
                            fontSize = 13.sp,
                            color = ProfileColors.TextSecondary
                        )
                    }
                }

                Switch(
                    checked = isEnabled && isRegistered,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (!isRegistered) {
                                // Need to register first
                                registerFingerprint()
                            } else {
                                // Just enable
                                FingerprintManager.setFingerprintEnabled(context, true)
                                isEnabled = true
                            }
                        } else {
                            // Disable
                            FingerprintManager.setFingerprintEnabled(context, false)
                            isEnabled = false
                        }
                    },
                    enabled = canAuthenticate && isRegistered,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ProfileColors.Success,
                        checkedTrackColor = ProfileColors.Success.copy(alpha = 0.5f)
                    )
                )
            }

            if (!isRegistered && canAuthenticate) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { registerFingerprint() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileColors.Secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Enregistrer l'empreinte")
                }
            }

            if (!canAuthenticate) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "L'authentification biométrique n'est pas disponible sur cet appareil",
                    fontSize = 12.sp,
                    color = ProfileColors.TextSecondary
                )
            }
        }
    }
}