package com.sim.darna.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.util.Base64
import coil.compose.AsyncImage

@Composable
fun QRCodeDisplay(
    qrCodeBase64: String?,
    couponCode: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!qrCodeBase64.isNullOrEmpty()) {
            // Décoder l'image base64
            val bitmap = remember(qrCodeBase64) {
                try {
                    // Retirer le préfixe "data:image/png;base64," si présent
                    val base64Data = if (qrCodeBase64.startsWith("data:image")) {
                        qrCodeBase64.substring(qrCodeBase64.indexOf(",") + 1)
                    } else {
                        qrCodeBase64
                    }
                    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                } catch (e: Exception) {
                    android.util.Log.e("QRCodeDisplay", "Erreur lors du décodage du QR code: ${e.message}", e)
                    null
                }
            }
            
            bitmap?.let {
                Card(
                    modifier = Modifier.size(250.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        if (!couponCode.isNullOrEmpty()) {
            Text(
                text = "Code promo: $couponCode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
            )
        }
    }
}

