package com.sim.darna.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sim.darna.ui.theme.AppTheme

@Composable
fun EmptyStateLottie(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val compositionResult = rememberLottieComposition(
        LottieCompositionSpec.Asset("empty.json")
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = compositionResult.value,
            modifier = Modifier.size(200.dp),
            iterations = Int.MAX_VALUE // Loop infinitely
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.textPrimary
        )
        
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = AppTheme.textSecondary
            )
        }
    }
}

