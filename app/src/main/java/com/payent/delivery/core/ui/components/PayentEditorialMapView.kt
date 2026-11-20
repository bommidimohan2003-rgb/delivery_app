package com.payent.delivery.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class EditorialMapMode {
    ORDER_DETAILS_PREVIEW,
    NAVIGATE_TO_VENDOR,
    ARRIVED_AT_VENDOR,
    NAVIGATE_TO_DELIVERY,
    ARRIVED_AT_RENTER
}

/**
 * Editorial Light Map Component matching the 9-screen reference specification.
 * Features:
 * - Subtle light city street network with light sage park blocks (#EDF7ED) and soft blue water (#E0F2FE).
 * - High-contrast route lines (Blue #2563EB or Black for Order Details preview).
 * - Motorcycle bearing icon on route, blue partner marker with pulsing radar wave.
 * - Custom red destination pin with white center dot.
 * - Right floating controls: Navigation/Compass, Layers, Zoom In/Out pill.
 * - Touch pan and zoom gesture support.
 */
@Composable
fun PayentEditorialMapView(
    mode: EditorialMapMode,
    modifier: Modifier = Modifier,
    onRecenterClick: () -> Unit = {},
    onViewRouteClick: () -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "mapRadar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (mode == EditorialMapMode.ORDER_DETAILS_PREVIEW) 16.dp else 0.dp))
            .background(Color(0xFFF4F6F5))
            .pointerInput(mode) {
                if (mode != EditorialMapMode.ORDER_DETAILS_PREVIEW) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.75f, 3.0f)
                        panOffset += pan
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = panOffset.x,
                    translationY = panOffset.y
                )
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw base city background & park blocks
            drawEditorialBackground(w, h)

            // 2. Draw streets
            drawEditorialStreetNetwork(w, h)

            // 3. Draw route and markers according to mode
            when (mode) {
                EditorialMapMode.ORDER_DETAILS_PREVIEW -> {
                    drawOrderDetailsPreviewRoute(w, h)
                }
                EditorialMapMode.NAVIGATE_TO_VENDOR -> {
                    drawNavigateToVendorRoute(w, h, radarPulse, radarAlpha)
                }
                EditorialMapMode.ARRIVED_AT_VENDOR -> {
                    drawArrivedAtDestination(w, h, radarPulse, radarAlpha, isVendor = true)
                }
                EditorialMapMode.NAVIGATE_TO_DELIVERY -> {
                    drawNavigateToDeliveryRoute(w, h, radarPulse, radarAlpha)
                }
                EditorialMapMode.ARRIVED_AT_RENTER -> {
                    drawArrivedAtDestination(w, h, radarPulse, radarAlpha, isVendor = false)
                }
            }
        }

        // Overlay Motorcycle icon for navigate screens
        if (mode == EditorialMapMode.NAVIGATE_TO_VENDOR || mode == EditorialMapMode.NAVIGATE_TO_DELIVERY) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset {
                        val x = (45.dp.toPx() + panOffset.x * 0.2f).roundToInt()
                        val y = (35.dp.toPx() + panOffset.y * 0.2f).roundToInt()
                        IntOffset(x, y)
                    }
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF111111))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.TwoWheeler,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Floating controls on map preview (Screen 1)
        if (mode == EditorialMapMode.ORDER_DETAILS_PREVIEW) {
            // Floating View Route pill on Bottom-Left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                    .clickable { onViewRouteClick() }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Navigation,
                        contentDescription = "View Route",
                        tint = Color(0xFF111111),
                        modifier = Modifier
                            .size(13.dp)
                            .graphicsLayer(rotationZ = 45f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "View Route",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        )
                    )
                }
            }

            // Floating Vertical Control Pill on Right (+ / - / Crosshair)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .width(36.dp)
                    .shadow(3.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onRecenterClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = "Target",
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(16.dp)
                    )
                }

                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp, modifier = Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { scale = (scale + 0.2f).coerceAtMost(2.5f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Zoom In",
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(16.dp)
                    )
                }

                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp, modifier = Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { scale = (scale - 0.2f).coerceAtLeast(0.8f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            // Screens 2, 3, 6, 7: Standard 3 floating controls on right side
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Navigation / Compass arrow button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                        .clickable {
                            scale = 1.0f
                            panOffset = Offset.Zero
                            onRecenterClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Navigation,
                        contentDescription = "Compass",
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 2. Layers button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                        .clickable {
                            // Toggle layers in demo mode
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Layers,
                        contentDescription = "Map Layers",
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 3. Zoom In / Out Pill
                Column(
                    modifier = Modifier
                        .width(40.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { scale = (scale + 0.3f).coerceAtMost(3.0f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Zoom In",
                            tint = Color(0xFF111111),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    HorizontalDivider(
                        color = Color(0xFFE5E7EB),
                        thickness = 1.dp,
                        modifier = Modifier.width(28.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { scale = (scale - 0.3f).coerceAtLeast(0.75f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color(0xFF111111),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Canvas Drawing Helper Functions
// ---------------------------------------------------------------------------

private fun DrawScope.drawEditorialBackground(w: Float, h: Float) {
    // Light base canvas background
    drawRect(color = Color(0xFFF3F5F4))

    // Soft green park blocks (#EBF5ED)
    drawRoundRect(
        color = Color(0xFFE8F5E9).copy(alpha = 0.85f),
        topLeft = Offset(w * 0.05f, h * 0.08f),
        size = Size(w * 0.28f, h * 0.22f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = Color(0xFFE8F5E9).copy(alpha = 0.85f),
        topLeft = Offset(w * 0.58f, h * 0.62f),
        size = Size(w * 0.36f, h * 0.26f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = Color(0xFFEDF7ED).copy(alpha = 0.9f),
        topLeft = Offset(w * 0.65f, h * 0.12f),
        size = Size(w * 0.26f, h * 0.18f),
        cornerRadius = CornerRadius(12f, 12f)
    )

    // Soft water canal / curved lake (#E0F2FE)
    val waterPath = Path().apply {
        moveTo(w * 0.38f, 0f)
        cubicTo(
            w * 0.44f, h * 0.25f,
            w * 0.30f, h * 0.65f,
            w * 0.42f, h
        )
        lineTo(w * 0.49f, h)
        cubicTo(
            w * 0.37f, h * 0.65f,
            w * 0.51f, h * 0.25f,
            w * 0.45f, 0f
        )
        close()
    }
    drawPath(path = waterPath, color = Color(0xFFE0F2FE))
}

private fun DrawScope.drawEditorialStreetNetwork(w: Float, h: Float) {
    val roadBorderColor = Color(0xFFE2E6E8)
    val roadFillColor = Color(0xFFFFFFFF)

    // Diagonal arterial avenues
    val diagonalAvenues = listOf(
        Pair(Offset(-w * 0.2f, h * 0.3f), Offset(w * 1.2f, h * 0.85f)),
        Pair(Offset(-w * 0.1f, h * 0.7f), Offset(w * 1.1f, h * 0.15f)),
        Pair(Offset(w * 0.1f, -h * 0.1f), Offset(w * 0.9f, h * 1.1f))
    )

    for ((start, end) in diagonalAvenues) {
        // Border
        drawLine(
            color = roadBorderColor,
            start = start,
            end = end,
            strokeWidth = 26f,
            cap = StrokeCap.Round
        )
        // Road fill
        drawLine(
            color = roadFillColor,
            start = start,
            end = end,
            strokeWidth = 22f,
            cap = StrokeCap.Round
        )
    }

    // Grid streets (horizontal and vertical)
    val horizontalYs = listOf(h * 0.22f, h * 0.45f, h * 0.68f, h * 0.90f)
    for (y in horizontalYs) {
        drawLine(
            color = roadBorderColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 16f
        )
        drawLine(
            color = roadFillColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 12f
        )
    }

    val verticalXs = listOf(w * 0.18f, w * 0.52f, w * 0.82f)
    for (x in verticalXs) {
        drawLine(
            color = roadBorderColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 16f
        )
        drawLine(
            color = roadFillColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 12f
        )
    }
}

private fun DrawScope.drawOrderDetailsPreviewRoute(w: Float, h: Float) {
    // Route for Screen 1: Simple dark/black route from bottom-left to top-right
    val startX = w * 0.14f
    val startY = h * 0.72f
    val endX = w * 0.82f
    val endY = h * 0.26f

    val routePath = Path().apply {
        moveTo(startX, startY)
        lineTo(w * 0.32f, startY)
        lineTo(w * 0.42f, h * 0.54f)
        lineTo(w * 0.54f, h * 0.66f)
        lineTo(w * 0.58f, h * 0.60f)
        lineTo(w * 0.72f, h * 0.70f)
        lineTo(w * 0.76f, h * 0.48f)
        lineTo(endX, endY)
    }

    // Shadow
    drawPath(
        path = routePath,
        color = Color(0x18000000),
        style = Stroke(width = 10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Dark Route Polyline
    drawPath(
        path = routePath,
        color = Color(0xFF111111),
        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Origin Marker: Double concentric ring (Black outer -> White middle -> Black center dot)
    drawCircle(color = Color(0xFF111111), radius = 12f, center = Offset(startX, startY))
    drawCircle(color = Color.White, radius = 7.5f, center = Offset(startX, startY))
    drawCircle(color = Color(0xFF111111), radius = 4f, center = Offset(startX, startY))

    // Destination Pin: Black teardrop pin with white center dot
    drawBlackDestinationPin(endX, endY)
}

private fun DrawScope.drawBlackDestinationPin(x: Float, y: Float) {
    // Shadow under pin
    drawOval(
        color = Color(0x33000000),
        topLeft = Offset(x - 9f, y + 10f),
        size = Size(18f, 7f)
    )

    // Black teardrop pin path
    val pinPath = Path().apply {
        moveTo(x, y + 14f) // pin tip
        cubicTo(x - 14f, y - 2f, x - 14f, y - 20f, x, y - 20f)
        cubicTo(x + 14f, y - 20f, x + 14f, y - 2f, x, y + 14f)
        close()
    }

    drawPath(path = pinPath, color = Color(0xFF111111), style = Fill)
    drawCircle(color = Color.White, radius = 5f, center = Offset(x, y - 7f))
}

private fun DrawScope.drawNavigateToVendorRoute(
    w: Float,
    h: Float,
    radarPulse: Float,
    radarAlpha: Float
) {
    // Route from delivery partner (bottom-left) to vendor (upper-right)
    val startX = w * 0.22f
    val startY = h * 0.68f
    val endX = w * 0.74f
    val endY = h * 0.28f

    val routePath = Path().apply {
        moveTo(startX, startY)
        lineTo(w * 0.32f, startY)
        lineTo(w * 0.38f, h * 0.60f)
        lineTo(w * 0.46f, h * 0.60f)
        lineTo(w * 0.52f, h * 0.48f)
        lineTo(w * 0.64f, h * 0.48f)
        lineTo(w * 0.68f, h * 0.36f)
        lineTo(endX, endY)
    }

    // Route Outer Glow / Shadow
    drawPath(
        path = routePath,
        color = Color(0x332563EB),
        style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Vibrant Blue Polyline (#2563EB)
    drawPath(
        path = routePath,
        color = Color(0xFF2563EB),
        style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Pulsing radar around partner location
    drawCircle(
        color = Color(0xFF2563EB).copy(alpha = radarAlpha),
        radius = 26f * radarPulse,
        center = Offset(startX, startY)
    )

    // Partner blue location dot
    drawCircle(color = Color.White, radius = 14f, center = Offset(startX, startY))
    drawCircle(color = Color(0xFF2563EB), radius = 10f, center = Offset(startX, startY))

    // Vendor Red Pin
    drawRedDestinationPin(endX, endY)
}

private fun DrawScope.drawNavigateToDeliveryRoute(
    w: Float,
    h: Float,
    radarPulse: Float,
    radarAlpha: Float
) {
    // Route from Vendor (picked up) to Renter
    val startX = w * 0.24f
    val startY = h * 0.65f
    val endX = w * 0.72f
    val endY = h * 0.32f

    val routePath = Path().apply {
        moveTo(startX, startY)
        lineTo(w * 0.34f, startY)
        lineTo(w * 0.40f, h * 0.56f)
        lineTo(w * 0.50f, h * 0.56f)
        lineTo(w * 0.56f, h * 0.42f)
        lineTo(w * 0.66f, h * 0.42f)
        lineTo(endX, endY)
    }

    // Shadow
    drawPath(
        path = routePath,
        color = Color(0x332563EB),
        style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Blue Route line
    drawPath(
        path = routePath,
        color = Color(0xFF2563EB),
        style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Origin green/blue marker (Picked up)
    drawCircle(
        color = Color(0xFF16A34A).copy(alpha = radarAlpha),
        radius = 26f * radarPulse,
        center = Offset(startX, startY)
    )
    drawCircle(color = Color.White, radius = 14f, center = Offset(startX, startY))
    drawCircle(color = Color(0xFF2563EB), radius = 10f, center = Offset(startX, startY))

    // Renter Red Pin
    drawRedDestinationPin(endX, endY)
}

private fun DrawScope.drawArrivedAtDestination(
    w: Float,
    h: Float,
    radarPulse: Float,
    radarAlpha: Float,
    isVendor: Boolean
) {
    // In arrival screens (Screens 3 & 7), map is zoomed centered on the destination.
    val destX = w * 0.50f
    val destY = h * 0.44f

    // Soft large blue arrival radius zone
    drawCircle(
        color = Color(0xFF2563EB).copy(alpha = 0.15f),
        radius = 56f,
        center = Offset(destX, destY + 20f)
    )

    // Continuous radar wave
    drawCircle(
        color = Color(0xFF2563EB).copy(alpha = radarAlpha),
        radius = 50f * radarPulse,
        center = Offset(destX, destY + 20f)
    )

    // Blue partner circle right at destination
    drawCircle(color = Color.White, radius = 14f, center = Offset(destX, destY + 20f))
    drawCircle(color = Color(0xFF2563EB), radius = 9f, center = Offset(destX, destY + 20f))

    // Red destination pin directly above
    drawRedDestinationPin(destX, destY)
}

private fun DrawScope.drawRedDestinationPin(x: Float, y: Float) {
    // Shadow under pin
    drawOval(
        color = Color(0x33000000),
        topLeft = Offset(x - 9f, y + 10f),
        size = Size(18f, 7f)
    )

    // Red teardrop pin path
    val pinPath = Path().apply {
        moveTo(x, y + 12f) // pin tip
        cubicTo(x - 14f, y - 2f, x - 14f, y - 18f, x, y - 18f) // left bulge and top
        cubicTo(x + 14f, y - 18f, x + 14f, y - 2f, x, y + 12f) // right bulge down to tip
        close()
    }

    drawPath(path = pinPath, color = Color(0xFFEF4444), style = Fill)
    drawPath(path = pinPath, color = Color(0xFFB91C1C), style = Stroke(width = 1.5f))

    // White circle inside pin head
    drawCircle(color = Color.White, radius = 5.5f, center = Offset(x, y - 7f))
}
