package com.sim.darna.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import java.util.Calendar

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

    fun openDatePicker(currentValue: String?, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        currentValue?.takeIf { it.isNotBlank() }?.split("-")?.let { parts ->
            if (parts.size == 3) {
                parts[0].toIntOrNull()?.let { year ->
                    val month = parts[1].toIntOrNull()?.minus(1) ?: calendar.get(Calendar.MONTH)
                    val day = parts[2].toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month, day)
                }
            }
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val date = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

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

            DatePickerField(
                label = "Date de début",
                value = startDate,
                error = startDateError,
                onClick = {
                    openDatePicker(startDate) {
                        startDate = it
                        startDateError = null
                    }
                }
            )

            DatePickerField(
                label = "Date de fin",
                value = endDate,
                error = endDateError,
                onClick = {
                    openDatePicker(endDate) {
                        endDate = it
                        endDateError = null
                    }
                }
            )

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

@Composable
private fun DatePickerField(
    label: String,
    value: String,
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
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Choisir une date"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4461F2),
                focusedLabelColor = Color(0xFF4461F2)
            )
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
