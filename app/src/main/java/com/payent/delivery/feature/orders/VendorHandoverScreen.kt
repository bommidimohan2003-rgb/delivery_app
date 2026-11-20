package com.payent.delivery.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.core.ui.components.EditorialDualLocationCard
import com.payent.delivery.core.ui.components.EditorialMapMode
import com.payent.delivery.core.ui.components.PayentEditorialMapView
import com.payent.delivery.core.ui.components.SixDigitOtpInput
import com.payent.delivery.domain.model.OrderStatus
import kotlinx.coroutines.launch

/**
 * SCREEN 3 & SCREEN 4 — Arrived at Vendor & Vendor Verification (Two Buttons)
 * Matches reference screens 3 and 4 with pixel-level editorial fidelity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorHandoverScreen(
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    onNavigateToHelp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrderForDetails ?: OrdersViewModel.sampleDemoOrders.first()

    val isPinVerified = uiState.isVendorPinSimulated || order.status == OrderStatus.VENDOR_PIN_SIMULATED
    val isOtpCompleted = uiState.isPickupCompleted || order.status == OrderStatus.DEMO_PICKUP_COMPLETED || order.status == OrderStatus.PICKED_UP

    var hasArrived by remember {
        mutableStateOf(
            uiState.isArrivedAtVendor ||
            order.status == OrderStatus.ARRIVED_AT_VENDOR ||
            isPinVerified ||
            isOtpCompleted
        )
    }

    var showPinRequestDialog by remember { mutableStateOf(false) }
    var showOtpSheet by remember { mutableStateOf(false) }
    var sequenceNotice by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // -------------------------------------------------------------------------
    // Dialog: Vendor Secret PIN Request Simulation (Delivery partner does NOT enter PIN)
    // -------------------------------------------------------------------------
    if (showPinRequestDialog) {
        AlertDialog(
            onDismissRequest = { showPinRequestDialog = false },
            title = {
                Text(
                    text = "Request Secret PIN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            },
            text = {
                Column {
                    Text(
                        text = "A secret PIN request was sent to the vendor's Payent app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF111111)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The vendor enters their 4-digit secret PIN in their own Payent app. The delivery partner does not collect or view this PIN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF626773)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Demo Simulation: In production, the vendor app notifies Payent servers. In demo mode, simulate the vendor submitting PIN '0429'.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF626773)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.simulateVendorPinEnteredInVendorApp(order.id)
                        showPinRequestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simulate Vendor Entering PIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinRequestDialog = false }) {
                    Text("Close", color = Color(0xFF626773))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // -------------------------------------------------------------------------
    // Modal Bottom Sheet: Vendor OTP Verification
    // -------------------------------------------------------------------------
    if (showOtpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOtpSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Vendor OTP",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ask the vendor for the 6-digit OTP sent to their mobile number (+91 98840 XXXXX).",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = Color(0xFF626773)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                SixDigitOtpInput(
                    otp = uiState.vendorOtpInput,
                    onOtpChange = { viewModel.onVendorOtpChanged(it) },
                    isError = uiState.vendorOtpError != null
                )

                if (uiState.vendorOtpError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.vendorOtpError ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Demo Autofill Helper
                OutlinedButton(
                    onClick = { viewModel.simulateVendorOtpFilled() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF111111))
                ) {
                    Text("Fill Demo OTP (982143)", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Verify & Complete Button
                Button(
                    onClick = {
                        val success = viewModel.verifyVendorOtp(order) {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showOtpSheet = false
                                onNavigateToResult()
                            }
                        }
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
                    Text(
                        text = "Verify OTP & Complete Pickup",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFFFF)
    ) {
        if (!hasArrived) {
            // =================================================================
            // SCREEN 3 — Arrived at Vendor
            // =================================================================
            Box(modifier = Modifier.fillMaxSize()) {
                // Editorial Map centered on destination with arrival radar zone
                PayentEditorialMapView(
                    mode = EditorialMapMode.ARRIVED_AT_VENDOR,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Bar & Floating Location Strip
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
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
                            text = "Arrived at Vendor",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            ),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.size(40.dp)) // Spacer for centering
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Floating Dual Location Card (From Your Location -> To Vendor)
                    EditorialDualLocationCard(
                        fromLabel = "From",
                        fromAddress = "Your Location",
                        toAddress = "${order.vendorAddress} (Vendor)",
                        fromDotColor = Color(0xFF2563EB),
                        showTransportSelector = false,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Bottom Overlay: Vendor Location Card & Green I've Arrived Button
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
                        // Vendor Info Card Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF5F6F7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Storefront,
                                    contentDescription = null,
                                    tint = Color(0xFF111111),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vendor Location",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111111)
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = order.vendorAddress,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.sp,
                                        color = Color(0xFF626773)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Green I've Arrived → Button (matching prompt specification for Screen 3)
                        Button(
                            onClick = {
                                viewModel.markArrivedAtVendor(order)
                                hasArrived = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF000000), // Primary black per reference image or green
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "I've Arrived",
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
        } else {
            // =================================================================
            // SCREEN 4 — Vendor Verification: Two Buttons
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header: Back arrow & Centered Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { hasArrived = false },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111111)
                        )
                    }

                    Text(
                        text = "Vendor Verification",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Introductory text
                Text(
                    text = "Complete verification to pick up the product.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = Color(0xFF626773)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Notice Banner (if any)
                if (sequenceNotice != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = sequenceNotice ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Exactly TWO Black Action Cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ---------------------------------------------------------
                    // BUTTON 1 — Request Secret PIN
                    // ---------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF111111))
                            .clickable {
                                sequenceNotice = null
                                viewModel.sendPinRequestToVendor(order)
                                showPinRequestDialog = true
                            }
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Lock Icon in circular container
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF262626)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPinVerified) Icons.Filled.Check else Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = if (isPinVerified) Color(0xFF22C55E) else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Text details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Request Secret PIN",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isPinVerified) "Vendor secret PIN verified ✓" else "Send request to vendor on Payent app for secret PIN.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = if (isPinVerified) Color(0xFF4ADE80) else Color(0xFF9CA3AF)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Icon(
                                imageVector = if (isPinVerified) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isPinVerified) Color(0xFF22C55E) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // ---------------------------------------------------------
                    // BUTTON 2 — Request OTP
                    // ---------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF111111))
                            .clickable {
                                if (!isPinVerified) {
                                    sequenceNotice = "First request Secret PIN, then request OTP to complete pickup verification."
                                } else {
                                    sequenceNotice = null
                                    viewModel.sendOtpToVendor()
                                    showOtpSheet = true
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Message Icon in circular container
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF262626)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOtpCompleted) Icons.Filled.Check else Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isOtpCompleted) Color(0xFF22C55E) else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Text details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Request OTP",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isOtpCompleted) "Vendor OTP verified ✓" else "Send OTP to vendor's registered mobile number.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = if (isOtpCompleted) Color(0xFF4ADE80) else Color(0xFF9CA3AF)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Icon(
                                imageVector = if (isOtpCompleted) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isOtpCompleted) Color(0xFF22C55E) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // Information Strip at Bottom
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFF626773),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "First request Secret PIN, then request OTP to complete pickup verification.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = Color(0xFF626773),
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}
