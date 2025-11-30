package com.sim.darna.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.sim.darna.R
import com.sim.darna.navigation.Routes
import com.sim.darna.ui.theme.*
import com.sim.darna.viewmodel.PaymentUiState
import com.sim.darna.viewmodel.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavHostController,
    viewModel: PaymentViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clientSecret by viewModel.clientSecret.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Montant en centimes (modifiable)
    var amountInEuros by remember { mutableStateOf("10.00") }
    var resourceId by remember { mutableStateOf("") }
    var resourceType by remember { mutableStateOf("publicite") }

    // Initialiser Stripe avec la clé depuis strings.xml
    val publishableKey = remember { context.getString(R.string.stripe_publishable_key) }
    
    // Initialiser PaymentConfiguration une seule fois
    DisposableEffect(publishableKey) {
        PaymentConfiguration.init(context, publishableKey)
        onDispose { }
    }
    
    // Callback pour les résultats du PaymentSheet
    val paymentResultCallback: (PaymentSheetResult) -> Unit = { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                viewModel.onPaymentSuccess()
            }
            is PaymentSheetResult.Canceled -> {
                viewModel.onPaymentError("Paiement annulé")
            }
            is PaymentSheetResult.Failed -> {
                viewModel.onPaymentError(result.error.message ?: "Erreur de paiement")
            }
        }
    }

    // Utiliser le PaymentSheetManager qui a été initialisé dans MainActivity.onCreate
    // Mettre à jour le callback du PaymentSheetManager
    LaunchedEffect(Unit) {
        if (activity != null) {
            com.sim.darna.payment.PaymentSheetManager.setCallback(paymentResultCallback)
        }
    }

    // Lancer le PaymentSheet quand le clientSecret est disponible
    LaunchedEffect(clientSecret) {
        clientSecret?.let { secret ->
            try {
                com.sim.darna.payment.PaymentSheetManager.presentPaymentIntent(
                    clientSecret = secret,
                    merchantDisplayName = "Darna"
                )
            } catch (e: IllegalStateException) {
                android.util.Log.e("PaymentScreen", "Erreur PaymentSheet: ${e.message}", e)
                viewModel.onPaymentError("Impossible d'initialiser le paiement. Retournez en arrière et réessayez.")
            } catch (e: Exception) {
                android.util.Log.e("PaymentScreen", "Erreur lors de la présentation: ${e.message}", e)
                viewModel.onPaymentError("Erreur lors de l'ouverture du paiement: ${e.message}")
            }
        }
    }

    // Observer le succès du paiement
    LaunchedEffect(uiState) {
        if (uiState is PaymentUiState.PaymentCompleted) {
            // Naviguer vers l'écran AddPublicite après un paiement réussi
            navController.navigate(Routes.AddPublicite) {
                // Retirer Payment de la pile de navigation pour éviter de revenir en arrière
                popUpTo(Routes.Payment) { inclusive = true }
            }
        }
    }

    // Afficher un Snackbar en cas d'erreur
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is PaymentUiState.Error) {
            snackbarHostState.showSnackbar(
                message = (uiState as PaymentUiState.Error).message,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Paiement") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF03A9F4),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Fond dégradé
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4FC3F7),
                                Color(0xFF29B6F6),
                                Color(0xFF03A9F4)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Card principale
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Icône de paiement
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(40.dp),
                                color = Color(0xFF03A9F4).copy(alpha = 0.1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(20.dp),
                                    tint = Color(0xFF03A9F4)
                                )
                            }
                        }

                        Text(
                            text = "Effectuer un paiement",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Entrez le montant à payer",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Champ montant
                        OutlinedTextField(
                            value = amountInEuros,
                            onValueChange = { newValue ->
                                // Permettre uniquement les nombres et un point décimal
                                if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}$")) || newValue.isEmpty()) {
                                    amountInEuros = newValue
                                }
                            },
                            label = { Text("Montant (€)") },
                            leadingIcon = {
                                Icon(Icons.Default.Euro, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = uiState !is PaymentUiState.Loading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BlueLight.copy(alpha = 0.3f),
                                unfocusedContainerColor = GreyLight,
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        // Champs optionnels
                        OutlinedTextField(
                            value = resourceId,
                            onValueChange = { resourceId = it },
                            label = { Text("ID Ressource (optionnel)") },
                            leadingIcon = {
                                Icon(Icons.Default.Tag, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = uiState !is PaymentUiState.Loading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BlueLight.copy(alpha = 0.3f),
                                unfocusedContainerColor = GreyLight,
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        OutlinedTextField(
                            value = resourceType,
                            onValueChange = { resourceType = it },
                            label = { Text("Type de ressource (optionnel)") },
                            leadingIcon = {
                                Icon(Icons.Default.Category, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = uiState !is PaymentUiState.Loading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BlueLight.copy(alpha = 0.3f),
                                unfocusedContainerColor = GreyLight,
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bouton Payer
                        Button(
                            onClick = {
                                val amount = amountInEuros.toDoubleOrNull() ?: 0.0 // Montant en euros
                                if (amount > 0) {
                                    viewModel.createPaymentIntent(
                                        amount = amount.toInt() // Le backend multiplie par 100 pour Stripe
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = amountInEuros.toDoubleOrNull()?.let { it > 0 } == true 
                                    && uiState !is PaymentUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF03A9F4),
                                disabledContainerColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            if (uiState is PaymentUiState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payment,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Payer ${amountInEuros}€",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Message d'erreur
                        if (uiState is PaymentUiState.Error) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = (uiState as PaymentUiState.Error).message,
                                        fontSize = 14.sp,
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

