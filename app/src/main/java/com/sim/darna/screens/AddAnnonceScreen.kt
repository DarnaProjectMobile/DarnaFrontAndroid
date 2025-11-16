package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sim.darna.ViewModel.AnnonceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnnonceScreen(
    navController: NavController,
    viewModel: AnnonceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    // Error messages
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }
    var typeError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }

    // Toast for success
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.contains("créée")) {
                navController.popBackStack()
            }
            viewModel.clearError()
        }
    }

    // Toast for error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // Validation
    fun validate(): Boolean {
        titleError = if (title.isBlank()) "Le titre est requis" else null
        descriptionError = if (description.isBlank()) "La description est requise" else null
        priceError = when {
            price.isBlank() -> "Le prix est requis"
            price.toDoubleOrNull() == null -> "Prix invalide"
            price.toDouble() <= 0 -> "Le prix doit être supérieur à 0"
            else -> null
        }
        imageError = if (image.isBlank()) "L'image est requise" else null
        typeError = if (type.isBlank()) "Le type est requis" else null
        locationError = if (location.isBlank()) "La localisation est requise" else null
        startDateError = if (startDate.isBlank()) "La date de début est requise" else null
        endDateError = if (endDate.isBlank()) "La date de fin est requise" else null

        return listOf(
            titleError, descriptionError, priceError,
            imageError, typeError, locationError, startDateError, endDateError
        ).all { it == null }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle annonce", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Créer une nouvelle annonce",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1D28)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reusable OutlinedTextField
            @Composable
            fun outlinedTextField(
                value: String,
                onValueChange: (String) -> Unit,
                label: String,
                error: String? = null,
                singleLine: Boolean = true,
                height: Int? = null
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label) },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (height != null) Modifier.height(height.dp) else Modifier),
                    singleLine = singleLine,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4461F2),
                        focusedLabelColor = Color(0xFF4461F2)
                    )
                )
            }

            outlinedTextField(title, { title = it; titleError = null }, "Titre", titleError)
            outlinedTextField(description, { description = it; descriptionError = null }, "Description", descriptionError, singleLine = false, height = 150)
            outlinedTextField(price, { if (it.all { c -> c.isDigit() || c == '.' }) price = it; priceError = null }, "Prix (DT)", priceError)
            outlinedTextField(image, { image = it; imageError = null }, "Image URL", imageError)
            outlinedTextField(type, { type = it; typeError = null }, "Type", typeError)
            outlinedTextField(location, { location = it; locationError = null }, "Localisation", locationError)
            outlinedTextField(startDate, { startDate = it; startDateError = null }, "Date de début (ISO 8601)", startDateError)
            outlinedTextField(endDate, { endDate = it; endDateError = null }, "Date de fin (ISO 8601)", endDateError)

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    if (validate()) {
                        viewModel.createAnnonce(
                            title, description, price.toDouble(),
                            image, type, location, startDate, endDate
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4461F2)),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = if (uiState.isLoading) "Création..." else "Créer l'annonce",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}
