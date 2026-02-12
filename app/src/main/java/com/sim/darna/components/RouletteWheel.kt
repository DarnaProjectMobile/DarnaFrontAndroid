package com.sim.darna.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RouletteWheel(
    items: List<String>,
    onSpinComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isSpinning by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "roulette")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val spinRotation = remember { Animatable(0f) }
    
    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            // Rotation aléatoire entre 5 et 10 tours complets + angle aléatoire
            val randomSpins = (5..10).random()
            val randomAngle = (0..360).random().toFloat()
            val targetRotation = (randomSpins * 360f) + randomAngle
            
            spinRotation.animateTo(
                targetRotation,
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
                )
            )
            
            // Calculer l'index gagnant
            val normalizedAngle = (targetRotation % 360f)
            val anglePerItem = 360f / items.size
            selectedIndex = ((items.size - 1) - (normalizedAngle / anglePerItem).toInt()) % items.size
            if (selectedIndex < 0) selectedIndex += items.size
            
            isSpinning = false
            onSpinComplete(items[selectedIndex])
        }
    }
    
    val colors = listOf(
        Color(0xFFE3F2FD),
        Color(0xFFBBDEFB),
        Color(0xFF90CAF9),
        Color(0xFF64B5F6),
        Color(0xFF42A5F5),
        Color(0xFF2196F3),
        Color(0xFF1E88E5),
        Color(0xFF1976D2)
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(if (isSpinning) spinRotation.value else 0f)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 20.dp.toPx()
                val anglePerItem = 360f / items.size
                
                items.forEachIndexed { index, item ->
                    val startAngle = index * anglePerItem - 90f
                    val sweepAngle = anglePerItem
                    
                    // Dessiner le secteur
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    // Dessiner le texte
                    val textAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                    val textRadius = radius * 0.7f
                    val textX = center.x + (cos(textAngle) * textRadius).toFloat()
                    val textY = center.y + (sin(textAngle) * textRadius).toFloat()
                    
                    // Note: Pour un vrai texte, utilisez drawContext.canvas.nativeCanvas
                    // Ici on dessine juste un cercle pour représenter le texte
                }
                
                // Bordure
                drawCircle(
                    color = Color.Black,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            
            // Flèche indicateur
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val arrowLength = 30.dp.toPx()
                val arrowWidth = 20.dp.toPx()
                
                drawLine(
                    color = Color.Red,
                    start = Offset(center.x, center.y - size.minDimension / 2 + 20.dp.toPx()),
                    end = Offset(center.x, center.y - size.minDimension / 2 + 20.dp.toPx() + arrowLength),
                    strokeWidth = 8.dp.toPx()
                )
                
                // Triangle de la flèche
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, center.y - size.minDimension / 2 + 20.dp.toPx())
                    lineTo(center.x - arrowWidth / 2, center.y - size.minDimension / 2 + 20.dp.toPx() + arrowLength)
                    lineTo(center.x + arrowWidth / 2, center.y - size.minDimension / 2 + 20.dp.toPx() + arrowLength)
                    close()
                }
                drawPath(path, Color.Red)
            }
        }
        
        // Liste des items
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Gains disponibles:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = colors[index % colors.size],
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item)
                }
            }
        }
        
        Button(
            onClick = { if (enabled && !isSpinning) isSpinning = true },
            enabled = enabled && !isSpinning,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSpinning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isSpinning) "En cours..." else "Jouer")
        }
    }
}

