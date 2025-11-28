package com.sim.darna.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.auth.RegisterRequest
import com.sim.darna.auth.RegisterViewModel
import com.sim.darna.factory.RegisterVmFactory
import com.sim.darna.network.NetworkConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(onScanIdClick: () -> Unit = {}) {
    // --- Fields ---
    var username by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf("") }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var birthDate by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf("") }
    var genderError by remember { mutableStateOf<String?>(null) }
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Homme", "Femme", "Autre")
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // --- ViewModel --- (backend Nest sur la machine hôte, accès depuis téléphone réel)
    val context = LocalContext.current
    val baseUrl = remember { NetworkConfig.getBaseUrl(context.applicationContext) }
    val viewModel: RegisterViewModel = viewModel(factory = RegisterVmFactory(baseUrl))
    val uiState = viewModel.state.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Navigation after success + Snackbar
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHostState.showSnackbar("Inscription réussie 🎉")
            onScanIdClick() // 🚀 navigate to ID Scan screen
        }
    }

    // --- Validation functions ---
    fun validateUsername(name: String) = when {
        name.isBlank() -> "Le nom d'utilisateur est requis"
        name.length < 3 -> "Minimum 3 caractères"
        !name.matches(Regex("^[a-zA-Z0-9_.-]+$")) -> "Caractères non valides"
        else -> null
    }

    fun validateFullName(name: String) = when {
        name.isBlank() -> "Le nom complet est requis"
        name.length < 3 -> "Minimum 3 caractères"
        else -> null
    }

    fun validateBirthDate(date: String) = when {
        date.isBlank() -> "La date de naissance est requise"
        !date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> "Format invalide (AAAA-MM-JJ)"
        else -> null
    }

    fun validateEmail(email: String) = when {
        email.isBlank() -> "Email requis"
        !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> "Format d'email invalide"
        else -> null
    }

    fun validatePassword(pass: String) = when {
        pass.isBlank() -> "Mot de passe requis"
        pass.length < 6 -> "Minimum 6 caractères"
        else -> null
    }

    fun validateConfirmPassword(pass: String, confirm: String) = when {
        confirm.isBlank() -> "Confirmez le mot de passe"
        pass != confirm -> "Les mots de passe ne correspondent pas"
        else -> null
    }

    fun validatePhoneNumber(phone: String) = when {
        phone.isBlank() -> "Numéro requis"
        !phone.matches(Regex("^[+]?[0-9]{8,15}$")) -> "Numéro invalide"
        else -> null
    }

    fun validateGender(genderStr: String) =
        if (genderStr.isBlank()) "Le genre est requis" else null

    fun validateAll(): Boolean {
        usernameError = validateUsername(username)
        fullNameError = validateFullName(fullName)
        birthDateError = validateBirthDate(birthDate)
        emailError = validateEmail(email)
        phoneNumberError = validatePhoneNumber(phoneNumber)
        genderError = validateGender(gender)
        passwordError = validatePassword(password)
        confirmPasswordError = validateConfirmPassword(password, confirmPassword)
        return listOf(
            usernameError, fullNameError, birthDateError, emailError,
            phoneNumberError, genderError, passwordError, confirmPasswordError
        ).all { it == null }
    }

    // --- UI ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00B8D4), modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Créer votre compte", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Remplissez vos informations pour commencer", fontSize = 14.sp, color = Color(0xFF757575), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            // --- Fields ---
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; usernameError = validateUsername(it) },
                label = { Text("Nom d'utilisateur") },
                leadingIcon = { Icon(Icons.Default.PersonOutline, null) },
                isError = usernameError != null,
                supportingText = usernameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; fullNameError = validateFullName(it) },
                label = { Text("Nom complet") },
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                isError = fullNameError != null,
                supportingText = fullNameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it; birthDateError = validateBirthDate(it) },
                label = { Text("Date de naissance (AAAA-MM-JJ)") },
                leadingIcon = { Icon(Icons.Default.DateRange, null) },
                placeholder = { Text("1999-05-01") },
                isError = birthDateError != null,
                supportingText = birthDateError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = validateEmail(it) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^[+]?[0-9]*$"))) {
                        phoneNumber = it
                        phoneNumberError = validatePhoneNumber(it)
                    }
                },
                label = { Text("Numéro de téléphone") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                placeholder = { Text("+216 12 345 678") },
                isError = phoneNumberError != null,
                supportingText = phoneNumberError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender Dropdown
            ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = it }) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Genre") },
                    leadingIcon = { Icon(Icons.Default.Face, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    isError = genderError != null,
                    supportingText = genderError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gender = option
                                genderError = validateGender(option)
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = validatePassword(it); confirmPasswordError = validateConfirmPassword(it, confirmPassword) },
                label = { Text("Mot de passe") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmPasswordError = validateConfirmPassword(password, it) },
                label = { Text("Confirmer le mot de passe") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.error?.let {
                Text(text = it, color = Color.Red, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (validateAll()) {
                        val request = RegisterRequest(
                            username = username,
                            email = email,
                            password = password,
                            role = "client", // must be lowercase for NestJS
                            dateDeNaissance = "${birthDate}T00:00:00.000Z",
                            numTel = phoneNumber,
                            gender = when (gender) {
                                "Homme" -> "Male"
                                "Femme" -> "Female"
                                else -> "Other"
                            },
                            image = null
                        )
                        coroutineScope.launch {
                            viewModel.register(request)
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
                        text = if (uiState.success) "Inscription réussie 🎉" else "Créer un compte",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "En continuant, vous acceptez nos conditions d'utilisation",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
