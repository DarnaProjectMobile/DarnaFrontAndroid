package com.sim.darna.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    
    // State variables
    var title by remember { mutableStateOf(propertyToEdit?.title ?: "") }
    var location by remember { mutableStateOf(propertyToEdit?.location ?: "") }
    var price by remember { mutableStateOf(propertyToEdit?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(propertyToEdit?.description ?: "") }
    var selectedType by remember { mutableStateOf(propertyToEdit?.type ?: "S") }
    var nbrCollocateurMax by remember { mutableStateOf(propertyToEdit?.nbrCollocateurMax?.toString() ?: "4") }
    var nbrCollocateurActuel by remember { mutableStateOf(propertyToEdit?.nbrCollocateurActuel?.toString() ?: "0") }
    
    // Image state - single image like iOS version
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var imageBase64 by remember { mutableStateOf(propertyToEdit?.images?.firstOrNull() ?: "") }
    
    // Date formatting
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    
    var startDate by remember { 
        mutableStateOf(propertyToEdit?.startDate?.let { 
            try { dateFormat.parse(it) } catch (e: Exception) { Date() }
        } ?: Date()) 
    }
    var endDate by remember { 
        mutableStateOf(propertyToEdit?.endDate?.let {
            try { dateFormat.parse(it) } catch (e: Exception) { Date() }
        } ?: Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) 
    }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    // Map-related state
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
    
    // Load existing image if editing
    LaunchedEffect(propertyToEdit) {
        if (propertyToEdit != null && propertyToEdit.images != null && propertyToEdit.images!!.isNotEmpty()) {
            val firstImage = propertyToEdit.images!!.first()
            if (firstImage.startsWith("data:image")) {
                try {
                    val base64String = firstImage.substringAfter(",")
                    val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    selectedImage = bitmap
                } catch (e: Exception) {
                    // Ignore decode errors
                }
            }
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
    
    // Helper functions
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
    
    fun convertImageToBase64(image: Bitmap): String {
        val processedBitmap = resizeBitmapIfNeeded(image)
        val byteStream = ByteArrayOutputStream()
        processedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteStream)
        val base64 = android.util.Base64.encodeToString(
            byteStream.toByteArray(),
            android.util.Base64.NO_WRAP
        )
        byteStream.close()
        return "data:image/jpeg;base64,$base64"
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
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        selectedImage = bitmap
                        imageBase64 = convertImageToBase64(bitmap)
                    } else {
                        errorMessage = "Impossible de lire l'image sélectionnée."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Erreur lors du traitement de l'image: ${e.message}"
            }
        }
    }
    
    // Save function
    suspend fun saveProperty() {
        // Validate inputs
        if (title.isEmpty() || location.isEmpty() || price.isEmpty() || 
            selectedImage == null || description.isEmpty()) {
            errorMessage = "Merci de remplir tous les champs requis."
            return
        }
        
        val priceValue = price.toDoubleOrNull()
        val nbrMax = nbrCollocateurMax.toIntOrNull()
        val nbrActuel = nbrCollocateurActuel.toIntOrNull()
        
        if (priceValue == null || nbrMax == null || nbrActuel == null) {
            errorMessage = "Valeurs numériques invalides."
            return
        }
        
        if (startDate.after(endDate)) {
            errorMessage = "La date de fin doit être après la date de début."
            return
        }
        
        if (nbrActuel > nbrMax) {
            errorMessage = "Le nombre actuel dépasse le maximum."
            return
        }
        
        isLoading = true
        errorMessage = null
        
        val startDateStr = dateFormat.format(startDate)
        val endDateStr = dateFormat.format(endDate)
        
        if (propertyToEdit == null) {
            // Create new property with file upload (triggers backend image verification)
            repository.createPropertyWithImageFile(
                title = title,
                description = description,
                price = priceValue,
                location = location,
                type = selectedType,
                imageBitmap = selectedImage!!, // Pass the bitmap directly
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
            // Update existing property (still using base64 for updates)
            val images = listOf(imageBase64)
            repository.updateProperty(
                id = propertyToEdit.id,
                title = title,
                description = description,
                price = priceValue,
                location = location,
                type = selectedType,
                images = images,
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
    }
    
    // Main UI
    Scaffold(
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(shadowElevation = 6.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        AppTheme.primary,
                                        AppTheme.primary.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color.White
                            )
                        }

                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (propertyToEdit == null) "Nouvelle Annonce" else "Modifier l'annonce",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Remplissez les informations ci-dessous",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }

                        Icon(
                            imageVector = if (propertyToEdit == null) Icons.Default.AddCircle else Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(24.dp)
                        )
                    }
                }
            }
        },
        containerColor = AppTheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Section
                HeaderSection(propertyToEdit)
                
                // Basic Information Section
                FormSection(
                    title = "Informations de base",
                    icon = Icons.Default.Info
                ) {
                    CustomTextField(
                        title = "Titre de l'annonce",
                        value = title,
                        onValueChange = { title = it },
                        icon = Icons.Default.Title,
                        placeholder = "Ex: Villa S+3 à Ariana"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Inline Map Location Picker
                    InlineMapLocationPicker(
                        location = location,
                        onLocationChange = { location = it },
                        addressSearchQuery = addressSearchQuery,
                        onAddressSearchQueryChange = { addressSearchQuery = it },
                        isSearchingLocation = isSearchingLocation,
                        searchError = searchError,
                        mapError = mapError,
                        searchResults = searchResults,
                        selectedGeoPoint = selectedGeoPoint,
                        selectedMapAddress = selectedMapAddress,
                        isResolvingAddress = isResolvingAddress,
                        mapView = mapView,
                        mapZoom = mapZoom,
                        defaultGeoPoint = defaultGeoPoint,
                        onSearch = { searchLocation() },
                        onSelectResult = { result ->
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
                        onConfirm = {
                            location = selectedMapAddress
                            addressSearchQuery = ""
                            searchError = null
                            mapError = null
                        },
                        onCancel = {
                            selectedGeoPoint = null
                            selectedMapAddress = ""
                            location = ""
                            addressSearchQuery = ""
                            mapError = null
                        },
                        onClearSearch = {
                            addressSearchQuery = ""
                            searchResults = emptyList()
                            searchError = null
                        },
                        formatCoordinates = { formatCoordinates(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    CustomTextField(
                        title = "Prix mensuel",
                        value = price,
                        onValueChange = { price = it },
                        icon = Icons.Default.AttachMoney,
                        placeholder = "0",
                        keyboardType = KeyboardType.Number
                    )
                }
                
                // Image Section
                FormSection(
                    title = "Photo",
                    icon = Icons.Default.Photo
                ) {
                    ImagePickerSection(
                        selectedImage = selectedImage,
                        onPickImage = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
                
                // Property Details Section
                FormSection(
                    title = "Détails du logement",
                    icon = Icons.Default.Home
                ) {
                    TypePicker(
                        selectedType = selectedType,
                        onTypeSelected = { selectedType = it }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    CustomTextEditor(
                        title = "Description",
                        value = description,
                        onValueChange = { description = it },
                        icon = Icons.Default.Description,
                        placeholder = "Décrivez votre logement, ses avantages, le quartier..."
                    )
                }
                
                // Availability Section
                FormSection(
                    title = "Disponibilité",
                    icon = Icons.Default.CalendarToday
                ) {
                    AvailabilitySection(
                        startDate = startDate,
                        endDate = endDate,
                        onStartDateClick = { showStartDatePicker = true },
                        onEndDateClick = { showEndDatePicker = true }
                    )
                }
                
                // Collocators Section
                FormSection(
                    title = "Colocataires",
                    icon = Icons.Default.People
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CustomTextField(
                                title = "Maximum",
                                value = nbrCollocateurMax,
                                onValueChange = { nbrCollocateurMax = it },
                                icon = Icons.Default.People,
                                placeholder = "4",
                                keyboardType = KeyboardType.Number
                            )
                        }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            CustomTextField(
                                title = "Actuel",
                                value = nbrCollocateurActuel,
                                onValueChange = { nbrCollocateurActuel = it },
                                icon = Icons.Default.Person,
                                placeholder = "0",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                }
                
                // Error Message - Show banner for non-image errors
                errorMessage?.let { msg ->
                    val isImageError = msg.contains("not house-related", ignoreCase = true) || 
                                      msg.contains("house-related", ignoreCase = true) ||
                                      msg.contains("images are not", ignoreCase = true) ||
                                      msg.contains("property_", ignoreCase = true) ||
                                      msg.contains(".jpg", ignoreCase = true) ||
                                      msg.contains("upload images", ignoreCase = true)
                    
                    if (!isImageError) {
                        ErrorBanner(message = msg)
                    } else {
                        // Trigger dialog for image-related errors
                        LaunchedEffect(msg) {
                            showErrorDialog = true
                        }
                    }
                }
                
                // Save Button
                SaveButton(
                    isLoading = isLoading,
                    isEdit = propertyToEdit != null,
                    onClick = {
                        focusManager.clearFocus()
                        coroutineScope.launch {
                            saveProperty()
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    
    // Image Error Dialog
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = false
                errorMessage = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = "Image non valide",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.textPrimary
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 15.sp,
                        color = AppTheme.textSecondary,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppTheme.primaryLight.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AppTheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Veuillez télécharger des images d'intérieurs ou d'extérieurs de maison uniquement.",
                                fontSize = 13.sp,
                                color = AppTheme.textPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showErrorDialog = false
                        errorMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Compris",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            },
            containerColor = AppTheme.card,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        )
    }
    
    // Date pickers
    if (showStartDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
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
            minDate = today.timeInMillis
        )
    }
    
    if (showEndDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.time = endDate
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
            minDate = startDate.time
        )
    }
}

// MARK: - Header Section
@Composable
private fun HeaderSection(propertyToEdit: Property?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (propertyToEdit == null) Icons.Default.AddCircle else Icons.Default.Edit,
            contentDescription = null,
            tint = AppTheme.primary,
            modifier = Modifier.size(50.dp)
        )
        
        Text(
            text = if (propertyToEdit == null) "Créer une nouvelle annonce" else "Modifier l'annonce",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.textPrimary
        )
        
        Text(
            text = "Remplissez les informations ci-dessous",
            fontSize = 14.sp,
            color = AppTheme.textSecondary
        )
    }
}

// MARK: - Form Section Wrapper
@Composable
private fun FormSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(AppTheme.card, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.textPrimary
            )
        }
        
        content()
    }
}

// MARK: - Custom Text Field
@Composable
private fun CustomTextField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.textPrimary
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isFocused) AppTheme.primary.copy(alpha = 0.05f) else Color.Gray.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.Gray.copy(alpha = 0.08f),
                focusedBorderColor = AppTheme.primary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = AppTheme.textPrimary,
                unfocusedTextColor = AppTheme.textPrimary
            ),
            singleLine = true
        )
    }
}

