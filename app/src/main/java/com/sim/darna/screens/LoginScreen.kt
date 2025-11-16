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
import androidx.lifecycle.viewmodel.compose.viewModel as viewModelCompose
import androidx.navigation.NavController
import com.sim.darna.R
import com.sim.darna.ViewModel.LoginViewModel
import com.sim.darna.factory.LoginVmFactory
import com.sim.darna.navigation.Routes
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun LoginScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val baseUrl = "http://10.61.177.155:3000/"
    val viewModel: LoginViewModel = viewModelCompose(factory = LoginVmFactory(baseUrl))
    val uiState = viewModel.state.collectAsState().value

    val coroutineScope = rememberCoroutineScope()

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

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            Toast.makeText(context, "Connexion réussie ✅", Toast.LENGTH_SHORT).show()
            val username = uiState.user?.username ?: "User"
            val encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.toString())
            navController.navigate("${Routes.Home}?username=$encodedUsername") {
                popUpTo(Routes.Login) { inclusive = true }
            }
        }
    }

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
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App logo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Connexion",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Connectez-vous à votre compte",
            fontSize = 14.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = validateEmail(it)
            },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
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

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = validatePassword(it)
            },
            label = { Text("Mot de passe") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
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

        TextButton(
            onClick = { navController.navigate(Routes.ForgotPassword) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Mot de passe oublié ?", color = Color(0xFF00B8D4), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        uiState.error?.let {
            Text(it, color = Color.Red, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (validateAll()) {
                    coroutineScope.launch { viewModel.login(email, password) }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(listOf(Color(0xFF00B8D4), Color(0xFF00E5FF)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.isLoading) "Connexion..." else "Se connecter",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            Text("  ou  ", color = Color(0xFF757575), fontSize = 14.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
        }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { navController.navigate(Routes.SignUp) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00B8D4)),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(Color(0xFF00B8D4), Color(0xFF00E5FF)))
            )
        ) {
            Text("Créer un compte", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
