package com.sim.darna.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sim.darna.auth.TokenStorage
import com.sim.darna.auth.UserSessionManager
import com.sim.darna.components.StripePaymentBottomSheet
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.repository.PubliciteUploadRepository
import com.sim.darna.viewmodel.PubliciteViewModel
import com.sim.darna.viewmodel.StripeViewModel
import com.sim.darna.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class PubliciteTypeOption {
    REDUCTION,
    PROMOTION,
    JEU
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPubliciteScreen(
    publiciteId: String? = null,
    onFinish: () -> Unit = {},
    onCancel: () -> Unit = {},
    publiciteViewModel: PubliciteViewModel = hiltViewModel(),
    stripeViewModel: StripeViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Récupérer le rôle depuis SharedPreferences
    val prefs = remember { context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE) }
    val userRole = remember(prefs) { prefs.getString("role", "user") ?: "user" }
    val isSponsor = remember(userRole) {
        userRole.lowercase() == "sponsor" || UserSessionManager.isSponsor()
    }

    if (!isSponsor) {
        NonSponsorContent(onCancel = onCancel)
        return
    }

    var showPaymentDialog by rememberSaveable { mutableStateOf(false) }
    var showPaymentSheet by rememberSaveable { mutableStateOf(false) }
    var hasPaid by rememberSaveable { mutableStateOf(false) }
    var paymentClientSecret by rememberSaveable { mutableStateOf<String?>(null) }
    var isUploadingImage by rememberSaveable { mutableStateOf(false) }

    val formState by publiciteViewModel.formState.collectAsState()
    val paymentState by stripeViewModel.paymentState.collectAsState()

    // États du formulaire
    var titre by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf<PubliciteTypeOption?>(null) }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var dateDebut by rememberSaveable { mutableStateOf<Date?>(null) }
    var dateFin by rememberSaveable { mutableStateOf<Date?>(null) }

    // Champs spécifiques pour REDUCTION
    var pourcentageReduction by rememberSaveable { mutableStateOf("") }

    // Champs spécifiques pour JEU
    var nombreCases by rememberSaveable { mutableStateOf(3) }
    var gainsJeu by rememberSaveable { mutableStateOf(listOf("", "", "")) }

    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    val isEditing = !publiciteId.isNullOrBlank()

    // Launcher pour sélectionner une image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // Upload l'image immédiatement
            isUploadingImage = true
            publiciteViewModel.uploadImage(context, it) { success, url ->
                isUploadingImage = false
                if (success && url != null) {
                    imageUrl = url
                    Toast.makeText(context, "Image uploadée avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Erreur lors de l'upload de l'image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Charger la publicité si édition
    LaunchedEffect(publiciteId) {
        if (isEditing) {
            publiciteViewModel.loadPublicite(publiciteId!!)
            hasPaid = true
        }
    }

    // Remplir les champs avec la publicité existante
    LaunchedEffect(formState) {
        val pub = (formState as? UiState.Success)?.data as? Publicite
        pub?.let {
            titre = it.titre
            description = it.description
            selectedType = when (it.type?.uppercase()) {
                "REDUCTION" -> PubliciteTypeOption.REDUCTION
                "PROMOTION" -> PubliciteTypeOption.PROMOTION
                "JEU" -> PubliciteTypeOption.JEU
                else -> null
            }
            imageUrl = it.image ?: it.imageUrl.orEmpty()

            it.dateExpiration?.let { dateStr ->
                try {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateFin = dateFormat.parse(dateStr)
                } catch (e: Exception) {
                    try {
                        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        dateFin = isoFormat.parse(dateStr)
                    } catch (e2: Exception) {}
                }
            }

            it.detailReduction?.let { detail ->
                pourcentageReduction = detail.pourcentage?.toString().orEmpty()
            }

            it.detailJeu?.let { detail ->
                val gains = detail.gains ?: listOf()
                nombreCases = gains.size.coerceAtLeast(3)
                gainsJeu = gains.ifEmpty { List(nombreCases) { "" } }
            }
        }
    }

    // Observer le résultat du paiement
    LaunchedEffect(paymentState.paymentUrl) {
        if (paymentState.paymentUrl != null && showPaymentSheet) {
            paymentClientSecret = paymentState.paymentUrl
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ajouter une publicité",
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color(0xFF2196F3)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Card pour les champs de base
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Titre
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Titre",
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = titre,
                            onValueChange = { titre = it },
                            placeholder = { Text("Entrez le titre de la publicité", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                focusedContainerColor = Color.White
                            )
                        )
                    }

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Description",
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Écrire une description", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                focusedContainerColor = Color.White
                            )
                        )
                    }

                    // Type de publicité
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Type de publicité",
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Réduction
                            TypeButton(
                                icon = Icons.Outlined.Percent,
                                label = "Réduction",
                                isSelected = selectedType == PubliciteTypeOption.REDUCTION,
                                onClick = { selectedType = PubliciteTypeOption.REDUCTION },
                                modifier = Modifier.weight(1f)
                            )
                            // Promotion
                            TypeButton(
                                icon = Icons.Outlined.LocalOffer,
                                label = "Promotion",
                                isSelected = selectedType == PubliciteTypeOption.PROMOTION,
                                onClick = { selectedType = PubliciteTypeOption.PROMOTION },
                                modifier = Modifier.weight(1f)
                            )
                            // Jeu
                            TypeButton(
                                icon = Icons.Outlined.Casino,
                                label = "Jeu",
                                isSelected = selectedType == PubliciteTypeOption.JEU,
                                onClick = { selectedType = PubliciteTypeOption.JEU },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Champs spécifiques selon le type
                    when (selectedType) {
                        PubliciteTypeOption.REDUCTION -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Pourcentage de réduction (%)",
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333),
                                    fontWeight = FontWeight.Medium
                                )
                                OutlinedTextField(
                                    value = pourcentageReduction,
                                    onValueChange = { pourcentageReduction = it.filter { ch -> ch.isDigit() } },
                                    placeholder = { Text("Ex: 25", fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2196F3),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        unfocusedContainerColor = Color(0xFFFAFAFA),
                                        focusedContainerColor = Color.White
                                    )
                                )
                            }
                        }
                        PubliciteTypeOption.JEU -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Configurer la roulette",
                                        fontSize = 14.sp,
                                        color = Color(0xFF333333),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "$nombreCases cases",
                                        fontSize = 12.sp,
                                        color = Color(0xFF666666)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Nombre de cases",
                                        fontSize = 14.sp,
                                        color = Color(0xFF666666)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (nombreCases > 1) {
                                                    nombreCases--
                                                    gainsJeu = gainsJeu.take(nombreCases)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .border(1.dp, Color(0xFF2196F3), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(
                                                Icons.Default.Remove,
                                                contentDescription = "Diminuer",
                                                tint = Color(0xFF2196F3),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            nombreCases.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF333333)
                                        )
                                        IconButton(
                                            onClick = {
                                                nombreCases++
                                                gainsJeu = gainsJeu + ""
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF2196F3), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Augmenter",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                gainsJeu.forEachIndexed { index, gain ->
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            "Gain ${index + 1}",
                                            fontSize = 14.sp,
                                            color = Color(0xFF666666)
                                        )
                                        OutlinedTextField(
                                            value = gain,
                                            onValueChange = { newValue ->
                                                gainsJeu = gainsJeu.toMutableList().apply {
                                                    this[index] = newValue
                                                }
                                            },
                                            placeholder = { Text("Nom du gain ${index + 1}", fontSize = 14.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF2196F3),
                                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                                focusedContainerColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            // Card pour l'image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Image de la publicité",
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Medium
                    )

                    if (imageUri != null || imageUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUri ?: imageUrl)
                                    .build(),
                                contentDescription = "Image sélectionnée",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFF2196F3),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8F9FA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF2196F3)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2196F3)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2196F3))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Télécharger une image")
                    }

                    if (isUploadingImage) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }

            // Card pour les dates
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Période de diffusion",
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        fontWeight = FontWeight.Medium
                    )

                    // Date de début
                    DatePickerFieldNew(
                        label = "Date de début",
                        date = dateDebut,
                        onDateSelected = { dateDebut = it }
                    )

                    // Date de fin
                    DatePickerFieldNew(
                        label = "Date de fin",
                        date = dateFin,
                        onDateSelected = { dateFin = it }
                    )
                }
            }

            // Boutons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        // Vérifier les champs requis
                        if (titre.isBlank() || description.isBlank() || selectedType == null ||
                            imageUrl.isBlank() || dateDebut == null || dateFin == null) {
                            Toast.makeText(context, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (dateDebut!! > dateFin!!) {
                            Toast.makeText(context, "La date de début doit être avant la date de fin", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (!isEditing && !hasPaid) {
                            showPaymentDialog = true
                        } else {
                            submitForm(
                                context = context,
                                publiciteViewModel = publiciteViewModel,
                                titre = titre,
                                description = description,
                                selectedType = selectedType,
                                imageUrl = imageUrl,
                                dateDebut = dateDebut,
                                dateFin = dateFin,
                                pourcentageReduction = pourcentageReduction,
                                gainsJeu = gainsJeu,
                                isEditing = isEditing,
                                publiciteId = publiciteId,
                                isSubmitting = { isSubmitting = it },
                                onSuccess = {
                                    Toast.makeText(context, "Publicité publiée avec succès!", Toast.LENGTH_SHORT).show()
                                    onFinish()
                                },
                                onError = { message ->
                                    Toast.makeText(context, message ?: "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Ajouter", color = Color.White)
                }

                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE0E0E0))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF666666)
                    )
                ) {
                    Text("Annuler")
                }
            }
        }
    }

    // Dialog de paiement
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Paiement requis") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pour publier une publicité, vous devez payer 10€.")
                    Text("Voulez-vous procéder au paiement ?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentDialog = false
                        stripeViewModel.createPaymentIntent(10.0) { success, paymentUrl ->
                            if (success) {
                                showPaymentSheet = true
                            } else {
                                Toast.makeText(context, "Erreur lors de la création du paiement", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Payer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Bottom Sheet Stripe
    if (showPaymentSheet) {
        StripePaymentBottomSheet(
            clientSecret = paymentClientSecret,
            publishableKey = "pk_test_51SWhKDHzDVVYaCTRXPPjTHX3wP0Qsz5aFDkOfK2ji9vd26xwucYJsFFKx271d767HVHN3f6hVC07wb6a0cnEcR5Y00UqB3vKCH",
            onPaymentResult = { success ->
                showPaymentSheet = false
                if (success) {
                    hasPaid = true
                    Toast.makeText(context, "Paiement réussi! Vous pouvez maintenant publier.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Paiement annulé", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showPaymentSheet = false }
        )
    }
}

@Composable
fun TypeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2196F3))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) Color(0xFF2196F3) else Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 12.sp,
                color = if (isSelected) Color(0xFF2196F3) else Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DatePickerFieldNew(
    label: String,
    date: Date?,
    onDateSelected: (Date) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance().apply { time = date ?: Date() }
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val cal = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateSelected(cal.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF2196F3),
                containerColor = Color(0xFFFAFAFA)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE0E0E0))
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                Icons.Outlined.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF2196F3)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                date?.let { dateFormat.format(it) } ?: "Sélectionner",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

