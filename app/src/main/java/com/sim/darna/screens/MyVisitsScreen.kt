package com.sim.darna.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sim.darna.ui.components.*
import com.sim.darna.visite.VisiteResponse
import com.sim.darna.visite.VisiteViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyVisitsScreen(
    viewModel: VisiteViewModel,
    navController: NavController? = null,
    parentNavController: NavHostController? = null
) {
    val context = LocalContext.current
    val uiState = viewModel.state.collectAsState().value
    var editingVisite by remember { mutableStateOf<VisiteResponse?>(null) }
    var showCancelConfirmation by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }
    var ratingVisite by remember { mutableStateOf<VisiteResponse?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoadingList)

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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.background,
                        AppColors.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.md)
        ) {
            // Header moderne avec animation
            ModernHeader(visitCount = uiState.visites.size)

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Progress Indicator avec animation
            AnimatedVisibility(
                visible = uiState.isSubmitting,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppRadius.round)),
                    color = AppColors.primary,
                    trackColor = AppColors.divider
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Error Banner
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn() + expandVertically(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut() + shrinkVertically()
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

            // Filtres de statut avec animation
            AnimatedStatusFilters(
                visites = uiState.visites,
                selectedFilter = selectedStatusFilter,
                onFilterSelected = { status -> 
                    selectedStatusFilter = if (selectedStatusFilter == status) null else status
                }
            )

            Spacer(modifier = Modifier.height(AppSpacing.md))

            // Filtrer les visites
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

            // Content avec animations
            when {
                uiState.isLoadingList && filteredVisites.isEmpty() -> {
                    AnimatedPageTransition(visible = true) {
                        ModernVisitSkeletonList()
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
                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = { viewModel.loadVisites(force = true) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                            contentPadding = PaddingValues(bottom = AppSpacing.xl)
                        ) {
                            itemsIndexed(
                                items = filteredVisites,
                                key = { _, visite -> visite.id ?: visite.dateVisite ?: "" }
                            ) { index, visite ->
                                AnimatedVisitCard(
                                    visite = visite,
                                    index = index,
                                    onEdit = { editingVisite = it },
                                    onCancel = { id -> showCancelConfirmation = id },
                                    onDelete = { id -> showDeleteConfirmation = id },
                                    onValidate = { id -> viewModel.validateVisite(id) },
                                    onRate = { ratingVisite = it },
                                    onChat = { visite ->
                                        val visiteId = visite.id
                                        if (visiteId == null) {
                                            Toast.makeText(context, "ID de visite manquant", Toast.LENGTH_SHORT).show()
                                            return@AnimatedVisitCard
                                        }
                                        val visiteTitle = getLogementTitle(visite)
                                        val encodedTitle = URLEncoder.encode(visiteTitle, StandardCharsets.UTF_8.name())
                                        try {
                                            val targetNavController = parentNavController ?: navController
                                            targetNavController?.navigate("chat/$visiteId/$encodedTitle") ?: run {
                                                Toast.makeText(context, "Navigation non disponible", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("MyVisitsScreen", "Erreur de navigation", e)
                                            Toast.makeText(context, "Erreur de navigation: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
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

    editingVisite?.let { visite ->
        ModificationDialog(
            visite = visite,
            onDismiss = { editingVisite = null },
            onSubmit = { dateMillis, hour, minute, notes, contactPhone ->
                visite.id?.let { id ->
                    viewModel.updateVisite(id, dateMillis, hour, minute, notes, contactPhone)
                }
                editingVisite = null
            }
        )
    }

    ratingVisite?.let { visite ->
        RatingDialog(
            visiteTitle = getLogementTitle(visite),
            onDismiss = { ratingVisite = null },
            onSubmit = { collector, clean, location, conformity, comment ->
                val visiteId = visite.id
                if (visiteId != null && visiteId.isNotBlank()) {
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
private fun ModernHeader(visitCount: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.lg, bottom = AppSpacing.md)
    ) {
        // Background gradient avec animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .alpha(alpha)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.primary.copy(alpha = 0.1f),
                            AppColors.secondary.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(AppRadius.lg)
                )
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mes visites",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppColors.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Text(
                        text = "$visitCount visite${if (visitCount > 1) "s" else ""}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedStatusFilters(
    visites: List<VisiteResponse>,
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit
) {
    val filters = remember(visites) {
        listOf(
            FilterItem("pending", "En attente", Icons.Default.Schedule, AppColors.warning),
            FilterItem("confirmed", "Acceptée", Icons.Default.CheckCircle, AppColors.success),
            FilterItem("completed", "Terminée", Icons.Default.TaskAlt, AppColors.info),
            FilterItem("refused", "Refusée", Icons.Default.Cancel, AppColors.danger)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        filters.forEach { filter ->
            val count = visites.count { visite ->
                when (filter.key) {
                    "pending" -> visite.status?.equals("pending", ignoreCase = true) == true || 
                                (visite.status == null || visite.status.equals("en attente", ignoreCase = true))
                    "confirmed" -> visite.status?.equals("confirmed", ignoreCase = true) == true ||
                                   visite.status?.equals("acceptée", ignoreCase = true) == true
                    "refused" -> visite.status?.equals("refused", ignoreCase = true) == true ||
                                 visite.status?.equals("refusée", ignoreCase = true) == true
                    "completed" -> visite.status?.equals("completed", ignoreCase = true) == true ||
                                  visite.status?.equals("terminée", ignoreCase = true) == true
                    else -> false
                }
            }
            
            AnimatedFilterChip(
                filter = filter,
                count = count,
                isSelected = selectedFilter == filter.key,
                onClick = { onFilterSelected(filter.key) }
            )
        }
    }
}

@Composable
private fun AnimatedFilterChip(
    filter: FilterItem,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chip_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) filter.color.copy(alpha = 0.2f) else AppColors.surface,
        animationSpec = tween(300),
        label = "chip_bg"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadius.round),
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) filter.color else AppColors.divider
        ),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = filter.icon,
                contentDescription = null,
                tint = if (isSelected) filter.color else AppColors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = filter.label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) filter.color else AppColors.textPrimary
            )
            if (count > 0) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) filter.color else AppColors.textSecondary
                ) {
                    Text(
                        text = count.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernVisitSkeletonList() {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "skeleton_$index")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "skeleton_alpha"
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha),
                shape = RoundedCornerShape(AppRadius.lg),
                colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(24.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp))
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(AppRadius.md)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedVisitCard(
    visite: VisiteResponse,
    index: Int,
    onEdit: (VisiteResponse) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit,
    onValidate: (String) -> Unit,
    onRate: (VisiteResponse) -> Unit,
    onChat: (VisiteResponse) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 100).toLong())
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500)) + 
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + 
                scaleIn(initialScale = 0.8f, animationSpec = tween(500)),
        exit = fadeOut() + slideOutVertically() + scaleOut()
    ) {
        ModernVisitCard(
            visite = visite,
            onEdit = onEdit,
            onCancel = onCancel,
            onDelete = onDelete,
            onValidate = onValidate,
            onRate = onRate,
            onChat = onChat
        )
    }
}

@Composable
private fun ModernVisitCard(
    visite: VisiteResponse,
    onEdit: (VisiteResponse) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit,
    onValidate: (String) -> Unit,
    onRate: (VisiteResponse) -> Unit,
    onChat: (VisiteResponse) -> Unit
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
    val canCancel = isAccepted && visite.id != null
    val canDelete = isPending && visite.id != null

    var isHovered by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 8.dp else 4.dp,
        animationSpec = tween(300),
        label = "card_elevation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(AppRadius.lg),
                spotColor = statusStyle.color.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(AppRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    statusStyle.color.copy(alpha = 0.4f),
                    statusStyle.color.copy(alpha = 0.2f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.surface,
                            AppColors.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            // Header avec effet glassmorphism
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                statusStyle.color.copy(alpha = 0.15f),
                                statusStyle.color.copy(alpha = 0.08f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icône animée
                        val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")
                        val iconScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "icon_scale"
                        )
                        
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .scale(iconScale),
                            shape = CircleShape,
                            color = statusStyle.color.copy(alpha = 0.2f),
                            border = BorderStroke(2.dp, statusStyle.color.copy(alpha = 0.4f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = statusStyle.icon,
                                    contentDescription = null,
                                    tint = statusStyle.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = getLogementTitle(visite),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.textPrimary
                            )
                        }
                    }
                    
                    // Badge de statut avec animation
                    Surface(
                        shape = RoundedCornerShape(AppRadius.round),
                        color = statusStyle.color.copy(alpha = 0.25f),
                        border = BorderStroke(1.5.dp, statusStyle.color.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = statusStyle.label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusStyle.color
                        )
                    }
                }
            }

            // Contenu avec animations
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                // Date / heure avec icône animée
                Surface(
                    shape = RoundedCornerShape(AppRadius.md),
                    color = AppColors.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = AppColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            androidx.compose.material3.Text(
                                text = formatVisitDate(visite.dateVisite),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.textPrimary
                            )
                            androidx.compose.material3.Text(
                                text = formatVisitTime(visite.dateVisite),
                                fontSize = 13.sp,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }

                // Contact et notes
                if (!visite.contactPhone.isNullOrBlank() || !visite.notes.isNullOrBlank()) {
                    Divider(
                        color = AppColors.divider.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = AppSpacing.xs)
                    )
                }

                if (!visite.contactPhone.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = visite.contactPhone,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.textPrimary
                        )
                    }
                }

                if (!visite.notes.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(AppRadius.sm),
                        color = AppColors.info.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, AppColors.info.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(AppSpacing.sm),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Note,
                                contentDescription = null,
                                tint = AppColors.info,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = visite.notes ?: "",
                                fontSize = 14.sp,
                                color = AppColors.textPrimary,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Actions avec animations
            if (!isRefused && (canValidate || canRate || canCancel || canDelete || visite.id != null)) {
                Divider(color = AppColors.divider.copy(alpha = 0.5f), thickness = 1.dp)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    if (isPending && visite.id != null) {
                        AnimatedActionButton(
                            onClick = { onEdit(visite) },
                            icon = Icons.Default.Edit,
                            label = "Modifier",
                            color = AppColors.primary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        AnimatedIconButton(
                            onClick = { onDelete(visite.id) },
                            icon = Icons.Default.Delete,
                            color = AppColors.danger
                        )
                    }
                    else if (canCancel) {
                        AnimatedIconButton(
                            onClick = { onChat(visite) },
                            icon = Icons.Default.Chat,
                            color = AppColors.primary
                        )
                        
                        AnimatedIconButton(
                            onClick = { visite.id?.let(onCancel) },
                            icon = Icons.Default.Cancel,
                            color = AppColors.danger
                        )
                    }

                    if (canValidate && visite.id != null) {
                        AnimatedActionButton(
                            onClick = { onValidate(visite.id) },
                            icon = Icons.Default.TaskAlt,
                            label = "Visite effectuée",
                            color = AppColors.success,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (canRate && visite.id != null) {
                        AnimatedActionButton(
                            onClick = { 
                                if (visite.validated == true && 
                                    visite.status.equals("completed", ignoreCase = true) &&
                                    visite.reviewId == null) {
                                    onRate(visite)
                                }
                            },
                            icon = Icons.Default.Star,
                            label = "Évaluer",
                            color = AppColors.primary,
                            modifier = Modifier.weight(1f),
                            isPrimary = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_scale"
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        },
        modifier = modifier.scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) color else color.copy(alpha = 0.1f),
            contentColor = if (isPrimary) Color.White else color
        ),
        shape = RoundedCornerShape(AppRadius.md),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPrimary) 4.dp else 0.dp
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AnimatedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    color: Color
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "icon_button_scale"
    )

    Surface(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
                isPressed = false
            },
        shape = CircleShape,
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
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
    var collectorRating by remember { mutableStateOf(3f) }
    var cleanlinessRating by remember { mutableStateOf(3f) }
    var locationRating by remember { mutableStateOf(3f) }
    var conformityRating by remember { mutableStateOf(3f) }
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            shape = RoundedCornerShape(AppRadius.xl),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.lg)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Évaluer la visite",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = visiteTitle,
                            fontSize = 14.sp,
                            color = AppColors.textSecondary
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = AppColors.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = AppColors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Divider(color = AppColors.divider)
                Spacer(modifier = Modifier.height(AppSpacing.lg))

                // Ratings
                RatingRow("Colocateur", collectorRating) { collectorRating = it }
                RatingRow("Propreté", cleanlinessRating) { cleanlinessRating = it }
                RatingRow("Localisation", locationRating) { locationRating = it }
                RatingRow("Conformité", conformityRating) { conformityRating = it }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Comment
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(AppRadius.md)
                )

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppRadius.md)
                    ) {
                        Text("Annuler")
                    }
                    Button(
                        onClick = {
                            onSubmit(
                                collectorRating.toInt(),
                                cleanlinessRating.toInt(),
                                locationRating.toInt(),
                                conformityRating.toInt(),
                                comment.takeIf { it.isNotBlank() }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.primary
                        ),
                        shape = RoundedCornerShape(AppRadius.md)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Soumettre", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModificationDialog(
    visite: VisiteResponse,
    onDismiss: () -> Unit,
    onSubmit: (Long, Int, Int, String?, String?) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // Initialiser avec la date existante
    val existingMillis = parseIsoToMillis(visite.dateVisite)
    if (existingMillis > 0) {
        calendar.timeInMillis = existingMillis
    }

    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var notes by remember { mutableStateOf(visite.notes ?: "") }
    var contactPhone by remember { mutableStateOf(visite.contactPhone ?: "") }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            selectedDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    // Limiter la date min à aujourd'hui
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedHour = hour
            selectedMinute = minute
        },
        selectedHour,
        selectedMinute,
        true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            shape = RoundedCornerShape(AppRadius.xl),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Text(
                    text = "Modifier la visite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                
                Text(
                    text = getLogementTitle(visite),
                    fontSize = 14.sp,
                    color = AppColors.textSecondary
                )
                
                Divider(color = AppColors.divider)
                
                // Sélection Date
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    val dateParams = Calendar.getInstance().apply { timeInMillis = selectedDate }
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
                    Text(dateFormat.format(dateParams.time))
                }
                
                // Sélection Heure
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppRadius.md)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(String.format(Locale.FRENCH, "%02d:%02d", selectedHour, selectedMinute))
                }
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(AppRadius.md)
                )

                // Contact Phone
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Téléphone de contact") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(AppRadius.md),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                )
                
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(AppRadius.md)
                    ) {
                        Text("Annuler")
                    }
                    Button(
                        onClick = { onSubmit(selectedDate, selectedHour, selectedMinute, notes, contactPhone) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                        shape = RoundedCornerShape(AppRadius.md)
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}

// Data classes
private data class FilterItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

data class VisitStatusStyle(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

// Mock data
internal val mockLogementsMap = mapOf(
    "mock-1" to "Appartement 3 pièces - Centre Ville",
    "mock-2" to "Studio meublé - Lyon",
    "mock-3" to "Chambre dans T4 - Marseille 8e",
    "appart-3pieces-centre-ville" to "Appartement 3 pièces - Centre Ville",
    "studio-meuble-lyon-1" to "Studio meublé - Lyon",
    "studio-meuble-lyon" to "Studio meublé - Lyon",
    "studio-meuble-lyon-2" to "Studio meublé - Lyon",
    "chambre-t4-marseille-8e" to "Chambre dans T4 - Marseille 8e"
)

private fun getLogementTitle(visite: VisiteResponse): String {
    return visite.logementTitle 
        ?: visite.logementId?.let { mockLogementsMap[it] }
        ?: visite.logementId
        ?: "Logement inconnu"
}

private fun mapStatus(status: String?): VisitStatusStyle {
    return when (status?.lowercase()) {
        "pending", "en attente", null -> VisitStatusStyle("En attente", AppColors.warning, Icons.Default.Schedule)
        "confirmed", "acceptée" -> VisitStatusStyle("Acceptée", AppColors.success, Icons.Default.CheckCircle)
        "refused", "refusée" -> VisitStatusStyle("Refusée", AppColors.danger, Icons.Default.Cancel)
        "completed", "terminée" -> VisitStatusStyle("Terminée", AppColors.info, Icons.Default.TaskAlt)
        else -> VisitStatusStyle(status ?: "Inconnu", AppColors.textSecondary, Icons.Default.EventNote)
    }
}

private fun parseIsoToMillis(dateString: String?): Long {
    if (dateString == null) return 0L
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        format.parse(dateString)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

private fun formatVisitDate(dateString: String?): String {
    if (dateString == null) return "Date inconnue"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}

private fun formatVisitTime(dateString: String?): String {
    if (dateString == null) return "Heure inconnue"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        
        val calendar = Calendar.getInstance()
        calendar.time = date ?: return dateString
        
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        String.format(Locale.FRENCH, "%02d:%02d", hour, minute)
    } catch (e: Exception) {
        dateString
    }
}

private fun extractHourMinute(dateString: String?): Pair<Int, Int> {
    if (dateString == null) return Pair(0, 0)
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        
        val calendar = Calendar.getInstance()
        calendar.time = date ?: return Pair(0, 0)
        
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        Pair(hour, minute)
    } catch (e: Exception) {
        Pair(0, 0)
    }
}