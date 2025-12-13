package com.sim.darna.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint
import android.graphics.Typeface

@Composable
fun RouletteWheel(
    items: List<String>,
    onSpinComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isSpinning by remember { mutableStateOf(false) }
    var currentRotation by remember { mutableStateOf(0f) }
    var selectedIndex by remember { mutableStateOf(0) }
    
    val spinRotation = remember { Animatable(0f) }
    
    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            // Rotation aléatoire entre 5 et 10 tours complets + angle aléatoire
            val randomSpins = (5..10).random()
            val randomAngle = (0..360).random().toFloat()
            val targetRotation = currentRotation + (randomSpins * 360f) + randomAngle
            
            spinRotation.animateTo(
                targetRotation,
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
                )
            )
            
            // Calculer l'index gagnant basé sur l'angle final
            // Le pointeur est fixe en haut (à 0°)
            // La roue tourne dans le sens horaire, donc on doit inverser
            val normalizedAngle = (targetRotation % 360f)
            val anglePerItem = 360f / items.size
            
            // Le pointeur pointe vers le haut (0°)
            // Les segments commencent à -90° (en haut), donc on ajuste
            // On inverse car la roue tourne dans le sens horaire
            val pointerAngle = 0f // Pointeur en haut
            val relativeAngle = (pointerAngle - normalizedAngle + 90f + 360f) % 360f
            selectedIndex = ((relativeAngle / anglePerItem).toInt()) % items.size
            if (selectedIndex < 0) selectedIndex += items.size
            
            currentRotation = targetRotation
            isSpinning = false
            onSpinComplete(items[selectedIndex])
        }
    }
    
    // Couleurs alternées pour la roue (bleu et bleu clair)
    val colors = listOf(
        Color(0xFF2196F3), // Bleu vif
        Color(0xFFE3F2FD)  // Bleu très clair
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier.size(320.dp),
            contentAlignment = Alignment.Center
        ) {
            // Roue avec ombre légère
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(spinRotation.value)
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2 - 10.dp.toPx()
                    val anglePerItem = 360f / items.size
                    
                    items.forEachIndexed { index, item ->
                        val startAngle = index * anglePerItem - 90f
                        val sweepAngle = anglePerItem
                        
                        // Dessiner le secteur avec couleur alternée
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                        
                        // Bordure entre les segments
                        val borderAngle = Math.toRadians(startAngle.toDouble())
                        val borderStartX = center.x + (cos(borderAngle) * radius).toFloat()
                        val borderStartY = center.y + (sin(borderAngle) * radius).toFloat()
                        drawLine(
                            color = Color.White,
                            start = center,
                            end = Offset(borderStartX, borderStartY),
                            strokeWidth = 2.dp.toPx()
                        )
                        
                        // Dessiner le texte
                        val textAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                        val textRadius = radius * 0.7f
                        val textX = center.x + (cos(textAngle) * textRadius).toFloat()
                        val textY = center.y + (sin(textAngle) * textRadius).toFloat()
                        
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = if (index % 2 == 0) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                                textSize = 32f
                                textAlign = Paint.Align.CENTER
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                isAntiAlias = true
                            }
                            
                            // Rotation du texte pour qu'il soit lisible
                            canvas.nativeCanvas.save()
                            canvas.nativeCanvas.translate(textX, textY)
                            canvas.nativeCanvas.rotate((startAngle + sweepAngle / 2 + 90) % 360)
                            
                            // Tronquer le texte si trop long
                            val text = if (item.length > 12) item.take(10) + "..." else item
                            canvas.nativeCanvas.drawText(text, 0f, 0f, paint)
                            canvas.nativeCanvas.restore()
                        }
                    }
                    
                    // Bordure extérieure
                    drawCircle(
                        color = Color(0xFF2196F3),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 6.dp.toPx())
                    )
                    
                    // Centre de la roue (hub)
                    drawCircle(
                        color = Color(0xFF2196F3),
                        radius = 20.dp.toPx(),
                        center = center
                    )
                }
            }
            
            // Pointeur triangulaire en haut (fixe) - plus visible
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val pointerY = 10.dp.toPx()
                val pointerSize = 35.dp.toPx()
                
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, pointerY)
                    lineTo(center.x - pointerSize / 2, pointerY + pointerSize)
                    lineTo(center.x + pointerSize / 2, pointerY + pointerSize)
                    close()
                }
                drawPath(path, Color(0xFF2196F3))
                
                // Bordure du pointeur pour plus de visibilité
                drawPath(
                    path = path,
                    color = Color(0xFF1976D2),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        
        // Bouton Jouer
        Button(
            onClick = { if (enabled && !isSpinning) isSpinning = true },
            enabled = enabled && !isSpinning,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSpinning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "En cours...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    "Jouer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