// MARK: - Custom Text Editor
@Composable
private fun CustomTextEditor(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.textPrimary
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxSize(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = AppTheme.textPrimary,
                    unfocusedTextColor = AppTheme.textPrimary
                ),
                maxLines = 5
            )
        }
    }
}

// MARK: - Type Picker
@Composable
private fun TypePicker(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("S", "S+1", "S+2", "S+3", "S+4", "Chambre")
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.HomeWork,
                contentDescription = null,
                tint = AppTheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Type de logement",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.textPrimary
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedType,
                    color = AppTheme.textPrimary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = AppTheme.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                types.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(type)
                                if (selectedType == type) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = AppTheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// MARK: - Availability Section
@Composable
private fun AvailabilitySection(
    startDate: Date,
    endDate: Date,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Start Date
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .clickable(onClick = onStartDateClick)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = AppTheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Date de début",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.textPrimary
                    )
                    Text(
                        text = dateFormat.format(startDate),
                        fontSize = 13.sp,
                        color = AppTheme.textSecondary
                    )
                }
            }
        }
        
        // End Date
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .clickable(onClick = onEndDateClick)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = AppTheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Date de fin",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.textPrimary
                    )
                    Text(
                        text = dateFormat.format(endDate),
                        fontSize = 13.sp,
                        color = AppTheme.textSecondary
                    )
                }
            }
        }
    }
}

