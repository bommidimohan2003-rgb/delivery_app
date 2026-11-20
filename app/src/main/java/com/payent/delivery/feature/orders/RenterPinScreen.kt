package com.payent.delivery.feature.orders

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkBorder
import com.payent.delivery.ui.theme.DarkBorderSubtle
import com.payent.delivery.ui.theme.DarkSurface
import com.payent.delivery.ui.theme.DarkSurfaceVariant
import com.payent.delivery.ui.theme.PrimaryBlack
import com.payent.delivery.ui.theme.PrimaryWhite
import com.payent.delivery.ui.theme.StatusError
import com.payent.delivery.ui.theme.TextPrimaryDark
import com.payent.delivery.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Customer Handover PIN Screen.
 * Streamlined to eliminate unwanted instructional text, banners, and redundant steps,
 * preserving only the essential verification and handover action buttons.
 * Fully theme-aware: seamlessly transitions between dark and light themes.
 */
@Composable
fun RenterPinScreen(
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    onNavigateToHelp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrderForDetails ?: OrdersViewModel.sampleDemoOrders.first()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var isRequestingPin by remember { mutableStateOf(false) }
    var pinRequestSent by remember { mutableStateOf(false) }

    val isPinVerified = uiState.isRenterPinSimulated ||
            uiState.isDeliveryCompleted ||
            (uiState.customerPinInput.length == 4 && uiState.customerPinError == null)

    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface
    val screenBg = if (isDark) DarkBackground else Color(0xFFF9FAFB)
    val cardBg = if (isDark) DarkSurface else Color.White
    val cardBorder = if (isDark) DarkBorder else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) TextPrimaryDark else Color(0xFF111827)
    val textSecondary = if (isDark) TextSecondaryDark else Color(0xFF6B7280)

    val primaryBtnBg = if (isDark) PrimaryWhite else Color(0xFF111827)
    val primaryBtnText = if (isDark) PrimaryBlack else Color.White
    val disabledBtnBg = if (isDark) DarkSurfaceVariant else Color(0xFFE5E7EB)
    val disabledBtnText = if (isDark) DarkBorderSubtle else Color(0xFF9CA3AF)
    val demoBtnBg = if (isDark) DarkSurfaceVariant else Color(0xFFF3F4F6)
    val demoBtnBorder = if (isDark) DarkBorderSubtle else Color(0xFFE5E7EB)
    val demoBtnText = if (isDark) TextSecondaryDark else Color(0xFF4B5563)
    val outlinedBtnBg = if (isDark) DarkSurface else Color.White
    val outlinedBtnBorder = if (isDark) DarkBorder else Color(0xFFD1D5DB)
    val outlinedBtnText = if (isDark) TextPrimaryDark else Color(0xFF111827)

    val destinationAddress = if (order.orderNumber == "PAYENT-ORD-8822") {
        "Hotel Initiative Conference"
    } else {
        order.deliveryAddress
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        // 1. Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Customer Delivery Handover",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textPrimary
                    )
                )
            }

            IconButton(
                onClick = onNavigateToHelp,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Headset,
                    contentDescription = "Help",
                    tint = textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 2. Scrollable Content Area
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Order & Destination Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) DarkSurfaceVariant else Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Order #${order.orderNumber}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Destination: $destinationAddress",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                color = textSecondary
                            )
                        )
                    }
                }
            }

            // Problem Report Banner (if active)
            if (uiState.hasOpenProblemReport) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.35f) else Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFFDC2626) else Color(0xFFFECACA))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ReportProblem,
                            contentDescription = null,
                            tint = StatusError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.problemReportSummary ?: "Handover blocked by active problem report.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = StatusError
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Verification Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isDark) DarkSurfaceVariant else Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Key,
                            contentDescription = null,
                            tint = textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Customer Secret PIN Verification",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isPinVerified) {
                        // Button: Send notification for secret PIN to customer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryBtnBg)
                                .clickable(enabled = !isRequestingPin && !uiState.hasOpenProblemReport) {
                                    coroutineScope.launch {
                                        isRequestingPin = true
                                        delay(900)
                                        pinRequestSent = true
                                        delay(1000)
                                        viewModel.quickFillDemoCustomerPin()
                                        isRequestingPin = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isRequestingPin) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = primaryBtnText,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (pinRequestSent) "Awaiting Customer PIN in App..." else "Sending PIN Notification...",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = primaryBtnText
                                        )
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = primaryBtnText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Send Notification for Secret PIN",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = primaryBtnText
                                        )
                                    )
                                }
                            }
                        }

                        // Demo simulation chip for customer PIN approval
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(demoBtnBg)
                                .border(1.dp, demoBtnBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.quickFillDemoCustomerPin()
                                }
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Simulate Customer App Approval",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = demoBtnText
                                )
                            )
                        }
                    } else {
                        // Secret PIN Verified confirmation row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF0D2818) else Color(0xFFE8F5E9))
                                .border(1.dp, if (isDark) Color(0xFF1B4D2E) else Color(0xFFC8E6C9), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Customer Secret PIN Verified",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF166534)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 3. Bottom Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .border(BorderStroke(1.dp, cardBorder))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Button 1: Complete Delivery Handover
            val isButtonEnabled = isPinVerified && !uiState.hasOpenProblemReport
            Button(
                onClick = {
                    if (isButtonEnabled) {
                        val success = viewModel.verifyCustomerPin(order, pinOverride = "0821") {
                            onNavigateToResult()
                        }
                        if (success) {
                            onNavigateToResult()
                        }
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBtnBg,
                    contentColor = primaryBtnText,
                    disabledContainerColor = disabledBtnBg,
                    disabledContentColor = disabledBtnText
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if (isButtonEnabled) primaryBtnText else disabledBtnText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete Delivery Handover",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isButtonEnabled) primaryBtnText else disabledBtnText
                        )
                    )
                }
            }

            // Button 2: Back to Inspection
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, outlinedBtnBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = outlinedBtnBg,
                    contentColor = outlinedBtnText
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = outlinedBtnText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Back to Inspection",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = outlinedBtnText
                        )
                    )
                }
            }
        }
    }
}
