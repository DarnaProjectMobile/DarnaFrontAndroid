package com.sim.darna.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.model.RegisterRequest
import com.sim.darna.utils.ApiConfig
import com.sim.darna.viewmodel.RegisterViewModel
import com.sim.darna.factory.RegisterVmFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Modern Color Palette
private val PrimaryColor = Color(0xFFFF4B6E)
private val SecondaryColor = Color(0xFF4C6FFF)
private val AccentColor = Color(0xFFFFC857)
private val BackgroundColor = Color(0xFFF7F7F7)
private val SurfaceColor = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val ErrorColor = Color(0xFFFF3B30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(onScanIdClick: () -> Unit = {}) {
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

    val sharedPreferences = LocalContext.current.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterVmFactory(
            baseUrl = ApiConfig.BASE_URL,
            sharedPreferences = sharedPreferences
        )
    )
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Bar with Progress
            Surface(
                color = SurfaceColor,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentStep > 1) currentStep-- },
                            enabled = currentStep > 1
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = if (currentStep > 1) TextPrimary else TextSecondary.copy(alpha = 0.3f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Étape $currentStep/3",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Text(
                                text = when (currentStep) {
                                    1 -> "Informations de base"
                                    2 -> "Coordonnées"
                                    else -> "Sécurité"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Box(modifier = Modifier.size(48.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Indicator
                    ModernProgressBar(currentStep = currentStep)
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                        }
                    },
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
                            roleError = roleError
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
                            genderError = genderError
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
                            }
                        )
                    }
                }
            }

            // Error Display
            if (uiState.error != null) {
                Surface(
                    color = ErrorColor.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = ErrorColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.error!!,
                            color = ErrorColor,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Button
            Surface(
                color = SurfaceColor,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                when (currentStep) {
                                    1 -> if (validateStep1()) currentStep++
                                    2 -> if (validateStep2()) currentStep++
                                    3 -> {
                                        if (validateStep3()) {
                                            val request = RegisterRequest(
                                                username = username,
                                                email = email,
                                                password = password,
                                                role = role,
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
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor,
                            disabledContainerColor = PrimaryColor.copy(alpha = 0.5f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = SurfaceColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = when (currentStep) {
                                    3 -> "Créer mon compte"
                                    else -> "Continuer"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SurfaceColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernProgressBar(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < currentStep) PrimaryColor else Color(0xFFE0E0E0)
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernStep1Content(
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
    roleError: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ModernTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = "Nom d'utilisateur",
            placeholder = "johndoe",
            leadingIcon = Icons.Outlined.Person,
            error = usernameError
        )

        ModernTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "Nom complet",
            placeholder = "John Doe",
            leadingIcon = Icons.Outlined.Badge,
            error = fullNameError
        )

        Column {
            Text(
                text = "Rôle",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = onRoleExpandedChange
            ) {
                OutlinedTextField(
                    value = roleOptions.find { it.second == role }?.first ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Sélectionnez votre rôle", color = TextSecondary) },
                    leadingIcon = {
                        Icon(Icons.Outlined.WorkOutline, null, tint = SecondaryColor)
                    },
                    trailingIcon = {
                        Icon(
                            if (roleExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            null,
                            tint = TextSecondary
                        )
                    },
                    isError = roleError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceColor,
                        unfocusedContainerColor = SurfaceColor,
                        focusedBorderColor = SecondaryColor,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        errorBorderColor = ErrorColor
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
                                            "client" -> Icons.Outlined.Person
                                            "collocator" -> Icons.Outlined.Group
                                            "sponsor" -> Icons.Outlined.Star
                                            else -> Icons.Outlined.WorkOutline
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = SecondaryColor
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
                    color = ErrorColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernStep2Content(
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
    genderError: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ModernTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "john@example.com",
            leadingIcon = Icons.Outlined.Email,
            error = emailError
        )

        ModernTextField(
            value = birthDate,
            onValueChange = onBirthDateChange,
            label = "Date de naissance",
            placeholder = "AAAA-MM-JJ",
            leadingIcon = Icons.Outlined.DateRange,
            error = birthDateError
        )

        ModernTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = "Téléphone",
            placeholder = "+216 12 345 678",
            leadingIcon = Icons.Outlined.Phone,
            error = phoneNumberError
        )

        Column {
            Text(
                text = "Genre",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = onGenderExpandedChange
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Sélectionnez votre genre", color = TextSecondary) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Face, null, tint = SecondaryColor)
                    },
                    trailingIcon = {
                        Icon(
                            if (genderExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            null,
                            tint = TextSecondary
                        )
                    },
                    isError = genderError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceColor,
                        unfocusedContainerColor = SurfaceColor,
                        focusedBorderColor = SecondaryColor,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        errorBorderColor = ErrorColor
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
                Text(
                    text = genderError,
                    color = ErrorColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ModernStep3Content(
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ModernPasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Mot de passe",
            placeholder = "Minimum 6 caractères",
            error = passwordError,
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange
        )

        ModernPasswordField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirmer le mot de passe",
            placeholder = "Ressaisissez votre mot de passe",
            error = confirmPasswordError,
            passwordVisible = confirmPasswordVisible,
            onPasswordVisibilityChange = onConfirmPasswordVisibilityChange
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SecondaryColor.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = SecondaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Utilisez un mot de passe fort avec lettres, chiffres et symboles",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    error: String?
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary) },
            leadingIcon = {
                Icon(leadingIcon, contentDescription = null, tint = SecondaryColor)
            },
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedBorderColor = SecondaryColor,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                errorBorderColor = ErrorColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (error != null) {
            Text(
                text = error,
                color = ErrorColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ModernPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    error: String?,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = SecondaryColor)
            },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedBorderColor = SecondaryColor,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                errorBorderColor = ErrorColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (error != null) {
            Text(
                text = error,
                color = ErrorColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}