// MARK: - Image Picker Section
@Composable
private fun ImagePickerSection(
    selectedImage: Bitmap?,
    onPickImage: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Photo,
                contentDescription = null,
                tint = AppTheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Photo principale",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.textPrimary
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onPickImage)
        ) {
            if (selectedImage != null) {
                Image(
                    bitmap = selectedImage.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Edit overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.08f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AppTheme.primary,
                            modifier = Modifier.size(50.dp)
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Ajouter une photo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.textPrimary
                            )
                            Text(
                                text = "Appuyez pour sélectionner",
                                fontSize = 14.sp,
                                color = AppTheme.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Inline Map Location Picker
@Composable
private fun InlineMapLocationPicker(
    location: String,
    onLocationChange: (String) -> Unit,
    addressSearchQuery: String,
    onAddressSearchQueryChange: (String) -> Unit,
    isSearchingLocation: Boolean,
    searchError: String?,
    mapError: String?,
    searchResults: List<NominatimResult>,
    selectedGeoPoint: GeoPoint?,
    selectedMapAddress: String,
    isResolvingAddress: Boolean,
    mapView: MapView,
    mapZoom: Double,
    defaultGeoPoint: GeoPoint,
    onSearch: () -> Unit,
    onSelectResult: (NominatimResult) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onClearSearch: () -> Unit,
    formatCoordinates: (GeoPoint) -> String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Sélectionner via la carte",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.textPrimary
        )
        
        Text(
            text = "Utilisez OpenStreetMap pour rechercher une adresse",
            fontSize = 13.sp,
            color = AppTheme.textSecondary
        )
        
        // Search field
        OutlinedTextField(
            value = addressSearchQuery,
            onValueChange = {
                onAddressSearchQueryChange(it)
                if (it.isEmpty()) {
                    onClearSearch()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                Text(
                    "Ex: Tunis, Avenue Habib Bourguiba",
                    color = AppTheme.textSecondary.copy(alpha = 0.6f)
                ) 
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
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
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Effacer",
                            tint = AppTheme.textSecondary
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppTheme.card,
                unfocusedContainerColor = AppTheme.card,
                focusedBorderColor = AppTheme.primary,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = AppTheme.textPrimary,
                unfocusedTextColor = AppTheme.textPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
        
        // Search button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onSearch,
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
        
        // Search error
        searchError?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = Color.Red
            )
        }
        
        // Search results
        if (searchResults.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                searchResults.forEach { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectResult(result) },
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
        
        // Map view
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.matchParentSize()
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
                
                map.invalidate()
            }
            
            // Address card overlay
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
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Annuler", maxLines = 1)
                        }
                        
                        Button(
                            onClick = onConfirm,
                            enabled = selectedMapAddress.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Confirmer", maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Error Banner
@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Red.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                fontSize = 13.sp,
                color = Color.Red
            )
        }
    }
}

// MARK: - Save Button
@Composable
private fun SaveButton(
    isLoading: Boolean,
    isEdit: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isLoading) AppTheme.primary.copy(alpha = 0.7f) else AppTheme.primary,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (isEdit) Icons.Default.Refresh else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Text(
                text = when {
                    isLoading -> "Enregistrement..."
                    isEdit -> "Enregistrer les modifications"
                    else -> "Publier l'annonce"
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}
