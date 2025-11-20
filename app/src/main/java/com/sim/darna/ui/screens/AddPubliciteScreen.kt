package com.sim.darna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sim.darna.data.model.*
import com.sim.darna.ui.theme.*
import com.sim.darna.viewmodel.AddPubliciteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPubliciteScreen(
    publiciteId: String? = null,
    onNavigateBack: () -> Unit,
    onPubliciteSaved: () -> Unit = {},
    viewModel: AddPubliciteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()

    var titre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf<Categorie>(Categorie.TOUT) }
    var dateExpiration by remember { mutableStateOf("") }

    // Champs pour Réduction
    var pourcentageReduction by remember { mutableStateOf("") }
    var conditionsReduction by remember { mutableStateOf("") }

    // Champs pour Promotion
    var offrePromotion by remember { mutableStateOf("") }
    var conditionsPromotion by remember { mutableStateOf("") }

    // Champs pour Jeu
    var descriptionJeu by remember { mutableStateOf("") }
    var gainsJeu by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publicités Étudiantes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = BluePrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GreyLight)
                .verticalScroll(rememberScrollState())
        ) {
            // Formulaire principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Titre de l'offre",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = titre,
                        onValueChange = { titre = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: -20% chez Pizza Hut") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Décrivez votre offre en détail...") },
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Image de l'offre",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GreyLight)
                            .clickable { /* Upload image */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                modifier = Modifier.size(48.dp),
                                tint = BluePrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Cliquez pour télécharger une image",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Type de publicité
                    Text(
                        text = "Type de publicité",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TypeButton(
                            type = PubliciteType.REDUCTION,
                            label = "Réduction",
                            icon = Icons.Default.Percent,
                            isSelected = selectedType == PubliciteType.REDUCTION,
                            onClick = { viewModel.setType(PubliciteType.REDUCTION) },
                            modifier = Modifier.weight(1f)
                        )
                        TypeButton(
                            type = PubliciteType.PROMOTION,
                            label = "Promotion",
                            icon = Icons.Default.CardGiftcard,
                            isSelected = selectedType == PubliciteType.PROMOTION,
                            onClick = { viewModel.setType(PubliciteType.PROMOTION) },
                            modifier = Modifier.weight(1f)
                        )
                        TypeButton(
                            type = PubliciteType.JEU,
                            label = "Jeu",
                            icon = Icons.Default.SportsEsports,
                            isSelected = selectedType == PubliciteType.JEU,
                            onClick = { viewModel.setType(PubliciteType.JEU) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Champs dynamiques selon le type
            selectedType?.let { type ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Détails de la ${when (type) {
                                PubliciteType.REDUCTION -> "réduction"
                                PubliciteType.PROMOTION -> "promotion"
                                PubliciteType.JEU -> "jeu"
                            }}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        when (type) {
                            PubliciteType.REDUCTION -> {
                                OutlinedTextField(
                                    value = pourcentageReduction,
                                    onValueChange = { pourcentageReduction = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Pourcentage de réduction (%)") },
                                    placeholder = { Text("Ex: 20") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = conditionsReduction,
                                    onValueChange = { conditionsReduction = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    label = { Text("Conditions d'utilisation") },
                                    placeholder = { Text("Ex: Valable sur tous les produits, hors boissons") },
                                    maxLines = 3
                                )
                            }
                            PubliciteType.PROMOTION -> {
                                OutlinedTextField(
                                    value = offrePromotion,
                                    onValueChange = { offrePromotion = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Détail de l'offre") },
                                    placeholder = { Text("Ex: 2 Pizzas Achetées = la 3ème Offerte") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = conditionsPromotion,
                                    onValueChange = { conditionsPromotion = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    label = { Text("Conditions") },
                                    placeholder = { Text("Décrivez les conditions de l'offre") },
                                    maxLines = 3
                                )
                            }
                            PubliciteType.JEU -> {
                                OutlinedTextField(
                                    value = descriptionJeu,
                                    onValueChange = { descriptionJeu = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    label = { Text("Description du jeu") },
                                    placeholder = { Text("Décrivez le jeu") },
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = gainsJeu,
                                    onValueChange = { gainsJeu = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    label = { Text("Gains possibles (séparés par des virgules)") },
                                    placeholder = { Text("Ex: -10%, -20%, -30%, Gratuit") },
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            }

            // Catégorie
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Catégorie",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = categorie.name,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            Categorie.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (cat) {
                                                Categorie.TOUT -> "Tout"
                                                Categorie.NOURRITURE -> "Nourriture"
                                                Categorie.TECH -> "Tech"
                                                Categorie.LOISIRS -> "Loisirs"
                                                Categorie.VOYAGE -> "Voyage"
                                                Categorie.MODE -> "Mode"
                                                Categorie.AUTRE -> "Autre"
                                            }
                                        )
                                    },
                                    onClick = {
                                        categorie = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bouton Publier
            Button(
                onClick = {
                    val detailReduction = if (selectedType == PubliciteType.REDUCTION && pourcentageReduction.isNotBlank()) {
                        DetailReduction(
                            pourcentage = pourcentageReduction.toIntOrNull() ?: 0,
                            conditionsUtilisation = conditionsReduction
                        )
                    } else null

                    val detailPromotion = if (selectedType == PubliciteType.PROMOTION && offrePromotion.isNotBlank()) {
                        DetailPromotion(
                            offre = offrePromotion,
                            conditions = conditionsPromotion
                        )
                    } else null

                    val detailJeu = if (selectedType == PubliciteType.JEU && gainsJeu.isNotBlank()) {
                        DetailJeu(
                            description = descriptionJeu,
                            gains = gainsJeu.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        )
                    } else null

                    val onSuccessCallback = {
                        onPubliciteSaved()
                        onNavigateBack()
                    }

                    if (publiciteId != null) {
                        viewModel.updatePublicite(
                            id = publiciteId,
                            titre = titre,
                            description = description,
                            imageUrl = imageUrl.takeIf { it.isNotBlank() },
                            type = selectedType ?: PubliciteType.REDUCTION,
                            categorie = categorie,
                            dateExpiration = dateExpiration.takeIf { it.isNotBlank() },
                            detailReduction = detailReduction,
                            detailPromotion = detailPromotion,
                            detailJeu = detailJeu,
                            onSuccess = onSuccessCallback
                        )
                    } else {
                        viewModel.createPublicite(
                            titre = titre,
                            description = description,
                            imageUrl = imageUrl.takeIf { it.isNotBlank() },
                            type = selectedType ?: PubliciteType.REDUCTION,
                            categorie = categorie,
                            dateExpiration = dateExpiration.takeIf { it.isNotBlank() },
                            detailReduction = detailReduction,
                            detailPromotion = detailPromotion,
                            detailJeu = detailJeu,
                            onSuccess = onSuccessCallback
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedType != null && titre.isNotBlank() && description.isNotBlank()
            ) {
                if (uiState is com.sim.darna.viewmodel.AddPubliciteUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = White
                    )
                } else {
                    Text(
                        text = "Publier la publicité",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Afficher les erreurs
            (uiState as? com.sim.darna.viewmodel.AddPubliciteUiState.Error)?.let { errorState ->
                Text(
                    text = errorState.message,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun TypeButton(
    type: PubliciteType,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) BluePrimary else GreyLight,
            contentColor = if (isSelected) White else TextPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

