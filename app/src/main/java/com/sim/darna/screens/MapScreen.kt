package com.sim.darna.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OtherHouses
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.sim.darna.components.PropertyImageView
import com.sim.darna.model.Property
import com.sim.darna.navigation.Routes
import com.sim.darna.ui.theme.AppTheme
import com.sim.darna.viewmodel.PropertyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current

    val viewModel: PropertyViewModel = remember {
        PropertyViewModel(context).apply {
            init(null)
            loadProperties()
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val properties = uiState.filteredProperties
    val markerColor = AppTheme.primary.toArgb()

    // User location permission and last known position
    var hasLocationPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasLocationPermission = granted
    }

    // Ask for location permission on first composition if not granted
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        hasLocationPermission = granted
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Once we have permission, try to fetch last known location
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            userLocation = getLastKnownLocation(context)
        }
    }

    // Map of "location string" -> GeoPoint resolved via Nominatim
    var geoPoints by remember { mutableStateOf<Map<String, GeoPoint>>(emptyMap()) }
    var selectedProperty by remember { mutableStateOf<Property?>(null) }

    // Resolve textual locations to coordinates when properties change
    LaunchedEffect(properties) {
        val current = geoPoints.toMutableMap()
        val locationsToResolve = properties
            .mapNotNull { it.location }
            .distinct()
            .filter { it.isNotBlank() && !current.containsKey(it) }

        locationsToResolve.forEach { locationText ->
            geocodeLocation(locationText, context.packageName)?.let { point ->
                current[locationText] = point
            }
        }
        geoPoints = current
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Carte des annonces",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = "${properties.size} résultats autour de vous",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* could open filters later */ }) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "Recentrer la carte",
                            tint = Color.White,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.primary,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppTheme.background),
        ) {
            // Map card with rounded corners and shadow
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // OpenStreetMap (osmdroid) map view
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                                setMultiTouchControls(true)
                                controller.setZoom(13.0)
                                // Default center: Tunis, Tunisia
                                controller.setCenter(GeoPoint(36.8065, 10.1815))
                            }
                        },
                        update = { mapView ->
                             // Clear previous markers
                             mapView.overlays.clear()

                             // Group properties by textual location so we can handle multiple
                             // annonces at the exact same place.
                             val groupedByLocation: Map<String, List<Property>> =
                                 properties.groupBy { it.location.orEmpty() }

                             // Build list of (property, GeoPoint) for those that were geocoded,
                             // spreading markers slightly when there are multiple at same location.
                             val itemsWithPoints = mutableListOf<Pair<Property, GeoPoint>>()
                             groupedByLocation.forEach { (loc, group) ->
                                 if (loc.isBlank()) return@forEach
                                 val center = geoPoints[loc] ?: return@forEach

                                 if (group.size == 1) {
                                     itemsWithPoints += group.first() to center
                                 } else {
                                     // Spread markers in a small circle around the center (≈20m radius)
                                     val radiusMeters = 20.0
                                     val radiusLat = radiusMeters / 111_000.0
                                     val radiusLon =
                                         radiusMeters / (111_000.0 * cos(center.latitude * PI / 180.0))

                                     group.forEachIndexed { index, property ->
                                         val angle = 2.0 * PI * index / group.size
                                         val latOffset = radiusLat * cos(angle)
                                         val lonOffset = radiusLon * sin(angle)
                                         val point = GeoPoint(
                                             center.latitude + latOffset,
                                             center.longitude + lonOffset,
                                         )
                                         itemsWithPoints += property to point
                                     }
                                 }
                             }

                            // Decide center: user location first, then first property, then Tunis
                            val centerPoint = userLocation
                                ?: itemsWithPoints.firstOrNull()?.second
                                ?: GeoPoint(36.8065, 10.1815)
                            mapView.controller.setCenter(centerPoint)

                            // Optional: show user's current location as small marker
                            userLocation?.let { myPoint ->
                                val meMarker = Marker(mapView).apply {
                                    position = myPoint
                                    title = "Vous êtes ici"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                }
                                mapView.overlays.add(meMarker)
                            }

                            // Add markers with custom price bubble icon that matches theme
                            itemsWithPoints.forEach { (property, point) ->
                                val label = "${property.price.toInt()} DT"
                                val markerIcon = createPriceMarkerIcon(mapView.context, label, markerColor)

                                val marker = Marker(mapView).apply {
                                    position = point
                                    icon = markerIcon
                                    title = label
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    setOnMarkerClickListener { _, _ ->
                                        selectedProperty = property
                                        mapView.controller.animateTo(point)
                                        true
                                    }
                                }

                                mapView.overlays.add(marker)
                            }

                            mapView.invalidate()
                        },
                    )

                    // Subtle gradient at top for readability over map tiles
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                }
            }

            // Bottom card showing selected property details
            selectedProperty?.let { property ->
                PropertyBottomCard(
                    property = property,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                ) {
                    navController.navigate("${Routes.PropertyDetail}/${property.id}")
                }
            }
        }
    }
}

