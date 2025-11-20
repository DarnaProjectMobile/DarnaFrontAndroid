package com.sim.darna.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.sim.darna.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun RouletteGame(
    gains: List<String>,
    onGainSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSpinning by remember { mutableStateOf(false) }
    var selectedGain by remember { mutableStateOf<String?>(null) }
    var currentRotation by remember { mutableStateOf(0f) }
    
    val animatedRotation by animateFloatAsState(
        targetValue = currentRotation,
        animationSpec = tween(
            durationMillis = 3000,
            easing = FastOutSlowInEasing
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎰 Roulette de la Chance",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Roulette
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(animatedRotation)
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.minDimension / 2 - 20.dp.toPx()
                    val anglePerSegment = 360f / gains.size

                    // Dessiner les segments
                    gains.forEachIndexed { index, gain ->
                        val startAngle = (index * anglePerSegment - 90) * (kotlin.math.PI / 180f)
                        val sweepAngle = anglePerSegment * (kotlin.math.PI / 180f)

                        val color = when (index % 3) {
                            0 -> BluePrimary
                            1 -> BlueSecondary
                            else -> BlueLight
                        }

                        drawArc(
                            color = color,
                            startAngle = index * anglePerSegment - 90f,
                            sweepAngle = anglePerSegment,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )

                        // Note: Le texte des gains pourrait être ajouté ici si nécessaire
                    }

                    // Flèche indicateur (fixe en haut)
                    drawLine(
                        color = Color.Red,
                        start = center,
                        end = Offset(center.x, center.y - radius - 10.dp.toPx()),
                        strokeWidth = 8.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            // Résultat
            if (selectedGain != null) {
                Text(
                    text = "🎉 Vous avez gagné: $selectedGain",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JeuColor,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Bouton pour jouer
            Button(
                onClick = {
                    if (!isSpinning && gains.isNotEmpty()) {
                        isSpinning = true
                        selectedGain = null

                        // Calculer la rotation cible (plusieurs tours + angle aléatoire)
                        val randomTurns = (5..10).random() * 360f
                        val randomAngle = (0..360).random().toFloat()
                        val targetRotation = currentRotation + randomTurns + randomAngle

                        // Lancer l'animation
                        currentRotation = targetRotation
                    }
                },
                enabled = !isSpinning && gains.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JeuColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = if (isSpinning) "Rotation..." else "Jouer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Gérer la fin de l'animation
    LaunchedEffect(isSpinning, currentRotation) {
        if (isSpinning) {
            delay(3000)
            isSpinning = false
            
            // Calculer l'angle final (en tenant compte de la rotation)
            val finalAngle = (currentRotation % 360f + 90f) % 360f
            val normalizedAngle = if (finalAngle < 0) finalAngle + 360f else finalAngle
            if (gains.isNotEmpty()) {
                val segmentIndex = ((normalizedAngle / (360f / gains.size)).toInt()) % gains.size
                if (segmentIndex in gains.indices) {
                    selectedGain = gains[segmentIndex]
                    onGainSelected(selectedGain!!)
                }
            }
        }
    }
}

