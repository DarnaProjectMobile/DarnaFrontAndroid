package com.sim.darna.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isGridMode) 3.dp else 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isGridMode) 140.dp else 220.dp)
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
                        .padding(if (isGridMode) 8.dp else 12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, Color(0xFFE6E6E6))
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            modifier = Modifier.padding(if (isGridMode) 8.dp else 10.dp),
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
                        fontSize = if (isGridMode) 15.sp else 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.textPrimary,
                        maxLines = if (isGridMode) 1 else Int.MAX_VALUE
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${property.price.toInt()} DT/mois",
                        fontSize = if (isGridMode) 14.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0066FF)
                    )
                }
                
                // Description - only show in list mode
                if (!isGridMode && !property.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = property.description,
                        fontSize = 14.sp,
                        color = AppTheme.textSecondary,
                        maxLines = 2
                    )
                }
                
                // Start date
                property.startDate?.let { dateStr ->
                    Spacer(modifier = Modifier.height(if (isGridMode) 4.dp else 10.dp))
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
                
                Spacer(modifier = Modifier.height(if (isGridMode) 6.dp else 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = property.ownerName ?: property.ownerUsername ?: property.user ?: "Non spécifié",
                        fontSize = if (isGridMode) 11.sp else 12.sp,
                        color = AppTheme.textSecondary,
                        maxLines = 1
                    )
                    
                    if (isGridMode) {
                        Spacer(modifier = Modifier.width(1.dp))
                    } else if (canManage) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ManageActionChip(
                                text = "Modifier",
                                icon = Icons.Default.Edit,
                                background = AppTheme.primary.copy(alpha = 0.12f),
                                contentColor = AppTheme.primary,
                                onClick = { onEdit?.invoke() }
                            )
                            ManageActionChip(
                                text = "Supprimer",
                                icon = Icons.Default.Delete,
                                background = Color(0xFFFFEBEE),
                                contentColor = Color(0xFFD32F2F),
                                onClick = { onDelete?.invoke() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageActionChip(
    text: String,
    icon: ImageVector,
    background: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = background,
        border = BorderStroke(1.dp, background.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
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


