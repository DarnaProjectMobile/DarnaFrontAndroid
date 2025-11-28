package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sim.darna.ui.components.AppColors
import com.sim.darna.ui.components.AppRadius
import com.sim.darna.ui.components.AppSpacing
import com.sim.darna.ui.components.ElevatedCard
import com.sim.darna.ui.components.EmptyStateCard
import com.sim.darna.ui.components.FeedbackBanner
import com.sim.darna.ui.components.SkeletonBox
import com.sim.darna.ui.components.AnimatedPageTransition
import com.sim.darna.ui.components.ConfirmationDialog
import com.sim.darna.ui.components.defaultSlideAnimationSpec
import com.sim.darna.visite.VisiteResponse
import com.sim.darna.visite.VisiteViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun MyVisitsScreen(viewModel: VisiteViewModel) {
    val context = LocalContext.current
    val uiState = viewModel.state.collectAsState().value
    var editingVisite by remember { mutableStateOf<VisiteResponse?>(null) }
    var showCancelConfirmation by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }
    var ratingVisite by remember { mutableStateOf<VisiteResponse?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadVisites()
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            // Si l'erreur indique qu'une visite n'existe plus, recharger la liste
            if (error.contains("n'existe plus", ignoreCase = true) || 
                error.contains("supprimée", ignoreCase = true)) {
                kotlinx.coroutines.delay(1000)
                viewModel.loadVisites(force = true)
            }
            viewModel.clearFeedback()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.md)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.lg, bottom = AppSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mes visites",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.visites.size} visite${if (uiState.visites.size > 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = AppColors.textSecondary
                    )
                }
                IconButton(
                    onClick = { viewModel.loadVisites(force = true) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualiser",
                        tint = AppColors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Progress Indicator
            AnimatedVisibility(
                visible = uiState.isSubmitting,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppRadius.md)),
                    color = AppColors.primary,
                    trackColor = AppColors.divider
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Error Banner
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                uiState.error?.let { error ->
                    FeedbackBanner(
                        message = error,
                        isError = true,
                        modifier = Modifier.fillMaxWidth(),
                        onDismiss = { viewModel.clearFeedback() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))

            // Statut filters avec compteurs
            StatusFiltersSection(
                visites = uiState.visites,
                selectedFilter = selectedStatusFilter,
                onFilterSelected = { status -> 
                    selectedStatusFilter = if (selectedStatusFilter == status) null else status
                }
            )

            Spacer(modifier = Modifier.height(AppSpacing.md))

            // Filtrer les visites selon le statut sélectionné
            val filteredVisites = remember(uiState.visites, selectedStatusFilter) {
                if (selectedStatusFilter == null) {
                    uiState.visites
                } else {
                    uiState.visites.filter { visite ->
                        when (selectedStatusFilter) {
                            "pending" -> visite.status?.equals("pending", ignoreCase = true) == true || 
                                        (visite.status == null || visite.status.equals("en attente", ignoreCase = true))
                            "confirmed" -> visite.status?.equals("confirmed", ignoreCase = true) == true ||
                                           visite.status?.equals("acceptée", ignoreCase = true) == true
                            "refused" -> visite.status?.equals("refused", ignoreCase = true) == true ||
                                         visite.status?.equals("refusée", ignoreCase = true) == true
                            "completed" -> visite.status?.equals("completed", ignoreCase = true) == true ||
                                          visite.status?.equals("terminée", ignoreCase = true) == true
                            else -> true
                        }
                    }
                }
            }

            // Content
            when {
                uiState.isLoadingList && filteredVisites.isEmpty() -> {
                    AnimatedPageTransition(visible = true) {
                        VisitSkeletonList()
                    }
                }
                filteredVisites.isEmpty() -> {
                    AnimatedPageTransition(visible = true) {
                        EmptyStateCard(
                            title = if (selectedStatusFilter != null) {
                                "Aucune visite ${when(selectedStatusFilter) {
                                    "pending" -> "en attente"
                                    "confirmed" -> "acceptée"
                                    "refused" -> "refusée"
                                    "completed" -> "terminée"
                                    else -> ""
                                }}"
                            } else {
                                "Aucune visite pour le moment"
                            },
                            description = if (selectedStatusFilter != null) {
                                "Aucune visite ne correspond à ce filtre."
                            } else {
                                "Réservez votre première visite pour la voir apparaître ici."
                            },
                            actionLabel = "Actualiser",
                            onAction = { viewModel.loadVisites(force = true) },
                            icon = Icons.Default.EventNote
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                        contentPadding = PaddingValues(bottom = AppSpacing.xl)
                    ) {
                        items(
                            items = filteredVisites,
                            key = { visite -> visite.id ?: visite.dateVisite ?: "" }
                        ) { visite ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = defaultSlideAnimationSpec
                                ),
                                exit = fadeOut() + slideOutVertically(
                                    targetOffsetY = { it / 2 },
                                    animationSpec = defaultSlideAnimationSpec
                                )
                            ) {
                                ModernVisitCard(
                                    visite = visite,
                                    onEdit = { editingVisite = it },
                                    onCancel = { id -> showCancelConfirmation = id },
                                    onDelete = { id -> showDeleteConfirmation = id },
                                    onValidate = { id -> viewModel.validateVisite(id) },
                                    onRate = { ratingVisite = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    editingVisite?.let { visite ->
        EditVisiteDialog(
            visite = visite,
            onDismiss = { editingVisite = null },
            onValidate = { dateMillis, hour, minute, notes, contact ->
                val visiteId = visite.id
                if (visiteId != null) {
                    viewModel.updateVisite(
                        visiteId = visiteId,
                        dateMillis = dateMillis,
                        hour = hour,
                        minute = minute,
                        notes = notes,
                        contactPhone = contact
                    )
                }
                editingVisite = null
            }
        )
    }

    // Cancel Confirmation
    showCancelConfirmation?.let { id ->
        ConfirmationDialog(
            title = "Annuler la visite",
            message = "Voulez-vous vraiment annuler cette visite ?",
            confirmText = "Oui",
            cancelText = "Non",
            onConfirm = {
                viewModel.cancelVisite(id)
                showCancelConfirmation = null
            },
            onDismiss = { showCancelConfirmation = null },
            isDestructive = true
        )
    }

    // Delete Confirmation
    showDeleteConfirmation?.let { id ->
        ConfirmationDialog(
            title = "Supprimer la visite",
            message = "Êtes-vous sûr de vouloir supprimer définitivement cette visite ?",
            confirmText = "Supprimer",
            cancelText = "Annuler",
            onConfirm = {
                viewModel.deleteVisite(id)
                showDeleteConfirmation = null
            },
            onDismiss = { showDeleteConfirmation = null },
            isDestructive = true
        )
    }

    // Rating Dialog
    ratingVisite?.let { visite ->
        RatingDialog(
            visiteTitle = getLogementTitle(visite),
            onDismiss = { ratingVisite = null },
            onSubmit = { collector, clean, location, conformity, comment ->
                val visiteId = visite.id
                if (visiteId != null && visiteId.isNotBlank()) {
                    // Vérifier que la visite peut être évaluée
                    if (visite.validated == true && 
                        visite.status.equals("completed", ignoreCase = true) &&
                        visite.reviewId == null) {
                        viewModel.submitReview(
                            visiteId = visiteId,
                            collectorRating = collector,
                            cleanlinessRating = clean,
                            locationRating = location,
                            conformityRating = conformity,
                            comment = comment
                        )
                        ratingVisite = null
                    } else {
                        Toast.makeText(
                            context,
                            "Cette visite ne peut pas être évaluée. Vérifiez qu'elle est validée et terminée.",
                            Toast.LENGTH_LONG
                        ).show()
                        ratingVisite = null
                    }
                } else {
                    // Afficher une erreur si l'ID est manquant
                    Toast.makeText(
                        context,
                        "Erreur: L'identifiant de la visite est manquant. Veuillez actualiser la liste.",
                        Toast.LENGTH_LONG
                    ).show()
                    ratingVisite = null
                }
            }
        )
    }
}

@Composable
private fun VisitSkeletonList() {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        repeat(3) {
            ElevatedCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(20.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(16.dp))
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(AppRadius.md)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernVisitCard(
    visite: VisiteResponse,
    onEdit: (VisiteResponse) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit,
    onValidate: (String) -> Unit,
    onRate: (VisiteResponse) -> Unit
) {
    val statusStyle = mapStatus(visite.status)
    val isPending = visite.status?.equals("pending", ignoreCase = true) == true || 
                    (visite.status == null || visite.status.equals("en attente", ignoreCase = true))
    val isAccepted = visite.status?.equals("confirmed", ignoreCase = true) == true ||
                     visite.status?.equals("acceptée", ignoreCase = true) == true
    val isCompleted = visite.status?.equals("completed", ignoreCase = true) == true ||
                      visite.status?.equals("terminée", ignoreCase = true) == true
    val isRefused = visite.status?.equals("refused", ignoreCase = true) == true ||
                    visite.status?.equals("refusée", ignoreCase = true) == true
    val canValidate = isAccepted && (visite.validated != true)
    val canRate = isCompleted &&
            (visite.validated == true) &&
            visite.reviewId == null &&
            visite.id != null
    // Permettre l'annulation même après acceptation
    val canCancel = isAccepted && visite.id != null
    // Permettre la suppression pour les visites en attente uniquement
    val canDelete = isPending && visite.id != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp)),
        color = AppColors.surface,
        border = BorderStroke(
            width = 4.dp,
            color = statusStyle.color.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusStyle.color.copy(alpha = 0.08f))
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(40.dp)
                            .background(statusStyle.color)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getLogementTitle(visite),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusStyle.color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusStyle.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusStyle.color
                    )
                }
            }

            // Contenu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                // Date / heure
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = formatDate(visite.dateVisite),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.textPrimary
                            )
                            Text(
                                text = formatTime(visite.dateVisite),
                                fontSize = 12.sp,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }

                // Séparateur
                if (!visite.contactPhone.isNullOrBlank() || !visite.notes.isNullOrBlank()) {
                    Divider(
                        color = AppColors.divider,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = AppSpacing.xs)
                    )
                }

                // Contact téléphone
                if (!visite.contactPhone.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = visite.contactPhone,
                            fontSize = 14.sp,
                            color = AppColors.textPrimary
                        )
                    }
                }

                // Notes
                if (!visite.notes.isNullOrBlank()) {
                    if (!visite.contactPhone.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                    }
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = visite.notes ?: "",
                            fontSize = 13.sp,
                            color = AppColors.textSecondary,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Actions - Ne pas afficher pour les visites refusées
            if (!isRefused && (canValidate || canRate || canCancel || canDelete || visite.id != null)) {
                Divider(color = AppColors.divider, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    // Pour les visites "En attente": Afficher Modifier et Supprimer (icône poubelle)
                    if (isPending && visite.id != null) {
                        // Modifier
                        TextButton(
                            onClick = { onEdit(visite) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AppColors.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Modifier", fontSize = 13.sp, color = AppColors.primary)
                        }
                        
                        // Supprimer (icône poubelle)
                        IconButton(
                            onClick = { onDelete(visite.id) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = AppColors.danger,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    // Pour les visites "Acceptée": Afficher Annuler (icône) - remplace Modifier
                    else if (canCancel) {
                        // Annuler (icône)
                        IconButton(
                            onClick = { visite.id?.let(onCancel) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = "Annuler",
                                tint = AppColors.danger,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    // Pour les autres statuts: Afficher Modifier si disponible (sauf terminée et refusée)
                    else if (visite.id != null && !isAccepted && !isCompleted && !isRefused) {
                        TextButton(
                            onClick = { onEdit(visite) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AppColors.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Modifier", fontSize = 13.sp, color = AppColors.primary)
                        }
                    }

                    // Visite effectuée
                    if (canValidate && visite.id != null) {
                        TextButton(
                            onClick = { onValidate(visite.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.TaskAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AppColors.success
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Visite effectuée", fontSize = 13.sp, color = AppColors.success)
                        }
                    }

                    // Évaluer - Bouton visible pour les visites terminées
                    if (canRate && visite.id != null) {
                        Button(
                            onClick = { 
                                // Vérifier toutes les conditions avant d'évaluer
                                if (visite.validated == true && 
                                    visite.status.equals("completed", ignoreCase = true) &&
                                    visite.reviewId == null) {
                                    onRate(visite)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = visite.id != null && visite.validated == true
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Évaluer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditVisiteDialog(
    visite: VisiteResponse,
    onDismiss: () -> Unit,
    onValidate: (Long, Int, Int, String?, String?) -> Unit
) {
    val initialDateMillis = parseIsoToMillis(visite.dateVisite) ?: System.currentTimeMillis()
    val initialTime = extractHourMinute(visite.dateVisite)

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    var selectedHour by remember(key1 = visite.id) {
        mutableStateOf(initialTime.first)
    }
    var selectedMinute by remember(key1 = visite.id) {
        mutableStateOf(initialTime.second)
    }
    var timeSelectorExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(visite.notes.orEmpty()) }
    var contact by remember { mutableStateOf(visite.contactPhone.orEmpty()) }

    fun buildFinalDateTime(): Long {
        val dateMillis = datePickerState.selectedDateMillis ?: initialDateMillis
        // Create calendar in LOCAL timezone with the selected date and time
        val calendar = Calendar.getInstance().apply { // Uses local timezone
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    val timeSlots = remember {
        listOf(
            9 to 0, 9 to 30, 10 to 0, 10 to 30,
            11 to 0, 11 to 30, 14 to 0, 14 to 30,
            15 to 0, 15 to 30, 16 to 0, 16 to 30
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modifier la visite",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Fermer",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(20.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Date Picker
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF0066FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Date de la visite",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            androidx.compose.material3.DatePicker(
                                state = datePickerState,
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.DatePickerDefaults.colors(
                                    containerColor = Color(0xFFF8FAFC)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Time Picker
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF0066FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Créneau horaire",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Box {
                            OutlinedButton(
                                onClick = { timeSelectorExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF1E293B),
                                    containerColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    Color(0xFFE2E8F0)
                                )
                            ) {
                                Text(
                                    text = String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d",
                                        selectedHour,
                                        selectedMinute
                                    ),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF0066FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            androidx.compose.material3.DropdownMenu(
                                expanded = timeSelectorExpanded,
                                onDismissRequest = { timeSelectorExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                timeSlots.forEach { (hour, minute) ->
                                    val isSelected = selectedHour == hour && selectedMinute == minute
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    String.format(
                                                        Locale.getDefault(),
                                                        "%02d:%02d",
                                                        hour,
                                                        minute
                                                    ),
                                                    fontSize = 16.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color(0xFF0066FF) else Color(0xFF1E293B)
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color(0xFF0066FF),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedHour = hour
                                            selectedMinute = minute
                                            timeSelectorExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Contact
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = {
                            Text(
                                "Téléphone de contact",
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0066FF),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = {
                            Text(
                                "Notes (optionnel)",
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0066FF),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Annuler",
                            tint = Color(0xFF64748B)
                        )
                    }
                    Button(
                        onClick = {
                            val finalDateTime = buildFinalDateTime()
                            onValidate(
                                finalDateTime,
                                selectedHour,
                                selectedMinute,
                                notes.ifBlank { null },
                                contact.ifBlank { null }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Enregistrer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingRow(label: String, value: Float, onChange: (Float) -> Unit) {
    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    Slider(
        value = value,
        onValueChange = { onChange(it.coerceIn(1f, 5f)) },
        valueRange = 1f..5f,
        steps = 3
    )
    Text(
        text = "${value.toInt()}/5",
        fontSize = 12.sp,
        color = AppColors.textSecondary
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun RatingDialog(
    visiteTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, Int, Int, Int, String?) -> Unit
) {
    var collector by remember { mutableStateOf(5f) }
    var cleanliness by remember { mutableStateOf(5f) }
    var location by remember { mutableStateOf(5f) }
    var conformity by remember { mutableStateOf(5f) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Évaluer la visite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = visiteTitle,
                    fontSize = 14.sp,
                    color = AppColors.textSecondary
                )

                Spacer(Modifier.height(16.dp))

                RatingRow("Collector", collector) { collector = it }
                RatingRow("Propreté du logement", cleanliness) { cleanliness = it }
                RatingRow("Localisation", location) { location = it }
                RatingRow("Conformité au descriptif", conformity) { conformity = it }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire (optionnel)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Annuler",
                            tint = AppColors.textSecondary
                        )
                    }
                    Button(
                        onClick = {
                            onSubmit(
                                collector.toInt(),
                                cleanliness.toInt(),
                                location.toInt(),
                                conformity.toInt(),
                                comment.ifBlank { null }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                    ) {
                        Text("Envoyer")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFiltersSection(
    visites: List<VisiteResponse>,
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit
) {
    // Compter les visites par statut
    val pendingCount = visites.count { 
        it.status?.equals("pending", ignoreCase = true) == true || 
        (it.status == null || it.status.equals("en attente", ignoreCase = true))
    }
    val confirmedCount = visites.count { 
        it.status?.equals("confirmed", ignoreCase = true) == true ||
        it.status?.equals("acceptée", ignoreCase = true) == true
    }
    // Annulées par le client (cancelled uniquement)
    val cancelledCount = visites.count { 
        it.status?.equals("cancelled", ignoreCase = true) == true
    }
    // Refusées par le colocataire (refused uniquement)
    val refusedCount = visites.count { 
        it.status?.equals("refused", ignoreCase = true) == true ||
        it.status?.equals("refusée", ignoreCase = true) == true
    }
    val completedCount = visites.count { 
        it.status?.equals("completed", ignoreCase = true) == true ||
        it.status?.equals("terminée", ignoreCase = true) == true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        // En attente
        StatusFilterCard(
            label = "En attente",
            count = pendingCount,
            color = AppColors.warning,
            icon = Icons.Default.Schedule,
            isSelected = selectedFilter == "pending",
            onClick = { onFilterSelected(if (selectedFilter == "pending") null else "pending") }
        )
        
        // Acceptée
        StatusFilterCard(
            label = "Acceptée",
            count = confirmedCount,
            color = AppColors.success,
            icon = Icons.Default.CheckCircle,
            isSelected = selectedFilter == "confirmed",
            onClick = { onFilterSelected(if (selectedFilter == "confirmed") null else "confirmed") }
        )
        
        // Refusée (par le colocataire)
        StatusFilterCard(
            label = "Refusée",
            count = refusedCount,
            color = AppColors.danger.copy(alpha = 0.7f),
            icon = Icons.Default.EventBusy,
            isSelected = selectedFilter == "refused",
            onClick = { onFilterSelected(if (selectedFilter == "refused") null else "refused") }
        )
        
        // Terminée
        StatusFilterCard(
            label = "Terminée",
            count = completedCount,
            color = AppColors.primary,
            icon = Icons.Default.TaskAlt,
            isSelected = selectedFilter == "completed",
            onClick = { onFilterSelected(if (selectedFilter == "completed") null else "completed") }
        )
    }
}

@Composable
private fun StatusFilterCard(
    label: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else color.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(AppRadius.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = color
                )
                Text(
                    text = "$count",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

private data class VisitStatusStyle(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

// Logements mock pour résoudre les titres
internal val mockLogementsMap = mapOf(
    "mock-1" to "Appartement 3 pièces - Centre Ville",
    "mock-2" to "Studio meublé - Lyon",
    "mock-3" to "Chambre dans T4 - Marseille 8e",
    "mock-4" to "Studio meublé - Lyon",
    "appartement-3-pieces-centre-ville" to "Appartement 3 pièces - Centre Ville",
    "studio-meuble-lyon-1" to "Studio meublé - Lyon",
    "studio-meuble-lyon" to "Studio meublé - Lyon",
    "studio-meuble-lyon-2" to "Studio meublé - Lyon",
    "chambre-t4-marseille-8e" to "Chambre dans T4 - Marseille 8e"
)

// Fonction pour obtenir le titre du logement
private fun getLogementTitle(visite: VisiteResponse): String {
    // 1. Utiliser logementTitle si disponible
    if (!visite.logementTitle.isNullOrBlank()) {
        return visite.logementTitle
    }
    
    // 2. Chercher dans les logements mock par logementId
    visite.logementId?.let { logementId ->
        mockLogementsMap[logementId]?.let { return it }
        
        // Si le logementId contient "LOGEMENT_ID", essayer de trouver un titre par défaut
        if (logementId.contains("LOGEMENT_ID", ignoreCase = true)) {
            // Extraire le numéro si possible
            val number = logementId.replace(Regex("[^0-9]"), "")
            if (number.isNotBlank()) {
                val index = number.toIntOrNull()?.let { 
                    if (it <= mockLogementsMap.size) it - 1 else null
                }
                if (index != null && index >= 0) {
                    val titles = mockLogementsMap.values.toList()
                    if (index < titles.size) {
                        return titles[index]
                    }
                }
            }
        }
    }
    
    // 3. Fallback par défaut
    return visite.logementId ?: "Logement inconnu"
}

private fun mapStatus(status: String?): VisitStatusStyle {
    return when (status?.lowercase()) {
        "confirmed" -> VisitStatusStyle("Acceptée", AppColors.success, Icons.Default.CheckCircle)
        "cancelled" -> VisitStatusStyle("Annulée", AppColors.danger, Icons.Default.Cancel)
        "refused" -> VisitStatusStyle("Refusée", AppColors.danger.copy(alpha = 0.7f), Icons.Default.EventBusy)
        "completed" -> VisitStatusStyle("Terminée", AppColors.primary, Icons.Default.TaskAlt)
        else -> VisitStatusStyle("En attente", AppColors.warning, Icons.Default.Schedule)
    }
}

private fun parseIsoToMillis(dateString: String?): Long? {
    if (dateString.isNullOrBlank()) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        parser.parse(dateString)?.time
    } catch (_: Exception) {
        null
    }
}

private fun formatDate(dateString: String?): String {
    val date = parseIsoToMillis(dateString) ?: return "-"
    val formatter = SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault())
    return formatter.format(date).replaceFirstChar { it.uppercase() }
}

private fun formatTime(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "-"

    try {
        // Parse the UTC date string to get milliseconds
        val millis = parseIsoToMillis(dateString) ?: return "-"
        
        // Create a calendar in LOCAL timezone to display the time correctly
        val calendar = Calendar.getInstance() // Uses local timezone by default
        calendar.timeInMillis = millis
        
        // Format in local time
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    } catch (_: Exception) {
        return "-"
    }
}

private fun extractHourMinute(dateString: String?): Pair<Int, Int> {
    if (dateString.isNullOrBlank()) {
        return 14 to 0
    }
    try {
        val timePart = dateString.substringAfter("T").substringBefore(".")
        val parts = timePart.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            return hour to minute
        }
    } catch (_: Exception) {
    }

    // Parse UTC date and convert to local time
    val millis = parseIsoToMillis(dateString)
    if (millis != null) {
        val calendar = Calendar.getInstance() // Uses local timezone
        calendar.timeInMillis = millis
        return calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)
    }
    return 14 to 0
}

// Composant d'alerte amélioré pour les visites terminées
@Composable
private fun BeautifulRatingAlert(
    count: Int,
    modifier: Modifier = Modifier
) {
    // Animation pour l'icône étoile
    val infiniteTransition = rememberInfiniteTransition(label = "star_animation")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "star_scale"
    )
    
    // Animation d'entrée
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(400, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(400)
        ) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(400)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(
            animationSpec = tween(300)
        )
    ) {
        Card(
            modifier = modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(AppRadius.lg),
                    spotColor = AppColors.primary.copy(alpha = 0.3f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(AppRadius.lg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                AppColors.primary.copy(alpha = 0.08f),
                                AppColors.primary.copy(alpha = 0.12f),
                                AppColors.primary.copy(alpha = 0.08f)
                            )
                        )
                    )
            ) {
                // Bordure gauche colorée
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AppColors.primary,
                                    AppColors.primaryVariant
                                )
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = AppSpacing.lg,
                            end = AppSpacing.md,
                            top = AppSpacing.md,
                            bottom = AppSpacing.md
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    // Icône étoile animée avec fond circulaire
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        AppColors.primary.copy(alpha = 0.2f),
                                        AppColors.primary.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = AppColors.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .scale(starScale)
                        )
                    }
                    
                    // Contenu textuel
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                        ) {
                            Text(
                                text = "Visite terminée",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.textPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AppColors.primary,
                                modifier = Modifier.padding(start = AppSpacing.xs)
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = "Vous avez ${if (count > 1) "$count visites" else "une visite"} terminée${if (count > 1) "s" else ""} à évaluer. Cliquez sur 'Évaluer' dans la carte.",
                            fontSize = 13.sp,
                            color = AppColors.textSecondary,
                            lineHeight = 18.sp
                        )
                    }
                    
                    // Icône flèche
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = AppColors.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}