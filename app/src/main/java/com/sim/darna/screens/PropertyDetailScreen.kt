package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun PropertyDetailScreen(
    navController: NavController,
    annonceId: String,
    userId: String,
    role: String,
    viewModel: AnnonceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val selectedAnnonce by viewModel.selectedAnnonce.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    LaunchedEffect(annonceId) {
        val decodedId = try {
            java.net.URLDecoder.decode(annonceId, "UTF-8")
        } catch (e: Exception) {
            annonceId
        }
        if (decodedId.isNotEmpty()) {
            viewModel.getAnnonceById(decodedId)
        }
    }

    LaunchedEffect(selectedAnnonce) {
        selectedAnnonce?.let {
            title = it.title
            description = it.description
            price = it.price.toString()
            image = it.image ?: ""
            type = it.type ?: ""
            location = it.location ?: ""
            startDate = it.startDate ?: ""
            endDate = it.endDate ?: ""
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.contains("modifiée") || it.contains("supprimée")) {
                navController.popBackStack()
            }
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    val isOwner = selectedAnnonce?.user?.id == userId
    val canEdit = role == "collocator" && isOwner

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de l'annonce", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (canEdit && !isEditing) {
                        TextButton(onClick = { isEditing = true }) {
                            Text("Modifier", color = Color(0xFF4461F2))
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        if (selectedAnnonce == null && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Annonce introuvable", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isEditing && canEdit) {
                    // EDIT MODE
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) price = it },
                        label = { Text("Prix") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = image,
                        onValueChange = { image = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Localisation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Date de début (ISO 8601)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Date de fin (ISO 8601)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                selectedAnnonce?.let {
                                    title = it.title
                                    description = it.description
                                    price = it.price.toString()
                                    image = it.image ?: ""
                                    type = it.type ?: ""
                                    location = it.location ?: ""
                                    startDate = it.startDate ?: ""
                                    endDate = it.endDate ?: ""
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Annuler")
                        }

                        Button(
                            onClick = {
                                val priceValue = price.toDoubleOrNull() ?: 0.0
                                val idToUse = selectedAnnonce?.id ?: annonceId
                                viewModel.updateAnnonce(
                                    idToUse, title, description, priceValue,
                                    image, type, location, startDate, endDate
                                )
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4461F2))
                        ) {
                            Text("Enregistrer")
                        }
                    }
                } else {
                    // VIEW MODE
                    selectedAnnonce?.let { annonce ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = annonce.title,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1D28)
                                )
                                Divider()
                                Text("Description", fontSize = 14.sp, color = Color(0xFF8A8E9F), fontWeight = FontWeight.Medium)
                                Text(annonce.description, fontSize = 16.sp, color = Color(0xFF1B1D28))
                                Divider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Prix", fontSize = 14.sp, color = Color(0xFF8A8E9F), fontWeight = FontWeight.Medium)
                                        Text("${annonce.price} DT", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4461F2))
                                    }
                                    annonce.user.username?.let {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Publié par", fontSize = 14.sp, color = Color(0xFF8A8E9F), fontWeight = FontWeight.Medium)
                                            Text(it, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B1D28))
                                        }
                                    }
                                }
                                annonce.createdAt?.let {
                                    Divider()
                                    Text("Créé le: ${it.take(10)}", fontSize = 12.sp, color = Color(0xFF8A8E9F))
                                }
                            }
                        }
                    }
                }

                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
