package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.viewmodel.ReviewViewModel
import kotlinx.coroutines.delay

// Modern Color Palette
private val PrimaryColor = Color(0xFFFF4B6E)
private val SecondaryColor = Color(0xFF4C6FFF)
private val AccentColor = Color(0xFFFFC857)
private val BackgroundColor = Color(0xFFF7F7F7)
private val SurfaceColor = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val TextTertiary = Color(0xFF9E9E9E)
private val PositiveColor = Color(0xFF4CAF50)
private val NegativeColor = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSummaryScreen(
    propertyId: String,
    propertyName: String = "",
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val reviewViewModel: ReviewViewModel = viewModel()
    val summary by reviewViewModel.reviewSummary.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var contentVisible by remember { mutableStateOf(false) }

    // Load summary
    LaunchedEffect(propertyId) {
        reviewViewModel.init(context)
        reviewViewModel.loadReviewSummary(propertyId)
        delay(1500) // Minimum loading time for smooth transition
        isLoading = false
        delay(100)
        contentVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Résumé IA des avis",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (propertyName.isNotEmpty()) {
                            Text(
                                text = propertyName,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor
                )
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    ModernLoadingState()
                }
                summary == null -> {
                    EmptyState()
                }
                else -> {
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(animationSpec = tween(600)) +
                                slideInVertically(
                                    initialOffsetY = { 50 },
                                    animationSpec = tween(600)
                                )
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Summary Card
                            item {
                                SummaryCard(summary!!.summary)
                            }

                            // Sentiment Score
                            item {
                                SentimentCard(summary!!.sentimentScore)
                            }

                            // Pros
                            if (summary!!.pros.isNotEmpty()) {
                                item {
                                    ProsCard(summary!!.pros)
                                }
                            }

                            // Cons
                            if (summary!!.cons.isNotEmpty()) {
                                item {
                                    ConsCard(summary!!.cons)
                                }
                            }

                            // Common Themes
                            if (summary!!.commonThemes.isNotEmpty()) {
                                item {
                                    ThemesCard(summary!!.commonThemes)
                                }
                            }

                            // Improvements
                            if (summary!!.improvements.isNotEmpty()) {
                                item {
                                    ImprovementsCard(summary!!.improvements)
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernLoadingState() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Animated loading circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
            ) {
                // Outer rotating circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    PrimaryColor.copy(alpha = 0.2f),
                                    SecondaryColor.copy(alpha = 0.8f),
                                    PrimaryColor.copy(alpha = 0.2f)
                                )
                            )
                        )
                )

                // Inner circle with icon
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center),
                    shape = CircleShape,
                    color = SurfaceColor,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // TODO: Replace with your loading GIF/animation
                        // Image(
                        //     painter = painterResource(id = R.drawable.loading_animation),
                        //     contentDescription = "Loading",
                        //     modifier = Modifier.size(60.dp)
                        // )

                        // Placeholder icon - replace with your GIF
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = "Loading",
                            tint = SecondaryColor,
                            modifier = Modifier
                                .size(48.dp)
                                .rotate(-rotation * 0.5f)
                        )
                    }
                }
            }

            // Loading text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Analyse en cours...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = "L'IA génère un résumé des avis",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Animated dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(SecondaryColor)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = TextTertiary.copy(alpha = 0.1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Aucun résumé disponible",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Impossible de générer un résumé IA pour cette propriété",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun SummaryCard(summary: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = SecondaryColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = SecondaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Résumé général",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = summary,
                fontSize = 15.sp,
                color = TextSecondary,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun SentimentCard(score: Double) {
    val percentage = ((score + 1) / 2 * 100).toInt()
    val color = when {
        score > 0.3 -> PositiveColor
        score < -0.3 -> NegativeColor
        else -> AccentColor
    }
    val sentiment = when {
        score > 0.3 -> "Positif"
        score < -0.3 -> "Négatif"
        else -> "Neutre"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = color.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.SentimentSatisfied,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Analyse de sentiment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sentiment,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "$percentage% positif",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = String.format("%.2f", score),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProsCard(pros: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = PositiveColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.ThumbUp,
                            contentDescription = null,
                            tint = PositiveColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Points positifs",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            pros.forEach { pro ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = PositiveColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = pro,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ConsCard(cons: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = NegativeColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.ThumbDown,
                            contentDescription = null,
                            tint = NegativeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Points négatifs",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            cons.forEach { con ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.Cancel,
                        contentDescription = null,
                        tint = NegativeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = con,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemesCard(themes: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = AccentColor.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.LocalOffer,
                            contentDescription = null,
                            tint = AccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Thèmes récurrents",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            @Composable
            fun FlowRow(
                modifier: Modifier = Modifier,
                content: @Composable () -> Unit
            ) {
                Column(modifier = modifier) {
                    content()
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                themes.forEach { theme ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = theme,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImprovementsCard(improvements: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = SecondaryColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Build,
                            contentDescription = null,
                            tint = SecondaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Améliorations suggérées",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            improvements.forEach { improvement ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = SecondaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = improvement,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}