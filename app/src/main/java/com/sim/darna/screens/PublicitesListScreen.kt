package com.sim.darna.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sim.darna.auth.UserSessionManager
import com.sim.darna.data.model.Publicite
import com.sim.darna.viewmodel.PubliciteViewModel
import com.sim.darna.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran principal affichant la liste des publicités.
 * Les sponsors peuvent ajouter, modifier ou supprimer des publicités.
 */
@Composable
fun PublicitesListScreen(
    viewModel: PubliciteViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},          // Callback pour ajouter une publicité
    onEdit: (String) -> Unit = {}         // Callback pour modifier une publicité
) {
    // État de la liste des publicités
    val listState by viewModel.listState.collectAsState()

    // Vérifie si l'utilisateur connecté est un sponsor
    val isSponsor = UserSessionManager.isSponsor()

    // Charger les publicités au lancement de l'écran
    LaunchedEffect(Unit) {
        viewModel.loadPublicites()
    }

    val publicites = when (val state = listState) {
        is UiState.Success -> state.data
        else -> emptyList()
    }
    val isLoading = listState is UiState.Loading
    val errorMessage = (listState as? UiState.Error)?.message

    // Affiche la liste ou l'état (loading / erreur)
    PublicitesListContent(
        publicites = publicites,
        isLoading = isLoading,
        errorMessage = errorMessage,
        isSponsor = isSponsor,
        onRetry = { viewModel.loadPublicites() },
        onAddClick = onAddClick,
        onEdit = onEdit,
        onDelete = { id ->
            viewModel.deletePublicite(id) { success, _ ->
                if (!success) {
                    // TODO: afficher snackbar si suppression échoue
                }
            }
        }
    )
}

/**
 * Contenu principal de la liste des publicités
 */
@Composable
private fun PublicitesListContent(
    publicites: List<Publicite>,
    isLoading: Boolean,
    errorMessage: String?,
    isSponsor: Boolean,
    onRetry: () -> Unit,
    onAddClick: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Publicités",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            when {
                // Affiche un loader si les données sont en chargement
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Affiche le message d'erreur et bouton de retry
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onRetry) {
                            Text("Réessayer")
                        }
                    }
                }

                // Cas où il n'y a aucune publicité
                publicites.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Aucune publicité trouvée")
                        if (isSponsor) {
                            Text("Appuyez sur + pour créer la première publicité.")
                        }
                    }
                }

                // Liste des publicités
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(publicites) { pub ->
                            PubliciteCard(
                                publicite = pub,
                                canManage = isSponsor,
                                onEdit = { pub._id?.let(onEdit) },
                                onDelete = { pub._id?.let(onDelete) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) } // espace pour FloatingActionButton
                    }
                }
            }
        }

        // Bouton flottant pour ajouter une publicité (visible seulement pour les sponsors)
        if (isSponsor) {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    }
}

/**
 * Carte représentant une publicité individuelle
 */
@Composable
private fun PubliciteCard(
    publicite: Publicite,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = rememberFormatter()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image ou placeholder
            if (!publicite.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(publicite.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = publicite.titre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(publicite.type ?: "Publicité")
                }
            }

            // Informations textuelles
            Column(modifier = Modifier.padding(16.dp)) {
                Text(publicite.titre, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(publicite.description, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Du ${publicite.dateDebut?.let(dateFormatter::format) ?: "--"} " +
                            "au ${publicite.dateFin?.let(dateFormatter::format) ?: "--"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Bouton pour copier le code promo
                if (!publicite.codePromo.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { /* TODO: copy code */ }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(publicite.codePromo!!)
                    }
                }

                // Actions disponibles pour les sponsors
                if (canManage) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onEdit, enabled = publicite._id != null) {
                            Text("Modifier")
                        }
                        OutlinedButton(
                            onClick = onDelete,
                            enabled = publicite._id != null,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Supprimer")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formatter pour afficher les dates
 */
@Composable
private fun rememberFormatter(): SimpleDateFormat {
    return remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
}

/**
 * Preview pour le design
 */
@Preview(showBackground = true)
@Composable
private fun PublicitesListPreview() {
    val now = Date()
    val later = Date(now.time + 86_400_000L)
    PublicitesListContent(
        publicites = listOf(
            Publicite(
                _id = "1",
                titre = "Promo rentrée",
                description = "Profitez de -20% sur nos services.",
                type = "Promotion",
                pourcentageReduction = 20,
                imageUrl = null,
                dateDebut = now,
                dateFin = later,
                codePromo = "RENTREE20"
            )
        ),
        isLoading = false,
        errorMessage = null,
        isSponsor = true,
        onRetry = {},
        onAddClick = {},
        onEdit = {},
        onDelete = {}
    )
}
