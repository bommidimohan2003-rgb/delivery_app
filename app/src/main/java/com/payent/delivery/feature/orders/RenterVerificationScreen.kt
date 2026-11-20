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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
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
 * SCREEN 7 & SCREEN 8 — Arrived at Renter & Customer Verification (Two Buttons)
 * Matches reference screens 7 and 8 with pixel-level editorial fidelity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenterVerificationScreen(
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit = {},
    onNavigateToInspection: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrderForDetails ?: OrdersViewModel.sampleDemoOrders.first()

    val isOtpVerified = uiState.isRenterOtpVerified || order.status == OrderStatus.RENTER_OTP_DEMO_COMPLETED
    val isPinVerified = uiState.isRenterPinSimulated || order.status == OrderStatus.DELIVERY_DEMO_COMPLETED || order.status == OrderStatus.DELIVERED

    var hasArrived by remember {
        mutableStateOf(
            uiState.isArrivedAtRenter ||
            order.status == OrderStatus.ARRIVED_AT_RENTER ||
            isOtpVerified ||
            isPinVerified
        )
    }

    var showOtpSheet by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var sequenceNotice by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // -------------------------------------------------------------------------
    // Modal Bottom Sheet: Customer OTP Verification (Button 1 in Screen 8)
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
                    text = "Enter Customer OTP",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ask the customer for the 6-digit OTP sent to their mobile number (+91 97721 XXXXX).",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = Color(0xFF626773)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                SixDigitOtpInput(
                    otp = uiState.renterOtpInput,
                    onOtpChange = { viewModel.onRenterOtpChanged(it) },
                    isError = uiState.renterOtpError != null
                )

                if (uiState.renterOtpError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.renterOtpError ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Demo Autofill Helper
                OutlinedButton(
                    onClick = { viewModel.simulateRenterOtpFilled() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF111111))
                ) {
                    Text("Fill Demo OTP (654321)", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Verify Button
                Button(
                    onClick = {
                        val success = viewModel.verifyRenterOtp(order) {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showOtpSheet = false
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
                        text = "Verify Customer OTP",
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

    // -------------------------------------------------------------------------
    // Dialog: Customer Secret PIN Request Simulation (Button 2 in Screen 8)
    // -------------------------------------------------------------------------
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
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
                        text = "A secret PIN request has been sent to the customer's Payent app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF111111)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The customer enters their 4-digit secret PIN inside their own Payent app. The delivery partner does NOT enter or view this PIN.",
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
                            text = "Demo Simulation: In demo mode, simulate the customer confirming delivery with PIN '0821' in their Payent app.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF626773)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.simulateRenterPinVerified(order)
                        showPinDialog = false
                        onNavigateToResult()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simulate Customer Entering PIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Close", color = Color(0xFF626773))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFFFF)
    ) {
        if (!hasArrived) {
            // =================================================================
            // SCREEN 7 — Arrived at Renter
            // =================================================================
            Box(modifier = Modifier.fillMaxSize()) {
                // Editorial Map centered on Renter destination
                PayentEditorialMapView(
                    mode = EditorialMapMode.ARRIVED_AT_RENTER,
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
                            text = "Arrived at Renter",
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Floating Dual Location Card (From Your Location -> To Renter)
                    EditorialDualLocationCard(
                        fromLabel = "From",
                        fromAddress = "Your Location",
                        toAddress = "${order.deliveryAddress} (Renter)",
                        fromDotColor = Color(0xFF2563EB),
                        showTransportSelector = false,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Bottom Overlay: Renter Information Card & Black I've Arrived Button
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
                        // Renter Location Row
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
                                    imageVector = Icons.Outlined.Home,
                                    contentDescription = null,
                                    tint = Color(0xFF111111),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Renter Location",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111111)
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = order.deliveryAddress,
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

                        // Black I've Arrived → Button
                        Button(
                            onClick = {
                                viewModel.markArrivedAtRenter(order)
                                hasArrived = true
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
            // SCREEN 8 — Customer Verification: Two Buttons
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
                        text = "Customer Verification",
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
                    text = "Complete verification to mark delivery.",
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
                    // BUTTON 1 — Request OTP (In customer verification, OTP is first)
                    // ---------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF111111))
                            .clickable {
                                sequenceNotice = null
                                viewModel.sendOtpToRenter()
                                showOtpSheet = true
                            }
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Message Icon in circle
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF262626)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOtpVerified) Icons.Filled.Check else Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isOtpVerified) Color(0xFF22C55E) else Color.White,
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
                                    text = if (isOtpVerified) "Customer OTP verified ✓" else "Send OTP to customer's registered mobile number.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = if (isOtpVerified) Color(0xFF4ADE80) else Color(0xFF9CA3AF)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Icon(
                                imageVector = if (isOtpVerified) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isOtpVerified) Color(0xFF22C55E) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // ---------------------------------------------------------
                    // BUTTON 2 — Request Secret PIN
                    // ---------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF111111))
                            .clickable {
                                if (!isOtpVerified) {
                                    sequenceNotice = "First request OTP, then request Secret PIN to complete delivery."
                                } else {
                                    sequenceNotice = null
                                    showPinDialog = true
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Lock Icon in circle
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
                                    text = if (isPinVerified) "Customer secret PIN verified ✓" else "Send request to customer on Payent app for secret PIN.",
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
                        text = "First request OTP, then request Secret PIN to complete delivery.",
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
