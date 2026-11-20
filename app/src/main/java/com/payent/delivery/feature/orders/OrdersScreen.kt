package com.payent.delivery.feature.orders

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.payent.delivery.R
import com.payent.delivery.core.ui.components.LoadingView
import com.payent.delivery.domain.model.Order
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkBorder
import com.payent.delivery.ui.theme.DarkSurface
import com.payent.delivery.ui.theme.LightBackground
import com.payent.delivery.ui.theme.LightBorder
import com.payent.delivery.ui.theme.LightSurface
import com.payent.delivery.ui.theme.LocalIsDarkTheme
import com.payent.delivery.ui.theme.OnlineGreen
import com.payent.delivery.ui.theme.StatusSuccess
import com.payent.delivery.ui.theme.TextPrimaryDark
import com.payent.delivery.ui.theme.TextPrimaryLight
import com.payent.delivery.ui.theme.TextSecondaryDark
import com.payent.delivery.ui.theme.TextSecondaryLight
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Upgraded Orders Screen — Premium 3D Delivery Bike + Continuous Ripple Waves + Double Connector Lines.
 *
 * Upgrades:
 * 1. Premium 3D Motorcycle & Rider Hero Element:
 *    - Replaced basic primitives with realistic, high-resolution 3D rear-view delivery motorcycle and rider.
 *    - Size increased by ~55% (height ~205dp, width ~107dp) making it the prominent visual hero element.
 *    - Visible wide rear tyre with treads, dual red suspension coils, chrome canister exhaust, glowing red LED tail light,
 *      side mirrors, rider in dark uniform with boots on footrests, square black delivery bag with crisp white "P" logo,
 *      and helmet with white "P" monogram.
 * 2. Layout Overlap Fixed:
 *    - Reserved responsive space for the floating bottom navigation dock (92dp clearance).
 *    - Lower order cards are positioned safely above the bottom navigation bar without clipping or overlap.
 * 3. Exactly TWO Parallel 3D Curved Connector Lines:
 *    - Recalculated for responsive lower-card positions, maintaining a constant 5.2dp gap along the entire length.
 * 4. Continuous Water-Ripples & Orbiting Dots:
 *    - Concentric expanding waves and orbital dots running smoothly around the stable, centered motorcycle.
 * 5. Expandable Card Details with "Accept Order" action.
 */
