package com.sim.darna.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.sim.darna.R

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }

    // Animate scale and alpha
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1000)
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    // Launch animation and navigate after 2 seconds
    LaunchedEffect(Unit) {
        startAnim = true
        delay(2000) // total splash duration
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App logo",
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
                .alpha(alpha)
        )
    }
}
