package com.bikeshare.app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bikeshare.app.R
import com.bikeshare.app.notification.FreeTimeNotificationScheduler
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.graphics.drawable.GradientDrawable
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onScanQr: () -> Unit,
    qrSavedStateHandle: SavedStateHandle?,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Location permission
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.returnResult) {
        uiState.returnResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearReturnResult()
        }
    }

    val appContext = context.applicationContext
    LaunchedEffect(uiState.myBikes, uiState.limits) {
        val bikes = uiState.myBikes
        val limits = uiState.limits
        val freeMinutes = limits?.freeTimeMinutes ?: 30
        if (bikes.isEmpty()) {
            FreeTimeNotificationScheduler.cancelAll(appContext)
        } else {
            bikes.forEach { bike ->
                val rentedSec = bike.rentedSeconds ?: 0
                FreeTimeNotificationScheduler.schedule(appContext, bike.bikeNum, rentedSec, freeMinutes)
            }
        }
    }

    val qrAction = qrSavedStateHandle?.get<String>("qr_action")
    val qrBikeNumber = qrSavedStateHandle?.get<Int>("qr_bike_number")
    val qrStandName = qrSavedStateHandle?.get<String>("qr_stand_name")
    val qrSnackbar = qrSavedStateHandle?.get<String>("snackbar")
    LaunchedEffect(qrAction, qrBikeNumber, qrStandName, qrSnackbar) {
        qrSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            qrSavedStateHandle?.remove<String>("snackbar")
        }
        if (qrAction == null) return@LaunchedEffect
        when (qrAction) {
            "rent" -> qrBikeNumber?.let { viewModel.rentBike(it) }
            "return" -> {
                val stand = qrStandName ?: return@LaunchedEffect
                val myBikes = uiState.myBikes
                if (myBikes.size == 1) {
                    viewModel.returnBike(myBikes.first().bikeNum, stand)
                } else {
                    snackbarHostState.showSnackbar("Scanned stand: $stand. Return only when 1 bike is rented.")
                }
            }
        }
        qrSavedStateHandle?.remove<String>("qr_action")
        qrSavedStateHandle?.remove<Int>("qr_bike_number")
        qrSavedStateHandle?.remove<String>("qr_stand_name")
    }

    val mapViewRef = remember { mutableStateOf<org.osmdroid.views.MapView?>(null) }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasLocationPermission) {
                    FloatingActionButton(
                        onClick = {
                            val mapView = mapViewRef.value
                            val overlay = mapView?.overlays?.find { it is MyLocationNewOverlay } as? MyLocationNewOverlay
                            val geo = overlay?.myLocation
                            if (geo != null) {
                                mapView?.controller?.animateTo(geo)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.my_location))
                    }
                }
                FloatingActionButton(
                    onClick = onScanQr,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = stringResource(R.string.qr_scan))
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val bikesAvailableText = stringResource(R.string.bikes_available)
            val serviceStandLabel = stringResource(R.string.service_stand)

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(16.0)
                        controller.setCenter(GeoPoint(48.1486, 17.1077))

                        // User location overlay
                        if (hasLocationPermission) {
                            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                            locationOverlay.enableMyLocation()
                            locationOverlay.enableFollowLocation()
                            overlays.add(locationOverlay)
                        }
                    }
                },
                update = { mapView ->
                    mapViewRef.value = mapView
                    mapView.overlays.removeAll { it is Marker }
                    uiState.stands.forEach { stand ->
                        val isService = stand.serviceTag != null && stand.serviceTag != 0
                        val (markerColor, label) = if (isService) {
                            AndroidColor.parseColor("#FF9800") to "S" // orange for service
                        } else {
                            val bikes = stand.bikeCount ?: 0
                            (if (bikes > 0) AndroidColor.parseColor("#4CAF50") else AndroidColor.parseColor("#F44336")) to bikes.toString()
                        }
                        val markerIcon = createCircleMarker(mapView.context, markerColor, label)
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(stand.latitude, stand.longitude)
                            title = stand.standName + if (isService) " ($serviceStandLabel)" else ""
                            snippet = if (isService) serviceStandLabel else "$label $bikesAvailableText"
                            icon = markerIcon
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            setOnMarkerClickListener { _, _ ->
                                viewModel.selectStand(stand)
                                showBottomSheet = true
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                },
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }

        // Stand detail bottom sheet
        val selectedStand = uiState.selectedStand
        if (showBottomSheet && selectedStand != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.clearSelectedStand()
                },
                sheetState = sheetState,
            ) {
                StandBottomSheet(
                    stand = selectedStand,
                    bikes = uiState.standBikes,
                    myBikes = uiState.myBikes,
                    onRentBike = { bikeNumber ->
                        viewModel.rentBike(bikeNumber)
                        showBottomSheet = false
                    },
                    onReturnBike = { bikeNumber ->
                        viewModel.returnBike(bikeNumber, selectedStand.standName)
                        showBottomSheet = false
                    },
                )
            }
        }

        // Rent code dialog (params only, no HTML)
        uiState.rentCodeInfo?.let { rentInfo ->
            AlertDialog(
                onDismissRequest = { viewModel.clearRentCodeInfo() },
                title = { Text(stringResource(R.string.rent_button)) },
                text = {
                    Column {
                        uiState.rentCodeMessage?.takeIf { it.isNotBlank() }?.let { Text(it) }
                        rentInfo.params?.currentCode?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🔓 ${stringResource(R.string.lock_code, it)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        rentInfo.params?.newCode?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🔒 New code: $it",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.clearRentCodeInfo() }) {
                        Text(stringResource(R.string.ok))
                    }
                },
            )
        }
    }
}

private fun createCircleMarker(context: android.content.Context, color: Int, text: String): BitmapDrawable {
    val size = (40 * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, circlePaint)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f * context.resources.displayMetrics.density
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - borderPaint.strokeWidth / 2, borderPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        textSize = 14f * context.resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(text, size / 2f, textY, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}
