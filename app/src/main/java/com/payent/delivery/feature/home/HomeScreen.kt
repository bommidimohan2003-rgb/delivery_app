package com.payent.delivery.feature.home

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.core.ui.components.LoadingView
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.model.Order
import com.payent.delivery.feature.orders.getOrderDisplayInfo
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkBorder
import com.payent.delivery.ui.theme.DarkSurface
import com.payent.delivery.ui.theme.OnlineGreen
import com.payent.delivery.ui.theme.TextMutedDark
import com.payent.delivery.ui.theme.TextPrimaryDark
import com.payent.delivery.ui.theme.TextSecondaryDark
import java.util.Locale

/**
 * Editorial Redesign of PayentDelivery Home Screen matching the reference design.
 *
 * Layout Sections:
 * A. Clean Header: `payent` wordmark + `DELIVERY` blue label + Notification bell with unread badge + Profile avatar.
 * B. Online/Offline & Location Controls: Status dropdown pill + My Location pill + GPS crosshairs circle button.
 * C. KYC Verification Banner: Prominent black rounded banner with blue shield, title, subtext, and "Complete Now ->".
 * D. Exactly Three Quick-Action Cards: Available Orders (selected/black), Ongoing Orders, and History. (No Earnings card).
 * E. Available Orders Section: Header ("Available Orders" + "View All >") + List of 3-column product cards.
 *
 * Supports both Light and Dark themes dynamically.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToOrders: () -> Unit = {},
    onNavigateToActiveOrders: () -> Unit = onNavigateToOrders,
    onNavigateToOrderDetails: (Order) -> Unit = {},
    onAcceptOrder: (Order) -> Unit = onNavigateToOrderDetails,
    onNavigateToEarnings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToKyc: () -> Unit = {},
    onNavigateToHistory: () -> Unit = onNavigateToOrders
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface

    // Editorial Palette (Clean Monochrome in Dark Mode, no blue tints)
    val screenBg = if (isDark) DarkBackground else Color.White
    val cardBg = if (isDark) Color(0xFF141414) else Color.White
    val pillBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F6F8)
    val borderCol = if (isDark) Color(0xFF282828) else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) Color.White else Color.Black
    val textSecondary = if (isDark) Color(0xFFAAAAAA) else Color(0xFF64748B)
    val textMuted = if (isDark) Color(0xFF757575) else Color(0xFF888888)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        if (uiState.isLoading) {
            LoadingView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Section A: Header
                HomeHeader(
                    textPrimary = textPrimary,
                    isDark = isDark,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToProfile = onNavigateToProfile
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section B: Online/Offline Status & Location Controls
                HomeStatusAndLocationRow(
                    isOnline = uiState.isOnline,
                    isUpdatingStatus = uiState.isUpdatingStatus,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    pillBg = pillBg,
                    borderCol = borderCol,
                    onToggleOnline = {
                        viewModel.toggleOnlineStatus(!uiState.isOnline)
                    },
                    onLocationClick = {
                        Toast.makeText(context, "Location updated: Bengaluru Hub", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section C: KYC Verification Banner
                HomeKycBanner(
                    kycStatus = uiState.partner?.kycStatus,
                    isDark = isDark,
                    onClick = onNavigateToKyc
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section D: Exactly Three Quick-Action Cards
                HomeQuickActionsRow(
                    textPrimary = textPrimary,
                    pillBg = pillBg,
                    borderCol = borderCol,
                    isDark = isDark,
                    onNavigateToOrders = onNavigateToOrders,
                    onNavigateToActiveOrders = onNavigateToActiveOrders,
                    onNavigateToHistory = onNavigateToHistory
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section E: Available Orders Heading Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Orders",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNavigateToOrders)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "View all available orders",
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section F: Available Orders List or Empty/Offline State
                if (!uiState.isOnline || uiState.availableOrdersPreview.isEmpty()) {
                    AvailableOrdersEmptyState(
                        isOnline = uiState.isOnline,
                        backgroundColor = pillBg,
                        borderColor = borderCol,
                        textPrimary = textPrimary,
                        textMuted = textMuted
                    )
                } else {
                    uiState.availableOrdersPreview.forEach { order ->
                        AvailableOrderCard(
                            order = order,
                            cardBg = cardBg,
                            thumbnailBg = pillBg,
                            borderCol = borderCol,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            isDark = isDark,
                            onAccept = { onAcceptOrder(order) },
                            onCardClick = { onNavigateToOrderDetails(order) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Bottom padding to ensure cards aren't clipped by the floating bottom navigation dock
                Spacer(modifier = Modifier.height(105.dp))
            }
        }
    }
}

/**
 * Section A: Top Header
 * Left: "payent" bold wordmark + "DELIVERY" blue uppercase lettering
 * Right: Notification bell with unread badge + Profile avatar circle button
 */
