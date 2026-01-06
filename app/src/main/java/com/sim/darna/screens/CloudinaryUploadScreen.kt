package com.sim.darna.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.sim.darna.viewmodel.CloudinaryUploadViewModel
import com.sim.darna.viewmodel.UploadState

@Composable
fun CloudinaryUploadScreen(
    viewModel: CloudinaryUploadViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uploadState by viewModel.uploadState.collectAsState()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var resultUrl by remember { mutableStateOf<String?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Reset state
            resultUrl = null
        }
    }

    LaunchedEffect(uploadState) {
        if (uploadState is UploadState.Success) {
            resultUrl = (uploadState as UploadState.Success).imageUrl
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Cloudinary Image Upload",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Image Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            if (resultUrl != null) {
                // Show uploaded image from URL
                Image(
                    painter = rememberAsyncImagePainter(resultUrl),
                    contentDescription = "Uploaded Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (selectedImageUri != null) {
                // Show selected local image
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No image selected", color = Color.Gray)
                }
            }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { pickerLauncher.launch("image/*") },
                enabled = uploadState !is UploadState.Loading
            ) {
                Text(if (selectedImageUri == null) "Pick Image" else "Change Image")
            }

            Button(
                onClick = {
                    selectedImageUri?.let { uri ->
                        viewModel.uploadImage(context, uri)
                    }
                },
                enabled = selectedImageUri != null && uploadState !is UploadState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uploadState is UploadState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Upload")
                }
            }
        }

        // Status / Error Message
        if (uploadState is UploadState.Error) {
            Text(
                text = (uploadState as UploadState.Error).message,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        if (uploadState is UploadState.Success) {
            Text(
                text = "Upload Successful!",
                color = Color.Green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            // Optional: Show the URL
            Text(
                text = (uploadState as UploadState.Success).imageUrl,
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
