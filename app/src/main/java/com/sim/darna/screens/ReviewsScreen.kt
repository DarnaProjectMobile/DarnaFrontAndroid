package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.sim.darna.model.Review
import com.sim.darna.viewmodel.ReviewViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

// Premium Color Palette
private val PrimaryGradientStart = Color(0xFF667EEA)
private val PrimaryGradientEnd = Color(0xFF764BA2)
private val AccentCyan = Color(0xFF00D9FF)
private val AccentPink = Color(0xFFFF6B9D)
private val AccentOrange = Color(0xFFFFAB5E)
private val SurfaceLight = Color(0xFFF8FAFF)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1D29)
private val TextSecondary = Color(0xFF6B7280)
private val GlassWhite = Color(0x40FFFFFF)
private val GlassBorder = Color(0x30FFFFFF)

// Custom easing functions
private val EaseOutBack: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
private val EaseInOutSine: Easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

@Composable
fun ReviewsScreen(onNavigateBack: () -> Unit = {}) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    val vm: ReviewViewModel = viewModel()
    val reviews by vm.reviews.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }

    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.init(context)
        vm.loadReviews()
    }

    // Animated background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceLight,
                        Color(0xFFEEF2FF),
                        Color(0xFFF5F3FF)
                    )
                )
            )
    ) {
        // Floating animated orbs in background
        AnimatedBackgroundOrbs()

        Column(modifier = Modifier.fillMaxSize()) {
            // Premium Glassmorphism Header
            PremiumHeader(
                reviewCount = reviews.size,
                onNavigateBack = onNavigateBack
            )

            // Reviews List with staggered animations
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(reviews) { index, review ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 80L)
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400)) +
                                slideInVertically(
                                    tween(500, easing = EaseOutBack)
                                ) { it / 2 } +
                                scaleIn(tween(400), initialScale = 0.8f)
                    ) {
                        PremiumReviewCard(
                            review = review,
                            onEdit = {
                                selectedReview = review
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedReview = review
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Premium Bottom Input Panel
        PremiumInputPanel(
            rating = rating,
            onRatingChange = { rating = it },
            comment = comment,
            onCommentChange = { comment = it },
            onSubmit = {
                if (comment.isNotBlank()) {
                    vm.addReview(rating, comment)
                    comment = ""
                    rating = 5
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Dialogs
    if (showEditDialog && selectedReview != null) {
        PremiumEditDialog(
            review = selectedReview!!,
            onDismiss = { showEditDialog = false },
            onSave = { edited ->
                vm.updateReview(edited._id, edited.rating, edited.comment)
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog && selectedReview != null) {
        PremiumDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                vm.deleteReview(selectedReview!!._id)
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun AnimatedBackgroundOrbs() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")

    val orb1Y by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "orb1"
    )

    val orb2X by infiniteTransition.animateFloat(
        initialValue = 250f,
        targetValue = 350f,
        animationSpec = infiniteRepeatable(
            tween(5000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "orb2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    PrimaryGradientStart.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                radius = 200f
            ),
            radius = 200f,
            center = Offset(80f, orb1Y)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AccentPink.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                radius = 250f
            ),
            radius = 250f,
            center = Offset(orb2X, 600f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AccentCyan.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                radius = 180f
            ),
            radius = 180f,
            center = Offset(size.width - 50f, 300f)
        )
    }
}

@Composable
fun PremiumHeader(reviewCount: Int, onNavigateBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            tween(2500, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Glassmorphism card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryGradientStart,
                            PrimaryGradientEnd
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(500f, 500f)
                    )
                )
                .drawWithContent {
                    drawContent()
                    // Shimmer overlay
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            start = Offset(shimmer, 0f),
                            end = Offset(shimmer + 200f, 200f)
                        )
                    )
                }
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated back button
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val backScale by animateFloatAsState(
                    if (isPressed) 0.85f else 1f,
                    spring(dampingRatio = 0.4f),
                    label = "backScale"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(backScale)
                        .clip(CircleShape)
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        fontSize = 22.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reviews",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Animated star
                        val starRotation by infiniteTransition.animateFloat(
                            0f, 360f,
                            infiniteRepeatable(tween(8000, easing = LinearEasing)),
                            label = "star"
                        )
                        Text(
                            text = "✦",
                            fontSize = 14.sp,
                            color = AccentOrange,
                            modifier = Modifier.graphicsLayer { rotationZ = starRotation }
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "$reviewCount reviews shared",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Decorative animated icon
                val floatAnim by infiniteTransition.animateFloat(
                    0f, 10f,
                    infiniteRepeatable(tween(1500), RepeatMode.Reverse),
                    label = "float"
                )
                Text(
                    text = "💬",
                    fontSize = 36.sp,
                    modifier = Modifier.offset(y = floatAnim.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedRatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ratingEmojis = listOf("😞", "😐", "🙂", "😊", "🤩")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { star ->
            var triggered by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                if (triggered) 1.4f else if (star <= rating) 1.1f else 0.9f,
                spring(dampingRatio = 0.3f, stiffness = 300f),
                label = "scale$star"
            )
            val rotation by animateFloatAsState(
                if (triggered) 15f else 0f,
                spring(dampingRatio = 0.3f),
                label = "rot$star"
            )

            // Glow effect for selected stars
            val glowAlpha by animateFloatAsState(
                if (star <= rating) 0.6f else 0f,
                tween(300),
                label = "glow$star"
            )

            LaunchedEffect(triggered) {
                if (triggered) {
                    delay(200)
                    triggered = false
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = rotation
                    }
                    .drawBehind {
                        if (star <= rating) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        AccentOrange.copy(alpha = glowAlpha * 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension
                            )
                        }
                    }
                    .clip(CircleShape)
                    .background(
                        brush = if (star <= rating)
                            Brush.linearGradient(
                                listOf(AccentOrange.copy(0.2f), AccentPink.copy(0.2f))
                            )
                        else
                            Brush.linearGradient(
                                listOf(Color(0xFFF3F4F6), Color(0xFFF3F4F6))
                            ),
                        shape = CircleShape
                    )
                    .border(
                        2.dp,
                        if (star <= rating) AccentOrange.copy(0.5f) else Color(0xFFE5E7EB),
                        CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggered = true
                        onRatingChange(star)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (star <= rating) ratingEmojis[star - 1] else "○",
                    fontSize = if (star <= rating) 22.sp else 18.sp,
                    color = if (star <= rating) Color.Unspecified else Color(0xFFD1D5DB)
                )
            }
        }
    }
}

@Composable
fun PremiumReviewCard(
    review: Review,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        spring(dampingRatio = 0.6f),
        label = "cardScale"
    )

    val elevation by animateDpAsState(
        if (expanded) 16.dp else 4.dp,
        tween(300),
        label = "elevation"
    )

    val username = review.user?.username ?: "Unknown"
    val avatarColors = remember(username) {
        listOf(
            listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
            listOf(Color(0xFFFF6B9D), Color(0xFFC44569)),
            listOf(Color(0xFF00D9FF), Color(0xFF0099CC)),
            listOf(Color(0xFFFFAB5E), Color(0xFFFF6B35)),
            listOf(Color(0xFF6DD5ED), Color(0xFF2193B0))
        )[username.hashCode().mod(5)]
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .scale(cardScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Animated gradient avatar
                val infiniteTransition = rememberInfiniteTransition(label = "avatar")
                val gradientAngle by infiniteTransition.animateFloat(
                    0f, 360f,
                    infiniteRepeatable(tween(4000, easing = LinearEasing)),
                    label = "avatarGrad"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = avatarColors,
                                start = Offset(
                                    50f * kotlin.math.cos(Math.toRadians(gradientAngle.toDouble())).toFloat(),
                                    50f * kotlin.math.sin(Math.toRadians(gradientAngle.toDouble())).toFloat()
                                ),
                                end = Offset(
                                    50f * kotlin.math.cos(Math.toRadians((gradientAngle + 180).toDouble())).toFloat(),
                                    50f * kotlin.math.sin(Math.toRadians((gradientAngle + 180).toDouble())).toFloat()
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = username,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(6.dp))

                    // Animated rating display
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(5) { idx ->
                            val starDelay = idx * 50
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(starDelay.toLong())
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = scaleIn(spring(dampingRatio = 0.4f)) + fadeIn()
                            ) {
                                val isFilled = idx < review.rating
                                Text(
                                    text = if (isFilled) "★" else "☆",
                                    fontSize = 16.sp,
                                    color = if (isFilled) AccentOrange else Color(0xFFE5E7EB)
                                )
                            }
                        }
                    }
                }

                // Expand indicator
                val rotateIcon by animateFloatAsState(
                    if (expanded) 180f else 0f,
                    spring(dampingRatio = 0.6f),
                    label = "expandIcon"
                )
                Text(
                    text = "⌄",
                    fontSize = 20.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .graphicsLayer { rotationZ = rotateIcon }
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Animated divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFE5E7EB),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(Modifier.height(14.dp))

            // Comment with nice typography
            Text(
                text = review.comment,
                fontSize = 15.sp,
                color = TextSecondary,
                lineHeight = 24.sp
            )

            // Animated action buttons
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) +
                        expandVertically(tween(300, easing = EaseOutBack)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumActionButton(
                        text = "Edit",
                        icon = "✏️",
                        color = PrimaryGradientStart,
                        onClick = onEdit
                    )

                    Spacer(Modifier.width(12.dp))

                    PremiumActionButton(
                        text = "Delete",
                        icon = "🗑️",
                        color = Color(0xFFEF4444),
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumActionButton(
    text: String,
    icon: String,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.9f else 1f,
        spring(dampingRatio = 0.5f),
        label = "btnScale"
    )

    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun PremiumInputPanel(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelOffset by animateDpAsState(
        if (comment.isNotBlank()) 0.dp else 0.dp,
        spring(dampingRatio = 0.7f),
        label = "panelOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = panelOffset)
    ) {
        // Glass effect background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(CardWhite.copy(alpha = 0.95f))
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color(0x20000000), Color.Transparent)
                    ),
                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // Rating section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "How was it?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                AnimatedRatingBar(
                    rating = rating,
                    onRatingChange = onRatingChange,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))

                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Premium text field
                    OutlinedTextField(
                        value = comment,
                        onValueChange = onCommentChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                        placeholder = {
                            Text(
                                "Share your thoughts...",
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGradientStart,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        maxLines = 4
                    )

                    // Animated send button
                    val hasText = comment.isNotBlank()
                    val buttonScale by animateFloatAsState(
                        if (hasText) 1f else 0.85f,
                        spring(dampingRatio = 0.5f),
                        label = "sendScale"
                    )
                    val buttonRotation by animateFloatAsState(
                        if (hasText) 0f else -20f,
                        spring(dampingRatio = 0.5f),
                        label = "sendRot"
                    )

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val pressScale by animateFloatAsState(
                        if (isPressed) 0.85f else 1f,
                        spring(dampingRatio = 0.4f),
                        label = "pressScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(buttonScale * pressScale)
                            .graphicsLayer { rotationZ = buttonRotation }
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (hasText)
                                    Brush.linearGradient(
                                        listOf(PrimaryGradientStart, PrimaryGradientEnd)
                                    )
                                else
                                    Brush.linearGradient(
                                        listOf(Color(0xFFE5E7EB), Color(0xFFD1D5DB))
                                    )
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                enabled = hasText
                            ) { onSubmit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↗",
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumEditDialog(
    review: Review,
    onDismiss: () -> Unit,
    onSave: (Review) -> Unit
) {
    var rating by remember { mutableStateOf(review.rating) }
    var comment by remember { mutableStateOf(review.comment) }

    // Dialog entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val dialogScale by animateFloatAsState(
        if (visible) 1f else 0.8f,
        spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "dialogScale"
    )
    val dialogAlpha by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(200),
        label = "dialogAlpha"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .scale(dialogScale)
            .alpha(dialogAlpha)
            .clip(RoundedCornerShape(28.dp)),
        containerColor = CardWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Animated pencil icon
                val infiniteTransition = rememberInfiniteTransition(label = "editIcon")
                val iconRotation by infiniteTransition.animateFloat(
                    initialValue = -5f,
                    targetValue = 5f,
                    animationSpec = infiniteRepeatable(
                        tween(600, easing = EaseInOutSine),
                        RepeatMode.Reverse
                    ),
                    label = "pencilRotate"
                )

                Text(
                    "✏️",
                    fontSize = 24.sp,
                    modifier = Modifier.graphicsLayer { rotationZ = iconRotation }
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Edit Review",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column {
                // Label for rating
                Text(
                    "Update your rating",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(12.dp))

                AnimatedRatingBar(
                    rating = rating,
                    onRatingChange = { rating = it }
                )

                Spacer(Modifier.height(20.dp))

                // Label for comment
                Text(
                    "Update your review",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGradientStart,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = {
                        Text(
                            "Share your updated thoughts...",
                            color = TextSecondary.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        },
        confirmButton = {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                if (isPressed) 0.92f else 1f,
                spring(dampingRatio = 0.5f),
                label = "saveScale"
            )

            Box(
                modifier = Modifier
                    .scale(scale)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PrimaryGradientStart, PrimaryGradientEnd)
                        )
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onSave(review.copy(rating = rating, comment = comment)) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    "Save Changes",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun PremiumDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var showConfirmation by remember { mutableStateOf(false) }

    val iconScale by animateFloatAsState(
        if (showConfirmation) 1.2f else 1f,
        spring(dampingRatio = 0.3f),
        label = "iconScale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        showConfirmation = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.clip(RoundedCornerShape(28.dp)),
        containerColor = CardWhite,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated warning icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🗑️",
                        fontSize = 32.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Delete Review?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary
                )
            }
        },
        text = {
            Text(
                "This action cannot be undone. Your review will be permanently removed.",
                color = TextSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF3F4F6))
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Cancel",
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Delete button with animation
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val deleteScale by animateFloatAsState(
                    if (isPressed) 0.95f else 1f,
                    spring(dampingRatio = 0.5f),
                    label = "deleteScale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(deleteScale)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                            )
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onConfirm() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = null
    )
}