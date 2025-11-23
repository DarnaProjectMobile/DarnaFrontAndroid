package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sim.darna.visite.VisiteResponse
import com.sim.darna.visite.VisiteViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun MyVisitsScreen(viewModel: VisiteViewModel) {
    val context = LocalContext.current
    val uiState = viewModel.state.collectAsState().value
    var editingVisite by remember { mutableStateOf<VisiteResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadVisites()
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Slightly lighter background
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mes visites",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Gérez vos demandes de visite",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
            IconButton(
                onClick = { viewModel.loadVisites(force = true) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir",
                    tint = Color(0xFF0066FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        StatusLegend()

        if (uiState.isSubmitting) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF0066FF),
                trackColor = Color(0xFFE2E8F0)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoadingList && uiState.visites.isEmpty() -> {
                LoadingVisitsPlaceholder()
            }

            uiState.visites.isEmpty() -> {
                EmptyVisitsState(onRefresh = { viewModel.loadVisites(force = true) })
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.visites, key = { it.id ?: it.dateVisite ?: "" }) { visite ->
                        VisitCard(
                            visite = visite,
                            onEdit = { editingVisite = it },
                            onCancel = { id -> viewModel.cancelVisite(id) },
                            onDelete = { id -> viewModel.deleteVisite(id) }
                        )
                    }
                }
            }
        }
    }

    editingVisite?.let { visite ->
        EditVisiteDialog(
            visite = visite,
            onDismiss = { editingVisite = null },
            onValidate = { dateMillis, hour, minute, notes, contact ->
                val visiteId = visite.id
                if (visiteId != null) {
                    viewModel.updateVisite(
                        visiteId = visiteId,
                        dateMillis = dateMillis,
                        hour = hour,
                        minute = minute,
                        notes = notes,
                        contactPhone = contact
                    )
                }
                editingVisite = null
            }
        )
    }
}

@Composable
private fun StatusLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            StatusChipData("En attente", Color(0xFFF59E0B), Icons.Default.Schedule),
            StatusChipData("Acceptée", Color(0xFF10B981), Icons.Default.CheckCircle),
            StatusChipData("Refusée", Color(0xFFEF4444), Icons.Default.Cancel),
            StatusChipData("Terminée", Color(0xFF3B82F6), Icons.Default.TaskAlt)
        ).forEach { chip ->
            SurfaceChip(icon = chip.icon, label = chip.label, color = chip.color)
        }
    }
}

@Composable
private fun SurfaceChip(icon: ImageVector, label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LoadingVisitsPlaceholder() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE2E8F0))
                )
            }
        }
    }
}

@Composable
private fun EmptyVisitsState(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Aucune visite pour le moment",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Vos demandes de visite apparaîtront ici une fois créées.",
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Actualiser", fontSize = 16.sp)
        }
    }
}

@Composable
private fun VisitCard(
    visite: VisiteResponse,
    onEdit: (VisiteResponse) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val statusChip = mapStatus(visite.status)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Softer shadow
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Logement ID + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visite.logementId ?: "Logement inconnu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDate(visite.dateVisite),
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                SurfaceChip(
                    icon = statusChip.icon,
                    label = statusChip.label,
                    color = statusChip.color
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                InfoRow(label = "Heure", value = formatTime(visite.dateVisite))
                if (!visite.contactPhone.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Contact", value = visite.contactPhone)
                }
                if (!visite.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Notes", value = visite.notes)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Modifier (Only if not cancelled/completed ideally, but keeping enabled for now)
                OutlinedButton(
                    onClick = { if (visite.id != null) onEdit(visite) },
                    enabled = visite.id != null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0066FF))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Modifier")
                }

                // Annuler (If not already cancelled)
                if (visite.status != "cancelled" && visite.status != "refused") {
                    Button(
                        onClick = { visite.id?.let(onCancel) },
                        enabled = visite.id != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFDC2626)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }

                // Supprimer
                Button(
                    onClick = { visite.id?.let(onDelete) },
                    enabled = visite.id != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFF7ED),
                        contentColor = Color(0xFFEA580C)
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditVisiteDialog(
    visite: VisiteResponse,
    onDismiss: () -> Unit,
    onValidate: (Long, Int, Int, String?, String?) -> Unit
) {
    val initialMillis = parseIsoToMillis(visite.dateVisite) ?: System.currentTimeMillis()
    val initialTime = extractHourMinute(visite.dateVisite)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    var selectedHour by remember { mutableStateOf(initialTime.first) }
    var selectedMinute by remember { mutableStateOf(initialTime.second) }
    var timeSelectorExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(visite.notes.orEmpty()) }
    var contact by remember { mutableStateOf(visite.contactPhone.orEmpty()) }

    val timeSlots = remember {
        listOf(
            9 to 0, 9 to 30, 10 to 0, 10 to 30,
            11 to 0, 11 to 30, 14 to 0, 14 to 30,
            15 to 0, 15 to 30, 16 to 0, 16 to 30
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val dateMillis = datePickerState.selectedDateMillis
                    if (dateMillis != null) {
                        onValidate(dateMillis, selectedHour, selectedMinute, notes, contact)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = Color(0xFF64748B))
            }
        },
        title = { Text("Modifier la visite", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                androidx.compose.material3.DatePicker(state = datePickerState)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Créneau horaire",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box {
                    OutlinedButton(
                        onClick = { timeSelectorExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                selectedHour,
                                selectedMinute
                            ),
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF64748B))
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = timeSelectorExpanded,
                        onDismissRequest = { timeSelectorExpanded = false }
                    ) {
                        timeSlots.forEach { (hour, minute) ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = {
                                    Text(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
                                },
                                onClick = {
                                    selectedHour = hour
                                    selectedMinute = minute
                                    timeSelectorExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Téléphone") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

private data class StatusChipData(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

private fun mapStatus(status: String?): StatusChipData {
    return when (status?.lowercase()) {
        "confirmed" -> StatusChipData("Acceptée", Color(0xFF10B981), Icons.Default.CheckCircle)
        "cancelled", "refused" -> StatusChipData("Refusée", Color(0xFFEF4444), Icons.Default.Cancel)
        "completed" -> StatusChipData("Terminée", Color(0xFF3B82F6), Icons.Default.TaskAlt)
        else -> StatusChipData("En attente", Color(0xFFF59E0B), Icons.Default.Schedule)
    }
}

private fun formatDate(dateString: String?): String {
    val date = parseIsoToMillis(dateString) ?: return "-"
    val formatter = SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault())
    return formatter.format(date).replaceFirstChar { it.uppercase() }
}

private fun formatTime(dateString: String?): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    val millis = parseIsoToMillis(dateString)
    if (millis != null) {
        calendar.timeInMillis = millis
    }
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(calendar.time)
}

private fun parseIsoToMillis(dateString: String?): Long? {
    if (dateString.isNullOrBlank()) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        parser.parse(dateString)?.time
    } catch (_: Exception) {
        null
    }
}

private fun extractHourMinute(dateString: String?): Pair<Int, Int> {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    parseIsoToMillis(dateString)?.let { calendar.timeInMillis = it }
    return calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)
}

