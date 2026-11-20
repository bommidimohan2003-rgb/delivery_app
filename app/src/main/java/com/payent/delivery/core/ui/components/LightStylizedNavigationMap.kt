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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkBorder
import com.payent.delivery.ui.theme.DarkSurface
import com.payent.delivery.ui.theme.TextPrimaryDark
import com.payent.delivery.ui.theme.TextSecondaryDark

/**
 * Modern light-themed vector navigation map component matching the reference design.
 * Features:
 * - Clean pastel palette (soft green parks, blue water body, light gray city blocks)
 * - Stylized street network grid
 * - Vibrant curved route lines (Royal Blue for Pickup, Emerald Green for Delivery)
 * - Animated pulsing origin location marker (blue)
 * - Custom red teardrop destination pin
 * - Floating ETA & distance pill along the path
 * - Floating circular control buttons (Compass, Zoom In/Out, Recenter Target)
 * - Named POI landmark chips (Tech Park, Metro, Hospital, Green Park / University, Lake View, City Center, Riverside Park)
 */
@Composable
fun LightStylizedNavigationMap(
    isPickupMap: Boolean,
    distanceText: String,
    etaText: String,
    modifier: Modifier = Modifier,
    onRecenterClick: () -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Palette
    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface
    val mapBgColor = if (isDark) Color(0xFF141715) else Color(0xFFF3F6F4)
    val blockColor = if (isDark) Color(0xFF1E2220) else Color(0xFFE8ECE9)
    val roadColor = if (isDark) Color(0xFF2C312E) else Color(0xFFFFFFFF)
    val roadBorderColor = if (isDark) Color(0xFF383E3B) else Color(0xFFDDE3DE)
    val parkColor = if (isDark) Color(0xFF10281C) else Color(0xFFDCFCE7)
    val waterColor = if (isDark) Color(0xFF0F263B) else Color(0xFFBAE6FD)
    val mapBorderColor = if (isDark) DarkBorder else Color(0xFFE5E7EB)

    val routeColor = if (isPickupMap) Color(0xFF2563EB) else Color(0xFF10B981)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, mapBorderColor, RoundedCornerShape(16.dp))
            .background(mapBgColor)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.8f, 2.5f)
                    panOffset += pan
                }
            }
    ) {
        // Vector Background & Roads Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = panOffset.x
                    translationY = panOffset.y
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Water Body
            if (isPickupMap) {
                // Bottom-right bay water
                val waterPath = Path().apply {
                    moveTo(w * 0.45f, h)
                    cubicTo(w * 0.55f, h * 0.88f, w * 0.75f, h * 0.90f, w, h * 0.78f)
                    lineTo(w, h)
                    close()
                }
                drawPath(waterPath, color = waterColor)
            } else {
                // Left-side lake (Lake View)
                val lakePath = Path().apply {
                    moveTo(0f, h * 0.28f)
                    cubicTo(w * 0.28f, h * 0.32f, w * 0.26f, h * 0.62f, 0f, h * 0.72f)
                    close()
                }
                drawPath(lakePath, color = waterColor)
            }

            // 2. Green Park Zones
            if (isPickupMap) {
                // Top-left park
                drawRoundRect(
                    color = parkColor,
                    topLeft = Offset(w * 0.05f, h * 0.08f),
                    size = Size(w * 0.35f, h * 0.22f),
                    cornerRadius = CornerRadius(16f, 16f)
                )
                // Bottom park (Green Park)
                drawRoundRect(
                    color = parkColor,
                    topLeft = Offset(w * 0.35f, h * 0.75f),
                    size = Size(w * 0.42f, h * 0.18f),
                    cornerRadius = CornerRadius(16f, 16f)
                )
            } else {
                // Top park
                drawRoundRect(
                    color = parkColor,
                    topLeft = Offset(w * 0.40f, h * 0.06f),
                    size = Size(w * 0.40f, h * 0.20f),
                    cornerRadius = CornerRadius(16f, 16f)
                )
                // Bottom-right park (Riverside Park)
                drawRoundRect(
                    color = parkColor,
                    topLeft = Offset(w * 0.58f, h * 0.72f),
                    size = Size(w * 0.36f, h * 0.22f),
                    cornerRadius = CornerRadius(16f, 16f)
                )
            }

            // 3. Grid of Secondary City Streets
            val streetStroke = 12f
            val streetBorder = 16f

            // Diagonal and horizontal grid lines
            val roads = listOf(
                Pair(Offset(0f, h * 0.22f), Offset(w, h * 0.26f)),
                Pair(Offset(0f, h * 0.48f), Offset(w, h * 0.44f)),
                Pair(Offset(0f, h * 0.74f), Offset(w, h * 0.76f)),
                Pair(Offset(w * 0.25f, 0f), Offset(w * 0.20f, h)),
                Pair(Offset(w * 0.52f, 0f), Offset(w * 0.50f, h)),
                Pair(Offset(w * 0.78f, 0f), Offset(w * 0.82f, h)),
                Pair(Offset(0f, h * 0.10f), Offset(w * 0.9f, h * 0.95f)),
                Pair(Offset(w * 0.15f, h), Offset(w * 0.95f, 0f))
            )

            // Draw road borders (underneath)
            roads.forEach { (start, end) ->
                drawLine(
                    color = roadBorderColor,
                    start = start,
                    end = end,
                    strokeWidth = streetBorder,
                    cap = StrokeCap.Round
                )
            }
            // Draw road surfaces
            roads.forEach { (start, end) ->
                drawLine(
                    color = roadColor,
                    start = start,
                    end = end,
                    strokeWidth = streetStroke,
                    cap = StrokeCap.Round
                )
            }

            // 4. Primary Route Curve
            val startPoint = Offset(w * 0.22f, h * 0.76f)
            val endPoint = Offset(w * 0.78f, h * 0.22f)

            val routePath = Path().apply {
                moveTo(startPoint.x, startPoint.y)
                if (isPickupMap) {
                    // S-curved blue route to vendor
                    cubicTo(
                        w * 0.38f, h * 0.64f,
                        w * 0.52f, h * 0.54f,
                        w * 0.62f, h * 0.46f
                    )
                    cubicTo(
                        w * 0.72f, h * 0.38f,
                        w * 0.68f, h * 0.30f,
                        endPoint.x, endPoint.y
                    )
                } else {
                    // Green route curving around lake
                    cubicTo(
                        w * 0.32f, h * 0.74f,
                        w * 0.48f, h * 0.62f,
                        w * 0.55f, h * 0.52f
                    )
                    cubicTo(
                        w * 0.62f, h * 0.42f,
                        w * 0.72f, h * 0.28f,
                        endPoint.x, endPoint.y
                    )
                }
            }

            // Route border / casing for contrast
            drawPath(
                path = routePath,
                color = if (isDark) DarkBackground else Color.White,
                style = Stroke(
                    width = 24f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Route Main Line
            drawPath(
                path = routePath,
                color = routeColor,
                style = Stroke(
                    width = 16f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 5. Origin Marker: Blue pulsing halo + circle + center dot
            drawCircle(
                color = Color(0xFF3B82F6).copy(alpha = pulseAlpha),
                radius = 32f * pulseScale,
                center = startPoint
            )
            drawCircle(
                color = Color.White,
                radius = 20f,
                center = startPoint
            )
            drawCircle(
                color = Color(0xFF2563EB),
                radius = 14f,
                center = startPoint
            )

            // 6. Destination Marker: Red Pin
            val pinTip = endPoint
            val pinRadius = 20f
            val pinCenter = Offset(pinTip.x, pinTip.y - pinRadius * 1.5f)

            // Teardrop pin shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = 12f,
                center = Offset(pinTip.x, pinTip.y + 4f)
            )

            // Teardrop pin body
            val pinPath = Path().apply {
                moveTo(pinTip.x, pinTip.y)
                lineTo(pinCenter.x - pinRadius * 0.85f, pinCenter.y + pinRadius * 0.5f)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = pinCenter.x - pinRadius,
                        top = pinCenter.y - pinRadius,
                        right = pinCenter.x + pinRadius,
                        bottom = pinCenter.y + pinRadius
                    ),
                    startAngleDegrees = 140f,
                    sweepAngleDegrees = 260f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(pinPath, color = Color(0xFFEF4444))

            // White inner dot on pin
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = pinCenter
            )
        }

        // -------------------------------------------------------------
        // POI Landmark Chips
        // -------------------------------------------------------------
        if (isPickupMap) {
            MapLandmarkChip(
                icon = Icons.Default.School,
                text = "Tech Park",
                modifier = Modifier.align(Alignment.TopStart).offset(x = 24.dp, y = 38.dp)
            )
            MapLandmarkChip(
                icon = Icons.Default.Subway,
                text = "Metro Station",
                modifier = Modifier.align(Alignment.CenterStart).offset(x = 16.dp, y = 20.dp)
            )
            MapLandmarkChip(
                icon = Icons.Default.LocalHospital,
                text = "City Hospital",
                iconTint = Color(0xFFDC2626),
                modifier = Modifier.align(Alignment.Center).offset(x = 28.dp, y = 24.dp)
            )
            MapLandmarkChip(
                icon = Icons.Default.Park,
                text = "Green Park",
                iconTint = Color(0xFF16A34A),
                modifier = Modifier.align(Alignment.BottomCenter).offset(x = (-10).dp, y = (-26).dp)
            )
        } else {
            MapLandmarkChip(
                icon = Icons.Default.School,
                text = "University",
                modifier = Modifier.align(Alignment.TopStart).offset(x = 52.dp, y = 32.dp)
            )
            MapLandmarkChip(
                icon = Icons.Default.Subway,
                text = "Metro Station",
                modifier = Modifier.align(Alignment.CenterStart).offset(x = 50.dp, y = 0.dp)
            )
            Text(
                text = "Lake View",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF0369A1)
                ),
                modifier = Modifier.align(Alignment.CenterStart).offset(x = 14.dp, y = (-4).dp)
            )
            MapLandmarkChip(
                icon = Icons.Default.LocationCity,
                text = "City Center",
                modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-42).dp, y = 46.dp)
            )
            MapLandmarkChip(
                icon = Icons.Default.Park,
                text = "Riverside Park",
                iconTint = Color(0xFF16A34A),
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-42).dp, y = (-26).dp)
            )
        }

        // -------------------------------------------------------------
        // Floating Route ETA Bubble (Center-path Pill)
        // -------------------------------------------------------------
        val pillEta = if (isPickupMap) "12 min" else "15 min"
        val pillDist = if (isPickupMap) "4.8 km" else "6.2 km"
        val pillBg = if (isDark) DarkSurface else Color.White
        val pillBorder = if (isDark) DarkBorder else Color(0xFFE5E7EB)
        val pillTextPrimary = if (isDark) TextPrimaryDark else Color(0xFF111827)
        val pillTextSecondary = if (isDark) TextSecondaryDark else Color(0xFF6B7280)

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(
                    x = if (isPickupMap) (-12).dp else 16.dp,
                    y = if (isPickupMap) (-16).dp else (-26).dp
                )
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = pillBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, pillBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = pillTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = pillEta,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = pillTextPrimary
                        )
                    )
                    Text(
                        text = pillDist,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = pillTextSecondary
                        )
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // Floating Map Controls (Right Edge: Compass, Zoom +, Zoom -, Target)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapCircularButton(
                icon = Icons.Default.Navigation,
                iconRotation = 45f,
                contentDescription = "Compass",
                onClick = {
                    panOffset = Offset.Zero
                    scale = 1.0f
                }
            )
            MapCircularButton(
                icon = Icons.Default.Add,
                contentDescription = "Zoom In",
                onClick = { scale = (scale + 0.25f).coerceAtMost(2.5f) }
            )
            MapCircularButton(
                icon = Icons.Default.Remove,
                contentDescription = "Zoom Out",
                onClick = { scale = (scale - 0.25f).coerceAtLeast(0.8f) }
            )
            MapCircularButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Recenter GPS",
                onClick = {
                    panOffset = Offset.Zero
                    scale = 1.0f
                    onRecenterClick()
                }
            )
        }
    }
}

@Composable
private fun MapLandmarkChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconTint: Color = Color(0xFF2563EB)
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface
    val chipBg = if (isDark) DarkSurface.copy(alpha = 0.94f) else Color.White.copy(alpha = 0.94f)
    val chipBorder = if (isDark) DarkBorder else Color(0xFFE5E7EB)
    val textColor = if (isDark) TextPrimaryDark else Color(0xFF1F2937)

    Surface(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = chipBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            )
        }
    }
}

@Composable
private fun MapCircularButton(
    icon: ImageVector,
    contentDescription: String,
    iconRotation: Float = 0f,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface
    val btnBg = if (isDark) DarkSurface else Color.White
    val btnBorder = if (isDark) DarkBorder else Color(0xFFE5E7EB)
    val iconColor = if (isDark) TextPrimaryDark else Color(0xFF111827)

    Box(
        modifier = Modifier
            .size(36.dp)
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(btnBg)
            .border(1.dp, btnBorder, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { rotationZ = iconRotation }
        )
    }
}
