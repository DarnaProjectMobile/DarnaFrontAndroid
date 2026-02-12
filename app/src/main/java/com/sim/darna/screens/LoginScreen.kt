package com.sim.darna.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.sim.darna.auth.TokenStorage
import com.sim.darna.notifications.FirebaseTokenRegistrar
import com.sim.darna.utils.ApiConfig
import com.sim.darna.utils.FingerprintManager
import com.sim.darna.viewmodel.LoginViewModel
import com.sim.darna.factory.LoginVmFactory
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
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val sharedPreferences = LocalContext.current.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val viewModel: LoginViewModel = viewModel(
        factory = LoginVmFactory(
            baseUrl = ApiConfig.BASE_URL,
            sharedPreferences = sharedPreferences
        )
    )
    val uiState by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    var visible by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Fingerprint state
    val isFingerprintEnabled = remember {
        mutableStateOf(FingerprintManager.isFingerprintEnabled(context))
    }
    val canUseFingerprint = remember {
        val biometricManager = BiometricManager.from(context)
        mutableStateOf(
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        )
    }

    // Validation functions
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

    // Load remember me
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

    // When login success
    LaunchedEffect(uiState.loginResponse) {
        val response = uiState.loginResponse ?: return@LaunchedEffect
        val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        TokenStorage.saveAuthData(context, response.token, response.user.id)

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

        // Save remember me credentials if checked
        if (rememberMe) {
            editor.putString("saved_email", email)
            editor.putString("saved_password", password)
            editor.putBoolean("remember_me", true)
        } else {
            editor.remove("saved_email")
            editor.remove("saved_password")
            editor.remove("remember_me")
        }

        // Always save credentials for fingerprint authentication (independent of remember me)
        // This allows fingerprint to work even if remember me is not checked
        FingerprintManager.saveFingerprintCredentials(context, email, password)

        editor.apply()
        FirebaseTokenRegistrar.syncCurrentToken(context)
        onLoginSuccess()
    }

    // Error handling
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF5F7),
                        Color(0xFFF8F9FF),
                        Color(0xFFFFFBF0)
                    )
                )
            )
    ) {
        // Animated background circles
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset((-100).dp, (-150).dp)
                .rotate(rotation)
                .alpha(0.15f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryColor,
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomEnd)
                .offset(100.dp, 100.dp)
                .rotate(-rotation * 0.7f)
                .alpha(0.12f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SecondaryColor,
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(50.dp, (-50).dp)
                .rotate(rotation * 0.5f)
                .alpha(0.1f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentColor,
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo Section with animation
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
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = SurfaceColor,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryColor.copy(alpha = 0.1f),
                                        SecondaryColor.copy(alpha = 0.1f)
                                    )
                                )
                            )
                    ) {
                        // TODO: Replace with your logo
                        // Image(
                        //     painter = painterResource(id = R.drawable.your_logo),
                        //     contentDescription = "Logo",
                        //     modifier = Modifier.size(80.dp)
                        // )

                        Icon(
                            Icons.Outlined.Home,
                            contentDescription = "Logo",
                            modifier = Modifier.size(60.dp),
                            tint = PrimaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome Text with animation
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                        slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = tween(600, delayMillis = 200)
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bon retour !",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ravi de vous revoir parmi nous",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Login Form Card with animation
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) +
                        slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceColor,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Email Field
                        Column {
                            Text(
                                text = "Email",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = validateEmail(it)
                                },
                                placeholder = { Text("votre@email.com", color = TextSecondary) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Email,
                                        contentDescription = null,
                                        tint = SecondaryColor
                                    )
                                },
                                isError = emailError != null,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SecondaryColor.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color(0xFFF8F9FA),
                                    focusedBorderColor = SecondaryColor,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    errorBorderColor = PrimaryColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )

                            if (emailError != null) {
                                Text(
                                    text = emailError!!,
                                    color = PrimaryColor,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }

                        // Password Field
                        Column {
                            Text(
                                text = "Mot de passe",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = validatePassword(it)
                                },
                                placeholder = { Text("••••••••", color = TextSecondary) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = SecondaryColor
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = passwordError != null,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SecondaryColor.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color(0xFFF8F9FA),
                                    focusedBorderColor = SecondaryColor,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    errorBorderColor = PrimaryColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )

                            if (passwordError != null) {
                                Text(
                                    text = passwordError!!,
                                    color = PrimaryColor,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }

                        // Remember Me
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    rememberMe = !rememberMe
                                    if (!rememberMe) {
                                        val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                                        prefs.edit()
                                            .remove("saved_email")
                                            .remove("saved_password")
                                            .remove("remember_me")
                                            .apply()
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = {
                                    rememberMe = it
                                    if (!it) {
                                        val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                                        prefs.edit()
                                            .remove("saved_email")
                                            .remove("saved_password")
                                            .remove("remember_me")
                                            .apply()
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SecondaryColor,
                                    uncheckedColor = Color(0xFFBDBDBD)
                                )
                            )
                            Text(
                                text = "Se souvenir de moi",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Login Button with gradient
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
                            shape = RoundedCornerShape(16.dp),
                            enabled = !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = PrimaryColor.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(0.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                PrimaryColor,
                                                Color(0xFFFF6B8A)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = SurfaceColor,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        "Se connecter",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SurfaceColor,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Fingerprint Login - Show if registered (even if not enabled, it will auto-enable on use)
            if (canUseFingerprint.value && FingerprintManager.isFingerprintRegistered(context)) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 600))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFFE0E0E0),
                                thickness = 1.dp
                            )
                            Text(
                                text = "OU",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFFE0E0E0),
                                thickness = 1.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            onClick = {
                                authenticateWithFingerprint(context, viewModel, onLoginSuccess)
                            },
                            modifier = Modifier.size(70.dp),
                            shape = CircleShape,
                            color = SurfaceColor,
                            shadowElevation = 6.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(SecondaryColor, PrimaryColor)
                                )
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                SecondaryColor.copy(alpha = 0.1f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Connexion par empreinte",
                                    tint = SecondaryColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Connexion rapide",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Sign Up Link with animation
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 700))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSignUp() }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pas encore de compte ? ",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "S'inscrire",
                        color = PrimaryColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Fingerprint authentication function
private fun authenticateWithFingerprint(
    context: Context,
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
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
                // Auto-enable fingerprint if not already enabled
                if (!FingerprintManager.isFingerprintEnabled(context)) {
                    FingerprintManager.setFingerprintEnabled(context, true)
                }
                
                // Get saved credentials for fingerprint
                val (email, password) = FingerprintManager.getSavedCredentials(context)
                if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
                    // Auto login with saved credentials
                    viewModel.login(email, password)
                } else {
                    Toast.makeText(context, "Aucune information de connexion sauvegardée", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {}
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
        .setTitle("Connexion par empreinte")
        .setSubtitle("Placez votre doigt sur le capteur pour vous connecter")
        .setNegativeButtonText("Annuler")
        .build()

    biometricPrompt.authenticate(promptInfo)
}