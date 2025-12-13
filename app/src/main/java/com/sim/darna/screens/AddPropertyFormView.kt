package com.sim.darna.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager
import com.sim.darna.components.CustomDatePickerDialog
import com.sim.darna.model.Property
import com.sim.darna.network.NominatimResult
import com.sim.darna.network.NominatimService
import com.sim.darna.repository.PropertyRepository
import com.sim.darna.ui.theme.AppTheme
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.ByteArrayOutputStream
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyFormView(
    propertyToEdit: Property? = null,
    onDismiss: () -> Unit,
    onPropertySaved: (Property) -> Unit
) {
    val context = LocalContext.current
    val repository = PropertyRepository(context)
    
    var propertyTitle by remember { mutableStateOf(propertyToEdit?.title ?: "") }
    var location by remember { mutableStateOf(propertyToEdit?.location ?: "") }
    var price by remember { mutableStateOf(propertyToEdit?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(propertyToEdit?.description ?: "") }
    var selectedType by remember { mutableStateOf(propertyToEdit?.type ?: "S") }
    var nbrCollocateurMax by remember { mutableStateOf(propertyToEdit?.nbrCollocateurMax?.toString() ?: "4") }
    var nbrCollocateurActuel by remember { mutableStateOf(propertyToEdit?.nbrCollocateurActuel?.toString() ?: "0") }

    val coroutineScope = rememberCoroutineScope()

    var addressSearchQuery by remember { mutableStateOf("") }
    var isSearchingLocation by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var mapError by remember { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var selectedGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var mapZoom by remember { mutableStateOf(6.0) }
    var selectedMapAddress by remember { mutableStateOf(propertyToEdit?.location ?: "") }
    var isResolvingAddress by remember { mutableStateOf(false) }
    val defaultGeoPoint = remember { GeoPoint(36.8065, 10.1815) }
    val sharedPreferences = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(6.0)
            controller.setCenter(defaultGeoPoint)
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, sharedPreferences)
        Configuration.getInstance().userAgentValue = context.packageName
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    fun formatCoordinates(point: GeoPoint): String {
        return "Lat: ${"%.5f".format(point.latitude)} / Lon: ${"%.5f".format(point.longitude)}"
    }

    fun updateAddressFromPoint(point: GeoPoint, presetAddress: String? = null) {
        selectedGeoPoint = point
        mapZoom = 15.5
        presetAddress?.let {
            selectedMapAddress = it
            mapError = null
            return
        }
        coroutineScope.launch {
            isResolvingAddress = true
            mapError = null
            try {
                val reverseResult = NominatimService.reverse(point.latitude, point.longitude)
                selectedMapAddress = reverseResult.displayName ?: formatCoordinates(point)
                searchError = null
            } catch (e: Exception) {
                selectedMapAddress = formatCoordinates(point)
                mapError = "Impossible de récupérer l'adresse sélectionnée: ${e.message ?: "inconnue"}"
            } finally {
                isResolvingAddress = false
            }
        }
    }

    fun extractServerError(rawBody: String?, code: Int): String {
        if (rawBody.isNullOrBlank()) return "Erreur de sauvegarde (code $code)"
        return try {
            val json = JSONObject(rawBody)
            when {
                json.has("message") -> {
                    val messageNode = json.get("message")
                    when (messageNode) {
                        is JSONArray -> (0 until messageNode.length())
                            .joinToString("\n") { messageNode.getString(it) }
                        is String -> messageNode
                        else -> messageNode.toString()
                    }
                }
                json.has("error") -> json.getString("error")
                else -> rawBody
            }
        } catch (e: Exception) {
            rawBody
        }
    }
    
    // Track which images are newly selected (have URIs) vs existing (base64 only)
    // Map of index in imageBase64List to URI (only for newly added images)
    var imageUriMap by remember { mutableStateOf<Map<Int, Uri>>(emptyMap()) }
    var imageBase64List by remember { 
        mutableStateOf(
            if (propertyToEdit != null && propertyToEdit.images != null) {
                propertyToEdit.images!!
            } else {
                emptyList<String>()
            }
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun resizeBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int = 1280): Bitmap {
        if (bitmap.width <= maxDimension && bitmap.height <= maxDimension) return bitmap

        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val (targetWidth, targetHeight) = if (bitmap.width >= bitmap.height) {
            val width = maxDimension
            val height = (maxDimension / aspectRatio).roundToInt().coerceAtLeast(1)
            width to height
        } else {
            val height = maxDimension
            val width = (maxDimension * aspectRatio).roundToInt().coerceAtLeast(1)
            width to height
        }
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    fun encodeImageUriToBase64(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                if (originalBitmap == null) {
                    errorMessage = "Impossible de lire l'image sélectionnée."
                    return null
                }
                val processedBitmap = resizeBitmapIfNeeded(originalBitmap)
                val byteStream = ByteArrayOutputStream()
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteStream)
                if (processedBitmap != originalBitmap) {
                    originalBitmap.recycle()
                }
                val base64 = android.util.Base64.encodeToString(
                    byteStream.toByteArray(),
                    android.util.Base64.NO_WRAP
                )
                byteStream.close()
                "data:image/jpeg;base64,$base64"
            }
        } catch (e: Exception) {
            errorMessage = "Erreur lors du traitement d'une image: ${e.message}"
            null
        }
    }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    
    var startDate by remember { mutableStateOf(propertyToEdit?.startDate?.let { 
        try { dateFormat.parse(it) } catch (e: Exception) { Date() }
    } ?: Date()) }
    var endDate by remember { mutableStateOf(propertyToEdit?.endDate?.let {
        try { dateFormat.parse(it) } catch (e: Exception) { Date() }
    } ?: Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val base64String = encodeImageUriToBase64(uri)
            if (base64String != null) {
                val newIndex = imageBase64List.size
                imageBase64List = imageBase64List + base64String
                imageUriMap = imageUriMap + (newIndex to uri)
            }
        }
    }
    
    fun removeImage(index: Int) {
        // Remove from base64 list
        imageBase64List = imageBase64List.toMutableList().apply { removeAt(index) }
        
        // Update URI map: remove the entry for this index and shift indices
        imageUriMap = imageUriMap
            .filterKeys { it != index }
            .mapKeys { if (it.key > index) it.key - 1 else it.key }
            .toMap()
    }
    
    fun searchLocation() {
        val query = addressSearchQuery.trim()
        if (query.length < 3) {
            searchError = "Entrez au moins 3 caractères pour lancer la recherche."
            return
        }
        coroutineScope.launch {
            isSearchingLocation = true
            searchError = null
            mapError = null
            try {
                val results = NominatimService.search(query)
                searchResults = results
                if (results.isEmpty()) {
                    searchError = "Aucun résultat trouvé pour cette recherche."
                }
            } catch (e: UnknownHostException) {
                searchError = "Impossible de contacter OpenStreetMap. Vérifiez votre connexion Internet."
            } catch (e: Exception) {
                searchError = "Erreur lors de la recherche: ${e.message ?: "inconnue"}"
            } finally {
                isSearchingLocation = false
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.card,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (propertyToEdit == null) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = null,
                    tint = AppTheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (propertyToEdit == null) "Nouvelle Annonce" else "Modifier l'annonce",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = propertyTitle,
                    onValueChange = { propertyTitle = it },
                    label = { 
                        Text(
                            "Titre de l'annonce",
                            color = AppTheme.textSecondary
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.card,
                        unfocusedContainerColor = AppTheme.card,
                        focusedBorderColor = AppTheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = AppTheme.textPrimary,
                        unfocusedTextColor = AppTheme.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null,
                            tint = AppTheme.textSecondary
                        )
                    }
                )
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { 
                        Text(
                            "Localisation",
                            color = AppTheme.textSecondary
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.card,
                        unfocusedContainerColor = AppTheme.card,
                        focusedBorderColor = AppTheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = AppTheme.textPrimary,
                        unfocusedTextColor = AppTheme.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AppTheme.textSecondary
                        )
                    }
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Trouver l'adresse via la carte",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.textPrimary
                    )
                    Text(
                        text = "Utilisez OpenStreetMap pour rechercher une adresse et remplir automatiquement ce champ.",
                        fontSize = 13.sp,
                        color = AppTheme.textSecondary
                    )
                    OutlinedTextField(
                        value = addressSearchQuery,
                        onValueChange = {
                            addressSearchQuery = it
                            if (it.isEmpty()) {
                                searchResults = emptyList()
                                searchError = null
                            }
                        },
                        label = { 
                            Text(
                                "Rechercher une adresse",
                                color = AppTheme.textSecondary
                            ) 
                        },
                        placeholder = { Text("Ex: Tunis, Avenue Habib Bourguiba", color = AppTheme.textSecondary.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.card,
                            unfocusedContainerColor = AppTheme.card,
                            focusedBorderColor = AppTheme.primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedTextColor = AppTheme.textPrimary,
                            unfocusedTextColor = AppTheme.textPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = AppTheme.textSecondary
                            )
                        },
                        trailingIcon = {
                            if (isSearchingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = AppTheme.primary
                                )
                            } else if (addressSearchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    addressSearchQuery = ""
                                    searchResults = emptyList()
                                    searchError = null
                                }) {
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Effacer",
                                        tint = AppTheme.textSecondary
                                    )
                                }
                            }
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { searchLocation() },
                            enabled = !isSearchingLocation && addressSearchQuery.length >= 3,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = AppTheme.primary.copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSearchingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                if (isSearchingLocation) "Recherche..." else "Rechercher",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (searchError != null) {
                        Text(
                            text = searchError ?: "",
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                    }
                    if (searchResults.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            searchResults.forEach { result ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val lat = result.lat.toDoubleOrNull()
                                            val lon = result.lon.toDoubleOrNull()
                                            if (lat != null && lon != null) {
                                                val geoPoint = GeoPoint(lat, lon)
                                                updateAddressFromPoint(geoPoint, result.displayName)
                                                addressSearchQuery = ""
                                                searchResults = emptyList()
                                                searchError = null
                                            } else {
                                                searchError = "Impossible de récupérer les coordonnées de ce résultat."
                                            }
                                        },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = result.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AppTheme.textPrimary
                                        )
                                        Text(
                                            text = "Lat: ${result.lat} / Lon: ${result.lon}",
                                            fontSize = 12.sp,
                                            color = AppTheme.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        AndroidView(
                            factory = { mapView },
                            modifier = Modifier
                                .matchParentSize()
                        ) { map ->
                            val center = selectedGeoPoint ?: defaultGeoPoint
                            map.controller.setZoom(mapZoom)
                            map.controller.setCenter(center)
                            map.overlays.clear()
                            selectedGeoPoint?.let { point ->
                                val marker = Marker(map).apply {
                                    position = point
                                    this.title = selectedMapAddress.ifEmpty { "Localisation sélectionnée" }
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                map.overlays.add(marker)
                            }
                            map.overlays.add(
                                MapEventsOverlay(object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                        updateAddressFromPoint(p)
                                        return true
                                    }

                                    override fun longPressHelper(p: GeoPoint): Boolean = false
                                })
                            )
                            map.invalidate()
                        }

                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Adresse sélectionnée",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.textPrimary
                                )
                                Text(
                                    text = when {
                                        selectedMapAddress.isNotEmpty() -> selectedMapAddress
                                        selectedGeoPoint != null -> formatCoordinates(selectedGeoPoint!!)
                                        else -> "Sélectionnez un lieu sur la carte"
                                    },
                                    fontSize = 13.sp,
                                    color = AppTheme.textSecondary,
                                    maxLines = 2
                                )
                                if (isResolvingAddress) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                                mapError?.let {
                                    Text(
                                        text = it,
                                        fontSize = 12.sp,
                                        color = Color.Red
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedGeoPoint = null
                                            selectedMapAddress = ""
                                            location = ""
                                            addressSearchQuery = ""
                                            mapError = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Annuler")
                                    }
                                    Button(
                                        onClick = {
                                            location = selectedMapAddress
                                            addressSearchQuery = ""
                                            searchError = null
                                            mapError = null
                                        },
                                        enabled = selectedMapAddress.isNotEmpty(),
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppTheme.primary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Confirmer")
                                    }
                                }
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { 
                        Text(
                            "Prix (DT)",
                            color = AppTheme.textSecondary
                        ) 
                    },
                    placeholder = { Text("Ex: 500", color = AppTheme.textSecondary.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.card,
                        unfocusedContainerColor = AppTheme.card,
                        focusedBorderColor = AppTheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = AppTheme.textPrimary,
                        unfocusedTextColor = AppTheme.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = AppTheme.textSecondary
                        )
                    }
                )
                
                // Multiple image picker
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate, 
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (imageBase64List.isEmpty()) "Sélectionner des images" else "Ajouter plus d'images",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Display selected images
                if (imageBase64List.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Images sélectionnées (${imageBase64List.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(imageBase64List) { index, imageBase64 ->
                            Box(modifier = Modifier.size(120.dp)) {
                                // Display image from base64 (all images are stored as base64)
                                if (imageBase64.startsWith("data:image")) {
                                    val base64String = imageBase64.substringAfter(",")
                                    val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    bitmap?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } ?: run {
                                        // Fallback if decoding fails
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Gray.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Image,
                                                contentDescription = null,
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                } else {
                                    // Fallback for non-base64 images
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Gray.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                // Remove button
                                IconButton(
                                    onClick = { removeImage(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(32.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.Red.copy(alpha = 0.8f)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Supprimer",
                                            tint = Color.White,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Type picker
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { 
                            Text(
                                "Type de logement",
                                color = AppTheme.textSecondary
                            ) 
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.card,
                            unfocusedContainerColor = AppTheme.card,
                            focusedBorderColor = AppTheme.primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedTextColor = AppTheme.textPrimary,
                            unfocusedTextColor = AppTheme.textPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = AppTheme.textSecondary
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("S", "S+1", "S+2", "S+3", "S+4", "Chambre").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Date pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { 
                            Text(
                                "Date de début",
                                color = AppTheme.textSecondary
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.card,
                            unfocusedContainerColor = AppTheme.card,
                            focusedBorderColor = AppTheme.primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedTextColor = AppTheme.textPrimary,
                            unfocusedTextColor = AppTheme.textPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarToday, 
                                    null,
                                    tint = AppTheme.primary
                                )
                            }
                        }
                    )
                    
                    OutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { 
                            Text(
                                "Date de fin",
                                color = AppTheme.textSecondary
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.card,
                            unfocusedContainerColor = AppTheme.card,
                            focusedBorderColor = AppTheme.primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedTextColor = AppTheme.textPrimary,
                            unfocusedTextColor = AppTheme.textPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarToday, 
                                    null,
                                    tint = AppTheme.primary
                                )
                            }
                        }
                    )
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { 
                        Text(
                            "Description",
                            color = AppTheme.textSecondary
                        ) 
                    },
                    placeholder = { Text("Décrivez votre annonce...", color = AppTheme.textSecondary.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.card,
                        unfocusedContainerColor = AppTheme.card,
                        focusedBorderColor = AppTheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = AppTheme.textPrimary,
                        unfocusedTextColor = AppTheme.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = AppTheme.textSecondary
                        )
                    }
                )
                
                OutlinedTextField(
                    value = nbrCollocateurMax,
                    onValueChange = { nbrCollocateurMax = it },
                    label = { 
                        Text(
                            "Nombre maximum de colocataires",
                            color = AppTheme.textSecondary
                        ) 
                    },
                    placeholder = { Text("Ex: 4", color = AppTheme.textSecondary.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.card,
                        unfocusedContainerColor = AppTheme.card,
                        focusedBorderColor = AppTheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = AppTheme.textPrimary,
                        unfocusedTextColor = AppTheme.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = AppTheme.textSecondary
                        )
                    }
                )
                
                OutlinedTextField(
                    value = nbrCollocateurActuel,
                    onValueChange = { nbrCollocateurActuel = it },
                    label = { 
                        Text(
                            "Nombre actuel de colocataires",
                            color = AppTheme.textSecondary
                        ) 
                    },
                    placeholder = { Text("Ex: 0", color = AppTheme.textSecondary.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.card,
                        unfocusedContainerColor = AppTheme.card,
                        focusedBorderColor = AppTheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = AppTheme.textPrimary,
                        unfocusedTextColor = AppTheme.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AppTheme.textSecondary
                        )
                    }
                )
                
                errorMessage?.let {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = it,
                                color = Color(0xFFD32F2F),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(
                    color = AppTheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = {
                        val trimmedTitle = propertyTitle.trim()
                        val trimmedLocation = location.trim()

                        if (trimmedTitle.isEmpty() || trimmedLocation.isEmpty() || price.isEmpty() || 
                            imageBase64List.isEmpty() || description.isEmpty()) {
                            errorMessage = "Merci de remplir tous les champs requis et de sélectionner au moins une image."
                            return@Button
                        }
                        
                        val priceValue = price.toDoubleOrNull()
                        val nbrMax = nbrCollocateurMax.toIntOrNull()
                        val nbrActuel = nbrCollocateurActuel.toIntOrNull()
                        
                        if (priceValue == null || nbrMax == null || nbrActuel == null) {
                            errorMessage = "Valeurs numériques invalides."
                            return@Button
                        }
                        
                        if (startDate.after(endDate)) {
                            errorMessage = "La date de fin doit être après la date de début."
                            return@Button
                        }
                        
                        if (nbrActuel > nbrMax) {
                            errorMessage = "Le nombre actuel dépasse le maximum."
                            return@Button
                        }
                        
                        isLoading = true
                        errorMessage = null
                        
                        // Format dates to ISO 8601 manually to guarantee standard format with no locale issues
                        val formatToIso8601 = { date: Date ->
                            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
                            cal.time = date
                            val y = cal.get(Calendar.YEAR)
                            val m = cal.get(Calendar.MONTH) + 1
                            val d = cal.get(Calendar.DAY_OF_MONTH)
                            val h = cal.get(Calendar.HOUR_OF_DAY)
                            val min = cal.get(Calendar.MINUTE)
                            val s = cal.get(Calendar.SECOND)
                            // Try +00:00 instead of Z
                            String.format(Locale.US, "%04d-%02d-%02dT%02d:%02d:%02d+00:00", y, m, d, h, min, s)
                        }

                        val startDateStr = formatToIso8601(startDate)
                        val endDateStr = formatToIso8601(endDate)
                        
                        // Debug log and TOAST visible to user
                        android.util.Log.e("DEBUG_DATE", "Sent dates: Start=$startDateStr, End=$endDateStr")
                        android.widget.Toast.makeText(context, "Date: $startDateStr", android.widget.Toast.LENGTH_SHORT).show()
                        
                        if (propertyToEdit == null) {
                            // Create
                            repository.createProperty(
                                title = trimmedTitle,
                                description = description,
                                price = priceValue,
                                location = trimmedLocation,
                                type = selectedType,
                                images = imageBase64List,
                                nbrCollocateurMax = nbrMax,
                                nbrCollocateurActuel = nbrActuel,
                                startDate = startDateStr,
                                endDate = endDateStr
                            ).enqueue(object : retrofit2.Callback<Property> {
                                override fun onResponse(call: retrofit2.Call<Property>, response: retrofit2.Response<Property>) {
                                    isLoading = false
                                    if (response.isSuccessful && response.body() != null) {
                                        onPropertySaved(response.body()!!)
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        errorMessage = extractServerError(errorBody, response.code())
                                    }
                                }
                                
                                override fun onFailure(call: retrofit2.Call<Property>, t: Throwable) {
                                    isLoading = false
                                    errorMessage = when (t) {
                                        is UnknownHostException -> "Impossible de contacter le serveur. Vérifiez votre connexion."
                                        else -> "Erreur: ${t.message ?: "inconnue"}"
                                    }
                                }
                            })
                        } else {
                            // Update
                            repository.updateProperty(
                                id = propertyToEdit.id,
                                title = trimmedTitle,
                                description = description,
                                price = priceValue,
                                location = trimmedLocation,
                                type = selectedType,
                                images = imageBase64List,
                                nbrCollocateurMax = nbrMax,
                                nbrCollocateurActuel = nbrActuel,
                                startDate = startDateStr,
                                endDate = endDateStr
                            ).enqueue(object : retrofit2.Callback<Property> {
                                override fun onResponse(call: retrofit2.Call<Property>, response: retrofit2.Response<Property>) {
                                    isLoading = false
                                    if (response.isSuccessful && response.body() != null) {
                                        onPropertySaved(response.body()!!)
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        errorMessage = extractServerError(errorBody, response.code())
                                    }
                                }
                                
                                override fun onFailure(call: retrofit2.Call<Property>, t: Throwable) {
                                    isLoading = false
                                    errorMessage = when (t) {
                                        is UnknownHostException -> "Impossible de contacter le serveur. Vérifiez votre connexion."
                                        else -> "Erreur: ${t.message ?: "inconnue"}"
                                    }
                                }
                            })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Check, 
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (propertyToEdit == null) "Créer" else "Enregistrer",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppTheme.textPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    "Annuler",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    )
    
    // Date pickers
    if (showStartDatePicker) {
        // Set minimum date to today (start of day)
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val minDateMillis = today.timeInMillis
        
        // Use today if startDate is in the past, otherwise use startDate
        val calendar = Calendar.getInstance()
        if (startDate.before(today.time)) {
            calendar.time = today.time
        } else {
            calendar.time = startDate
        }
        
        CustomDatePickerDialog(
            initialYear = calendar.get(Calendar.YEAR),
            initialMonth = calendar.get(Calendar.MONTH),
            initialDay = calendar.get(Calendar.DAY_OF_MONTH),
            onDateSelected = { year, month, day ->
                calendar.set(year, month, day)
                startDate = calendar.time
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false },
            minDate = minDateMillis
        )
    }
    
    if (showEndDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        val minDateMillis = startDate.time
        CustomDatePickerDialog(
            initialYear = calendar.get(Calendar.YEAR),
            initialMonth = calendar.get(Calendar.MONTH),
            initialDay = calendar.get(Calendar.DAY_OF_MONTH),
            onDateSelected = { year, month, day ->
                calendar.set(year, month, day)
                endDate = calendar.time
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
            minDate = minDateMillis
        )
    }
}


