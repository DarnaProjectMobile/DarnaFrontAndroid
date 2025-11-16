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
import com.sim.darna.navigation.Routes

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

    LaunchedEffect(annonceId) {
        // Decode the annonceId in case it was URL encoded
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

    // Check ownership - user.id can be the user ID string
    val isOwner = selectedAnnonce?.let { annonce ->
        val annonceUserId = annonce.user.id
        annonceUserId == userId && userId.isNotEmpty()
    } ?: false
    val canEdit = role == "collocator" && isOwner

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de l'annonce", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) price = it },
                        label = { Text("Prix") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

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
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Annuler")
                        }

                        Button(
                            onClick = {
                                val priceValue = price.toDoubleOrNull() ?: 0.0
                                // Use the selected annonce ID directly to avoid encoding issues
                                val idToUse = selectedAnnonce?.id ?: try {
                                    java.net.URLDecoder.decode(annonceId, "UTF-8")
                                } catch (e: Exception) {
                                    annonceId
                                }
                                android.util.Log.d("PropertyDetailScreen", "Updating annonce with ID: $idToUse")
                                if (idToUse.isNotEmpty()) {
                                    viewModel.updateAnnonce(idToUse, title, description, priceValue)
                                    isEditing = false
                                } else {
                                    Toast.makeText(context, "ID d'annonce invalide", Toast.LENGTH_SHORT).show()
                                }
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

                                Text(
                                    text = "Description",
                                    fontSize = 14.sp,
                                    color = Color(0xFF8A8E9F),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = annonce.description,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1B1D28)
                                )

                                Divider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Prix",
                                            fontSize = 14.sp,
                                            color = Color(0xFF8A8E9F),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${annonce.price} DT",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4461F2)
                                        )
                                    }

                                    if (annonce.user.username != null) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Publié par",
                                                fontSize = 14.sp,
                                                color = Color(0xFF8A8E9F),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = annonce.user.username,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF1B1D28)
                                            )
                                        }
                                    }
                                }

                                if (annonce.createdAt != null) {
                                    Divider()
                                    Text(
                                        text = "Créé le: ${annonce.createdAt.take(10)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF8A8E9F)
                                    )
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
