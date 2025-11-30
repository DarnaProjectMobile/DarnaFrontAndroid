package com.sim.darna.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.sim.darna.data.model.*
import com.sim.darna.ui.theme.*
import com.sim.darna.viewmodel.AddPubliciteViewModel
import com.sim.darna.viewmodel.AddPubliciteUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPubliciteScreen(
    navController: NavController,
    publiciteId: String? = null,
    viewModel: AddPubliciteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Variables d'état pour le formulaire
    var titre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategorie by remember { mutableStateOf(Categorie.NOURRITURE) }
    var dateExpiration by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Détails selon le type
    var pourcentageReduction by remember { mutableStateOf("") }
    var conditionsReduction by remember { mutableStateOf("") }
    var offrePromotion by remember { mutableStateOf("") }
    var conditionsPromotion by remember { mutableStateOf("") }

    // Format de date pour l'affichage
    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    // Launcher pour la sélection d'image avec gestion des permissions
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    // Launcher pour les permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    // Fonction pour ouvrir le sélecteur d'image
    fun openImagePicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        // Pour Android 13+ (API 33+), on peut directement ouvrir le sélecteur
        // Pour les versions antérieures, on demande la permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            imagePickerLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(permission)
        }
    }

    // Fonction pour ouvrir le DatePicker natif
    fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDate = selectedCalendar.time
                dateExpiration = dateFormatter.format(selectedCalendar.time)
            },
            year,
            month,
            day
        ).show()
    }

    // Observer le succès
    LaunchedEffect(uiState) {
        if (uiState is AddPubliciteUiState.Success) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refreshOffers", true)
            navController.navigateUp()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fond dégradé bleu ciel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4FC3F7),
                            Color(0xFF29B6F6),
                            Color(0xFF03A9F4)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar personnalisée
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Contenu défilable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // En-tête avec icône
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF03A9F4),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (publiciteId == null) "Ajouter une nouvelle publicité" else "Modifier la publicité",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Créez une offre attrayante pour les étudiants",
                        fontSize = 14.sp,
                        color = White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Card principale avec formulaire
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Message d'erreur
                        if (uiState is AddPubliciteUiState.Error) {
                            ErrorBanner(message = (uiState as AddPubliciteUiState.Error).message)
                        }

                        // Titre de l'offre
                        FormSection(title = "Titre de l'offre") {
                            ModernTextField(
                                value = titre,
                                onValueChange = { titre = it },
                                placeholder = "Ex: -20% chez Pizza Hut",
                                leadingIcon = Icons.Default.Edit
                            )
                        }

                        // Description
                        FormSection(title = "Description") {
                            ModernTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = "Décrivez votre offre en détail...",
                                leadingIcon = Icons.Default.Info,
                                minLines = 4,
                                maxLines = 6
                            )
                        }

                        Divider(color = GreyLight, thickness = 1.dp)

                        // Type de publicité
                        FormSection(title = "Type de publicité") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PubliciteType.values().forEach { type ->
                                    ModernTypeButton(
                                        type = type,
                                        isSelected = selectedType == type,
                                        onClick = { viewModel.setType(type) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Détails selon le type
                        if (selectedType != null) {
                            when (selectedType) {
                                PubliciteType.REDUCTION -> {
                                    ReductionDetailsSection(
                                        pourcentage = pourcentageReduction,
                                        onPourcentageChange = { pourcentageReduction = it },
                                        conditions = conditionsReduction,
                                        onConditionsChange = { conditionsReduction = it }
                                    )
                                }
                                PubliciteType.PROMOTION -> {
                                    PromotionDetailsSection(
                                        offre = offrePromotion,
                                        onOffreChange = { offrePromotion = it },
                                        conditions = conditionsPromotion,
                                        onConditionsChange = { conditionsPromotion = it }
                                    )
                                }
                                PubliciteType.JEU -> {
                                    JeuInfoSection()
                                }
                                null -> {
                                    // Aucun type sélectionné, ne rien afficher
                                }
                            }
                        }

                        // Image de l'offre
                        FormSection(title = "Image de l'offre") {
                            ImageUploadButton(
                                selectedImageUri = selectedImageUri,
                                onImageSelected = { openImagePicker() }
                            )
                        }

                        // Catégorie
                        FormSection(title = "Catégorie") {
                            ModernDropdown(
                                selectedCategorie = selectedCategorie,
                                onCategorieSelected = { selectedCategorie = it }
                            )
                        }

                        // Date d'expiration
                        FormSection(title = "Date d'expiration (optionnel)") {
                            DatePickerField(
                                selectedDate = selectedDate,
                                dateFormatter = dateFormatter,
                                onDateClick = { openDatePicker() },
                                onDateCleared = {
                                    selectedDate = null
                                    dateExpiration = ""
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bouton principal
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

                                if (publiciteId == null) {
                                    viewModel.createPublicite(
                                        titre = titre,
                                        description = description,
                                        imageUri = selectedImageUri,
                                        type = selectedType ?: PubliciteType.PROMOTION,
                                        categorie = selectedCategorie,
                                        dateExpiration = dateExpiration.ifBlank { null },
                                        detailReduction = detailReduction,
                                        detailPromotion = detailPromotion,
                                        detailJeu = null,
                                        onSuccess = { }
                                    )
                                } else {
                                    viewModel.updatePublicite(
                                        id = publiciteId,
                                        titre = titre,
                                        description = description,
                                        imageUri = selectedImageUri,
                                        type = selectedType ?: PubliciteType.PROMOTION,
                                        categorie = selectedCategorie,
                                        dateExpiration = dateExpiration.ifBlank { null },
                                        detailReduction = detailReduction,
                                        detailPromotion = detailPromotion,
                                        detailJeu = null,
                                        onSuccess = { }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF03A9F4),
                                disabledContainerColor = TextSecondary
                            ),
                            enabled = titre.isNotBlank() && description.isNotBlank() && selectedType != null && uiState !is AddPubliciteUiState.Loading,
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            if (uiState is AddPubliciteUiState.Loading) {
                                CircularProgressIndicator(
                                    color = White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (publiciteId == null) "Publier la publicité" else "Modifier la publicité",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// MARK: - Composants personnalisés

@Composable
fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        content()
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = TextSecondary,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = BlueLight.copy(alpha = 0.3f),
            unfocusedContainerColor = GreyLight,
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = BluePrimary
        ),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 14.sp,
            color = TextPrimary
        )
    )
}

@Composable
fun ModernTypeButton(
    type: PubliciteType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        PubliciteType.REDUCTION -> Color(0xFF00C853)
        PubliciteType.PROMOTION -> Color(0xFF0066FF)
        PubliciteType.JEU -> Color(0xFFFF6D00)
    }

    val typeName = when (type) {
        PubliciteType.REDUCTION -> "Réduction"
        PubliciteType.PROMOTION -> "Promotion"
        PubliciteType.JEU -> "Jeu"
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) backgroundColor else White
        ),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(
            2.dp,
            backgroundColor.copy(alpha = 0.3f)
        ) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (type) {
                    PubliciteType.REDUCTION -> Icons.Default.Favorite
                    PubliciteType.PROMOTION -> Icons.Default.Star
                    PubliciteType.JEU -> Icons.Default.Refresh
                },
                contentDescription = null,
                tint = if (isSelected) White else backgroundColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = typeName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) White else backgroundColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDropdown(
    selectedCategorie: Categorie,
    onCategorieSelected: (Categorie) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val categorieName = when (selectedCategorie) {
        Categorie.TOUT -> "Tout"
        Categorie.NOURRITURE -> "Nourriture"
        Categorie.TECH -> "Tech"
        Categorie.LOISIRS -> "Loisirs"
        Categorie.VOYAGE -> "Voyage"
        Categorie.MODE -> "Mode"
        Categorie.AUTRE -> "Autre"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = categorieName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BlueLight.copy(alpha = 0.3f),
                unfocusedContainerColor = GreyLight,
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = Color.Transparent
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Categorie.values().filter { it != Categorie.TOUT }.forEach { categorie ->
                val name = when (categorie) {
                    Categorie.TOUT -> "Tout"
                    Categorie.NOURRITURE -> "Nourriture"
                    Categorie.TECH -> "Tech"
                    Categorie.LOISIRS -> "Loisirs"
                    Categorie.VOYAGE -> "Voyage"
                    Categorie.MODE -> "Mode"
                    Categorie.AUTRE -> "Autre"
                }

                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onCategorieSelected(categorie)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ImageUploadButton(
    selectedImageUri: Uri?,
    onImageSelected: () -> Unit
) {
    Card(
        onClick = onImageSelected,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (selectedImageUri != null) 200.dp else 120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedImageUri != null)
                Color(0xFF00C853).copy(alpha = 0.1f)
            else
                BlueLight.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (selectedImageUri != null)
                Color(0xFF00C853).copy(alpha = 0.5f)
            else
                BluePrimary.copy(alpha = 0.3f)
        )
    ) {
        if (selectedImageUri != null) {
            // Aperçu de l'image sélectionnée
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = selectedImageUri),
                    contentDescription = "Image sélectionnée",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
                // Overlay avec bouton pour changer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clip(RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cliquez pour changer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choisir une image",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BluePrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cliquez pour ouvrir la galerie",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DatePickerField(
    selectedDate: Date?,
    dateFormatter: SimpleDateFormat,
    onDateClick: () -> Unit,
    onDateCleared: () -> Unit
) {
    val dateText = remember(selectedDate) {
        selectedDate?.let { dateFormatter.format(it) } ?: ""
    }

    OutlinedTextField(
        value = dateText,
        onValueChange = {},
        readOnly = true,
        placeholder = {
            Text(
                text = "Sélectionnez une date",
                color = TextSecondary,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (selectedDate != null) {
                IconButton(onClick = onDateCleared) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Effacer",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDateClick() },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = BlueLight.copy(alpha = 0.3f),
            unfocusedContainerColor = GreyLight,
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = BluePrimary
        ),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 14.sp,
            color = TextPrimary
        )
    )
}


@Composable
fun ReductionDetailsSection(
    pourcentage: String,
    onPourcentageChange: (String) -> Unit,
    conditions: String,
    onConditionsChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF00C853).copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Détails de la réduction",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00C853)
            )

            ModernTextField(
                value = pourcentage,
                onValueChange = onPourcentageChange,
                placeholder = "Ex: 20",
                leadingIcon = Icons.Default.Star
            )

            ModernTextField(
                value = conditions,
                onValueChange = onConditionsChange,
                placeholder = "Ex: Valable sur tous les produits, hors boissons",
                leadingIcon = Icons.Default.Info,
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@Composable
fun PromotionDetailsSection(
    offre: String,
    onOffreChange: (String) -> Unit,
    conditions: String,
    onConditionsChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0066FF).copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Détails de la promotion",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0066FF)
            )

            ModernTextField(
                value = offre,
                onValueChange = onOffreChange,
                placeholder = "Ex: 2 Pizzas achetées = 1 offerte",
                leadingIcon = Icons.Default.Star
            )

            ModernTextField(
                value = conditions,
                onValueChange = onConditionsChange,
                placeholder = "Ex: Sur présentation de la carte étudiante",
                leadingIcon = Icons.Default.Info,
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@Composable
fun JeuInfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF6D00).copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFF6D00),
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Détails du jeu",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6D00)
            )
            Text(
                text = "La roulette sera automatiquement créée avec des gains aléatoires",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorBanner(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFFD32F2F)
            )
        }
    }
}