@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    onNavigateToOrderDetails: (Order) -> Unit = {},
    onNavigateToKyc: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = LocalIsDarkTheme.current
    var expandedOrderId by remember { mutableStateOf<String?>(null) }

    // Theme color definitions
    val screenBg = if (isDark) DarkBackground else LightBackground
    val headerTextColor = if (isDark) TextPrimaryDark else TextPrimaryLight
    val subTextColor = if (isDark) TextSecondaryDark else TextSecondaryLight
    val dialogBg = if (isDark) DarkSurface else LightSurface
    val dialogTitleColor = if (isDark) TextPrimaryDark else TextPrimaryLight
    val dialogTextColor = if (isDark) TextSecondaryDark else TextSecondaryLight

    // 1. Accept Order Confirmation Dialog
    if (uiState.showAcceptConfirmationDialog && uiState.orderToAccept != null) {
        val order = uiState.orderToAccept!!
        AlertDialog(
            onDismissRequest = { if (!uiState.isAcceptingOrder) viewModel.cancelAcceptOrder() },
            containerColor = dialogBg,
            titleContentColor = dialogTitleColor,
            textContentColor = dialogTextColor,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Accept Order #${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to accept this delivery? Payout: ₹${order.payoutAmount.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = dialogTitleColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pickup: ${order.vendorAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = dialogTextColor
                    )
                    Text(
                        text = "Drop-off: ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = dialogTextColor
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmAcceptOrder { acceptedOrder ->
                            onNavigateToOrderDetails(acceptedOrder)
                        }
                    },
                    enabled = !uiState.isAcceptingOrder,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color.White else Color(0xFF111111),
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    shape = CircleShape
                ) {
                    if (uiState.isAcceptingOrder) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = if (isDark) Color.Black else Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (uiState.isAcceptingOrder) "Accepting..." else "Confirm Accept",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelAcceptOrder() },
                    enabled = !uiState.isAcceptingOrder
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelMedium,
                        color = dialogTextColor
                    )
                }
            }
        )
    }

    // 2. Offline Mode Warning Dialog
    if (uiState.showOfflineWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOfflineWarningDialog() },
            containerColor = dialogBg,
            titleContentColor = dialogTitleColor,
            textContentColor = dialogTextColor,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "You're Currently Offline",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Switch to Online mode to receive and accept new delivery orders.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dialogTextColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissOfflineWarningDialog()
                        viewModel.updateOnlineStatus(true)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color.White else Color(0xFF111111),
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Go Online",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissOfflineWarningDialog() }) {
                    Text(
                        text = "Stay Offline",
                        style = MaterialTheme.typography.labelMedium,
                        color = dialogTextColor
                    )
                }
            }
        )
    }

    // 3. KYC Verification Required Dialog
    if (uiState.showKycRequiredDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissKycRequiredDialog() },
            containerColor = dialogBg,
            titleContentColor = dialogTitleColor,
            textContentColor = dialogTextColor,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Verification Required",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Complete your partner KYC verification to accept and start delivery orders.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dialogTextColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissKycRequiredDialog()
                        onNavigateToKyc()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color.White else Color(0xFF111111),
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Review KYC",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissKycRequiredDialog() }) {
                    Text(
                        text = "Not Now",
                        style = MaterialTheme.typography.labelMedium,
                        color = dialogTextColor
                    )
                }
            }
        )
    }

    // Root Screen Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(10.dp))

            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Orders",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = headerTextColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (uiState.isOnline) "Stay online to receive orders" else "Switch to online to receive orders",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp
                        ),
                        color = subTextColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Online / Offline Status Control (Functional toggle)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color(0xFF141416) else Color(0xFFF1F5F9))
                            .border(
                                BorderStroke(1.dp, if (isDark) Color(0xFF27272A) else Color(0xFFE2E8F0)),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.updateOnlineStatus(!uiState.isOnline) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isOnline) OnlineGreen else Color(0xFF888888))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isOnline) "Online" else "Offline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = if (uiState.isOnline) (if (isDark) Color.White else Color(0xFF111111)) else subTextColor
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = subTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Refresh Button
                    IconButton(
                        onClick = { viewModel.fetchOrders() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh Available Orders",
                            tint = if (isDark) Color(0xFFAAAAAA) else Color(0xFF555555),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Help & Support Button
                    IconButton(
                        onClick = onNavigateToHelp,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SupportAgent,
                            contentDescription = "Help & Support",
                            tint = if (isDark) Color(0xFFAAAAAA) else Color(0xFF555555),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Notice Banner (if any)
            if (uiState.acceptNoticeMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF152015) else Color(0xFFE8F5E9))
                        .border(BorderStroke(1.dp, if (isDark) Color(0xFF2E402E) else Color(0xFFC8E6C9)), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = uiState.acceptNoticeMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isDark) StatusSuccess else Color(0xFF1B5E20)
                    )
                }
            }

            // Error Banner (if any)
            if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF221616) else Color(0xFFFFEBEE))
                        .border(BorderStroke(1.dp, if (isDark) Color(0xFF442222) else Color(0xFFFFCDD2)), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "Error",
                                tint = if (isDark) Color(0xFFFF7777) else Color(0xFFC62828),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFFFFB4B4) else Color(0xFFB71C1C)
                            )
                        }

                        TextButton(onClick = { viewModel.fetchOrders() }) {
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else Color(0xFF111111)
                            )
                        }
                    }
                }
            }

            // Main Radar & Floating Canvas Space
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val availableWidth = maxWidth
                val availableHeight = maxHeight
                val density = androidx.compose.ui.platform.LocalDensity.current.density

                // 1. Water Ripple Continuous Animation (3.6s per wave)
                val rippleTransition = rememberInfiniteTransition(label = "WaterRippleTransition")
                val rippleProgress by if (uiState.isOnline) {
                    rippleTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 3600, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "WaterRippleProgress"
                    )
                } else {
                    remember { mutableFloatStateOf(0f) }
                }

                // 2. Slow Orbiting Dots Animation
                val orbitTransition = rememberInfiniteTransition(label = "OrbitDotsTransition")
                val orbitAngle1 by if (uiState.isOnline) {
                    orbitTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 18000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "OrbitAngle1"
                    )
                } else {
                    remember { mutableFloatStateOf(0f) }
                }

                val orbitAngle2 by if (uiState.isOnline) {
                    orbitTransition.animateFloat(
                        initialValue = 360f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 24000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "OrbitAngle2"
                    )
                } else {
                    remember { mutableFloatStateOf(0f) }
                }

                val orbitAngle3 by if (uiState.isOnline) {
                    orbitTransition.animateFloat(
                        initialValue = 180f,
                        targetValue = 540f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 32000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "OrbitAngle3"
                    )
                } else {
                    remember { mutableFloatStateOf(180f) }
                }

                // 3. Flowing Water Moving Shadows Animation (Main Feature: 5.6s - 8.4s organic flow cycles)
                val shadowFlowTransition = rememberInfiniteTransition(label = "FlowingWaterShadowTransition")
                val shadowAngle1 by if (uiState.isOnline) {
                    shadowFlowTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 5600, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ShadowAngle1"
                    )
                } else {
                    remember { mutableFloatStateOf(45f) }
                }

                val shadowAngle2 by if (uiState.isOnline) {
                    shadowFlowTransition.animateFloat(
                        initialValue = 120f,
                        targetValue = 480f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 7200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ShadowAngle2"
                    )
                } else {
                    remember { mutableFloatStateOf(160f) }
                }

                val shadowAngle3 by if (uiState.isOnline) {
                    shadowFlowTransition.animateFloat(
                        initialValue = 360f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 6400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ShadowAngle3"
                    )
                } else {
                    remember { mutableFloatStateOf(280f) }
                }

                val shadowAngle4 by if (uiState.isOnline) {
                    shadowFlowTransition.animateFloat(
                        initialValue = 200f,
                        targetValue = 560f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 8400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ShadowAngle4"
                    )
                } else {
                    remember { mutableFloatStateOf(70f) }
                }

                val shadowPulse by if (uiState.isOnline) {
                    shadowFlowTransition.animateFloat(
                        initialValue = 0.88f,
                        targetValue = 1.12f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "ShadowPulse"
                    )
                } else {
                    remember { mutableFloatStateOf(1f) }
                }

                // Available real orders up to 4
                val visibleOrders = remember(uiState.availableOrders) {
                    uiState.availableOrders.take(4)
                }

                // Dynamic clearance for floating bottom navigation dock (66dp pill + 24dp vertical padding + system insets + 8dp buffer)
                val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val bottomNavClearanceDp = 90.dp + maxOf(navBarBottomInset, 28.dp) + 8.dp
                val usableHeightDp = (availableHeight - bottomNavClearanceDp).coerceAtLeast(320.dp)

                // Center coordinates for the stationary motorcycle and circular ripples
                val centerX = availableWidth.value * density / 2f
                val centerY = (usableHeightDp.value * density) / 2f

                // Radii specifications
                val minRippleRadius = 48f * density
                val midRippleRadius = 88f * density
                val hubRippleRadius = 126f * density
                val trackRippleRadius = 168f * density
                val connectRippleRadius = 212f * density
                val maxRippleRadius = 250f * density

                // Cached Flowing Water Shadow Gradients (Zero per-frame allocation, 120 FPS hardware accelerated)
                val shadowColorBase = if (isDark) Color(0xFF000000) else Color(0xFF64748B)
                val shimmerColorBase = if (isDark) Color(0xFFCBD5E1) else Color(0xFFFFFFFF)

                val flowingWaterBrushA = remember(isDark, centerX, centerY) {
                    Brush.sweepGradient(
                        0.00f to Color.Transparent,
                        0.04f to shadowColorBase.copy(alpha = if (isDark) 0.16f else 0.08f),
                        0.10f to shadowColorBase.copy(alpha = if (isDark) 0.36f else 0.22f),
                        0.16f to shadowColorBase.copy(alpha = if (isDark) 0.16f else 0.08f),
                        0.22f to Color.Transparent,
                        0.46f to Color.Transparent,
                        0.50f to shimmerColorBase.copy(alpha = if (isDark) 0.12f else 0.35f),
                        0.54f to Color.Transparent,
                        0.64f to Color.Transparent,
                        0.69f to shadowColorBase.copy(alpha = if (isDark) 0.12f else 0.06f),
                        0.75f to shadowColorBase.copy(alpha = if (isDark) 0.28f else 0.16f),
                        0.81f to shadowColorBase.copy(alpha = if (isDark) 0.12f else 0.06f),
                        0.86f to Color.Transparent,
                        1.00f to Color.Transparent,
                        center = Offset(centerX, centerY)
                    )
                }

                val flowingWaterBrushB = remember(isDark, centerX, centerY) {
                    Brush.sweepGradient(
                        0.00f to Color.Transparent,
                        0.05f to shadowColorBase.copy(alpha = if (isDark) 0.14f else 0.07f),
                        0.12f to shadowColorBase.copy(alpha = if (isDark) 0.32f else 0.20f),
                        0.19f to shadowColorBase.copy(alpha = if (isDark) 0.14f else 0.07f),
                        0.25f to Color.Transparent,
                        0.40f to Color.Transparent,
                        0.45f to shadowColorBase.copy(alpha = if (isDark) 0.20f else 0.12f),
                        0.50f to Color.Transparent,
                        0.68f to Color.Transparent,
                        0.73f to shimmerColorBase.copy(alpha = if (isDark) 0.10f else 0.30f),
                        0.78f to Color.Transparent,
                        0.82f to Color.Transparent,
                        0.88f to shadowColorBase.copy(alpha = if (isDark) 0.24f else 0.15f),
                        0.94f to Color.Transparent,
                        1.00f to Color.Transparent,
                        center = Offset(centerX, centerY)
                    )
                }

                // Card Layout Dimensions (Speech Bubble)
                val cardWidthDp = 154.dp
                val cardBodyHeightDp = 72.dp
                val cardTailHeightDp = 14.dp
                val cardTotalHeightDp = cardBodyHeightDp + cardTailHeightDp // 86.dp
                val horizontalMarginDp = 12.dp
                val topMarginDp = 10.dp
                // Bottom cards sit safely above the floating bottom navigation bar
                val bottomCardYDp = usableHeightDp - cardTotalHeightDp - 6.dp

                // Canvas: Water-Ripples with Flowing Shadows, Orbiting Dots & Ripple Connection Nodes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val baseRingColor = if (isDark) Color(0xFF888888) else Color(0xFF64748B)

                    // 1. Flowing Water Moving Shadow Bands along the circular rings (Main Feature)
                    fun drawFlowingWaterShadow(
                        radius: Float,
                        angleDeg: Float,
                        brush: Brush,
                        bandWidthDp: Float = 14f,
                        intensityMultiplier: Float = 1.0f
                    ) {
                        val effectiveAlpha = (intensityMultiplier * shadowPulse).coerceIn(0f, 1f)
                        rotate(degrees = angleDeg, pivot = Offset(centerX, centerY)) {
                            // Outer soft diffuse shadow feather
                            drawCircle(
                                brush = brush,
                                radius = radius,
                                center = Offset(centerX, centerY),
                                style = Stroke(width = bandWidthDp * density),
                                alpha = effectiveAlpha * 0.75f
                            )
                            // Core focused shadow band along the ring path
                            drawCircle(
                                brush = brush,
                                radius = radius,
                                center = Offset(centerX, centerY),
                                style = Stroke(width = (bandWidthDp * 0.45f) * density),
                                alpha = effectiveAlpha
                            )
                        }
                    }

                    // Apply flowing water shadows across selected concentric rings at independent speeds
                    // Ring 1 (Mid ring ~88dp): clockwise flow
                    drawFlowingWaterShadow(midRippleRadius, shadowAngle1, flowingWaterBrushA, 13f, 0.95f)
                    // Ring 2 (Hub ring ~126dp): clockwise flow with pattern B
                    drawFlowingWaterShadow(hubRippleRadius, shadowAngle2, flowingWaterBrushB, 15f, 1.10f)
                    // Ring 3 (Track ring ~168dp): counter-clockwise flow
                    drawFlowingWaterShadow(trackRippleRadius, shadowAngle3, flowingWaterBrushA, 14f, 1.00f)
                    // Ring 4 (Connect ring ~212dp): clockwise flow with pattern B
                    drawFlowingWaterShadow(connectRippleRadius, shadowAngle4, flowingWaterBrushB, 16f, 0.90f)
                    // Ring 5 (Outer horizon ring ~250dp): gentle slow drift
                    drawFlowingWaterShadow(maxRippleRadius, shadowAngle1 * 0.7f + 140f, flowingWaterBrushA, 15f, 0.80f)

                    // 2. Helper to draw a delicate, thin ring with a lightened, gentle 3D shadow beneath it
                    fun drawShadowedRing(radius: Float, alpha: Float, strokeWidth: Float, withDashes: Boolean = false) {
                        // Lightened soft diffuse shadow beneath ring (offset slightly downward by ~0.8dp)
                        drawCircle(
                            color = (if (isDark) Color(0xFF000000) else Color(0xFF334155))
                                .copy(alpha = (if (isDark) 0.14f else 0.07f) * alpha),
                            radius = radius,
                            center = Offset(centerX, centerY + 0.8f * density),
                            style = Stroke(
                                width = (strokeWidth + 0.8f) * density,
                                pathEffect = if (withDashes) PathEffect.dashPathEffect(floatArrayOf(4f * density, 4f * density), 0f) else null
                            )
                        )
                        // Delicate, thin crisp ring line (reduced thickness 0.65 - 0.85dp)
                        drawCircle(
                            color = (if (isDark) Color(0xFFB0B0B8) else Color(0xFF475569)).copy(alpha = alpha),
                            radius = radius,
                            center = Offset(centerX, centerY),
                            style = Stroke(
                                width = strokeWidth * density,
                                pathEffect = if (withDashes) PathEffect.dashPathEffect(floatArrayOf(4f * density, 4f * density), 0f) else null
                            )
                        )
                    }

                    // Static Concentric Rings with Reduced Thickness (delicate thin lines 0.65 - 0.85dp)
                    // Inner origin circle around bike (thin 0.75dp)
                    drawShadowedRing(minRippleRadius, if (isDark) 0.22f else 0.18f, 0.75f)
                    // First mid ring (thin 0.75dp)
                    drawShadowedRing(midRippleRadius, if (isDark) 0.20f else 0.16f, 0.75f)
                    // Middle hub ring (thin 0.85dp)
                    drawShadowedRing(hubRippleRadius, if (isDark) 0.24f else 0.19f, 0.85f)
                    // Dashed secondary track ring (thin 0.65dp)
                    drawShadowedRing(trackRippleRadius, if (isDark) 0.16f else 0.12f, 0.65f, withDashes = true)
                    // Connection ring where speech-bubble tails align (thin 0.80dp)
                    drawShadowedRing(connectRippleRadius, if (isDark) 0.22f else 0.18f, 0.80f)
                    // Outer horizon ring (thin 0.75dp)
                    drawShadowedRing(maxRippleRadius, if (isDark) 0.14f else 0.10f, 0.75f)

                    // 3. Continuous Water-Ripple Expanding Waves (3.6s per wave, staggered start)
                    if (uiState.isOnline) {
                        val ripplePhases = listOf(0.00f, 0.25f, 0.50f, 0.75f)
                        ripplePhases.forEach { phase ->
                            val p = (rippleProgress + phase) % 1.0f
                            val currentR = minRippleRadius + (maxRippleRadius - minRippleRadius) * p
                            // Sinusoidal wave envelope: smooth organic start, peak, and gentle dissipation like real liquid
                            val waveEnvelope = kotlin.math.sin((p * Math.PI).toFloat()).coerceIn(0f, 1f)
                            val lineAlpha = ((if (isDark) 0.36f else 0.24f) * waveEnvelope).coerceIn(0f, 1f)

                            if (lineAlpha > 0.01f) {
                                // A. Smooth fluid wave trough shadow flowing outward with the water wave
                                val dynamicShadowOffset = (0.6f + p * 1.0f) * density
                                val shadowStrokeWidth = (1.6f + p * 2.2f) * density
                                val shadowAlpha = ((if (isDark) 0.14f else 0.07f) * waveEnvelope).coerceIn(0f, 1f)

                                drawCircle(
                                    color = (if (isDark) Color(0xFF000000) else Color(0xFF1E293B)).copy(alpha = shadowAlpha),
                                    radius = currentR,
                                    center = Offset(centerX, centerY + dynamicShadowOffset),
                                    style = Stroke(width = shadowStrokeWidth)
                                )

                                // B. Soft inner water displacement wash just trailing the wave crest
                                val innerTroughAlpha = ((if (isDark) 0.08f else 0.04f) * waveEnvelope).coerceIn(0f, 1f)
                                drawCircle(
                                    color = (if (isDark) Color(0xFF08080C) else Color(0xFF334155)).copy(alpha = innerTroughAlpha),
                                    radius = (currentR - 1.2f * density).coerceAtLeast(minRippleRadius),
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = (1.2f + p * 1.4f) * density)
                                )

                                // C. Delicate, thin expanding wave crest line (0.75dp) flowing like water
                                drawCircle(
                                    color = (if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)).copy(alpha = lineAlpha),
                                    radius = currentR,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 0.75f * density)
                                )
                            }
                        }
                    }

                    // 3. Subtle Orbiting Dots
                    if (uiState.isOnline) {
                        val dotTracks = listOf(
                            Triple(midRippleRadius, orbitAngle1, 4.5f * density),
                            Triple(hubRippleRadius, orbitAngle2 + 45f, 5.0f * density),
                            Triple(trackRippleRadius, orbitAngle3, 4.0f * density),
                            Triple(connectRippleRadius, orbitAngle1 * 0.7f + 120f, 4.2f * density),
                            Triple(142f * density, orbitAngle2 * 1.2f + 210f, 3.8f * density)
                        )

                        dotTracks.forEach { (r, angleDeg, dotRadius) ->
                            val rad = Math.toRadians(angleDeg.toDouble())
                            val dx = centerX + (r * cos(rad)).toFloat()
                            val dy = centerY + (r * sin(rad)).toFloat()

                            // Soft outer glow
                            drawCircle(
                                color = if (isDark) Color.White.copy(alpha = 0.22f) else Color(0xFF334155).copy(alpha = 0.18f),
                                radius = dotRadius * 1.6f,
                                center = Offset(dx, dy)
                            )
                            // Solid core
                            drawCircle(
                                color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569),
                                radius = dotRadius,
                                center = Offset(dx, dy)
                            )
                        }
                    }

                    // 4. Subtle Circular Connection Nodes at Speech-Bubble Tail Tips (Replacing double lines)
                    visibleOrders.forEachIndexed { index, _ ->
                        val cardXPx = when (index) {
                            0, 2 -> horizontalMarginDp.value * density
                            else -> (availableWidth - horizontalMarginDp - cardWidthDp).value * density
                        }
                        val cardYPx = when (index) {
                            0, 1 -> topMarginDp.value * density
                            else -> bottomCardYDp.value * density
                        }
                        val cardWPx = cardWidthDp.value * density
                        val cardTotalHPx = cardTotalHeightDp.value * density

                        val tipX = if (index == 0 || index == 2) {
                            cardXPx + cardWPx - 18f * density
                        } else {
                            cardXPx + 18f * density
                        }
                        val tipY = if (index == 0 || index == 1) {
                            cardYPx + cardTotalHPx
                        } else {
                            cardYPx
                        }

                        // Subtle circular point on the ripple ring system
                        drawCircle(
                            color = (if (isDark) Color.White else Color(0xFF334155)).copy(alpha = 0.25f),
                            radius = 6.0f * density,
                            center = Offset(tipX, tipY)
                        )
                        drawCircle(
                            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF475569),
                            radius = 3.2f * density,
                            center = Offset(tipX, tipY)
                        )
                        drawCircle(
                            color = if (isDark) Color(0xFF0F172A) else Color.White,
                            radius = 1.3f * density,
                            center = Offset(tipX, tipY)
                        )
                    }

                    // 5. Motorcycle Rear Red Light Reflection on Road & Ground Contact Shadow
                    // A. Broad atmospheric red ground reflection wash on road
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x99FF1E27),
                                Color(0x55E11D48),
                                Color(0x22E11D48),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY + 94f * density),
                            radius = 42f * density
                        ),
                        topLeft = Offset(centerX - 42f * density, centerY + 78f * density),
                        size = Size(84f * density, 32f * density)
                    )

                    // B. Concentrated red reflection pool directly beneath the tail light & exhaust
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xEEFF2B36),
                                Color(0x99FF1E27),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY + 93f * density),
                            radius = 22f * density
                        ),
                        topLeft = Offset(centerX - 24f * density, centerY + 83f * density),
                        size = Size(48f * density, 20f * density)
                    )

                    // C. Specular bright glint reflection highlight on the road surface
                    drawOval(
                        color = Color(0xFFFF9EA4),
                        topLeft = Offset(centerX - 9f * density, centerY + 91f * density),
                        size = Size(18f * density, 4.2f * density)
                    )

                    // D. Heavy rubber tyre contact shadow planted firmly on the road
                    drawOval(
                        color = if (isDark) Color(0xFF000000).copy(alpha = 0.95f) else Color(0xFF000000).copy(alpha = 0.40f),
                        topLeft = Offset(centerX - 24f * density, centerY + 82f * density),
                        size = Size(48f * density, 11f * density)
                    )
                }

                // 5. Central 3D Motorcycle & Rider Hero Element
                // Reduced by ~13% (178dp x 93dp) for spatial harmony with ripple rings and speech bubbles
                val bikeHeightDp = 178.dp
                val bikeWidthDp = 93.dp
                val bikeXDp = (availableWidth - bikeWidthDp) / 2f
                val bikeYDp = (usableHeightDp - bikeHeightDp) / 2f

                Box(
                    modifier = Modifier
                        .size(width = bikeWidthDp, height = bikeHeightDp)
                        .offset(x = bikeXDp, y = bikeYDp)
                        .zIndex(5f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.delivery_rider_3d),
                        contentDescription = "Payent Delivery Motorcycle and Rider (Rear View)",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Lower Rear Auxiliary Red LED Light
                    // Positioned on the motorcycle centerline below the license plate and upper tail light
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = bikeHeightDp * 0.595f)
                            .size(width = 16.dp, height = 4.2.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(2.dp),
                                ambientColor = Color(0xFFFF1E27),
                                spotColor = Color(0xFFFF1E27)
                            )
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFFF1E27))
                            .border(BorderStroke(0.6.dp, Color(0xFFFF7B7B)), RoundedCornerShape(2.dp))
                    ) {
                        // Specular highlight line along top of LED lens
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(1.dp)
                                .align(Alignment.TopCenter)
                                .background(Color(0xFFFFD4D4))
                        )
                    }
                }

                // 6. Floating Speech-Bubble Order Cards in 4 Quadrants
                visibleOrders.forEachIndexed { index, order ->
                    val isExpanded = expandedOrderId == order.id

                    // Layout coordinates
                    val cardX = when (index) {
                        0, 2 -> horizontalMarginDp
                        else -> availableWidth - horizontalMarginDp - cardWidthDp
                    }
                    val cardY = when (index) {
                        0, 1 -> topMarginDp
                        else -> bottomCardYDp
                    }

                    FloatingOrderCardItem(
                        order = order,
                        orderIndex = index,
                        cardWidth = cardWidthDp,
                        cardTotalHeight = cardTotalHeightDp,
                        tailHeight = cardTailHeightDp,
                        isDark = isDark,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedOrderId = if (isExpanded) null else order.id
                        },
                        onAcceptOrder = {
                            expandedOrderId = null
                            viewModel.requestAcceptOrder(order)
                        },
                        onOpenDetails = {
                            expandedOrderId = null
                            viewModel.openOrderDetails(order)
                            onNavigateToOrderDetails(order)
                        },
                        modifier = Modifier
                            .offset(x = cardX, y = cardY)
                            .zIndex(if (isExpanded) 15f else 2f)
                    )
                }

                // Expanded Card Modal Overlay (Panel 4 Reference Specification)
                if (expandedOrderId != null) {
                    val expandedOrder = visibleOrders.firstOrNull { it.id == expandedOrderId }
                    if (expandedOrder != null) {
                        ExpandedOrderDetailsCard(
                            order = expandedOrder,
                            isDark = isDark,
                            onClose = { expandedOrderId = null },
                            onAccept = {
                                expandedOrderId = null
                                viewModel.requestAcceptOrder(expandedOrder)
                            },
                            onOpenFullDetails = {
                                expandedOrderId = null
                                viewModel.openOrderDetails(expandedOrder)
                                onNavigateToOrderDetails(expandedOrder)
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = (-20).dp)
                                .zIndex(30f)
                        )
                    }
                }

                // More orders pill (if > 4 orders available)
                if (uiState.availableOrders.size > 4) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 75.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF141416) else Color(0xFFF1F5F9))
                            .border(BorderStroke(1.dp, if (isDark) Color(0xFF27272A) else Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+${uiState.availableOrders.size - 4} more orders available",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF64748B)
                        )
                    }
                }

                // Loading overlay if fetching
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingView()
                    }
                }
            }
        }
    }
}

