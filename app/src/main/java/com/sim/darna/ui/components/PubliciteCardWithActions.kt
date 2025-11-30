package com.sim.darna.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.model.PubliciteType
import com.sim.darna.data.model.Categorie

@Composable
fun PubliciteCardWithActions(
    publicite: Publicite,
    onClick: () -> Unit,
    showEditDelete: Boolean,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onDeleteConfirm: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'annonce") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette annonce ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        publicite.id?.let { onDeleteConfirm(it) }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Supprimer", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    PubliciteCard(
        publicite = publicite,
        onClick = onClick,
        showEditDelete = showEditDelete,
        onEdit = onEdit,
        onDelete = onDelete?.let { { showDeleteDialog = true } }
    )
}

// Preview pour PubliciteCardWithActions
@Preview(showBackground = true, widthDp = 360)
@Composable
fun PubliciteCardWithActionsPreview() {
    val samplePublicite = Publicite(
        titre = "2 Pizzas Achetées = 1 Offerte",
        description = "Profitez de notre offre spéciale étudiants",
        imageUrl = null,
        type = PubliciteType.PROMOTION,
        sponsorName = "Pizza Express",
        categorie = Categorie.NOURRITURE,
        dateExpiration = "31 décembre 2025"
    )

    PubliciteCardWithActions(
        publicite = samplePublicite,
        onClick = {},
        showEditDelete = true,
        onEdit = { },
        onDelete = { },
        onDeleteConfirm = { }
    )
}