package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.sim.darna.factory.VisiteVmFactory
import com.sim.darna.ui.components.*
import com.sim.darna.visite.VisiteResponse
import com.sim.darna.visite.VisiteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitRequestsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
    val baseUrl = "http://192.168.1.101:3009/"
    
    val viewModel: VisiteViewModel = viewModel(factory = VisiteVmFactory(baseUrl, context))
    val uiState = viewModel.state.collectAsState().value
    
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoadingList)
    
    LaunchedEffect(Unit) {
        viewModel.loadLogementsVisites()
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
            viewModel.clearFeedback()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            "Demandes de visite",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                Spacer(modifier = Modifier.height(AppSpacing.md))
                
                // Progress Indicator
                AnimatedVisibility(
                    visible = uiState.isSubmitting,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppRadius.round)),
                        color = Color(0xFFFF9800),
                        trackColor = AppColors.divider
                    )
                }
                
                Spacer(modifier = Modifier.height(AppSpacing.md))
                
                // Tabs
                var selectedTabIndex by remember { mutableStateOf(0) }
                val tabs = listOf("Tous", "En attente", "Acceptées", "Refusées")
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color.White,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title, 
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                ) 
                            },
                            selectedContentColor = Color.White,
                            unselectedContentColor = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(AppSpacing.md))
                
                // Filter visits based on selected tab
                val filteredVisits = remember(uiState.visites, selectedTabIndex) {
                    uiState.visites.filter { visite ->
                        val status = visite.status?.lowercase() ?: "pending"
                        when (selectedTabIndex) {
                            0 -> true // Tous
                            1 -> status == "pending" || status == "en attente" || visite.status == null
                            2 -> status == "accepted" || status == "acceptée" || status == "validée" || status == "confirmed"
                            3 -> status == "refused" || status == "refusée" || status == "rejetée" || status == "rejected"
                            else -> false
                        }
                    }
                }
                
                when {
                    uiState.isLoadingList && filteredVisits.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFF9800))
                        }
                    }
                    filteredVisits.isEmpty() -> {
                        val filteredVisitsIsEmpty = filteredVisits.isEmpty()
                        val emptyMessage = when (selectedTabIndex) {
                            1 -> "Aucune demande en attente"
                            2 -> "Aucune visite acceptée"
                            3 -> "Aucune visite refusée"
                            else -> "Aucune demande de visite"
                        }
                        val emptyDescription = when (selectedTabIndex) {
                            1 -> "Vous n'avez pas de demandes de visite à traiter pour le moment."
                            2 -> "Vous n'avez pas encore accepté de visites."
                            3 -> "Vous n'avez pas de visites refusées."
                            else -> "Vous n'avez aucune demande de visite pour le moment."
                        }
                        
                        EmptyStateCard(
                            title = emptyMessage,
                            description = emptyDescription,
                            actionLabel = "Actualiser",
                            onAction = { viewModel.loadLogementsVisites(force = true) },
                            icon = if (selectedTabIndex == 2) Icons.Default.EventAvailable else Icons.Default.EventBusy
                        )
                    }
                    else -> {
                        SwipeRefresh(
                            state = swipeRefreshState,
                            onRefresh = { viewModel.loadLogementsVisites(force = true) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                                contentPadding = PaddingValues(bottom = AppSpacing.xl)
                            ) {
                                itemsIndexed(
                                    items = filteredVisits,
                                    key = { _, visite -> visite.id ?: visite.dateVisite ?: "" }
                                ) { index, visite ->
                                    VisitRequestCard(
                                        visite = visite,
                                        index = index,
                                        showActions = (visite.status?.lowercase() ?: "pending").let { status ->
                                            status == "pending" || status == "en attente" || visite.status == null
                                        },
                                        onAccept = { id -> viewModel.acceptVisite(id) },
                                        onReject = { id -> viewModel.rejectVisite(id) },
                                        onChatClick = { id, title ->
                                            navController.navigate(
                                                "chat/$id/${java.net.URLEncoder.encode(title, "UTF-8")}"
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitRequestCard(
    visite: VisiteResponse,
    index: Int,
    showActions: Boolean,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onChatClick: (String, String) -> Unit
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
                scaleIn(initialScale = 0.8f, animationSpec = tween(500))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(AppRadius.lg),
                    spotColor = Color(0xFFFF9800).copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(AppRadius.lg),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF9800).copy(alpha = 0.4f),
                        Color(0xFFF57C00).copy(alpha = 0.2f)
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
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF9800).copy(alpha = 0.15f),
                                    Color(0xFFF57C00).copy(alpha = 0.08f)
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
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Color(0xFFFF9800).copy(alpha = 0.2f),
                                border = BorderStroke(2.dp, Color(0xFFFF9800).copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = visite.clientUsername ?: "Client",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.textPrimary
                                )
                                Text(
                                    text = visite.logementTitle ?: "Logement",
                                    fontSize = 14.sp,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                        
                        val statusColor = when {
                            visite.status?.equals("accepted", ignoreCase = true) == true || visite.status?.equals("confirmed", ignoreCase = true) == true || visite.status?.equals("acceptée", ignoreCase = true) == true -> AppColors.success
                            visite.status?.equals("refused", ignoreCase = true) == true || visite.status?.equals("refusée", ignoreCase = true) == true -> AppColors.danger
                            else -> Color(0xFFFF9800)
                        }

                        Surface(
                            shape = RoundedCornerShape(AppRadius.round),
                            color = statusColor.copy(alpha = 0.25f),
                            border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = when {
                                    visite.status?.equals("accepted", ignoreCase = true) == true || visite.status?.equals("confirmed", ignoreCase = true) == true || visite.status?.equals("acceptée", ignoreCase = true) == true -> "Acceptée"
                                    visite.status?.equals("refused", ignoreCase = true) == true || visite.status?.equals("refusée", ignoreCase = true) == true -> "Refusée"
                                    else -> "En attente"
                                },
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }
                }
                
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    // Date/Time
                    visite.dateVisite?.let { dateStr ->
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = AppColors.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = AppColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = formatVisitDate(dateStr),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
                                )
                            }
                        }
                    }
                    
                    // Notes
                    visite.notes?.let { notes ->
                        if (notes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(AppRadius.md),
                                color = AppColors.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppSpacing.sm),
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                ) {
                                    Icon(
                                        Icons.Default.Notes,
                                        contentDescription = null,
                                        tint = AppColors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = notes,
                                        fontSize = 14.sp,
                                        color = AppColors.textSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Contact Phone
                    visite.contactPhone?.let { phone ->
                        if (phone.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(AppRadius.md),
                                color = AppColors.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppSpacing.sm),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = AppColors.success,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = phone,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    
                    // Action Buttons
                    if (showActions) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
                        ) {
                            // Reject Button
                            OutlinedButton(
                                onClick = { visite.id?.let { onReject(it) } },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(AppRadius.md),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AppColors.danger
                                ),
                                border = BorderStroke(2.dp, AppColors.danger)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Refuser", fontWeight = FontWeight.Bold)
                            }
                            
                            // Accept Button
                            Button(
                                onClick = { visite.id?.let { onAccept(it) } },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(AppRadius.md),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.success
                                )
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Accepter", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Show Chat button if Accepted
                    if (visite.status?.equals("accepted", ignoreCase = true) == true || 
                        visite.status?.equals("confirmed", ignoreCase = true) == true || 
                        visite.status?.equals("acceptée", ignoreCase = true) == true ||
                        visite.status?.equals("validée", ignoreCase = true) == true) {
                            
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        
                        Button(
                            onClick = { 
                                visite.id?.let { id ->
                                    val title = visite.logementTitle ?: "Visite"
                                    onChatClick(id, title)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppRadius.md),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Discuter avec le visiteur", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatVisitDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        
        val outputFormat = SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