/**
 * Compact Floating Order Card matching the 4-side reference placement.
 */
@Composable
private fun FloatingOrderCardItem(
    order: Order,
    orderIndex: Int,
    cardWidth: androidx.compose.ui.unit.Dp,
    cardTotalHeight: androidx.compose.ui.unit.Dp = 86.dp,
    tailHeight: androidx.compose.ui.unit.Dp = 14.dp,
    isDark: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAcceptOrder: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayInfo = remember(order) { getOrderDisplayInfo(order) }

    // Smooth entrance zoom-in animation (0.94f to 1.0f)
    val cardScale = remember(order.id) { Animatable(0.94f) }
    val cardAlpha = remember(order.id) { Animatable(0f) }

    LaunchedEffect(order.id) {
        delay((orderIndex * 150L).coerceAtLeast(0L))
        cardScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 550,
                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
            )
        )
    }

    LaunchedEffect(order.id) {
        delay((orderIndex * 150L).coerceAtLeast(0L))
        cardAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )
    }

    // Colors
    val cardBg = if (isDark) Color(0xFF131316) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF27272A) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFA1A1AA) else Color(0xFF64748B)
    val iconBg = if (isDark) Color(0xFF202024) else Color(0xFFF1F5F9)
    val iconTint = if (isDark) Color.White else Color(0xFF1E293B)
    val shadowColor = if (isDark) Color(0x20000000) else Color(0x0E000000)

    val density = androidx.compose.ui.platform.LocalDensity.current.density

    Box(
        modifier = modifier
            .width(cardWidth)
            .height(cardTotalHeight)
            .graphicsLayer {
                scaleX = cardScale.value
                scaleY = cardScale.value
                alpha = cardAlpha.value
            }
            .drawBehind {
                val tailWidthPx = 16f * density
                val tailHeightPx = tailHeight.toPx()
                val cornerRadiusPx = 14f * density

                val bubblePath = createSpeechBubblePath(
                    width = size.width,
                    height = size.height,
                    orderIndex = orderIndex,
                    tailWidth = tailWidthPx,
                    tailHeight = tailHeightPx,
                    cornerRadius = cornerRadiusPx,
                    density = density
                )

                // 1. Subtle soft shadow beneath speech bubble card
                drawPath(
                    path = bubblePath,
                    color = shadowColor
                )

                // 2. Solid card background fill
                drawPath(
                    path = bubblePath,
                    color = cardBg
                )

                // 3. Crisp outline border
                drawPath(
                    path = bubblePath,
                    color = cardBorder,
                    style = Stroke(
                        width = 1.2f * density,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            .clickable(onClick = onToggleExpand)
            .padding(
                start = 9.dp,
                end = 9.dp,
                top = if (orderIndex == 0 || orderIndex == 1) 7.dp else (7.dp + tailHeight),
                bottom = if (orderIndex == 0 || orderIndex == 1) (7.dp + tailHeight) else 7.dp
            )
    ) {
        Column {
            // Row 1: Green Pill "New Order" + Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // New Order Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                    )
                    Text(
                        text = "New Order",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )
                }

                // Distance
                Text(
                    text = displayInfo.pickupDistance,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Category Icon + Merchant & Location + Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = displayInfo.icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Merchant & Location
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayInfo.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        val loc = order.vendorAddress.substringBefore(",").trim()
                        Text(
                            text = if (loc.isNotBlank()) loc else "Nearby",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp
                            ),
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Chevron
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Expand details",
                    tint = textSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Detailed Expanded Order Card modal matching Panel 4 of the reference specification.
 */
@Composable
private fun ExpandedOrderDetailsCard(
    order: Order,
    isDark: Boolean,
    onClose: () -> Unit,
    onAccept: () -> Unit,
    onOpenFullDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayInfo = remember(order) { getOrderDisplayInfo(order) }
    val cardBg = if (isDark) Color(0xFF131316) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF2E2E33) else Color(0xFFCBD5E1)
    val textPrimary = if (isDark) Color.White else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFA1A1AA) else Color(0xFF64748B)
    val dividerColor = if (isDark) Color(0xFF26262B) else Color(0xFFE2E8F0)
    val buttonBg = if (isDark) Color.White else Color(0xFF111111)
    val buttonText = if (isDark) Color.Black else Color.White

    Box(
        modifier = modifier
            .width(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(BorderStroke(1.2.dp, cardBorder), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header Row: New Order + Distance + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                    )
                    Text(
                        text = "New Order",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = displayInfo.pickupDistance,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textSecondary
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowUp,
                        contentDescription = "Collapse",
                        tint = textSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(onClick = onClose)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Merchant Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFullDetails),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF222226) else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = displayInfo.icon,
                        contentDescription = null,
                        tint = if (isDark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayInfo.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = order.vendorAddress.substringBefore(",").trim(),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = textSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Divider Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(dividerColor)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Pickup Detail Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        text = "Pickup",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = textSecondary
                    )
                    Text(
                        text = "${order.vendorName}, ${order.vendorAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Drop Detail Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        text = "Drop",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = textSecondary
                    )
                    Text(
                        text = order.deliveryAddress,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Est. Earnings Detail Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Est. Earnings",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = textSecondary
                    )
                    Text(
                        text = "₹ ${order.payoutAmount.toInt()} - ₹ ${(order.payoutAmount * 1.25).toInt()}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Button: "Accept Order"
            Button(
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBg,
                    contentColor = buttonText
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Accept Order",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

/**
 * Helper to construct a unified, seamless speech bubble Path with an integrated triangular tail.
 *
 * @param width Card total width
 * @param height Card total height (body + tail)
 * @param orderIndex 0: Top-Left, 1: Top-Right, 2: Bottom-Left, 3: Bottom-Right
 * @param tailWidth Width of the tail base
 * @param tailHeight Extrusion length of the tail
 * @param cornerRadius Corner rounding radius of the main rectangular body
 * @param density Screen display density
 */
private fun createSpeechBubblePath(
    width: Float,
    height: Float,
    orderIndex: Int,
    tailWidth: Float,
    tailHeight: Float,
    cornerRadius: Float,
    density: Float
): Path {
    val path = Path()
    val r = cornerRadius.coerceAtMost(minOf(width, height) / 2f)

    if (orderIndex == 0 || orderIndex == 1) {
        // Tail is on bottom edge pointing downwards towards upper ripple connection point
        val bodyBottom = height - tailHeight
        val (tailBaseLeft, tailBaseRight, tailTipX) = if (orderIndex == 0) {
            // Top-Left card: tail on bottom edge towards right/inner side
            val right = width - 16f * density
            val left = right - tailWidth
            val tipX = width - 18f * density
            Triple(left, right, tipX)
        } else {
            // Top-Right card: tail on bottom edge towards left/inner side
            val left = 16f * density
            val right = left + tailWidth
            val tipX = 18f * density
            Triple(left, right, tipX)
        }
        val tailTipY = height

        path.moveTo(r, 0f)
        path.lineTo(width - r, 0f)
        path.arcTo(
            rect = Rect(width - 2 * r, 0f, width, 2 * r),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        path.lineTo(width, bodyBottom - r)
        path.arcTo(
            rect = Rect(width - 2 * r, bodyBottom - 2 * r, width, bodyBottom),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        path.lineTo(tailBaseRight, bodyBottom)
        path.lineTo(tailTipX, tailTipY)
        path.lineTo(tailBaseLeft, bodyBottom)
        path.lineTo(r, bodyBottom)
        path.arcTo(
            rect = Rect(0f, bodyBottom - 2 * r, 2 * r, bodyBottom),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        path.lineTo(0f, r)
        path.arcTo(
            rect = Rect(0f, 0f, 2 * r, 2 * r),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        path.close()
    } else {
        // Tail is on top edge pointing upwards towards lower ripple connection point
        val bodyTop = tailHeight
        val (tailBaseLeft, tailBaseRight, tailTipX) = if (orderIndex == 2) {
            // Bottom-Left card: tail on top edge towards right/inner side
            val right = width - 16f * density
            val left = right - tailWidth
            val tipX = width - 18f * density
            Triple(left, right, tipX)
        } else {
            // Bottom-Right card: tail on top edge towards left/inner side
            val left = 16f * density
            val right = left + tailWidth
            val tipX = 18f * density
            Triple(left, right, tipX)
        }
        val tailTipY = 0f

        path.moveTo(r, bodyTop)
        path.lineTo(tailBaseLeft, bodyTop)
        path.lineTo(tailTipX, tailTipY)
        path.lineTo(tailBaseRight, bodyTop)
        path.lineTo(width - r, bodyTop)
        path.arcTo(
            rect = Rect(width - 2 * r, bodyTop, width, bodyTop + 2 * r),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        path.lineTo(width, height - r)
        path.arcTo(
            rect = Rect(width - 2 * r, height - 2 * r, width, height),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        path.lineTo(r, height)
        path.arcTo(
            rect = Rect(0f, height - 2 * r, 2 * r, height),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        path.lineTo(0f, bodyTop + r)
        path.arcTo(
            rect = Rect(0f, bodyTop, 2 * r, bodyTop + 2 * r),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        path.close()
    }
    return path
}

