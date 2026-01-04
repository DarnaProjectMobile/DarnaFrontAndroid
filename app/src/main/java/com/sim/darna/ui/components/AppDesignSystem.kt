package com.sim.darna.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== DESIGN TOKENS ====================

object AppColors {
    val primary = Color(0xFF0066FF)
    val primaryVariant = Color(0xFF0052CC)
    val secondary = Color(0xFF6C757D)
    val success = Color(0xFF10B981)
    val warning = Color(0xFFF59E0B)
    val danger = Color(0xFFEF4444)
    val info = Color(0xFF3B82F6)
    
    val background = Color(0xFFF8FAFC)
    val surface = Color(0xFFFFFFFF)
    val surfaceVariant = Color(0xFFF1F5F9)
    
    val textPrimary = Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)
    val textTertiary = Color(0xFF94A3B8)
    
    val divider = Color(0xFFE2E8F0)
    val border = Color(0xFFCBD5E1)
    
    // Gradients
    val gradientPrimary = listOf(Color(0xFF0066FF), Color(0xFF0052CC))
    val gradientSuccess = listOf(Color(0xFF10B981), Color(0xFF059669))
    val gradientWarning = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    val gradientDanger = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
}

object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object AppRadius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val round = 50.dp
}

object AppElevation {
    val none = 0.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 12.dp
}

// ==================== ANIMATION SPECS ====================

val defaultAnimationSpec = tween<Float>(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

val fastAnimationSpec = tween<Float>(
    durationMillis = 200,
    easing = FastOutSlowInEasing
)

val slowAnimationSpec = tween<Float>(
    durationMillis = 500,
    easing = FastOutSlowInEasing
)

// Animation spec pour les slides (IntOffset)
val defaultSlideAnimationSpec = tween<androidx.compose.ui.unit.IntOffset>(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

val fastSlideAnimationSpec = tween<androidx.compose.ui.unit.IntOffset>(
    durationMillis = 200,
    easing = FastOutSlowInEasing
)

// ==================== COMPOSANTS RÉUTILISABLES ====================

@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (onClick != null) 1f else 1f,
        animationSpec = defaultAnimationSpec,
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(AppRadius.lg),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.md)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            content = content
        )
    }
}

@Composable
fun StatusPill(
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.round)),
        color = tint.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon?.invoke()
            Text(
                text = label,
                color = tint,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled && !isLoading) 1f else 0.95f,
        animationSpec = fastAnimationSpec,
        label = "button_scale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.primary,
            disabledContainerColor = AppColors.textTertiary
        ),
        shape = RoundedCornerShape(AppRadius.md),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = fastAnimationSpec,
        label = "button_scale"
    )
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.primary
        ),
        shape = RoundedCornerShape(AppRadius.md),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(AppColors.primary, AppColors.primary))
        ),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
    ) {
        icon?.invoke()
        if (icon != null) Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = AppColors.textSecondary,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = AppColors.textPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun FeedbackBanner(
    message: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val backgroundColor = if (isError) AppColors.danger else AppColors.success
    val icon = if (isError) Icons.Default.Error else Icons.Default.CheckCircle
    val gradientColors = if (isError) AppColors.gradientDanger else AppColors.gradientSuccess
    
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = fastSlideAnimationSpec
        ) + fadeIn(fastAnimationSpec) + scaleIn(
            initialScale = 0.95f,
            animationSpec = fastAnimationSpec
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = fastSlideAnimationSpec
        ) + fadeOut(fastAnimationSpec)
    ) {
        Card(
            modifier = modifier
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(AppRadius.md),
                    spotColor = backgroundColor.copy(alpha = 0.2f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(AppRadius.md),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = 0.08f),
                                backgroundColor.copy(alpha = 0.12f),
                                backgroundColor.copy(alpha = 0.08f)
                            )
                        )
                    )
            ) {
                // Bordure gauche colorée avec gradient
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = gradientColors
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
                    // Icône avec fond circulaire
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        backgroundColor.copy(alpha = 0.2f),
                                        backgroundColor.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = backgroundColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    // Message
                    Text(
                        text = message,
                        color = backgroundColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        lineHeight = 20.sp
                    )
                    
                    // Bouton fermer
                    onDismiss?.let {
                        IconButton(
                            onClick = it,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = backgroundColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    icon: ImageVector = Icons.Default.Inbox,
    modifier: Modifier = Modifier,
    titleColor: Color = AppColors.textPrimary,
    descriptionColor: Color = AppColors.textSecondary
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AppColors.textTertiary
            )
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = descriptionColor,
                textAlign = TextAlign.Center
            )
            actionLabel?.let { label ->
                onAction?.let { action ->
                    SecondaryActionButton(
                        text = label,
                        onClick = action,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(AppRadius.md)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(AppColors.divider.copy(alpha = alpha))
    )
}

@Composable
fun TimeSlotRow(
    slots: List<Pair<Int, Int>>,
    selectedHour: Int,
    selectedMinute: Int,
    onSelect: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        slots.forEach { (hour, minute) ->
            val isSelected = selectedHour == hour && selectedMinute == minute
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = fastAnimationSpec,
                label = "slot_scale"
            )
            
            Surface(
                modifier = Modifier
                    .scale(scale)
                    .clip(RoundedCornerShape(AppRadius.md))
                    .clickable { onSelect(hour, minute) },
                color = if (isSelected) {
                    AppColors.primary
                } else {
                    AppColors.surfaceVariant
                },
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) AppColors.primary else AppColors.border
                )
            ) {
                Text(
                    text = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = if (isSelected) Color.White else AppColors.textPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Animation pour les transitions de page
@Composable
fun AnimatedPageTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(defaultAnimationSpec) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = defaultSlideAnimationSpec
        ),
        exit = fadeOut(defaultAnimationSpec) + slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = defaultSlideAnimationSpec
        )
    ) {
        content()
    }
}

// Composant pour les confirmations
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirmer",
    cancelText: String = "Annuler",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 16.sp,
                color = AppColors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) AppColors.danger else AppColors.primary
                ),
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(AppRadius.md)
            ) {
                Text(cancelText, color = AppColors.textSecondary)
            }
        },
        shape = RoundedCornerShape(AppRadius.lg),
        containerColor = AppColors.surface
    )
}
