package com.sim.darna.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.sim.darna.model.RegisterRequest
import com.sim.darna.ViewModel.RegisterViewModel
import com.sim.darna.factory.RegisterVmFactory
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(onScanIdClick: () -> Unit = {}) {
    var currentStep by remember { mutableStateOf(1) }

    // Step 1 Fields
    var username by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf("") }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    // Step 2 Fields
    var birthDate by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf("") }
    var genderError by remember { mutableStateOf<String?>(null) }
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Homme", "Femme")

    // Step 3 Fields
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val baseUrl = "http://10.61.177.155:3000/"
    val viewModel: RegisterViewModel = viewModel(factory = RegisterVmFactory(baseUrl))
    val uiState = viewModel.state.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHostState.showSnackbar("Inscription réussie 🎉")
            onScanIdClick()
        }
    }

    // Validation functions
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

    fun validateStep1(): Boolean {
        usernameError = validateUsername(username)
        fullNameError = validateFullName(fullName)
        emailError = validateEmail(email)
        return listOf(usernameError, fullNameError, emailError).all { it == null }
    }

    fun validateStep2(): Boolean {
        birthDateError = validateBirthDate(birthDate)
        phoneNumberError = validatePhoneNumber(phoneNumber)
        genderError = validateGender(gender)
        return listOf(birthDateError, phoneNumberError, genderError).all { it == null }
    }

    fun validateStep3(): Boolean {
        passwordError = validatePassword(password)
        confirmPasswordError = validateConfirmPassword(password, confirmPassword)
        return listOf(passwordError, confirmPasswordError).all { it == null }
    }

    fun openBirthDatePicker(currentValue: String?, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        currentValue?.takeIf { it.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) }?.split("-")?.let { parts ->
            val year = parts.getOrNull(0)?.toIntOrNull()
            val month = parts.getOrNull(1)?.toIntOrNull()?.minus(1)
            val day = parts.getOrNull(2)?.toIntOrNull()
            if (year != null && month != null && day != null) {
                calendar.set(year, month, day)
            }
        }

        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF5F9FF), Color.White)
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ✅ Centered Step Progress Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                StepProgressIndicator(currentStep = currentStep)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Animated Header
            AnimatedHeader(currentStep = currentStep)

            Spacer(modifier = Modifier.height(32.dp))

            // Step Content with Animation
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "step_content"
            ) { step ->
                when (step) {
                    1 -> Step1Content(
                        username = username,
                        onUsernameChange = { username = it; usernameError = validateUsername(it) },
                        usernameError = usernameError,
                        fullName = fullName,
                        onFullNameChange = { fullName = it; fullNameError = validateFullName(it) },
                        fullNameError = fullNameError,
                        email = email,
                        onEmailChange = { email = it; emailError = validateEmail(it) },
                        emailError = emailError
                    )

                    2 -> Step2Content(
                        birthDate = birthDate,
                        onBirthDateChange = {
                            // Text change handled via date picker
                        },
                        onBirthDateClick = {
                            openBirthDatePicker(birthDate) { date ->
                                birthDate = date
                                birthDateError = validateBirthDate(date)
                            }
                        },
                        birthDateError = birthDateError,
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = {
                            if (it.isEmpty() || it.matches(Regex("^[+]?[0-9]*$"))) {
                                phoneNumber = it
                                phoneNumberError = validatePhoneNumber(it)
                            }
                        },
                        phoneNumberError = phoneNumberError,
                        gender = gender,
                        genderExpanded = genderExpanded,
                        onGenderExpandedChange = { genderExpanded = it },
                        genderOptions = genderOptions,
                        onGenderSelect = {
                            gender = it
                            genderError = validateGender(it)
                            genderExpanded = false
                        },
                        genderError = genderError
                    )

                    3 -> Step3Content(
                        password = password,
                        onPasswordChange = {
                            password = it
                            passwordError = validatePassword(it)
                            confirmPasswordError = validateConfirmPassword(it, confirmPassword)
                        },
                        passwordError = passwordError,
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = {
                            confirmPassword = it
                            confirmPasswordError = validateConfirmPassword(password, it)
                        },
                        confirmPasswordError = confirmPasswordError,
                        confirmPasswordVisible = confirmPasswordVisible,
                        onConfirmPasswordVisibilityChange = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF00B8D4)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            uiState.error?.let {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF00B8D4)
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retour")
                    }
                }

                Button(
                    onClick = {
                        when (currentStep) {
                            1 -> if (validateStep1()) currentStep++
                            2 -> if (validateStep2()) currentStep++
                            3 -> {
                                if (validateStep3()) {
                                    val request = RegisterRequest(
                                        username = username,
                                        email = email,
                                        password = password,
                                        role = "client",
                                        dateDeNaissance = "${birthDate}T00:00:00.000Z",
                                        numTel = phoneNumber,
                                        gender = if (gender == "Homme") "Male" else "Female",
                                        image = null
                                    )
                                    coroutineScope.launch {
                                        viewModel.register(request)
                                    }
                                }
                            }
                        }
                    },
                    enabled = !uiState.success, // ✅ disable button when success is true
                    modifier = Modifier
                        .weight(if (currentStep > 1) 1f else 1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.4f) // optional visual feedback
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (uiState.success)
                                        listOf(
                                            Color.Gray,
                                            Color.LightGray
                                        ) // grayed-out when disabled
                                    else
                                        listOf(Color(0xFF00B8D4), Color(0xFF00E5FF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when (currentStep) {
                                    3 -> if (uiState.success) "Terminé ✨" else "Créer mon compte"
                                    else -> "Continuer"
                                },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (currentStep < 3 && !uiState.success) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Étape $currentStep sur 3",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )
        }

    }
}

