package com.sim.darna.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.sim.darna.data.model.Categorie
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.model.PubliciteType
import com.sim.darna.ui.theme.*

@Composable
fun PubliciteCard(
    publicite: Publicite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showEditDelete: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (publicite.imageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(publicite.imageUrl),
                        contentDescription = publicite.titre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GreyLight)
                    )
                }

                // Boutons Edit/Delete en haut à droite (seulement en mode sponsor)
                if (showEditDelete && (onEdit != null || onDelete != null)) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        onEdit?.let {
                            Surface(
                                onClick = it,
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color(0xFF2196F3), // Bleu plus visible
                                shadowElevation = 6.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Modifier",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                        onDelete?.let {
                            Surface(
                                onClick = it,
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color(0xFFF44336), // Rouge plus visible
                                shadowElevation = 6.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Contenu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Nom de la marque en bleu
                if (publicite.sponsorName != null) {
                    Text(
                        text = publicite.sponsorName,
                        color = BluePrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Titre de la promotion
                Text(
                    text = publicite.titre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Date d'expiration
                if (publicite.dateExpiration != null) {
                    Text(
                        text = "Expire le ${publicite.dateExpiration}",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// Preview pour PubliciteCard
@Preview(showBackground = true, widthDp = 360)
@Composable
fun PubliciteCardPreview() {
    val samplePublicite = Publicite(
        id = "1",
        titre = "2 Pizzas Achetées = 1 Offerte",
        description = "Profitez de notre offre spéciale étudiants",
        imageUrl = null,
        type = PubliciteType.PROMOTION,
        sponsorId = "sponsor1",
        sponsorName = "Pizza Express",
        categorie = Categorie.NOURRITURE,
        dateExpiration = "31 décembre 2025"
    )
    
    PubliciteCard(
        publicite = samplePublicite,
        onClick = {},
        showEditDelete = true,
        onEdit = { },
        onDelete = { }
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PubliciteCardWithoutActionsPreview() {
    val samplePublicite = Publicite(
        id = "1",
        titre = "2 Pizzas Achetées = 1 Offerte",
        description = "Profitez de notre offre spéciale étudiants",
        imageUrl = null,
        type = PubliciteType.PROMOTION,
        sponsorId = "sponsor1",
        sponsorName = "Baristas",
        categorie = Categorie.NOURRITURE,
        dateExpiration = "31 décembre 2025"
    )
    
    PubliciteCard(
        publicite = samplePublicite,
        onClick = {},
        showEditDelete = false
    )
}

