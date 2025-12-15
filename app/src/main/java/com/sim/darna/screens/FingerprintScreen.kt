package com.sim.darna.screens

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.sim.darna.utils.FingerprintManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

// Modern Color Palette
private val PrimaryColor = Color(0xFFFF4B6E)
private val SecondaryColor = Color(0xFF4C6FFF)
private val AccentColor = Color(0xFFFFC857)
private val BackgroundColor = Color(0xFFF7F7F7)
private val SurfaceColor = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)

@Composable
fun FingerprintScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit = onNext
) {
    val context = LocalContext.current
    var isRegistering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Check if biometric is available
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> false
    }

    // Register fingerprint
    fun registerFingerprint() {
        if (!canAuthenticate) {
            errorMessage = "L'authentification biométrique n'est pas disponible sur cet appareil"
            return
        }

        isRegistering = true
        errorMessage = null

        if (context !is FragmentActivity) {
            errorMessage = "Erreur: Activity non supportée"
            isRegistering = false
            return
        }

        val executor: Executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            context,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isRegistering = false
                    FingerprintManager.setFingerprintRegistered(context, true)
                    successMessage = "Empreinte digitale enregistrée avec succès!"
                    
                    // Auto-enable fingerprint login
                    FingerprintManager.setFingerprintEnabled(context, true)
                    
                    // Navigate after a short delay
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1500)
                        onNext()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isRegistering = false
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            // User canceled, don't show error
                        }
                        else -> {
                            errorMessage = "Erreur: $errString"
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    isRegistering = false
                    errorMessage = "Authentification échouée. Veuillez réessayer."
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enregistrer l'empreinte digitale")
            .setSubtitle("Placez votre doigt sur le capteur pour sécuriser votre compte")
            .setNegativeButtonText("Annuler")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = PrimaryColor.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = PrimaryColor
                    )
                }
            }

            // Title
            Text(
                text = "Sécurisez votre compte",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            // Description
            Text(
                text = "Activez l'authentification par empreinte digitale pour un accès rapide et sécurisé à votre compte",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            // Error/Success Messages
            errorMessage?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            successMessage?.let { success ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = success,
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register Button
            Button(
                onClick = { registerFingerprint() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canAuthenticate && !isRegistering,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    disabledContainerColor = PrimaryColor.copy(alpha = 0.5f)
                )
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SurfaceColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ajouter l'empreinte",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Skip Button
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Passer cette étape",
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }

            // Biometric availability info
            if (!canAuthenticate) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "L'authentification biométrique n'est pas disponible sur cet appareil",
                            color = Color(0xFFFF9800),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
