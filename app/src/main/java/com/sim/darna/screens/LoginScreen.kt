package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.R
import com.sim.darna.auth.LoginViewModel
import com.sim.darna.auth.SessionManager
import com.sim.darna.factory.LoginVmFactory
import com.sim.darna.network.NetworkConfig
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit) {

    val context = LocalContext.current // ✅ Needed for Toast messages

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    // ✅ ViewModel setup (backend Nest sur la machine hôte, accès depuis téléphone réel)
    val sessionManager = remember { SessionManager(context.applicationContext) }
    // Forcer le rafraîchissement de l'URL à chaque affichage de l'écran
    val baseUrl = remember { NetworkConfig.getBaseUrl(context.applicationContext, forceRefresh = true) }
    val viewModel: LoginViewModel = viewModel(
        factory = LoginVmFactory(baseUrl, sessionManager)
    )
    val uiState = viewModel.state.collectAsState().value
    
    // Rafraîchir l'URL si une erreur de connexion survient
    LaunchedEffect(uiState.error) {
        val error = uiState.error
        if (error != null && (error.contains("Timeout") || error.contains("Impossible de joindre"))) {
            // Forcer le rafraîchissement du cache pour la prochaine tentative
            NetworkConfig.clearCache()
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // ✅ Validation
    fun validateEmail(emailStr: String): String? {
        return when {
            emailStr.isBlank() -> "L'email est requis"
            !emailStr.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> "Format d'email invalide"
            else -> null
        }
    }

    fun validatePassword(pass: String): String? {
        return when {
            pass.isBlank() -> "Le mot de passe est requis"
            pass.length < 6 -> "Le mot de passe doit contenir au moins 6 caractères"
            else -> null
        }
    }

    fun validateAll(): Boolean {
        emailError = validateEmail(email)
        passwordError = validatePassword(password)
        return emailError == null && passwordError == null
    }

    // ✅ React to successful login
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            Toast.makeText(context, "Connexion réussie ✅", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    // ✅ React to login error (auto-toast)
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Connexion",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Connectez-vous à votre compte",
            fontSize = 14.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = validateEmail(it)
            },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email"
                )
            },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00B8D4),
                focusedLabelColor = Color(0xFF00B8D4)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = validatePassword(it)
            },
            label = { Text("Mot de passe") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password"
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Masquer" else "Afficher"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00B8D4),
                focusedLabelColor = Color(0xFF00B8D4)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot Password
        TextButton(
            onClick = { /* TODO: Forgot password */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Mot de passe oublié ?",
                color = Color(0xFF00B8D4),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ Loading indicator
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ✅ Error text in red (in addition to Toast) - Formaté avec Card pour meilleure lisibilité
        uiState.error?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFCDD2))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Erreur de connexion",
                            color = Color(0xFFD32F2F),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Parser et afficher le message d'erreur avec formatage
                    val lines = errorMessage.split("\n")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        lines.forEach { line ->
                            when {
                                line.contains("⚠️") -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = line.replace("⚠️", "").trim(),
                                            color = Color(0xFFB71C1C),
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                line.contains("✅") -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = line.replace("✅", "").trim(),
                                            color = Color(0xFF1B5E20),
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                line.contains("💡") -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = line.replace("💡", "").trim(),
                                            color = Color(0xFF757575),
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                                line.trim().isEmpty() -> {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                line.trim().startsWith("1.") || line.trim().startsWith("2.") || 
                                line.trim().startsWith("3.") || line.trim().startsWith("4.") -> {
                                    Text(
                                        text = line.trim(),
                                        color = Color(0xFF424242),
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = line.trim(),
                                        color = Color(0xFFB71C1C),
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Afficher l'URL actuelle avec possibilité de rafraîchir
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "URL actuelle du serveur:",
                                        fontSize = 11.sp,
                                        color = Color(0xFF757575),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = baseUrl,
                                        fontSize = 11.sp,
                                        color = Color(0xFF424242),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        NetworkConfig.clearCache()
                                        // Recharger l'URL
                                        val newUrl = NetworkConfig.getBaseUrl(context.applicationContext, forceRefresh = true)
                                        // Note: Pour vraiment recharger, il faudrait recréer le ViewModel
                                        // mais pour l'instant, on affiche juste un message
                                        Toast.makeText(
                                            context,
                                            "Cache vidé. Recompilez l'app si l'IP a changé.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                ) {
                                    Text(
                                        text = "Rafraîchir",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "💡 Si vous avez changé de WiFi, modifiez backend_url.txt avec la nouvelle IP du serveur",
                                fontSize = 10.sp,
                                color = Color(0xFF757575),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Login Button
        Button(
            onClick = {
                if (validateAll()) {
                    coroutineScope.launch {
                        viewModel.login(email, password)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF00B8D4), Color(0xFF00E5FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.isLoading) "Connexion..." else "Se connecter", // ✅ shows loading text
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            Text(
                text = "  ou  ",
                color = Color(0xFF757575),
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Button
        OutlinedButton(
            onClick = onSignUp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00B8D4)),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF00B8D4), Color(0xFF00E5FF))
                )
            )
        ) {
            Text(
                text = "Créer un compte",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
