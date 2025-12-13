package com.sim.darna.components

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheetResult

@Composable
fun StripePaymentBottomSheet(
    clientSecret: String?,
    publishableKey: String,
    onPaymentResult: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    var isInitialized by remember { mutableStateOf(false) }
    var hasPresented by remember { mutableStateOf(false) }
    
    // Initialiser Stripe Configuration
    LaunchedEffect(publishableKey) {
        if (!isInitialized) {
            try {
                PaymentConfiguration.init(context, publishableKey)
                isInitialized = true
                Log.d("StripePaymentSheet", "Stripe initialized")
            } catch (e: Exception) {
                Log.e("StripePaymentSheet", "Error initializing Stripe", e)
            }
        }
    }
    
    // Callback pour les résultats du PaymentSheet
    val paymentResultCallback: (PaymentSheetResult) -> Unit = { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                Log.d("StripePaymentSheet", "Payment completed")
                onPaymentResult(true)
                onDismiss()
            }
            is PaymentSheetResult.Canceled -> {
                Log.d("StripePaymentSheet", "Payment canceled")
                onPaymentResult(false)
                onDismiss()
            }
            is PaymentSheetResult.Failed -> {
                Log.e("StripePaymentSheet", "Payment failed: ${result.error}")
                onPaymentResult(false)
                onDismiss()
            }
        }
    }

    // Utiliser le PaymentSheetManager qui a été initialisé dans MainActivity.onCreate
    // Mettre à jour le callback du PaymentSheetManager
    LaunchedEffect(Unit) {
        if (activity != null) {
            PaymentSheetManager.setCallback(paymentResultCallback)
        }
    }

    // Lancer le PaymentSheet quand le clientSecret est disponible
    LaunchedEffect(clientSecret, isInitialized, hasPresented) {
        if (clientSecret != null && isInitialized && !hasPresented) {
            try {
                hasPresented = true
                Log.d("StripePaymentSheet", "Presenting PaymentSheet with clientSecret: ${clientSecret.take(20)}...")
                PaymentSheetManager.presentPaymentIntent(
                    clientSecret = clientSecret,
                    merchantDisplayName = "Darna"
                )
            } catch (e: IllegalStateException) {
                Log.e("StripePaymentSheet", "Erreur PaymentSheet: ${e.message}", e)
                onPaymentResult(false)
                onDismiss()
            } catch (e: Exception) {
                Log.e("StripePaymentSheet", "Erreur lors de la présentation: ${e.message}", e)
                onPaymentResult(false)
                onDismiss()
            }
        }
    }
}