@Composable
fun StepProgressIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..3) {
            val isActive = step <= currentStep
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.8f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            if (isActive)
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00B8D4), Color(0xFF00E5FF))
                                )
                            else
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFE0E0E0), Color(0xFFE0E0E0))
                                )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (step < currentStep) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "$step",
                            color = if (isActive) Color.White else Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (step < 3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (step < currentStep) Color(0xFF00B8D4) else Color(0xFFE0E0E0)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedHeader(currentStep: Int) {
    val (icon, title, subtitle) = when (currentStep) {
        1 -> Triple(
            Icons.Default.Person,
            "Informations de base",
            "Commençons par les informations essentielles"
        )

        2 -> Triple(
            Icons.Default.ContactPage,
            "Détails personnels",
            "Quelques informations supplémentaires"
        )

        3 -> Triple(Icons.Default.Lock, "Sécurité", "Protégez votre compte avec un mot de passe")
        else -> Triple(Icons.Default.Person, "", "")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFE3F2FD), Color(0xFFB3E5FC))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF00B8D4),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun Step1Content(
    username: String,
    onUsernameChange: (String) -> Unit,
    usernameError: String?,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    fullNameError: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Nom d'utilisateur") },
            leadingIcon = { Icon(Icons.Default.PersonOutline, null, tint = Color(0xFF00B8D4)) },
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00B8D4),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = { Text("Nom complet") },
            leadingIcon = { Icon(Icons.Default.Badge, null, tint = Color(0xFF00B8D4)) },
            isError = fullNameError != null,
            supportingText = fullNameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00B8D4),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF00B8D4)) },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00B8D4),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Content(
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    onBirthDateClick: () -> Unit,
    birthDateError: String?,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    phoneNumberError: String?,
    gender: String,
    genderExpanded: Boolean,
    onGenderExpandedChange: (Boolean) -> Unit,
    genderOptions: List<String>,
    onGenderSelect: (String) -> Unit,
    genderError: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DatePickerTextField(
            value = birthDate,
            label = "Date de naissance",
            error = birthDateError,
            onClick = onBirthDateClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Numéro de téléphone") },
            leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFF00B8D4)) },
            placeholder = { Text("+216 12 345 678") },
            isError = phoneNumberError != null,
            supportingText = phoneNumberError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00B8D4),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = onGenderExpandedChange
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Genre") },
                leadingIcon = { Icon(Icons.Default.Face, null, tint = Color(0xFF00B8D4)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                isError = genderError != null,
                supportingText = genderError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00B8D4),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { onGenderExpandedChange(false) }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onGenderSelect(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DatePickerTextField(
    value: String,
    label: String,
    error: String?,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = Color(0xFF00B8D4)) },
            placeholder = { Text("1999-05-01") },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00B8D4),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            trailingIcon = {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF00B8D4))
            }
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        )
    }
}

@Composable
fun Step3Content(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordError: String?,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Mot de passe") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF00B8D4)) },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        tint = Color(0xFF00B8D4)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00B8D4),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirmer le mot de passe") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF00B8D4)) },
            trailingIcon = {
                IconButton(onClick = onConfirmPasswordVisibilityChange) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        tint = Color(0xFF00B8D4)
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00B8D4),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF0F9FF),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF00B8D4),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Votre mot de passe doit contenir au moins 6 caractères",
                    fontSize = 12.sp,
                    color = Color(0xFF546E7A)
                )
            }
        }
    }
}