@Composable
private fun PropertyBottomCard(
    property: Property,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Image on the left
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                tonalElevation = 2.dp,
            ) {
                PropertyImageView(
                    imageString = property.getFirstImage(),
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth(fraction = 0.0f)
                        .width(70.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "${property.price.toInt()} DT / mois",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.primary,
                )
                Text(
                    text = property.location ?: "Lieu non spécifié",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.textPrimary,
                    maxLines = 1,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = AppTheme.primary,
                    )
                    Text(
                        text = "Voir les détails de l'annonce",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

// --- Simple OpenStreetMap (Nominatim) geocoding + custom marker icon ---

private val geoClient: OkHttpClient by lazy {
    OkHttpClient.Builder().build()
}

private suspend fun geocodeLocation(query: String, userAgent: String): GeoPoint? =
    withContext(Dispatchers.IO) {
        try {
            val url: HttpUrl = HttpUrl.Builder()
                .scheme("https")
                .host("nominatim.openstreetmap.org")
                .addPathSegment("search")
                .addQueryParameter("q", query)
                .addQueryParameter("format", "json")
                .addQueryParameter("limit", "1")
                .build()

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent.ifBlank { "darna-android" })
                .build()

            geoClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val array = JSONArray(body)
                if (array.length() == 0) return@withContext null
                val obj = array.getJSONObject(0)
                val lat = obj.getString("lat").toDoubleOrNull() ?: return@withContext null
                val lon = obj.getString("lon").toDoubleOrNull() ?: return@withContext null
                GeoPoint(lat, lon)
            }
        } catch (_: Exception) {
            null
        }
    }

// Cache icons per label so we don't recreate bitmaps every time
private val priceMarkerCache = mutableMapOf<String, BitmapDrawable>()

private fun createPriceMarkerIcon(context: Context, label: String, bubbleColor: Int): BitmapDrawable {
    val cacheKey = "$label-$bubbleColor"
    priceMarkerCache[cacheKey]?.let { return it }

    val paddingH = 24f
    val paddingV = 12f
    val cornerRadius = 16f
    val pointerHeight = 16f

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val textWidth = textPaint.measureText(label)
    val textHeight = textPaint.fontMetrics.run { bottom - top }

    val bubbleWidth = textWidth + paddingH * 2
    val bubbleHeight = textHeight + paddingV * 2

    val bitmapWidth = bubbleWidth
    val bitmapHeight = bubbleHeight + pointerHeight

    val bitmap = Bitmap.createBitmap(
        bitmapWidth.toInt(),
        bitmapHeight.toInt(),
        Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(bitmap)

    // Draw rounded rect bubble (black)
    val bubbleRect = RectF(
        0f,
        0f,
        bubbleWidth,
        bubbleHeight,
    )
    val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bubbleColor
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(bubbleRect, cornerRadius, cornerRadius, bubblePaint)

    // Draw pointer triangle
    val centerX = bubbleRect.centerX()
    val path = Path().apply {
        moveTo(centerX - pointerHeight, bubbleHeight)
        lineTo(centerX + pointerHeight, bubbleHeight)
        lineTo(centerX, bubbleHeight + pointerHeight)
        close()
    }
    canvas.drawPath(path, bubblePaint)

    // Draw text centered in bubble
    val textX = (bubbleWidth - textWidth) / 2f
    val textBaseline = paddingV + (textHeight - textPaint.fontMetrics.bottom)
    canvas.drawText(label, textX, textBaseline, textPaint)

    val drawable = BitmapDrawable(context.resources, bitmap)
    priceMarkerCache[cacheKey] = drawable
    return drawable
}

// Read best-effort last known location from system providers
@Suppress("MissingPermission")
private fun getLastKnownLocation(context: Context): GeoPoint? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        ?: return null

    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    )

    var bestLocation: Location? = null
    for (provider in providers) {
        val loc = try {
            locationManager.getLastKnownLocation(provider)
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }

        if (loc != null && (bestLocation == null || loc.time > bestLocation!!.time)) {
            bestLocation = loc
        }
    }

    return bestLocation?.let { GeoPoint(it.latitude, it.longitude) }
}
