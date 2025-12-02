package com.sim.darna.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.sim.darna.viewmodel.RegisterViewModel
import com.sim.darna.factory.RegisterVmFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(onScanIdClick: () -> Unit = {}) {
    val isDark = isSystemInDarkTheme()

    var currentStep by remember { mutableStateOf(1) }

    // Step 1 Fields
    var username by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var fullName by remember { mutableStateOf("") }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var role by remember { mutableStateOf("") }
    var roleError by remember { mutableStateOf<String?>(null) }
    var roleExpanded by remember { mutableStateOf(false) }
    val roleOptions = listOf(
        "Client" to "client",
        "Collocataire" to "collocator",
        "Sponsor" to "sponsor"
    )

    // Step 2 Fields
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
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

    val baseUrl = "http://172.16.14.253:3000/"
    val sharedPreferences = LocalContext.current.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterVmFactory(
            baseUrl = baseUrl,
            sharedPreferences = sharedPreferences
        )
    )
    val uiState = viewModel.state.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animation states
    var visible by remember { mutableStateOf(false) }
    var buttonScale by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

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

    fun validateRole(roleStr: String) =
        if (roleStr.isBlank()) "Le rôle est requis" else null

    fun validateStep1(): Boolean {
        usernameError = validateUsername(username)
        fullNameError = validateFullName(fullName)
        roleError = validateRole(role)
        return listOf(usernameError, fullNameError, roleError).all { it == null }
    }

    fun validateStep2(): Boolean {
        emailError = validateEmail(email)
        birthDateError = validateBirthDate(birthDate)
        phoneNumberError = validatePhoneNumber(phoneNumber)
        genderError = validateGender(gender)
        return listOf(emailError, birthDateError, phoneNumberError, genderError).all { it == null }
    }

    fun validateStep3(): Boolean {
        passwordError = validatePassword(password)
        confirmPasswordError = validateConfirmPassword(password, confirmPassword)
        return listOf(passwordError, confirmPasswordError).all { it == null }
    }

    // Gradient colors matching login
    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF1A237E),
            Color(0xFF0D47A1),
            Color(0xFF01579B)
        )
    } else {
        listOf(
            Color(0xFF667EEA),
            Color(0xFF764BA2),
            Color(0xFF5E72E4)
        )
    }

    val textColor = Color.White
    val inputBgColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.25f)
    val inputTextColor = Color.White

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = gradientColors))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp)
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Animated Header with Icon
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
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Sign Up",
                            modifier = Modifier.size(64.dp),
                            tint = textColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "CRÉER UN COMPTE",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Étape $currentStep sur 3",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            color = textColor.copy(alpha = 0.85f),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Step Progress Indicator
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 200))
                ) {
                    ModernStepProgressIndicator(currentStep = currentStep, textColor = textColor)
                }

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),                   // <-- THIS makes the step area fill full screen
                    contentAlignment = Alignment.TopCenter
                ) {
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
                        modifier = Modifier.fillMaxSize(),   // <-- Forces animation content to expand inside the box
                        label = "step_content"
                    ) { step ->

                        when (step) {
                            1 -> ModernStep1Content(
                                username = username,
                                onUsernameChange = { username = it; usernameError = validateUsername(it) },
                                usernameError = usernameError,
                                fullName = fullName,
                                onFullNameChange = { fullName = it; fullNameError = validateFullName(it) },
                                fullNameError = fullNameError,
                                role = role,
                                roleExpanded = roleExpanded,
                                onRoleExpandedChange = { roleExpanded = it },
                                roleOptions = roleOptions,
                                onRoleSelect = { selectedRole ->
                                    role = selectedRole
                                    roleError = validateRole(selectedRole)
                                    roleExpanded = false
                                },
                                roleError = roleError,
                                inputBgColor = inputBgColor,
                                inputTextColor = inputTextColor,
                                isDark = isDark,
                                modifier = Modifier.fillMaxSize()        // <-- VERY IMPORTANT
                            )

                            2 -> ModernStep2Content(
                                email = email,
                                onEmailChange = { email = it; emailError = validateEmail(it) },
                                emailError = emailError,
                                birthDate = birthDate,
                                onBirthDateChange = {
                                    birthDate = it; birthDateError = validateBirthDate(it)
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
                                genderError = genderError,
                                inputBgColor = inputBgColor,
                                inputTextColor = inputTextColor,
                                isDark = isDark,
                                modifier = Modifier.fillMaxSize()
                            )

                            3 -> ModernStep3Content(
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
                                },
                                inputBgColor = inputBgColor,
                                inputTextColor = inputTextColor,
                                isDark = isDark,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Error Display
                if (uiState.error != null) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFCDD2).copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFFFCDD2),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = uiState.error!!,
                                    color = textColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Navigation Buttons
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 400))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        buttonScale = 0.95f
                                        delay(100)
                                        buttonScale = 1f
                                        currentStep--
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .scale(buttonScale),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp,
                                    brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
                                )
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("RETOUR", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    buttonScale = 0.95f
                                    delay(100)
                                    buttonScale = 1f

                                    when (currentStep) {
                                        1 -> if (validateStep1()) currentStep++
                                        2 -> if (validateStep2()) currentStep++
                                        3 -> {
                                            if (validateStep3()) {
                                                val request = RegisterRequest(
                                                    username = username,
                                                    email = email,
                                                    password = password,
                                                    role = role,  // Use selected role
                                                    dateDeNaissance = "${birthDate}T00:00:00.000Z",
                                                    numTel = phoneNumber,
                                                    gender = when (gender) {
                                                        "Homme" -> "Male"
                                                        "Femme" -> "Female"
                                                        else -> "Other"
                                                    },
                                                    image = null
                                                )
                                                viewModel.register(request)
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !uiState.success && !uiState.isLoading,
                            modifier = Modifier
                                .weight(if (currentStep > 1) 1f else 1f)
                                .height(56.dp)
                                .scale(buttonScale),
                            shape = RoundedCornerShape(28.dp),
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = when (currentStep) {
                                            3 -> if (uiState.success) "TERMINÉ ✨" else "CRÉER"
                                            else -> "CONTINUER"
                                        },
                                        color = if (isDark) Color(0xFF1A237E) else Color(0xFF667EEA),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                    if (currentStep < 3 && !uiState.success) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF1A237E) else Color(0xFF667EEA)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ModernStepProgressIndicator(currentStep: Int, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..3) {
            val isActive = step <= currentStep
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.85f,
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
                                Color.White
                            else
                                Color.White.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (step < currentStep) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF667EEA),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "$step",
                            color = if (isActive) Color(0xFF667EEA) else textColor.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                if (step < 3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (step < currentStep) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernStep1Content(
    modifier: Modifier = Modifier,
    username: String,
    onUsernameChange: (String) -> Unit,
    usernameError: String?,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    fullNameError: String?,
    role: String,
    roleExpanded: Boolean,
    onRoleExpandedChange: (Boolean) -> Unit,
    roleOptions: List<Pair<String, String>>,
    onRoleSelect: (String) -> Unit,
    roleError: String?,
    inputBgColor: Color,
    inputTextColor: Color,
    isDark: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "NOM D'UTILISATEUR",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = { Text("Choisissez un nom d'utilisateur", color = inputTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = inputTextColor.copy(alpha = 0.7f)
                )
            },
            isError = usernameError != null,
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
                errorIndicatorColor = Color(0xFFFFCDD2),
                errorTextColor = inputTextColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (usernameError != null) {
            Text(
                text = usernameError,
                color = Color(0xFFFFCDD2),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "NOM COMPLET",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = fullName,
            onValueChange = onFullNameChange,
            placeholder = { Text("Votre nom complet", color = inputTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    tint = inputTextColor.copy(alpha = 0.7f)
                )
            },
            isError = fullNameError != null,
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
                errorIndicatorColor = Color(0xFFFFCDD2),
                errorTextColor = inputTextColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (fullNameError != null) {
            Text(
                text = fullNameError,
                color = Color(0xFFFFCDD2),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Role Selection
        Text(
            text = "RÔLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = roleExpanded,
            onExpandedChange = onRoleExpandedChange
        ) {
            TextField(
                value = roleOptions.find { it.second == role }?.first ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Sélectionnez votre rôle", color = inputTextColor.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(Icons.Default.WorkOutline, null, tint = inputTextColor.copy(alpha = 0.7f))
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded)
                },
                isError = roleError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
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
                    errorIndicatorColor = Color(0xFFFFCDD2),
                    errorTextColor = inputTextColor,
                    disabledContainerColor = inputBgColor,
                    disabledTextColor = inputTextColor
                ),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = roleExpanded,
                onDismissRequest = { onRoleExpandedChange(false) }
            ) {
                roleOptions.forEach { (displayName, value) ->
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    when (value) {
                                        "client" -> Icons.Default.Person
                                        "collocator" -> Icons.Default.Group
                                        "sponsor" -> Icons.Default.Star
                                        else -> Icons.Default.WorkOutline
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isDark) Color(0xFF1A237E) else Color(0xFF667EEA)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(displayName)
                            }
                        },
                        onClick = { onRoleSelect(value) }
                    )
                }
            }
        }

        if (roleError != null) {
            Text(
                text = roleError,
                color = Color(0xFFFFCDD2),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernStep2Content(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    birthDateError: String?,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    phoneNumberError: String?,
    gender: String,
    genderExpanded: Boolean,
    onGenderExpandedChange: (Boolean) -> Unit,
    genderOptions: List<String>,
    onGenderSelect: (String) -> Unit,
    genderError: String?,
    inputBgColor: Color,
    inputTextColor: Color,
    isDark: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Email
        Text(
            text = "EMAIL",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("votre@email.com", color = inputTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Default.Email, null, tint = inputTextColor.copy(alpha = 0.7f))
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
                errorIndicatorColor = Color(0xFFFFCDD2),
                errorTextColor = inputTextColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (emailError != null) {
            Text(emailError, color = Color(0xFFFFCDD2), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Birth Date
        Text(
            text = "DATE DE NAISSANCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = birthDate,
            onValueChange = onBirthDateChange,
            placeholder = { Text("AAAA-MM-JJ", color = inputTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Default.DateRange, null, tint = inputTextColor.copy(alpha = 0.7f))
            },
            isError = birthDateError != null,
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
                errorIndicatorColor = Color(0xFFFFCDD2),
                errorTextColor = inputTextColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (birthDateError != null) {
            Text(birthDateError, color = Color(0xFFFFCDD2), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Phone
        Text(
            text = "TÉLÉPHONE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            placeholder = { Text("+216 12 345 678", color = inputTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Default.Phone, null, tint = inputTextColor.copy(alpha = 0.7f))
            },
            isError = phoneNumberError != null,
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
                errorIndicatorColor = Color(0xFFFFCDD2),
                errorTextColor = inputTextColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (phoneNumberError != null) {
            Text(phoneNumberError, color = Color(0xFFFFCDD2), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gender
        Text(
            text = "GENRE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = onGenderExpandedChange
        ) {
            TextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Sélectionnez votre genre", color = inputTextColor.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(Icons.Default.Face, null, tint = inputTextColor.copy(alpha = 0.7f))
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                },
                isError = genderError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
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
                    errorIndicatorColor = Color(0xFFFFCDD2),
                    errorTextColor = inputTextColor
                ),
                shape = RoundedCornerShape(12.dp)
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

        if (genderError != null) {
            Text(genderError, color = Color(0xFFFFCDD2), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }
    }
}

@Composable
fun ModernStep3Content(
    modifier: Modifier = Modifier,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordError: String?,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: () -> Unit,
    inputBgColor: Color,
    inputTextColor: Color,
    isDark: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ){
        Text(
            text = "MOT DE PASSE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Minimum 6 caractères", color = inputTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Default.Lock, null, tint = inputTextColor.copy(alpha = 0.7f))
            },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
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
                errorIndicatorColor = Color(0xFFFFCDD2),
                errorTextColor = inputTextColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (passwordError != null) {
            Text(passwordError, color = Color(0xFFFFCDD2), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CONFIRMER LE MOT DE PASSE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = { Text("Ressaisissez votre mot de passe", color = inputTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Default.Lock, null, tint = inputTextColor.copy(alpha = 0.7f))
            },
            trailingIcon = {
                IconButton(onClick = onConfirmPasswordVisibilityChange) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        tint = inputTextColor.copy(alpha = 0.7f)
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
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
                errorIndicatorColor = Color(0xFFFFCDD2),
                errorTextColor = inputTextColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (confirmPasswordError != null) {
            Text(confirmPasswordError, color = Color(0xFFFFCDD2), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Utilisez un mot de passe fort avec lettres, chiffres et symboles",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}