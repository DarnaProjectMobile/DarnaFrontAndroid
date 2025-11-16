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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sim.darna.ViewModel.AnnonceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnnonceScreen(
    navController: NavController,
    viewModel: AnnonceViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var price by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf<String?>(null) }

    // Handle success messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.contains("créée")) navController.popBackStack()
            viewModel.clearError()
        }
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    fun validate(): Boolean {
        titleError = if (title.isBlank()) "Le titre est requis" else null
        descriptionError = if (description.isBlank()) "La description est requise" else null
        priceError = when {
            price.isBlank() -> "Le prix est requis"
            price.toDoubleOrNull() == null -> "Prix invalide"
            price.toDouble() <= 0 -> "Le prix doit être supérieur à 0"
            else -> null
        }
        return titleError == null && descriptionError == null && priceError == null
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

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = null },
                label = { Text("Titre") },
                isError = titleError != null,
                supportingText = { titleError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4461F2),
                    focusedLabelColor = Color(0xFF4461F2)
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; descriptionError = null },
                label = { Text("Description") },
                isError = descriptionError != null,
                supportingText = { descriptionError?.let { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4461F2),
                    focusedLabelColor = Color(0xFF4461F2)
                )
            )

            OutlinedTextField(
                value = price,
                onValueChange = {
                    if (it.all { char -> char.isDigit() || char == '.' }) {
                        price = it
                        priceError = null
                    }
                },
                label = { Text("Prix (DT)") },
                isError = priceError != null,
                supportingText = { priceError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4461F2),
                    focusedLabelColor = Color(0xFF4461F2)
                )
            )

            if (uiState.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            Button(
                onClick = { if (validate()) viewModel.createAnnonce(title, description, price.toDouble()) },
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
