package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import com.sim.darna.factory.LoginVmFactory
import com.sim.darna.utils.SessionManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit) {

    val context = LocalContext.current // ✅ Needed for Toast messages
    val sessionManager = remember { SessionManager(context) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    // ✅ ViewModel setup (your base URL here)
    val baseUrl = "http://10.0.2.2:3000/api/" // Pour l'émulateur Android (utilise le préfixe /api)
    // val baseUrl = "http://192.168.1.11:3000/api/" // Pour un appareil physique
    val viewModel: LoginViewModel = viewModel(factory = LoginVmFactory(baseUrl, sessionManager))
    val uiState = viewModel.state.collectAsState().value

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

        // ✅ Error text in red (in addition to Toast)
        uiState.error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
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
