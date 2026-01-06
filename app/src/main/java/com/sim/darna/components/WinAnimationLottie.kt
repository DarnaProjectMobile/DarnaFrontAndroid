package com.sim.darna.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Composant pour afficher une animation Lottie de victoire
 * 
 * @param assetFileName Nom du fichier JSON Lottie dans assets/ (ex: "win_animation.json")
 * @param onAnimationEnd Callback appelé quand l'animation se termine
 * @param modifier Modifier pour personnaliser l'apparence
 */
@Composable
fun WinAnimationLottie(
    assetFileName: String = "win_animation.json",
    onAnimationEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var hasCalledCallback by remember { mutableStateOf(false) }
    
    val compositionResult = rememberLottieComposition(
        LottieCompositionSpec.Asset(assetFileName)
    )
    
    // Calculer la durée de l'animation en millisecondes
    val animationDuration = remember(compositionResult.value) {
        compositionResult.value?.let { composition ->
            // Durée = nombre de frames / frame rate * 1000 (pour convertir en ms)
            val frameRate = composition.frameRate
            val frameCount = composition.endFrame - composition.startFrame
            ((frameCount / frameRate) * 1000).toLong()
        } ?: 3000L // Durée par défaut de 3 secondes
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = compositionResult.value,
            modifier = Modifier.size(300.dp),
            iterations = 1, // Jouer une seule fois
            speed = 1f
        )
        
        // Appeler le callback quand l'animation se termine
        LaunchedEffect(compositionResult.value, hasCalledCallback) {
            if (compositionResult.value != null && !hasCalledCallback) {
                kotlinx.coroutines.delay(animationDuration)
                if (!hasCalledCallback) {
                    hasCalledCallback = true
                    onAnimationEnd()
                }
            }
        }
    }
}

/**
 * Dialog avec animation de victoire
 */
@Composable
fun WinAnimationDialog(
    assetFileName: String = "win_animation.json",
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            WinAnimationLottie(
                assetFileName = assetFileName,
                onAnimationEnd = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

