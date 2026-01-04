package com.sim.darna.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.factory.VerificationVmFactory
import com.sim.darna.utils.ApiConfig
import com.sim.darna.viewmodel.VerificationViewModel

private val PrimaryColor = Color(0xFF1382B3)
private val SurfaceColor = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val BackgroundColor = Color(0xFFF7F7F7)

@Composable
fun VerificationScreen(onVerificationSuccess: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    
    val viewModel: VerificationViewModel = viewModel(
        factory = VerificationVmFactory(ApiConfig.BASE_URL, sharedPreferences, context)
    )
    val state by viewModel.state.collectAsState()
    
    var code by remember { mutableStateOf("") }

    LaunchedEffect(state.success) {
        if (state.success) {
            onVerificationSuccess()
        }
    }

    Surface(
        color = BackgroundColor,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceColor,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Vérification Email",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Entrez le code envoyé à votre email",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        label = { Text("Code de vérification") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceColor,
                            unfocusedContainerColor = SurfaceColor,
                            focusedBorderColor = PrimaryColor,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = Color(0xFFFF3B30),
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.verify(code) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Vérifier", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