@Composable
private fun HomeHeader(
    textPrimary: Color,
    isDark: Boolean,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "payent",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                color = textPrimary
            )
            Text(
                text = "DELIVERY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp
                ),
                color = if (isDark) Color.White else Color(0xFF2563EB)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notifications Icon Button with badge dot
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNavigateToNotifications),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = textPrimary,
                    modifier = Modifier.size(24.dp)
                )

                // Unread notification badge at top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(14.dp)
                        .background(Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }

            // Profile Avatar Circle Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF222222) else Color(0xFFE5E7EB))
                    .clickable(onClick = onNavigateToProfile),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Section B: Online / Offline Status and Location Controls
 * Left: Status capsule pill with green indicator dot, label, and dropdown chevron
 * Right: "My Location" pill + GPS target icon button
 */
@Composable
private fun HomeStatusAndLocationRow(
    isOnline: Boolean,
    isUpdatingStatus: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    pillBg: Color,
    borderCol: Color,
    onToggleOnline: () -> Unit,
    onLocationClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Online / Offline Status Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(pillBg)
                .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                .clickable(enabled = !isUpdatingStatus, onClick = onToggleOnline)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUpdatingStatus) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 1.5.dp,
                    color = textPrimary
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(
                            if (isOnline) OnlineGreen else Color(0xFF757575),
                            CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = if (isOnline) "Online" else "Offline",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = textPrimary
            )

            Spacer(modifier = Modifier.width(5.dp))

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = "Toggle status",
                tint = textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        // Right: Location Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "My Location" pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(pillBg)
                    .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                    .clickable(onClick = onLocationClick)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = textPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "My Location",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = textPrimary
                )
            }

            // Target / GPS circular button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(pillBg)
                    .border(1.dp, borderCol, CircleShape)
                    .clickable(onClick = onLocationClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = "Current location",
                    tint = textPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/**
 * Section C: Prominent Black KYC Verification Banner
 * - Blue rounded shield icon on the left
 * - Heading: "KYC Verification"
 * - Subtext: "Complete your document verification to access all features."
 * - White action button: "Complete Now ->"
 */
@Composable
private fun HomeKycBanner(
    kycStatus: KycStatus?,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val (title, subtitle, buttonText) = when (kycStatus) {
        KycStatus.APPROVED, KycStatus.VERIFIED -> Triple(
            "KYC Verified",
            "Your identity is verified. Ready to accept all deliveries.",
            "Verified ✓"
        )
        KycStatus.DOCUMENTS_SUBMITTED, KycStatus.PENDING, KycStatus.SUBMITTED_DEMO -> Triple(
            "KYC Verification Pending",
            "Verification in progress. Estimated turnaround 24-48 hrs.",
            "View Status"
        )
        KycStatus.REJECTED -> Triple(
            "KYC Needs Attention",
            "Verification was not approved. Tap to update documents.",
            "Re-verify"
        )
        else -> Triple(
            "KYC Verification",
            "Complete your document verification to access all features.",
            "Complete Now"
        )
    }

    val bannerBg = if (isDark) Color(0xFF141414) else Color(0xFF0F1117)
    val bannerBorder = if (isDark) Color(0xFF282828) else Color(0xFF1E2433)
    val shieldBg = if (isDark) Color.White else Color(0xFF2563EB)
    val shieldTint = if (isDark) Color.Black else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bannerBg)
            .border(1.dp, bannerBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shield badge (pure white in dark mode, blue in light mode)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shieldBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = shieldTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center text column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                ),
                color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF94A3B8),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // White rounded action button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Section D: Exactly Three Quick-Action Cards
 * Card 1: Available Orders (Selected/White in dark mode, Black in light mode)
 * Card 2: Ongoing Orders (Very light grey background in light mode, dark neutral in dark mode)
 * Card 3: History (Very light grey background in light mode, dark neutral in dark mode)
 * Strictly NO Earnings card or widget here!
 */
