package com.sim.darna.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sim.darna.auth.UserSessionManager
import com.sim.darna.data.model.Publicite
import com.sim.darna.viewmodel.PubliciteViewModel
import com.sim.darna.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran pour ajouter ou modifier une publicité
 */
@Composable
fun AddPubliciteScreen(
    publiciteId: String? = null,             // ID si édition
    onFinish: () -> Unit = {},               // Callback après enregistrement
    onCancel: () -> Unit = {},               // Callback pour annuler
    viewModel: PubliciteViewModel = hiltViewModel()
) {
    // Vérifie si l'utilisateur est sponsor
    val isSponsor = UserSessionManager.isSponsor()

    // Si l'utilisateur n'est pas sponsor, afficher message et bouton retour
    if (!isSponsor) {
        NonSponsorContent(onCancel = onCancel)
        return
    }

    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current

    // États des champs du formulaire
    var titre by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("Promotion") }
    var pourcentage by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var codePromo by rememberSaveable { mutableStateOf("") }
    var dateDebut by rememberSaveable { mutableStateOf<Date?>(null) }
    var dateFin by rememberSaveable { mutableStateOf<Date?>(null) }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val isEditing = !publiciteId.isNullOrBlank()

    // Charger la publicité si édition
    LaunchedEffect(publiciteId) {
        if (isEditing) viewModel.loadPublicite(publiciteId!!)
    }

    // Remplir les champs avec la publicité existante
    LaunchedEffect(formState) {
        val pub = (formState as? UiState.Success)?.data as? Publicite
        pub?.let {
            titre = it.titre
            description = it.description
            type = it.type ?: "Promotion"
            pourcentage = it.pourcentageReduction?.toString().orEmpty()
            imageUrl = it.imageUrl.orEmpty()
            codePromo = it.codePromo.orEmpty()
            dateDebut = it.dateDebut
            dateFin = it.dateFin
        }
    }

    // Contenu du formulaire
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isEditing) "Modifier la publicité" else "Ajouter une publicité",
            style = MaterialTheme.typography.titleLarge
        )

        // Affichage loading ou erreur
        when (formState) {
            is UiState.Loading -> if (isEditing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            is UiState.Error -> Text(
                (formState as UiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            else -> Unit
        }

        // Champs texte
        OutlinedTextField(
            value = titre,
            onValueChange = { titre = it },
            label = { Text("Titre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Type") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = pourcentage,
            onValueChange = { pourcentage = it.filter { ch -> ch.isDigit() } },
            label = { Text("Pourcentage de réduction (%)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("https://...") }
        )

        // Sélecteur de dates
        DatePickersRow(
            labelStart = "Date de début",
            dateStart = dateDebut,
            labelEnd = "Date de fin",
            dateEnd = dateFin,
            onDateStartChange = { dateDebut = it },
            onDateEndChange = { dateFin = it },
            formatter = dateFormat
        )

        OutlinedTextField(
            value = codePromo,
            onValueChange = { codePromo = it },
            label = { Text("Code promo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Boutons Annuler / Enregistrer
        Row(
            modifier = Modifier.align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, enabled = !isSubmitting) {
                Text("Annuler")
            }
            Button(
                onClick = {
                    // Préparer payload pour API
                    val payload = buildMap<String, Any> {
                        put("titre", titre)
                        put("description", description)
                        put("type", type)
                        pourcentage.toIntOrNull()?.let { put("pourcentageReduction", it) }
                        if (imageUrl.isNotBlank()) put("imageUrl", imageUrl)
                        dateDebut?.let { put("dateDebut", it) }
                        dateFin?.let { put("dateFin", it) }
                        if (codePromo.isNotBlank()) put("codePromo", codePromo)
                    }

                    isSubmitting = true
                    val callback: (Boolean, String?) -> Unit = { success, message ->
                        isSubmitting = false
                        if (success) {
                            onFinish() // retour à la liste
                        } else {
                            Toast.makeText(context, message ?: "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Création ou modification selon le mode
                    if (isEditing && publiciteId != null) {
                        viewModel.updatePublicite(publiciteId, payload, callback)
                    } else {
                        viewModel.createPublicite(payload, callback)
                    }
                },
                enabled = !isSubmitting && titre.isNotBlank() && description.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSubmitting) "Enregistrement..." else "Enregistrer")
            }
        }
    }
}

/**
 * Ligne avec deux DatePickers (début / fin)
 */
@Composable
private fun DatePickersRow(
    labelStart: String,
    dateStart: Date?,
    labelEnd: String,
    dateEnd: Date?,
    onDateStartChange: (Date) -> Unit,
    onDateEndChange: (Date) -> Unit,
    formatter: SimpleDateFormat
) {
    val context = LocalContext.current

    fun openPicker(initial: Date?, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance().apply { time = initial ?: Date() }
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
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = dateStart?.let(formatter::format) ?: "",
            onValueChange = {},
            label = { Text(labelStart) },
            modifier = Modifier.weight(1f),
            enabled = false
        )
        Button(onClick = { openPicker(dateStart, onDateStartChange) }) { Text("Choisir") }

        OutlinedTextField(
            value = dateEnd?.let(formatter::format) ?: "",
            onValueChange = {},
            label = { Text(labelEnd) },
            modifier = Modifier.weight(1f),
            enabled = false
        )
        Button(onClick = { openPicker(dateEnd, onDateEndChange) }) { Text("Choisir") }
    }
}

/**
 * Contenu affiché si l'utilisateur n'est pas sponsor
 */
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
