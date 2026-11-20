package com.payent.delivery.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.ui.theme.StatusSuccess

@Composable
fun PayentFullScreenNavigationMap(
    isPickupMap: Boolean,
    originLabel: String,
    destinationName: String,
    destinationAddress: String,
    distanceText: String,
    etaText: String,
    isNavigationActive: Boolean,
    modifier: Modifier = Modifier,
    onRecenterClick: () -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    // Dark Vector/Satellite Styling Palette matching the target black-and-white theme
    val mapBgColor = Color(0xFF0D151B)
    val roadColor = Color(0xFF1B2733)
    val laneDashColor = Color(0xFF2C3E50)
    val waterColor = Color(0xFF093959)
    val parkBgColor = Color(0xFF0E2A1A)
    val neonRouteColor = if (isPickupMap) Color(0xFF00E676) else Color(0xFF00A3FF)
    val partnerLocationColor = Color(0xFF007AFF)

    Box(modifier = modifier.fillMaxSize().background(mapBgColor)) {
        // Interactive Canvas Layer with Zoom and Pan
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.7f, 2.5f)
                        panOffset += pan
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = panOffset.x,
                    translationY = panOffset.y
                )
        ) {
            val w = size.width
            val h = size.height

            // Base Map Surface
            drawRect(color = mapBgColor)

            // Scenic Water Body along bottom edge
            val riverPath = Path().apply {
                moveTo(0f, h * 0.85f)
                quadraticTo(w * 0.4f, h * 0.92f, w * 0.75f, h * 0.86f)
                lineTo(w, h * 0.98f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = riverPath, color = waterColor)

            // Green Parks
            drawRoundRect(
                color = parkBgColor,
                topLeft = Offset(w * 0.28f, h * 0.15f),
                size = Size(w * 0.42f, h * 0.18f),
                cornerRadius = CornerRadius(20f, 20f)
            )

            drawRoundRect(
                color = parkBgColor,
                topLeft = Offset(w * 0.65f, h * 0.55f),
                size = Size(w * 0.32f, h * 0.22f),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // Street & Avenue Grid
            val mainRoadStroke = 20.dp.toPx()
            val secRoadStroke = 12.dp.toPx()
            val dashStroke = 2.dp.toPx()

            // Diagonal Expressway
            val road1 = Path().apply {
                moveTo(0f, h * 0.35f)
                lineTo(w * 0.45f, h * 0.25f)
                lineTo(w, h * 0.30f)
            }
            drawPath(path = road1, color = roadColor, style = Stroke(width = mainRoadStroke))

            val road2 = Path().apply {
                moveTo(w * 0.15f, h * 0.9f)
                lineTo(w * 0.60f, h * 0.45f)
                lineTo(w * 0.92f, h * 0.12f)
            }
            drawPath(path = road2, color = roadColor, style = Stroke(width = mainRoadStroke))

            // Cross Streets
            val crossRoad = Path().apply {
                moveTo(0f, h * 0.52f)
                lineTo(w, h * 0.52f)
            }
            drawPath(path = crossRoad, color = roadColor, style = Stroke(width = secRoadStroke))
            drawPath(
                path = crossRoad,
                color = laneDashColor,
                style = Stroke(
                    width = dashStroke,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
                )
            )

            // High-Contrast Navigation Route Line from Partner to Destination
            val startX = w * 0.24f
            val startY = h * 0.68f
            val endX = w * 0.76f
            val endY = h * 0.26f

            val routePath = Path().apply {
                moveTo(startX, startY)
                lineTo(startX + (endX - startX) * 0.35f, startY - (startY - endY) * 0.1f)
                lineTo(startX + (endX - startX) * 0.45f, startY - (startY - endY) * 0.65f)
                lineTo(endX, endY)
            }

            // Glow / Outer halo stroke
            drawPath(
                path = routePath,
                color = neonRouteColor.copy(alpha = 0.35f),
                style = Stroke(width = 14.dp.toPx())
            )
            // Core route polyline
            drawPath(
                path = routePath,
                color = neonRouteColor,
                style = Stroke(width = 6.dp.toPx())
            )

            // Partner Location Pulsing Radar
            drawCircle(
                color = partnerLocationColor.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = Offset(startX, startY)
            )
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(startX, startY)
            )
            drawCircle(
                color = partnerLocationColor,
                radius = 5.dp.toPx(),
                center = Offset(startX, startY)
            )

            // Destination Pin Marker
            drawCircle(
                color = Color(0xFFFF3B30),
                radius = 10.dp.toPx(),
                center = Offset(endX, endY)
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(endX, endY)
            )
        }

        // Floating Map Controls (Zoom In, Zoom Out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E1E1E).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = { scale = (scale + 0.25f).coerceAtMost(2.5f) }) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFF1E1E1E).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = { scale = (scale - 0.25f).coerceAtLeast(0.7f) }) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFF1E1E1E).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = {
                    scale = 1.0f
                    panOffset = Offset.Zero
                    onRecenterClick()
                }) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = "Recenter",
                        tint = StatusSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Floating Route Info Pill (Distance • ETA)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 120.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF141414).copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Navigation,
                    contentDescription = null,
                    tint = neonRouteColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$distanceText • $etaText",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isNavigationActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusSuccess)
                    )
                }
            }
        }
    }
}
