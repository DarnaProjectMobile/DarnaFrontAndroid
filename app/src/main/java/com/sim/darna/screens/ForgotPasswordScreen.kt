package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sim.darna.ViewModel.ForgotPasswordViewModel
import com.sim.darna.factory.ForgotPasswordVmFactory
import com.sim.darna.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // 🧠 ViewModel setup
    val baseUrl = "http://10.61.177.155:3000/"
    val viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordVmFactory(baseUrl)
    )
    val uiState = viewModel.state.collectAsState().value

    // ✅ Toast or success handling
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            Toast.makeText(context, uiState.message ?: "Code envoyé ✅", Toast.LENGTH_SHORT).show()
            navController.navigate(Routes.ResetPassword) {
                popUpTo(Routes.ForgotPassword) { inclusive = true }
            }
        }
    }


    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    fun validateEmail(input: String): String? {
        return when {
            input.isBlank() -> "L'email est requis"
            !input.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) ->
                "Format d'email invalide"
            else -> null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mot de passe oublié", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Entrez votre adresse e-mail pour recevoir un code de réinitialisation.",
                fontSize = 15.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = validateEmail(it.text)
                },
                label = { Text("Email") },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    emailError = validateEmail(email.text)
                    if (emailError == null) {
                        viewModel.sendResetCode(email.text)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B8D4))
            ) {
                Text(
                    text = if (uiState.isLoading) "Envoi..." else "Envoyer le code",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Retour à la connexion", color = Color(0xFF00B8D4), fontSize = 14.sp)
            }
        }
    }
}
