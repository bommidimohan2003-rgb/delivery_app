package com.payent.delivery.core.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.payent.delivery.ui.theme.StatusWarning
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayentRealMapView(
    destinationLatLng: LatLng,
    destinationTitle: String,
    destinationSnippet: String,
    modifier: Modifier = Modifier,
    originLatLng: LatLng? = LatLng(12.9600, 77.6000), // Default partner location
    originTitle: String = "Partner Start Location",
    isPickupMap: Boolean = true,
    journeyStatusText: String = "READY FOR NAVIGATION",
    onStartNavigationClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isMapError by remember { mutableStateOf(false) }
    var showDirectionsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destinationLatLng, 14f)
    }
    val coroutineScope = rememberCoroutineScope()

    // Route points for polyline over real map
    val routePoints = remember(destinationLatLng, originLatLng) {
        if (originLatLng != null) {
            listOf(
                originLatLng,
                LatLng((originLatLng.latitude + destinationLatLng.latitude) / 2.0 + 0.005, (originLatLng.longitude + destinationLatLng.longitude) / 2.0),
                destinationLatLng
            )
        } else {
            listOf(destinationLatLng)
        }
    }

    // Auto-frame camera to fit origin and destination
    LaunchedEffect(destinationLatLng, originLatLng) {
        try {
            if (originLatLng != null) {
                val bounds = LatLngBounds.builder()
                    .include(destinationLatLng)
                    .include(originLatLng)
                    .build()
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            } else {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15f))
            }
        } catch (_: Exception) {
            // Fallback zoom if bounds calculation fails in preview
        }
    }

    // Directions Bottom Sheet
    if (showDirectionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDirectionsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Directions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Real Map Route Directions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    StatusTag(text = "LIVE MAP DATA", color = StatusWarning)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Destination: $destinationTitle ($destinationSnippet)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action to launch external Google Maps Navigation Intent
                PayentPrimaryButton(
                    text = "Open in Google Maps Navigation",
                    onClick = {
                        try {
                            val gmmIntentUri = Uri.parse("google.navigation:q=${destinationLatLng.latitude},${destinationLatLng.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {
                            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${destinationLatLng.latitude},${destinationLatLng.longitude}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PayentSecondaryButton(
                    text = "Close Directions",
                    onClick = { showDirectionsSheet = false }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isMapError) {
                // Fallback Notice Card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Real Map View",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Google Maps API Key is unconfigured. Set MAPS_API_KEY in AndroidManifest.xml to render map tiles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Real Google Map View
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false,
                        compassEnabled = true
                    ),
                    properties = MapProperties(
                        isMyLocationEnabled = false
                    )
                ) {
                    // Destination Marker
                    Marker(
                        state = MarkerState(position = destinationLatLng),
                        title = destinationTitle,
                        snippet = destinationSnippet
                    )

                    // Origin Marker
                    if (originLatLng != null) {
                        Marker(
                            state = MarkerState(position = originLatLng),
                            title = originTitle,
                            snippet = "Start Position (Demo)"
                        )
                    }

                    // Real Route Polyline Overlay
                    if (routePoints.size > 1) {
                        Polyline(
                            points = routePoints,
                            color = MaterialTheme.colorScheme.primary,
                            width = 10f
                        )
                    }
                }

                // Top Floating Header Overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPickupMap) Icons.Outlined.Storefront else Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = if (isPickupMap) "REAL MAP — VENDOR PICKUP" else "REAL MAP — RENTER DELIVERY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Action Controls Overlay (Right Side)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Directions Button
                    FloatingActionButton(
                        onClick = { showDirectionsSheet = true },
                        modifier = Modifier.size(42.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Directions,
                            contentDescription = "Directions Button"
                        )
                    }

                    // Recenter Camera Button
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                if (originLatLng != null) {
                                    val bounds = LatLngBounds.builder()
                                        .include(destinationLatLng)
                                        .include(originLatLng)
                                        .build()
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 120))
                                } else {
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15f))
                                }
                            }
                        },
                        modifier = Modifier.size(42.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MyLocation,
                            contentDescription = "Recenter Camera"
                        )
                    }
                }

                // Bottom Left Tag
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "Real Google Map Tiles • Interactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