fun submitForm(
    context: Context,
    publiciteViewModel: PubliciteViewModel,
    titre: String,
    description: String,
    selectedType: PubliciteTypeOption?,
    imageUrl: String,
    dateDebut: Date?,
    dateFin: Date?,
    pourcentageReduction: String,
    gainsJeu: List<String>,
    isEditing: Boolean,
    publiciteId: String?,
    isSubmitting: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String?) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val payload = buildMap<String, Any> {
        put("titre", titre)
        put("description", description)
        put("type", selectedType?.name?.lowercase() ?: "promotion")
        put("image", imageUrl)
        dateDebut?.let { put("dateDebut", dateFormat.format(it)) }
        dateFin?.let { put("dateExpiration", dateFormat.format(it)) }

        when (selectedType) {
            PubliciteTypeOption.REDUCTION -> {
                put("detailReduction", mapOf(
                    "pourcentage" to (pourcentageReduction.toIntOrNull() ?: 0),
                    "conditionsUtilisation" to ""
                ))
            }
            PubliciteTypeOption.PROMOTION -> {
                put("detailPromotion", mapOf(
                    "offre" to description,
                    "conditions" to ""
                ))
            }
            PubliciteTypeOption.JEU -> {
                put("detailJeu", mapOf(
                    "description" to description,
                    "gains" to gainsJeu.filter { it.isNotBlank() }
                ))
            }
            null -> {}
        }
    }

    isSubmitting(true)
    val callback: (Boolean, String?) -> Unit = { success, message ->
        isSubmitting(false)
        if (success) {
            onSuccess()
        } else {
            onError(message)
        }
    }

    if (isEditing && publiciteId != null) {
        publiciteViewModel.updatePublicite(publiciteId, payload, callback)
    } else {
        publiciteViewModel.createPublicite(payload, callback)
    }
}

@Composable
private fun NonSponsorContent(onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Accès réservé",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Seuls les sponsors peuvent créer ou modifier des publicités.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCancel) { Text("Retour") }
    }
}