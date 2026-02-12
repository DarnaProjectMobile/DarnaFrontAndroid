package com.sim.darna.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StripePaymentBottomSheet(
    clientSecret: String?,
    publishableKey: String,
    onPaymentResult: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isInitialized by remember { mutableStateOf(false) }
    var hasPresented by remember { mutableStateOf(false) }
    var paymentSheet by remember { mutableStateOf<PaymentSheet?>(null) }
    
    // Initialiser Stripe
    LaunchedEffect(publishableKey) {
        if (!isInitialized) {
            PaymentConfiguration.init(context, publishableKey)
            isInitialized = true
        }
    }
    
    // Créer le PaymentSheet quand clientSecret est disponible
    LaunchedEffect(clientSecret, isInitialized, activity) {
        if (clientSecret != null && isInitialized && activity != null && paymentSheet == null) {
            paymentSheet = PaymentSheet(
                activity = activity,
                callback = { result ->
                    when (result) {
                        is PaymentSheetResult.Completed -> {
                            onPaymentResult(true)
                            onDismiss()
                        }
                        is PaymentSheetResult.Canceled -> {
                            onPaymentResult(false)
                            onDismiss()
                        }
                        is PaymentSheetResult.Failed -> {
                            onPaymentResult(false)
                            onDismiss()
                        }
                    }
                }
            )
        }
    }
    
    // Présenter le PaymentSheet
    LaunchedEffect(paymentSheet, clientSecret, hasPresented) {
        if (paymentSheet != null && clientSecret != null && !hasPresented) {
            hasPresented = true
            paymentSheet?.presentWithPaymentIntent(
                paymentIntentClientSecret = clientSecret,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "Darna"
                )
            )
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Paiement de l'annonce",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Montant: 10€",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (clientSecret == null) {
                CircularProgressIndicator()
                Text(
                    text = "Préparation du paiement...",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Le formulaire de paiement va s'ouvrir...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
