package com.sim.darna.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sim.darna.components.QRCodeScanner
import com.sim.darna.data.model.QRCodeVerificationResponse
import com.sim.darna.viewmodel.PubliciteViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScanResultScreen(
    onNavigateBack: () -> Unit,
    viewModel: PubliciteViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showScanner by remember { mutableStateOf(true) }
    var verificationResult by remember { mutableStateOf<QRCodeVerificationResponse?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner QR") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                showScanner -> {
                    QRCodeScanner(
                        onQRCodeScanned = { qrData ->
                            showScanner = false
                            isVerifying = true
                            errorMessage = null
                            
                            viewModel.verifyQRCode(context, qrData) { success, response ->
                                isVerifying = false
                                if (success && response != null) {
                                    verificationResult = response
                                } else {
                                    val errorMsg = if (response != null && !response.message.isNullOrEmpty()) {
                                        response.message
                                    } else {
                                        "QR Code invalide ou expiré"
                                    }
                                    errorMessage = errorMsg
                                }
                            }
                        },
                        onDismiss = onNavigateBack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                isVerifying -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Vérification en cours...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666)
                        )
                    }
                }
                
                verificationResult != null -> {
                    // Affichage du résultat de succès
                    val result = verificationResult!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            ),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(64.dp)
                                )
                                
                                Text(
                                    text = "QR Code valide !",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                
                                if (result.reduction != null) {
                                    Text(
                                        text = "Vous avez une réduction de ${result.reduction}%",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1B5E20),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                result.message?.let {
                                    Text(
                                        text = it,
                                        fontSize = 16.sp,
                                        color = Color(0xFF666666),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    )
                                ) {
                                    Text("Retour", color = Color.White)
                                }
                            }
                        }
                    }
                }
                
                errorMessage != null -> {
                    // Affichage de l'erreur
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(64.dp)
                                )
                                
                                Text(
                                    text = "Erreur",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                
                                Text(
                                    text = errorMessage ?: "QR Code invalide ou expiré",
                                    fontSize = 16.sp,
                                    color = Color(0xFF666666),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onNavigateBack,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Retour")
                                    }
                                    
                                    Button(
                                        onClick = {
                                            showScanner = true
                                            verificationResult = null
                                            errorMessage = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2196F3)
                                        )
                                    ) {
                                        Text("Réessayer", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

