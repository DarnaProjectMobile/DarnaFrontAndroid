package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sim.darna.R
import com.sim.darna.ViewModel.AnnonceViewModel
import com.sim.darna.navigation.Routes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(
    navController: NavController,
    username: String,
    userId: String,
    role: String,
    viewModel: AnnonceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val annonces by viewModel.annonces.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var minPriceInput by remember { mutableStateOf("") }
    var maxPriceInput by remember { mutableStateOf("") }

    val minPrice = minPriceInput.toDoubleOrNull()
    val maxPrice = maxPriceInput.toDoubleOrNull()
    val filteredAnnonces = remember(annonces, searchQuery, minPriceInput, maxPriceInput) {
        annonces.filter { annonce ->
            val matchesQuery = annonce.title.contains(searchQuery, ignoreCase = true)
            val matchesMin = minPrice?.let { annonce.price >= it } ?: true
            val matchesMax = maxPrice?.let { annonce.price <= it } ?: true
            matchesQuery && matchesMin && matchesMax
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAnnonces()
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F6FA),
        floatingActionButton = {
            if (role == "collocator") {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Routes.AddAnnonce)
                    },
                    containerColor = Color(0xFF4461F2)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une annonce",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GreetingHome(username)
                    SearchAndFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onFilterClick = { showFilterDialog = true },
                        onClearQuery = { searchQuery = "" }
                    )
                }
            }

            items(filteredAnnonces, key = { it.id }) { annonce ->
                AnnonceCard(
                    annonce = annonce,
                    userId = userId,
                    role = role,
                    onCardClick = {
                        val encodedId = URLEncoder.encode(annonce.id, StandardCharsets.UTF_8.toString())
                        val encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8.toString())
                        val encodedRole = URLEncoder.encode(role, StandardCharsets.UTF_8.toString())
                        navController.navigate("${Routes.PropertyDetail}?id=$encodedId&userId=$encodedUserId&role=$encodedRole")
                    },
                    onEditClick = {
                        // Navigate to edit screen or show dialog
                        val encodedId = URLEncoder.encode(annonce.id, StandardCharsets.UTF_8.toString())
                        val encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8.toString())
                        val encodedRole = URLEncoder.encode(role, StandardCharsets.UTF_8.toString())
                        navController.navigate("${Routes.PropertyDetail}?id=$encodedId&userId=$encodedUserId&role=$encodedRole&edit=true")
                    },
                    onDeleteClick = {
                        viewModel.deleteAnnonce(annonce.id)
                    }
                )
            }

            if (filteredAnnonces.isEmpty()) {
                item {
                    Text(
                        text = "Aucune annonce ne correspond à votre recherche.",
                        color = Color(0xFF8A8E9F),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        PriceFilterDialog(
            minPriceInput = minPriceInput,
            maxPriceInput = maxPriceInput,
            onMinPriceChange = { minPriceInput = it },
            onMaxPriceChange = { maxPriceInput = it },
            onApply = { showFilterDialog = false },
            onReset = {
                minPriceInput = ""
                maxPriceInput = ""
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun GreetingHome(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Bonjour 👋",
                color = Color(0xFF8A8E9F),
                fontSize = 14.sp
            )
            Text(
                text = username,
                color = Color(0xFF1B1D28),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            tonalElevation = 2.dp,
            modifier = Modifier.size(46.dp)
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun AnnonceCard(
    annonce: com.sim.darna.model.Annonce,
    userId: String,
    role: String,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Check if user owns this annonce
    // user.id can be the user ID string or the full user object's _id
    val isOwner = annonce.user.id == userId
    val showActions = role == "collocator" && isOwner && userId.isNotEmpty()
    val ownerName = annonce.user.username ?: "Propriétaire"
    val place = annonce.location?.ifBlank { null } ?: "Lieu non renseigné"
    val formattedDate = annonce.startDate?.take(10)
        ?: annonce.createdAt?.take(10)
        ?: "Date non renseignée"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCardClick() }
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.house),
                contentDescription = annonce.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text = annonce.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1D28),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${annonce.price} DT/mois",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C63FF)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = annonce.description,
                    fontSize = 14.sp,
                    color = Color(0xFF6B6B74),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Lieu",
                        tint = Color(0xFF8A8E9F),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = place,
                        fontSize = 14.sp,
                        color = Color(0xFF8A8E9F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Propriétaire",
                        tint = Color(0xFF6C63FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Propriétaire : $ownerName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1B1D28)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = Color(0xFF8A8E9F),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Disponible depuis $formattedDate",
                        fontSize = 13.sp,
                        color = Color(0xFF8A8E9F)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Visite 360°") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null
                            )
                        }
                    )

                    if (showActions) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AssistChip(
                            onClick = onEditClick,
                            label = { Text("Modifier") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier"
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = onDeleteClick,
                            label = { Text("Supprimer") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = Color.Red
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = Color.Red,
                                leadingIconContentColor = Color.Red
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onClearQuery: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Rechercher par titre") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Recherche"
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Effacer"
                        )
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        FilledTonalIconButton(
            onClick = onFilterClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0xFFE8EDFF),
                contentColor = Color(0xFF3D5AFE)
            )
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filtrer"
            )
        }
    }
}

@Composable
private fun PriceFilterDialog(
    minPriceInput: String,
    maxPriceInput: String,
    onMinPriceChange: (String) -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrer par prix") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minPriceInput,
                    onValueChange = onMinPriceChange,
                    label = { Text("Prix minimum") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = maxPriceInput,
                    onValueChange = onMaxPriceChange,
                    label = { Text("Prix maximum") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onApply) {
                Text("Appliquer")
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text("Réinitialiser")
            }
        }
    )
}