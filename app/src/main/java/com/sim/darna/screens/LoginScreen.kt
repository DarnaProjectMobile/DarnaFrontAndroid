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
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment
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
import com.sim.darna.network.NetworkConfigManager
import com.sim.darna.firebase.FirebaseNotificationManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit) {

    val context = LocalContext.current // ✅ Needed for Toast messages

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showIpConfigDialog by remember { mutableStateOf(false) }
    var ipInput by remember { mutableStateOf("") }

    // ✅ ViewModel setup (backend Nest sur la machine hôte, accès depuis téléphone réel)
    // Forcer le rafraîchissement de l'URL à chaque affichage de l'écran
    var baseUrl by remember { mutableStateOf(NetworkConfig.getBaseUrl(context.applicationContext, forceRefresh = true)) }
    val sessionManager = remember { SessionManager(context.applicationContext) }
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
            
            // Enregistrer le token Firebase après connexion réussie
            coroutineScope.launch {
                try {
                    val baseUrl = NetworkConfig.getBaseUrl(context.applicationContext)
                    val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                    val logging = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    val client = OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            try {
                                val token = runBlocking { sessionManager.getToken() }
                                val requestBuilder = chain.request().newBuilder()
                                if (!token.isNullOrBlank()) {
                                    requestBuilder.addHeader("Authorization", "Bearer $token")
                                }
                                requestBuilder.addHeader("Accept", "application/json")
                                chain.proceed(requestBuilder.build())
                            } catch (e: Exception) {
                                chain.proceed(chain.request().newBuilder()
                                    .addHeader("Accept", "application/json")
                                    .build())
                            }
                        }
                        .addInterceptor(logging)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build()
                    
                    val retrofit = Retrofit.Builder()
                        .baseUrl(normalizedBaseUrl)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    
                    val notificationApi = retrofit.create(com.sim.darna.firebase.FirebaseNotificationApi::class.java)
                    FirebaseNotificationManager.registerToken(context.applicationContext, sessionManager, notificationApi)
                    android.util.Log.d("LoginScreen", "Token Firebase enregistré avec succès")
                } catch (e: Exception) {
                    android.util.Log.e("LoginScreen", "Erreur lors de l'enregistrement du token Firebase", e)
                    // Ne pas bloquer la connexion si l'enregistrement du token échoue
                }
            }
            
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
    
    // Dialog pour configurer l'IP manuellement
    if (showIpConfigDialog) {
        AlertDialog(
            onDismissRequest = { showIpConfigDialog = false },
            title = {
                Text(
                    text = "Configurer l'adresse du serveur",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Entrez l'adresse IP du serveur (sans http:// et :3007)",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it },
                        label = { Text("Adresse IP") },
                        placeholder = { Text("192.168.1.101") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null
                            )
                        }
                    )
                    Text(
                        text = "💡 L'URL complète sera: http://$ipInput:3007/",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showIpConfigDialog = false }
                    ) {
                        Text("Annuler")
                    }
                    Button(
                        onClick = {
                            if (ipInput.isNotBlank()) {
                                val newUrl = "http://${ipInput.trim()}:3007/"
                                NetworkConfigManager.saveBackendUrl(context.applicationContext, newUrl)
                                // Refresh baseUrl from NetworkConfig to ensure consistency
                                baseUrl = NetworkConfig.getBaseUrl(context.applicationContext, forceRefresh = true)
                                showIpConfigDialog = false
                                ipInput = ""
                                Toast.makeText(
                                    context,
                                    "IP configurée: $newUrl",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = ipInput.isNotBlank()
                    ) {
                        Text("Sauvegarder")
                    }
                }
            }
        )
    }
}
