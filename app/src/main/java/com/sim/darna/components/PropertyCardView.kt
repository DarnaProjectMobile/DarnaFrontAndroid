package com.sim.darna.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sim.darna.model.Property
import com.sim.darna.ui.theme.AppTheme
import com.sim.darna.utils.FavoritesManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PropertyCardView(
    property: Property,
    canManage: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit = {},
    isGridMode: Boolean = false
) {
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }
    
    LaunchedEffect(property.id) {
        FavoritesManager.init(context)
        isFavorite = FavoritesManager.isFavorite(property.id)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isGridMode) 120.dp else 160.dp)
                    .background(AppTheme.primaryLight)
            ) {
                PropertyImageView(
                    imageString = property.getFirstImage(),
                    modifier = Modifier.fillMaxSize()
                )
                
                // Favorite button
                IconButton(
                    onClick = {
                        FavoritesManager.toggleFavorite(context, property.id)
                        isFavorite = !isFavorite
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(if (isGridMode) 4.dp else 8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            modifier = Modifier.padding(if (isGridMode) 6.dp else 8.dp),
                            tint = if (isFavorite) Color(0xFFFF6B6B) else AppTheme.textSecondary
                        )
                    }
                }
            }
            
            // Content section
            Column(modifier = Modifier.padding(if (isGridMode) 12.dp else 16.dp)) {
                // Title and price
                Column {
                    Text(
                        text = property.title,
                        fontSize = if (isGridMode) 14.sp else 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.textPrimary,
                        maxLines = if (isGridMode) 1 else Int.MAX_VALUE
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${property.price.toInt()} DT/mois",
                        fontSize = if (isGridMode) 14.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.primary
                    )
                }
                
                // Description - only show in list mode
                if (!isGridMode && !property.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = property.description,
                        fontSize = 14.sp,
                        color = AppTheme.textSecondary,
                        maxLines = 3
                    )
                }
                
                // Start date
                property.startDate?.let { dateStr ->
                    Spacer(modifier = Modifier.height(if (isGridMode) 4.dp else 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(if (isGridMode) 12.dp else 14.dp),
                            tint = AppTheme.textSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(dateStr),
                            fontSize = if (isGridMode) 10.sp else 12.sp,
                            color = AppTheme.textSecondary
                        )
                    }
                }
                
                // Owner name
                Spacer(modifier = Modifier.height(if (isGridMode) 4.dp else 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = property.ownerName ?: property.ownerUsername ?: property.user ?: "Non spécifié",
                        fontSize = if (isGridMode) 10.sp else 12.sp,
                        color = AppTheme.textSecondary,
                        maxLines = 1
                    )
                    
                    if (!isGridMode && canManage) {
                        Row {
                            IconButton(onClick = { onEdit?.invoke() }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(18.dp),
                                    tint = AppTheme.primary
                                )
                            }
                            IconButton(onClick = { onDelete?.invoke() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyImageView(
    imageString: String?,
    modifier: Modifier = Modifier
) {
    if (imageString != null && imageString.isNotEmpty()) {
        if (imageString.startsWith("data:image")) {
            // Base64 image
            val base64String = imageString.substringAfter(",")
            val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Property image",
                    modifier = modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                PlaceholderImage(modifier)
            }
        } else {
            // URL image - using Coil
            AsyncImage(
                model = imageString,
                contentDescription = "Property image",
                modifier = modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        PlaceholderImage(modifier)
    }
}

@Composable
private fun PlaceholderImage(modifier: Modifier) {
    Box(
        modifier = modifier.background(AppTheme.primaryLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Property",
            modifier = Modifier.size(80.dp),
            tint = AppTheme.primary.copy(alpha = 0.3f)
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}


