package com.sim.darna.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FingerprintScreen(onNext: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sécurisez votre compte", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onNext) { Text("Ajouter l’empreinte") }
            TextButton(onClick = onNext) { Text("Passer cette étape") }
        }
    }
}
