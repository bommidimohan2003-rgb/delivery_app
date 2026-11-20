package com.payent.delivery.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Subway
import androidx.compose.material.icons.outlined.TurnRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.payent.delivery.ui.theme.StatusSuccess
import com.payent.delivery.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayentIllustrativeMapView(
    destinationName: String,
    destinationAddress: String,
    modifier: Modifier = Modifier,
    originLabel: String = "Partner Start Location",
    isPickupMap: Boolean = true,
    journeyStatusText: String = "READY FOR NAVIGATION",
    onDirectionsClick: () -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var isExpanded by remember { mutableStateOf(false) }

    // Dark Vector/Satellite Styling Palette matching the target image
    val mapBgColor = Color(0xFF0F1B22)
    val roadColor = Color(0xFF1E2D3B)
    val laneDashColor = Color(0xFF384A5C)
    val waterColor = Color(0xFF0C4B75)
    val parkBgColor = Color(0xFF123420)
    val neonRouteColor = if (isPickupMap) Color(0xFF00E676) else Color(0xFF00A3FF)
    val destinationPinColor = Color(0xFFFF3B30)
    val originBlueColor = Color(0xFF007AFF)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        color = mapBgColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Main Map Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        )
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw Map Base Surface
                    drawRect(color = mapBgColor)

                    // Draw Waterway / River along the bottom or side
                    val riverPath = Path().apply {
                        moveTo(0f, h * 0.78f)
                        quadraticTo(w * 0.35f, h * 0.88f, w * 0.7f, h * 0.82f)
                        lineTo(w, h * 0.95f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path = riverPath, color = waterColor)

                    // Draw Green Park Areas
                    drawRoundRect(
                        color = parkBgColor,
                        topLeft = Offset(w * 0.32f, h * 0.08f),
                        size = Size(w * 0.38f, h * 0.22f),
                        cornerRadius = CornerRadius(16f, 16f)
                    )

                    drawRoundRect(
                        color = parkBgColor,
                        topLeft = Offset(w * 0.18f, h * 0.52f),
                        size = Size(w * 0.32f, h * 0.22f),
                        cornerRadius = CornerRadius(16f, 16f)
                    )

                    drawRoundRect(
                        color = parkBgColor,
                        topLeft = Offset(w * 0.68f, h * 0.52f),
                        size = Size(w * 0.28f, h * 0.24f),
                        cornerRadius = CornerRadius(16f, 16f)
                    )

                    // Draw Curved & Grid Road Networks
                    val mainRoadStroke = 18.dp.toPx()
                    val secRoadStroke = 12.dp.toPx()
                    val laneStroke = 1.5.dp.toPx()

                    // Diagonal Avenue 1
                    val road1 = Path().apply {
                        moveTo(0f, h * 0.28f)
                        lineTo(w * 0.48f, h * 0.12f)
                        lineTo(w, h * 0.2f)
                    }
                    drawPath(path = road1, color = roadColor, style = Stroke(width = mainRoadStroke))

                    // Diagonal Avenue 2
                    val road2 = Path().apply {
                        moveTo(w * 0.1f, h * 0.85f)
                        lineTo(w * 0.65f, h * 0.35f)
                        lineTo(w * 0.95f, h * 0.05f)
                    }
                    drawPath(path = road2, color = roadColor, style = Stroke(width = mainRoadStroke))

                    // Cross Street 1
                    val road3 = Path().apply {
                        moveTo(0f, h * 0.42f)
                        lineTo(w, h * 0.42f)
                    }
                    drawPath(path = road3, color = roadColor, style = Stroke(width = secRoadStroke))
                    drawPath(
                        path = road3,
                        color = laneDashColor,
                        style = Stroke(width = laneStroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
                    )

                    // Cross Street 2
                    val road4 = Path().apply {
                        moveTo(w * 0.48f, 0f)
                        lineTo(w * 0.48f, h)
                    }
                    drawPath(path = road4, color = roadColor, style = Stroke(width = secRoadStroke))
                    drawPath(
                        path = road4,
                        color = laneDashColor,
                        style = Stroke(width = laneStroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
                    )

                    // Curved Expressway
                    val exprRoad = Path().apply {
                        moveTo(0f, h * 0.65f)
                        cubicTo(w * 0.3f, h * 0.58f, w * 0.6f, h * 0.72f, w, h * 0.62f)
                    }
                    drawPath(path = exprRoad, color = roadColor.copy(alpha = 0.85f), style = Stroke(width = secRoadStroke))

                    // Draw Glowing Neon Route Path
                    val startPt = Offset(w * 0.22f, h * 0.7f)
                    val wayPt1 = Offset(w * 0.35f, h * 0.58f)
                    val wayPt2 = Offset(w * 0.48f, h * 0.42f)
                    val wayPt3 = Offset(w * 0.72f, h * 0.42f)
                    val endPt = Offset(w * 0.72f, h * 0.28f)

                    val neonPath = Path().apply {
                        moveTo(startPt.x, startPt.y)
                        lineTo(wayPt1.x, wayPt1.y)
                        lineTo(wayPt2.x, wayPt2.y)
                        lineTo(wayPt3.x, wayPt3.y)
                        lineTo(endPt.x, endPt.y)
                    }

                    // Neon Outer Glow Shadow
                    drawPath(
                        path = neonPath,
                        color = neonRouteColor.copy(alpha = 0.35f),
                        style = Stroke(width = 12.dp.toPx())
                    )

                    // Neon Main Line
                    drawPath(
                        path = neonPath,
                        color = neonRouteColor,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Waypoint Dots
                    drawCircle(color = neonRouteColor, radius = 5.dp.toPx(), center = wayPt1)
                    drawCircle(color = neonRouteColor, radius = 5.dp.toPx(), center = wayPt2)
                    drawCircle(color = neonRouteColor, radius = 5.dp.toPx(), center = wayPt3)
                }

                // Landmark Labels Overlays
                // Central Park Landmark
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 140.dp, top = 40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Park,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Central Park",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E676)
                    )
                }

                // City Mall Landmark
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 24.dp, top = 80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "City Mall",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE1BEE7)
                    )
                }

                // Metro Station Landmark
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 36.dp, top = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Subway,
                        contentDescription = null,
                        tint = Color(0xFF29B6F6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Metro Station",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB3E5FC)
                    )
                }

                // Green Avenue Landmark
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 130.dp, bottom = 80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Park,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Green Avenue",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E676)
                    )
                }

                // Origin Navigation Node (Glowing Blue Circle with Pointer Arrow)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 54.dp, bottom = 62.dp)
                        .size(46.dp)
                        .background(originBlueColor.copy(alpha = 0.25f), CircleShape)
                        .border(1.5.dp, originBlueColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(originBlueColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Navigation,
                            contentDescription = originLabel,
                            tint = Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(45f)
                        )
                    }
                }

                // Destination Node Pin (Red Glowing Pin with Pulsing Ripple & Label Badge)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 68.dp, top = 54.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Location Pill Badge above Marker
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = Color(0xFF141414).copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, Color(0xFF333333))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPickupMap) Icons.Outlined.Storefront else Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = destinationName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Glowing Red Destination Pin Node
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(destinationPinColor.copy(alpha = 0.25f), CircleShape)
                            .border(1.dp, destinationPinColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = destinationName,
                            tint = destinationPinColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Text(
                        text = "Evergreen Terrace",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }

                // Top-Right Status Capsule Tag (Green Glowing Dot + DEMO MAP)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF141414).copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00E676), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DEMO MAP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Vertical Floating Map Zoom & Recenter Controls Capsule (Right Side)
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF141414).copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { scale = (scale + 0.25f).coerceAtMost(2.5f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Zoom In",
                                tint = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(1.dp)
                                .background(Color(0xFF333333))
                        )

                        IconButton(
                            onClick = { scale = (scale - 0.25f).coerceAtLeast(0.8f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Remove,
                                contentDescription = "Zoom Out",
                                tint = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(1.dp)
                                .background(Color(0xFF333333))
                        )

                        IconButton(
                            onClick = { scale = 1.0f },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MyLocation,
                                contentDescription = "Recenter Map",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Address Details Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Destination: $destinationName",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = destinationAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Illustrative Street Map Canvas • Local Prototype Simulation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoDirectionsSheet(
    destinationName: String,
    destinationAddress: String,
    isPickupMap: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                        text = "Step-by-Step Directions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                StatusTag(text = "DEMO DIRECTIONS", color = StatusWarning)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Illustrative navigation route for $destinationName ($destinationAddress)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Direction Steps List
            DirectionStepRow(
                stepNumber = "1",
                instruction = "Head north on Commercial Ave toward 4th Street",
                distance = "300 m",
                icon = Icons.Outlined.Navigation
            )

            DirectionStepRow(
                stepNumber = "2",
                instruction = "Turn right onto Film Center Boulevard",
                distance = "500 m",
                icon = Icons.Outlined.TurnRight
            )

            DirectionStepRow(
                stepNumber = "3",
                instruction = "Continue straight past Creative Hub Bay 9",
                distance = "250 m",
                icon = Icons.Outlined.Navigation
            )

            DirectionStepRow(
                stepNumber = "4",
                instruction = "Arrive at $destinationName",
                distance = "Destination",
                icon = if (isPickupMap) Icons.Outlined.Storefront else Icons.Outlined.LocationOn
            )

            Spacer(modifier = Modifier.height(16.dp))

            PayentCard {
                Text(
                    text = "DEMO DIRECTIONS — For local prototype preview only. Not geographically accurate.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PayentSecondaryButton(
                text = "Close Directions",
                onClick = onDismiss
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DirectionStepRow(
    stepNumber: String,
    instruction: String,
    distance: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$stepNumber. $instruction",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = distance,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