@Composable
private fun HomeQuickActionsRow(
    textPrimary: Color,
    pillBg: Color,
    borderCol: Color,
    isDark: Boolean,
    onNavigateToOrders: () -> Unit,
    onNavigateToActiveOrders: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Available Orders (Selected: Solid white card in dark mode, solid black in light mode)
        val card1Bg = if (isDark) Color.White else Color(0xFF000000)
        val card1Fg = if (isDark) Color.Black else Color.White

        Box(
            modifier = Modifier
                .weight(1f)
                .height(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(card1Bg)
                .clickable(onClick = onNavigateToOrders)
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = "Available Orders",
                        tint = card1Fg,
                        modifier = Modifier.size(24.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = card1Fg,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "Available\nOrders",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                    ),
                    color = card1Fg
                )
            }
        }

        // Card 2: Ongoing Orders
        Box(
            modifier = Modifier
                .weight(1f)
                .height(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(pillBg)
                .border(1.dp, borderCol, RoundedCornerShape(16.dp))
                .clickable(onClick = onNavigateToActiveOrders)
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = "Ongoing Orders",
                    tint = textPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Ongoing\nOrders",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                    ),
                    color = textPrimary
                )
            }
        }

        // Card 3: History
        Box(
            modifier = Modifier
                .weight(1f)
                .height(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(pillBg)
                .border(1.dp, borderCol, RoundedCornerShape(16.dp))
                .clickable(onClick = onNavigateToHistory)
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = "History",
                    tint = textPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "History",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                    ),
                    color = textPrimary
                )
            }
        }
    }
}

/**
 * Section F: Available Order Card
 * 3-Column Layout:
 * - Left: Product thumbnail in rounded square
 * - Middle: Product title, address pin, schedule, and 3 compact chips (distance, pickup, drop)
 * - Right: Rental rate (e.g. ₹45/day) + Black rounded Accept button
 */
@Composable
private fun AvailableOrderCard(
    order: Order,
    cardBg: Color,
    thumbnailBg: Color,
    borderCol: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean,
    onAccept: () -> Unit,
    onCardClick: () -> Unit
) {
    val displayInfo = remember(order) { getOrderDisplayInfo(order) }
    val displayAddress = remember(order) {
        val raw = if (order.vendorAddress.isNotBlank()) order.vendorAddress else order.deliveryAddress
        raw.substringBefore(",").trim()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 0.dp else 2.5.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x0D000000)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .clickable(onClick = onCardClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Product Thumbnail in smooth rounded square (no harsh border)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F6F8)),
                contentAlignment = Alignment.Center
            ) {
                if (displayInfo.drawableRes != null) {
                    Image(
                        painter = painterResource(id = displayInfo.drawableRes),
                        contentDescription = displayInfo.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = displayInfo.icon,
                        contentDescription = null,
                        tint = textPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Middle Column: Product details and compact chips
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayInfo.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Location Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = displayAddress,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Schedule Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = displayInfo.schedule,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // FlowRow of Compact Chips: Distance, Pickup, Drop (wraps naturally, zero vertical clipping lines)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Chip 1: Distance
                    OrderCompactChip(
                        icon = Icons.Outlined.Schedule,
                        label = displayInfo.pickupDistance,
                        isDark = isDark,
                        textSecondary = textSecondary
                    )

                    // Chip 2: Pickup
                    OrderCompactChip(
                        icon = Icons.Outlined.Inventory2,
                        label = "Pickup",
                        isDark = isDark,
                        textSecondary = textSecondary
                    )

                    // Chip 3: Drop
                    OrderCompactChip(
                        icon = Icons.Outlined.NearMe,
                        label = "Drop",
                        isDark = isDark,
                        textSecondary = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Column: Rental Rate + Black Accept Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Rate: ₹XX/day
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹${order.payoutAmount.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )
                    Text(
                        text = "/day",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = textSecondary,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Black Rounded Accept Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color.White else Color.Black)
                        .clickable(onClick = onAccept)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Accept",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isDark) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

/**
 * Compact info chip for Distance, Pickup, and Drop (line-free, smooth rounded pill)
 */
@Composable
private fun OrderCompactChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDark: Boolean,
    textSecondary: Color
) {
    val chipBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF1F5F9)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(chipBg)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textSecondary,
            modifier = Modifier.size(9.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = textSecondary
        )
    }
}

/**
 * Empty state when no orders are available or partner is offline
 */
@Composable
private fun AvailableOrdersEmptyState(
    isOnline: Boolean,
    backgroundColor: Color,
    borderColor: Color,
    textPrimary: Color,
    textMuted: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 32.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = "No orders",
                tint = textMuted,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isOnline) "No available orders" else "You are offline",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isOnline) {
                    "New delivery requests in your area will appear here."
                } else {
                    "Switch to Online mode above to receive orders."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = textMuted
            )
        }
    }
}
