package com.sim.darna.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.auth.TokenStorage
import com.sim.darna.notifications.FirebaseTokenRegistrar
import com.sim.darna.viewmodel.LoginViewModel
import com.sim.darna.factory.LoginVmFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit) {

    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    // ✅ ViewModel setup (your base URL here)
    // Pour l'émulateur Android, utilisez: "http://10.0.2.2:3000/"
    // Pour un appareil physique sur le même réseau WiFi,
    //val baseUrl =  "http://192.168.1.14:3000/"
    val baseUrl = "http://10.0.2.2:3000/"
    val viewModel: LoginViewModel = viewModel(factory = LoginVmFactory(baseUrl, context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)))
    val uiState = viewModel.state.collectAsState().value

    val coroutineScope = rememberCoroutineScope()
    var rememberMe by remember { mutableStateOf(false) }

    // Animation states
    var visible by remember { mutableStateOf(false) }
    var buttonScale by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // ---------------- VALIDATION ---------------------

    fun validateEmail(emailStr: String): String? {
        return when {
            emailStr.isBlank() -> "L'email est requis"
            !emailStr.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) ->
                "Format d'email invalide"
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

    // ---------------- LOAD REMEMBER ME ---------------------
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)

        val savedEmail = prefs.getString("saved_email", "")
        val savedPassword = prefs.getString("saved_password", "")
        val remember = prefs.getBoolean("remember_me", false)

        if (remember && !savedEmail.isNullOrBlank()) {
            email = savedEmail
            password = savedPassword ?: ""
            rememberMe = true
        }
    }

    // ---------------- WHEN LOGIN SUCCESS ---------------------

    LaunchedEffect(uiState.loginResponse) {
        val response = uiState.loginResponse ?: return@LaunchedEffect

        val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)

        val editor = prefs.edit()

        // ---- SAVE TOKEN ----
        TokenStorage.saveAuthData(context, response.token, response.user.id)

        // ---- SAVE FULL USER INFO ----
        val user = response.user

        editor.putString("user_id", user.id)
        editor.putString("username", user.username)
        editor.putString("email", user.email)
        editor.putString("role", user.role)
        editor.putString("dateDeNaissance", user.dateDeNaissance)
        editor.putString("numTel", user.numTel)
        editor.putString("gender", user.gender)
        editor.putString("image", user.image ?: "")
        editor.putString("createdAt", user.createdAt)
        editor.putString("updatedAt", user.updatedAt)

        // ---- REMEMBER ME ----
        if (rememberMe) {
            editor.putString("saved_email", email)
            editor.putString("saved_password", password)
            editor.putBoolean("remember_me", true)
        } else {
            editor.remove("saved_email")
            editor.remove("saved_password")
            editor.remove("remember_me")
        }

        editor.apply()

        FirebaseTokenRegistrar.syncCurrentToken(context)
        onLoginSuccess()
    }

    // ---------------- ERROR HANDLING ---------------------

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- GRADIENT COLORS ---------------------
    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF1A237E),  // Deep indigo
            Color(0xFF0D47A1),  // Deep blue
            Color(0xFF01579B)   // Darker blue
        )
    } else {
        listOf(
            Color(0xFF667EEA),  // Purple
            Color(0xFF764BA2),  // Purple-pink
            Color(0xFF5E72E4)   // Blue
        )
    }

    val textColor = if (isDark) Color.White else Color.White
    val inputBgColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.25f)
    val inputTextColor = if (isDark) Color.White else Color.White

    // ---------------- UI ---------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            // App Icon + Title with animation
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Chat bubble icon
                    Icon(
                        Icons.Default.ChatBubble,
                        contentDescription = "App Icon",
                        modifier = Modifier.size(64.dp),
                        tint = textColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "DARNA",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Bienvenue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        color = textColor.copy(alpha = 0.85f),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // EMAIL INPUT
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                        slideInVertically(
                            initialOffsetY = { 40 },
                            animationSpec = tween(600, delayMillis = 200)
                        )
            ) {
                Column {
                    Text(
                        text = "EMAIL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = validateEmail(it)
                        },
                        placeholder = {
                            Text(
                                "Entrez votre email",
                                color = inputTextColor.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = inputTextColor.copy(alpha = 0.7f)
                            )
                        },
                        isError = emailError != null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = inputBgColor,
                            unfocusedContainerColor = inputBgColor,
                            focusedTextColor = inputTextColor,
                            unfocusedTextColor = inputTextColor,
                            cursorColor = inputTextColor,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                            errorContainerColor = inputBgColor,
                            errorIndicatorColor = Color(0xFFEF5350),
                            errorTextColor = inputTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (emailError != null) {
                        Text(
                            text = emailError!!,
                            color = Color(0xFFFFCDD2),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PASSWORD INPUT
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                        slideInVertically(
                            initialOffsetY = { 40 },
                            animationSpec = tween(600, delayMillis = 300)
                        )
            ) {
                Column {
                    Text(
                        text = "MOT DE PASSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = validatePassword(it)
                        },
                        placeholder = {
                            Text(
                                "Entrez votre mot de passe",
                                color = inputTextColor.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = inputTextColor.copy(alpha = 0.7f)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = inputTextColor.copy(alpha = 0.7f)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = inputBgColor,
                            unfocusedContainerColor = inputBgColor,
                            focusedTextColor = inputTextColor,
                            unfocusedTextColor = inputTextColor,
                            cursorColor = inputTextColor,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                            errorContainerColor = inputBgColor,
                            errorIndicatorColor = Color(0xFFEF5350),
                            errorTextColor = inputTextColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = Color(0xFFFFCDD2),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // REMEMBER ME
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 400))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            rememberMe = !rememberMe
                            if (!rememberMe) {
                                val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                                prefs.edit()
                                    .remove("saved_email")
                                    .remove("saved_password")
                                    .remove("remember_me")
                                    .apply()
                            }
                        }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = {
                            rememberMe = it
                            if (!it) {
                                val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                                prefs.edit()
                                    .remove("saved_email")
                                    .remove("saved_password")
                                    .remove("remember_me")
                                    .apply()
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.White.copy(alpha = 0.7f),
                            checkmarkColor = if (isDark) Color(0xFF1A237E) else Color(0xFF667EEA)
                        )
                    )
                    Text(
                        text = "Se souvenir de moi",
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // LOGIN BUTTON with scale animation
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 500)) +
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
            ) {
                Button(
                    onClick = {
                        if (validateAll()) {
                            coroutineScope.launch {
                                buttonScale = 0.95f
                                delay(100)
                                buttonScale = 1f
                                viewModel.login(email, password)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(buttonScale),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = if (isDark) Color(0xFF1A237E) else Color(0xFF667EEA),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            "SE CONNECTER",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF1A237E) else Color(0xFF667EEA),
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // CREATE ACCOUNT
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 600))
            ) {
                TextButton(onClick = onSignUp) {
                    Text(
                        text = "Pas de compte ? ",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Créer un compte",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}