package com.payent.delivery.feature.orders

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.core.ui.components.EditorialMapMode
import com.payent.delivery.core.ui.components.PayentEditorialMapView
import com.payent.delivery.domain.model.OrderStatus

/**
 * SCREEN 1 — Order Details
 * Editorial White and Black design matching reference image:
 * - Header with back arrow, centered title, #OD12846 badge
 * - Product card with black thumbnail container, dynamic product name, category, Rent & Ride tag, and ₹45/day
 * - Location rows with Pickup (Vendor), Drop (Renter), and Pickup Time
 * - Map preview with floating "↗ View Route" button and vertical zoom pill
 * - 3-Column Metric Stats Strip (2.4 km To Pickup | 6.8 km To Drop | 8 min Est. to Pickup)
 * - Prominent full-width black "Accept Order →" pill button
 */
@Composable
fun OrderDetailsScreen(
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPickupMap: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrderForDetails ?: OrdersViewModel.sampleDemoOrders.first()
    val displayInfo = getOrderDisplayInfo(order)
    val scrollState = rememberScrollState()

    // Acceptance Confirmation Dialog
    if (uiState.showAcceptConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelAcceptOrder() },
            title = {
                Text(
                    text = "Accept Order",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            },
            text = {
                Text(
                    text = "Accept order ${order.orderNumber} for delivery dispatch?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF626773)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmAcceptOrder {
                            onNavigateToPickupMap()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Accept Order", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelAcceptOrder() }) {
                    Text("Cancel", color = Color(0xFF626773))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // KYC Required Dialog
    if (uiState.showKycRequiredDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissKycRequiredDialog() },
            title = {
                Text(
                    text = "KYC Verification Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            },
            text = {
                Text(
                    text = "You must complete and verify your partner KYC before you can accept delivery orders.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF626773)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissKycRequiredDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Offline Warning Dialog
    if (uiState.showOfflineWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOfflineWarningDialog() },
            title = {
                Text(
                    text = "You're Offline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            },
            text = {
                Text(
                    text = "Go online to accept orders and receive delivery dispatches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF626773)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateOnlineStatus(true)
                        viewModel.dismissOfflineWarningDialog()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Go Online", fontWeight = FontWeight.Bold)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // -------------------------------------------------------------
            // Header: Back arrow, Centered "Order Details", Order ID badge
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111111)
                    )
                }

                Text(
                    text = "Order Details",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    ),
                    textAlign = TextAlign.Center
                )

                // Order ID Tag on the right (#OD12846)
                val orderIdFormatted = if (order.orderNumber.contains("8821")) {
                    "#OD12846"
                } else {
                    "#${order.orderNumber.replace("PAYENT-ORD-", "OD")}"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = orderIdFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // -------------------------------------------------------------
                // Product Card: Black box thumbnail, title, category, Rent & Ride tag, ₹45/day
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product image container (Black rounded container per reference image)
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF111111)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (displayInfo.drawableRes != null) {
                                Image(
                                    painter = painterResource(id = displayInfo.drawableRes),
                                    contentDescription = displayInfo.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    imageVector = displayInfo.icon,
                                    contentDescription = displayInfo.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Details: Name, Category, Rent & Ride pill
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayInfo.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111111)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = displayInfo.category,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    color = Color(0xFF626773),
                                    fontWeight = FontWeight.Normal
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // "Rent & Ride" pill tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Rent & Ride",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF111111)
                                    )
                                )
                            }
                        }

                        // Rental Price on the right (₹45/day)
                        val priceDayText = if (order.payoutAmount > 0) {
                            "₹${order.payoutAmount.toInt()}/day"
                        } else {
                            "₹45/day"
                        }
                        Text(
                            text = priceDayText,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // -------------------------------------------------------------
                // Order Information: Pickup, Drop, Pickup Time
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Pickup (Vendor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF111111),
                            modifier = Modifier
                                .size(22.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pickup (Vendor)",
                                style = MaterialTheme.typography.bodyMedium.copy(
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
                        Text(
                            text = displayInfo.pickupDistance, // e.g. 2.4 km
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF626773)
                            )
                        )
                    }

                    // 2. Drop (Renter)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF111111),
                            modifier = Modifier
                                .size(22.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Drop (Renter)",
                                style = MaterialTheme.typography.bodyMedium.copy(
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
                        Text(
                            text = displayInfo.deliveryDistance, // e.g. 6.8 km
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF626773)
                            )
                        )
                    }

                    // 3. Pickup Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF111111),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pickup Time",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111111)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = displayInfo.schedule,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    color = Color(0xFF626773)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // -------------------------------------------------------------
                // Map Preview: Route, concentric ring, black pin, View Route pill, controls
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                ) {
                    PayentEditorialMapView(
                        mode = EditorialMapMode.ORDER_DETAILS_PREVIEW,
                        modifier = Modifier.fillMaxSize(),
                        onViewRouteClick = onNavigateToPickupMap
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // -------------------------------------------------------------
                // Three-Column Metric Stats Strip (Right above Accept Order button)
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Metric 1: To Pickup (2.4 km)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Curved Route Icon
                            Canvas(modifier = Modifier.size(22.dp)) {
                                val path = Path().apply {
                                    moveTo(3.dp.toPx(), 18.dp.toPx())
                                    cubicTo(
                                        3.dp.toPx(), 10.dp.toPx(),
                                        19.dp.toPx(), 12.dp.toPx(),
                                        19.dp.toPx(), 4.dp.toPx()
                                    )
                                }
                                drawPath(
                                    path = path,
                                    color = Color(0xFF111111),
                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawCircle(color = Color(0xFF111111), radius = 3.dp.toPx(), center = Offset(3.dp.toPx(), 18.dp.toPx()))
                                drawCircle(color = Color(0xFF111111), radius = 3.dp.toPx(), center = Offset(19.dp.toPx(), 4.dp.toPx()))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = displayInfo.pickupDistance,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111111)
                                    )
                                )
                                Text(
                                    text = "To Pickup",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = Color(0xFF626773)
                                    )
                                )
                            }
                        }

                        // Divider 1
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(Color(0xFFE5E7EB))
                        )

                        // Metric 2: To Drop (6.8 km)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            // Highway / Road Icon
                            Canvas(modifier = Modifier.size(22.dp)) {
                                // Left tapered road edge
                                drawLine(
                                    color = Color(0xFF111111),
                                    start = Offset(4.dp.toPx(), 20.dp.toPx()),
                                    end = Offset(8.dp.toPx(), 2.dp.toPx()),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // Right tapered road edge
                                drawLine(
                                    color = Color(0xFF111111),
                                    start = Offset(18.dp.toPx(), 20.dp.toPx()),
                                    end = Offset(14.dp.toPx(), 2.dp.toPx()),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // Dashed center line
                                val dashPath = Path().apply {
                                    moveTo(11.dp.toPx(), 18.dp.toPx())
                                    lineTo(11.dp.toPx(), 4.dp.toPx())
                                }
                                drawPath(
                                    path = dashPath,
                                    color = Color(0xFF111111),
                                    style = Stroke(
                                        width = 1.8.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = displayInfo.deliveryDistance,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111111)
                                    )
                                )
                                Text(
                                    text = "To Drop",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = Color(0xFF626773)
                                    )
                                )
                            }
                        }

                        // Divider 2
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(Color(0xFFE5E7EB))
                        )

                        // Metric 3: Est. to Pickup (8 min)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF111111),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "8 min",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111111)
                                    )
                                )
                                Text(
                                    text = "Est. to Pickup",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = Color(0xFF626773)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // -------------------------------------------------------------
            // Bottom Action: Prominent Black "Accept Order →" Pill Button
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                val isAlreadyAccepted = order.status != OrderStatus.AVAILABLE

                Button(
                    onClick = {
                        if (isAlreadyAccepted) {
                            onNavigateToPickupMap()
                        } else {
                            viewModel.requestAcceptOrder(order)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isAlreadyAccepted) "Proceed to Pickup" else "Accept Order",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
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
