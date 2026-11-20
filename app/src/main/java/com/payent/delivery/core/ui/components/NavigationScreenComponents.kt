package com.payent.delivery.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Headset
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TurnRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkBorder
import com.payent.delivery.ui.theme.DarkSurface

/**
 * Top bar matching the reference image for Pickup and Delivery Navigation.
 * Displays Back button, Screen Title, Order Subtitle, and Headset "Help" button.
 * Adapts to active theme (Dark / Light).
 */
@Composable
fun NavigationScreenTopBar(
    title: String,
    orderNumber: String,
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface
    val topBarBg = if (isDark) DarkBackground else Color.White
    val textPrimary = if (isDark) Color.White else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFB8B8B8) else Color(0xFF6B7280)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(topBarBg)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = textPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textPrimary
                )
            )
            Text(
                text = "Order $orderNumber",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = textSecondary,
                    fontSize = 13.sp
                )
            )
        }

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onHelpClick() }
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Headset,
                contentDescription = "Help",
                tint = textPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Help",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary
                )
            )
        }
    }
}

/**
 * Clean dual location card matching the reference screenshot.
 * Displays Origin ("Your Current Location" + GPS) and Destination (Vendor or Renter)
 * with concentric blue GPS target, 3-dot vertical connector, red pin, and chevron indicators.
 * Adapts to active theme (Dark / Light).
 */
@Composable
fun NavigationDualLocationCard(
    currentLocationText: String,
    destinationLabel: String,
    destinationAddressText: String,
    modifier: Modifier = Modifier,
    onOriginClick: () -> Unit = {},
    onDestinationClick: () -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface
    val cardBg = if (isDark) DarkSurface else Color.White
    val borderCol = if (isDark) DarkBorder else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) Color.White else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFB8B8B8) else Color(0xFF6B7280)
    val chevronTint = if (isDark) Color(0xFF757575) else Color(0xFF9CA3AF)
    val dottedCol = if (isDark) Color(0xFF333333) else Color(0xFFCBD5E1)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Origin Row: Your Current Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOriginClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Concentric Blue GPS Dot
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your Current Location",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                    )
                    Text(
                        text = currentLocationText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            color = textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = chevronTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Dotted vertical connector
            Column(
                modifier = Modifier
                    .padding(start = 9.dp, top = 3.dp, bottom = 3.dp)
                    .height(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(dottedCol)
                    )
                }
            }

            // Destination Row: Vendor Location (Pickup) or Renter Location (Delivery)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDestinationClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = destinationLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                    )
                    Text(
                        text = destinationAddressText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            color = textSecondary
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = chevronTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Route stats card matching the reference image.
 * Displays 3 evenly distributed columns:
 * 1. Highway road lanes icon + Distance + "Distance"
 * 2. Clock icon + Est. time + "Est. time"
 * 3. Route live traffic icon + "Best Route" + "Live traffic"
 * Adapts to active theme (Dark / Light).
 */
@Composable
fun RouteStatsCard(
    distanceText: String,
    etaText: String,
    routeStatusText: String = "Best Route",
    trafficStatusText: String = "Live traffic",
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface
    val cardBg = if (isDark) DarkSurface else Color.White
    val borderCol = if (isDark) DarkBorder else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) Color.White else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFB8B8B8) else Color(0xFF6B7280)
    val dividerCol = if (isDark) Color(0xFF262626) else Color(0xFFE5E7EB)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Col 1: Distance
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Highway lanes Canvas icon
                Canvas(modifier = Modifier.size(22.dp)) {
                    val green = Color(0xFF10B981)
                    // Left lane
                    val leftPath = Path().apply {
                        moveTo(size.width * 0.36f, 2f)
                        lineTo(size.width * 0.08f, size.height - 2f)
                    }
                    drawPath(leftPath, color = green, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

                    // Right lane
                    val rightPath = Path().apply {
                        moveTo(size.width * 0.64f, 2f)
                        lineTo(size.width * 0.92f, size.height - 2f)
                    }
                    drawPath(rightPath, color = green, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

                    // Dashed center divider
                    drawLine(
                        color = green,
                        start = Offset(size.width * 0.5f, 4f),
                        end = Offset(size.width * 0.5f, size.height - 4f),
                        strokeWidth = 2.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f),
                        cap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = textPrimary
                        )
                    )
                    Text(
                        text = "Distance",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            color = textSecondary
                        )
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(dividerCol)
            )

            // Col 2: Est. time
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = etaText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = textPrimary
                        )
                    )
                    Text(
                        text = "Est. time",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            color = textSecondary
                        )
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(dividerCol)
            )

            // Col 3: Live traffic
            Row(
                modifier = Modifier
                    .weight(1.15f)
                    .padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Curved route path with traffic node Canvas
                Canvas(modifier = Modifier.size(22.dp)) {
                    val green = Color(0xFF10B981)
                    val routePath = Path().apply {
                        moveTo(size.width * 0.15f, size.height * 0.85f)
                        cubicTo(
                            size.width * 0.3f, size.height * 0.4f,
                            size.width * 0.5f, size.height * 0.7f,
                            size.width * 0.85f, size.height * 0.25f
                        )
                    }
                    drawPath(routePath, color = green, style = Stroke(width = 3.2f, cap = StrokeCap.Round))
                    drawCircle(color = green, radius = 4f, center = Offset(size.width * 0.85f, size.height * 0.25f))
                    drawCircle(color = green, radius = 3.5f, center = Offset(size.width * 0.15f, size.height * 0.85f))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = routeStatusText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = textPrimary
                        )
                    )
                    Text(
                        text = trafficStatusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            color = textSecondary
                        )
                    )
                }
            }
        }
    }
}

/**
 * Bottom action buttons matching the reference image.
 * Stacked vertically:
 * 1. Primary Button with Play icon: "Start Navigation" / "Navigate to Pickup/Delivery Location"
 * 2. Secondary Outlined Button with Turn Arrow: "Directions" / "View Full Route"
 * Adapts to active theme (Dark / Light).
 */
@Composable
fun NavigationActionButtons(
    primaryTitle: String,
    primarySubtitle: String,
    onPrimaryClick: () -> Unit,
    onDirectionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface
    val primaryBg = if (isDark) Color.White else Color(0xFF000000)
    val primaryText = if (isDark) Color.Black else Color.White
    val primarySubtext = if (isDark) Color(0xFF555555) else Color(0xFF9CA3AF)

    val secondaryBg = if (isDark) DarkSurface else Color.White
    val secondaryBorder = if (isDark) DarkBorder else Color(0xFFD1D5DB)
    val secondaryText = if (isDark) Color.White else Color(0xFF111827)
    val secondarySubtext = if (isDark) Color(0xFFB8B8B8) else Color(0xFF6B7280)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Button 1: Primary Action Button
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBg,
                contentColor = primaryText
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = primaryText,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = primaryTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = primaryText
                        )
                    )
                    Text(
                        text = primarySubtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            color = primarySubtext
                        )
                    )
                }

                // Balance play icon spacing on right
                Spacer(modifier = Modifier.size(24.dp))
            }
        }

        // Button 2: Secondary Outlined Button
        OutlinedButton(
            onClick = onDirectionsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, secondaryBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = secondaryBg,
                contentColor = secondaryText
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.TurnRight,
                    contentDescription = null,
                    tint = secondaryText,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Directions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = secondaryText
                        )
                    )
                    Text(
                        text = "View Full Route",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            color = secondarySubtext
                        )
                    )
                }

                // Balance turn icon spacing on right
                Spacer(modifier = Modifier.size(22.dp))
            }
        }
    }
}
