package com.payent.delivery.feature.orders

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.core.location.LocationHelper
import com.payent.delivery.core.ui.components.EditorialDualLocationCard
import com.payent.delivery.core.ui.components.EditorialMapMode
import com.payent.delivery.core.ui.components.PayentEditorialMapView
import com.payent.delivery.core.ui.components.TransportMode

/**
 * SCREEN 6 — Navigate to Delivery
 * Editorial design matching Screen 6 in reference image.
 * Route strictly Vendor (Picked up) -> Renter.
 */
@Composable
fun DeliveryScreen(
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRenterVerification: () -> Unit,
    onNavigateToHelp: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrderForDetails ?: OrdersViewModel.sampleDemoOrders.first()
    val displayInfo = getOrderDisplayInfo(order)

    var selectedMode by remember { mutableStateOf(TransportMode.MOTORCYCLE) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        LocationHelper.getCurrentOrLastKnownLocation(context) { locationState ->
            viewModel.updateLocationState(
                lat = locationState.latitude,
                lon = locationState.longitude,
                address = locationState.resolvedAddress,
                hasPermission = locationState.hasPermission,
                isGpsEnabled = locationState.isGpsEnabled,
                errorMessage = locationState.errorMessage
            )
        }
    }

    LaunchedEffect(Unit) {
        LocationHelper.getCurrentOrLastKnownLocation(context) { locationState ->
            viewModel.updateLocationState(
                lat = locationState.latitude,
                lon = locationState.longitude,
                address = locationState.resolvedAddress,
                hasPermission = locationState.hasPermission,
                isGpsEnabled = locationState.isGpsEnabled,
                errorMessage = locationState.errorMessage
            )
        }
    }

    val fromPickedUpText = "${order.vendorAddress} (Picked up)"
    val toRenterText = "${order.deliveryAddress} (Renter)"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFFFF)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full screen Editorial Map (Vendor -> Renter)
            PayentEditorialMapView(
                mode = EditorialMapMode.NAVIGATE_TO_DELIVERY,
                modifier = Modifier.fillMaxSize(),
                onRecenterClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            // Top Overlay: App Bar and Floating Dual Location Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                // Top Bar: Back arrow, Centered Title, Settings icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(3.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111111)
                        )
                    }

                    Text(
                        text = "Navigate to Delivery",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        ),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = onNavigateToHelp,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(3.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF111111)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Floating Dual Location Card (From Vendor [Picked up] -> To Renter)
                EditorialDualLocationCard(
                    fromLabel = "From",
                    fromAddress = fromPickedUpText,
                    toAddress = toRenterText,
                    fromDotColor = Color(0xFF16A34A), // Green dot for picked up origin
                    showTransportSelector = true,
                    selectedMode = selectedMode,
                    onModeSelected = { selectedMode = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bottom Overlay: Travel Summary Card & Start Navigation Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.12f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    // Travel stats: 18 min, 6.8 km, Moderate traffic
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "18 min",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111111)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayInfo.deliveryDistance, // e.g. 6.8 km
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF626773)
                                    ),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Fastest route • Moderate traffic",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF626773)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent Black Start Navigation → Button
                    Button(
                        onClick = {
                            viewModel.startDeliveryJourney(order)
                            onNavigateToRenterVerification()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF000000),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Start Navigation",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
