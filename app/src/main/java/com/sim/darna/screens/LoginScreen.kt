package com.sim.darna.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.sim.darna.auth.TokenStorage
import com.sim.darna.viewmodel.LoginViewModel
import com.sim.darna.factory.LoginVmFactory
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit) {

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val baseUrl = "http://10.0.2.2:3000/"
    val sharedPreferences = LocalContext.current.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val viewModel: LoginViewModel = viewModel(
        factory = LoginVmFactory(
            baseUrl = baseUrl,
            sharedPreferences = sharedPreferences
        )
    )
    val uiState by viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var rememberMe by remember { mutableStateOf(false) }

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
        onLoginSuccess()
    }




    // ---------------- ERROR HANDLING ---------------------

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- UI ---------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Card(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "App logo",
                        modifier = Modifier.size(70.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Connexion",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Connectez-vous à votre compte",
                fontSize = 15.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // EMAIL INPUT
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = validateEmail(it)
                    },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color(0xFF2196F3)
                        )
                    },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it, color = Color(0xFFEF5350)) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedLabelColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PASSWORD INPUT
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = validatePassword(it)
                    },
                    label = { Text("Mot de passe") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = Color(0xFF2196F3)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF757575)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it, color = Color(0xFFEF5350)) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedLabelColor = Color(0xFF2196F3),
                        cursorColor = Color(0xFF2196F3)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // REMEMBER ME & FORGOT PASSWORD
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
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
                            checkedColor = Color(0xFF2196F3),
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        text = "Se souvenir de moi",
                        color = Color(0xFF424242),
                        fontSize = 14.sp
                    )
                }

                TextButton(onClick = {}) {
                    Text(
                        text = "Mot de passe oublié ?",
                        color = Color(0xFF2196F3),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ERROR MESSAGE
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error!!,
                            color = Color(0xFFEF5350),
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // LOGIN BUTTON
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
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    disabledContainerColor = Color(0xFFBBDEFB)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Se connecter",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0)
                )
                Text(
                    "  ou  ",
                    color = Color(0xFF9E9E9E),
                    fontSize = 14.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF2196F3)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2196F3))
                )
            ) {
                Text(
                    "Créer un compte",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}