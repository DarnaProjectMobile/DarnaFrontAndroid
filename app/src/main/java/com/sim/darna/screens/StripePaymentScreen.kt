package com.sim.darna.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sim.darna.viewmodel.StripeViewModel

@Composable
fun StripePaymentScreen(
    amount: Double,
    onPaymentSuccess: () -> Unit,
    onPaymentCancel: () -> Unit,
    viewModel: StripeViewModel = hiltViewModel()
) {
    //val
    val paymentState by viewModel.paymentState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.createPaymentIntent(context, amount) { success, paymentUrl ->
            if (success && !paymentUrl.isNullOrEmpty()) {
                // Ouvrir le navigateur pour le paiement
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                context.startActivity(intent)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            paymentState.isLoading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Préparation du paiement...")
            }
            
            paymentState.error != null -> {
                Text(
                    text = "Erreur: ${paymentState.error}",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onPaymentCancel) {
                    Text("Annuler")
                }
            }
            
            else -> {
                Text(
                    text = "Redirection vers Stripe...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Montant: ${amount}€",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

