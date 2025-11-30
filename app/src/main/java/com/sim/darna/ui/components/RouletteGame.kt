package com.sim.darna.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sim.darna.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun RouletteGame(
    gains: List<String>,
    modifier: Modifier = Modifier
) {
    var isSpinning by remember { mutableStateOf(false) }
    var rotation by remember { mutableStateOf(0f) }
    var selectedGain by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(
            durationMillis = 3000,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            isSpinning = false
            showResult = true
        },
        label = "rotation"
    )

    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFF95E1D3),
        Color(0xFFF38181),
        Color(0xFFAA96DA),
        Color(0xFFFCBF49),
        Color(0xFF06FFA5)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Roulette
        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            // Flèche indicatrice en haut
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .size(40.dp)
                    .background(Color(0xFFFF6D00), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(90f)
                )
            }

            // Roue
            Canvas(
                modifier = Modifier
                    .size(250.dp)
                    .rotate(animatedRotation)
            ) {
                val sectionAngle = 360f / gains.size
                gains.forEachIndexed { index, _ ->
                    val startAngle = index * sectionAngle
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sectionAngle,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                }

                // Bordure centrale
                drawCircle(
                    color = Color.White,
                    radius = 40f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFFF6D00),
                    radius = 35f,
                    center = center
                )
            }

            // Centre de la roue
            Surface(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.Center),
                shape = CircleShape,
                color = Color(0xFFFF6D00),
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "SPIN",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bouton tourner
        Button(
            onClick = {
                if (!isSpinning) {
                    isSpinning = true
                    showResult = false

                    // Rotation aléatoire (5-10 tours complets + angle aléatoire)
                    val randomTurns = Random.nextInt(5, 11) * 360f
                    val randomAngle = Random.nextFloat() * 360f
                    rotation += randomTurns + randomAngle

                    // Calculer le gain sélectionné
                    val finalAngle = rotation % 360f
                    val sectionAngle = 360f / gains.size
                    val selectedIndex = ((360f - finalAngle) / sectionAngle).toInt() % gains.size
                    selectedGain = gains[selectedIndex]
                }
            },
            enabled = !isSpinning,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6D00),
                disabledContainerColor = Color(0xFFFF6D00).copy(alpha = 0.5f)
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isSpinning) "En cours..." else "Tourner la roue !",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Résultat
        if (showResult && selectedGain != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF00C853).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎉 Félicitations !",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00C853)
                    )
                    Text(
                        text = "Vous avez gagné :",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = selectedGain!!,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Liste des gains disponibles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Gains possibles :",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                gains.forEachIndexed { index, gain ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    colors[index % colors.size],
                                    CircleShape
                                )
                        )
                        Text(
                            text = gain,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
