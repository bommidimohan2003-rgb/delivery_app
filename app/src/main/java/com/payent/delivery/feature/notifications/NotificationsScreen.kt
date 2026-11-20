package com.payent.delivery.feature.notifications

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkSurface

/**
 * Editorial Redesign of Notifications (Screen 4) matching the reference design.
 *
 * Structure:
 * 1. Top Bar: Back arrow on left + Centered "Notifications" title + Settings gear on right
 * 2. Four Horizontal Filter Tabs: All, Orders, Payments, System (Selected: Black, Inactive: Light grey)
 * 3. Grouped Notifications by Time:
 *    - Today: Order Assigned, Pickup Verified, Delivery Completed (with unread blue dots)
 *    - Yesterday: Payout Initiated, KYC Under Review
 *    - This Week: Support Ticket Updated
 */
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit = onNavigateBack,
    onNavigateToRoute: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val isDark = MaterialTheme.colorScheme.background == DarkBackground ||
            MaterialTheme.colorScheme.surface == DarkSurface

    // Editorial Palette
    val screenBg = if (isDark) DarkBackground else Color.White
    val textPrimary = if (isDark) Color.White else Color(0xFF111111)
    val textSecondary = if (isDark) Color(0xFFAAAAAA) else Color(0xFF6B7280)
    val filterInactiveBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val dividerCol = if (isDark) Color(0xFF222222) else Color(0xFFF1F3F5)

    // Filter notifications based on selected filter
    val filteredList = uiState.notifications.filter { item ->
        when (uiState.selectedFilter) {
            NotificationFilter.ALL -> true
            NotificationFilter.ORDERS -> item.filterCategory == NotificationFilter.ORDERS
            NotificationFilter.PAYMENTS -> item.filterCategory == NotificationFilter.PAYMENTS
            NotificationFilter.SYSTEM -> item.filterCategory == NotificationFilter.SYSTEM
        }
    }

    val todayItems = filteredList.filter { it.group == NotificationGroup.TODAY }
    val yesterdayItems = filteredList.filter { it.group == NotificationGroup.YESTERDAY }
    val thisWeekItems = filteredList.filter { it.group == NotificationGroup.THIS_WEEK }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Section 1: Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Notification Settings",
                        tint = textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Four Horizontal Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    NotificationFilter.ALL to "All",
                    NotificationFilter.ORDERS to "Orders",
                    NotificationFilter.PAYMENTS to "Payments",
                    NotificationFilter.SYSTEM to "System"
                ).forEach { (filter, label) ->
                    val isSelected = uiState.selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(
                                if (isSelected) {
                                    if (isDark) Color.White else Color.Black
                                } else filterInactiveBg
                            )
                            .clickable { viewModel.selectFilter(filter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) {
                                if (isDark) Color.Black else Color.White
                            } else textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Grouped Notifications
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No notifications in this filter",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = textSecondary
                        )
                    }
                }
            } else {
                // Group 1: Today
                if (todayItems.isNotEmpty()) {
                    NotificationGroupSection(
                        title = "Today",
                        items = todayItems,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        dividerCol = dividerCol,
                        isDark = isDark,
                        onItemClick = { item ->
                            viewModel.markAsRead(item.id)
                            item.targetRoute?.let { onNavigateToRoute(it) }
                        }
                    )
                }

                // Group 2: Yesterday
                if (yesterdayItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NotificationGroupSection(
                        title = "Yesterday",
                        items = yesterdayItems,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        dividerCol = dividerCol,
                        isDark = isDark,
                        onItemClick = { item ->
                            viewModel.markAsRead(item.id)
                            item.targetRoute?.let { onNavigateToRoute(it) }
                        }
                    )
                }

                // Group 3: This Week
                if (thisWeekItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NotificationGroupSection(
                        title = "This Week",
                        items = thisWeekItems,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        dividerCol = dividerCol,
                        isDark = isDark,
                        onItemClick = { item ->
                            viewModel.markAsRead(item.id)
                            item.targetRoute?.let { onNavigateToRoute(it) }
                        }
                    )
                }
            }

            // Bottom padding to avoid overlapping the floating bottom navigation dock
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun NotificationGroupSection(
    title: String,
    items: List<NotificationItem>,
    textPrimary: Color,
    textSecondary: Color,
    dividerCol: Color,
    isDark: Boolean,
    onItemClick: (NotificationItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = textPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        items.forEachIndexed { index, item ->
            NotificationRow(
                item = item,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                isDark = isDark,
                onClick = { onItemClick(item) }
            )

            if (index < items.size - 1) {
                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = dividerCol,
                    modifier = Modifier.padding(start = 52.dp)
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon in Circular Background matching reference design
        val (iconBg, iconTint) = when (item.iconType) {
            NotificationIconType.ORDER_ASSIGNED -> Pair(
                if (isDark) Color(0xFF3B1E1E) else Color(0xFFFEE2E2),
                Color(0xFFEF4444)
            )
            NotificationIconType.PICKUP_VERIFIED -> Pair(
                if (isDark) Color(0xFF132E1E) else Color(0xFFDCFCE7),
                Color(0xFF16A34A)
            )
            NotificationIconType.DELIVERY_COMPLETED -> Pair(
                if (isDark) Color(0xFF1E222A) else Color(0xFFF3F4F6),
                if (isDark) Color.White else Color(0xFF111111)
            )
            NotificationIconType.PAYOUT_INITIATED -> Pair(
                if (isDark) Color(0xFF1E2430) else Color(0xFFF1F5F9),
                if (isDark) Color.White else Color(0xFF111111)
            )
            NotificationIconType.KYC_UNDER_REVIEW -> Pair(
                if (isDark) Color(0xFF332A15) else Color(0xFFFEF3C7),
                Color(0xFFD97706)
            )
            NotificationIconType.SUPPORT_TICKET_UPDATED -> Pair(
                if (isDark) Color(0xFF1E222A) else Color(0xFFF3F4F6),
                if (isDark) Color.White else Color(0xFF111111)
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            when (item.iconType) {
                NotificationIconType.ORDER_ASSIGNED -> {
                    Icon(
                        imageVector = Icons.Outlined.TwoWheeler,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                NotificationIconType.PICKUP_VERIFIED -> {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                NotificationIconType.DELIVERY_COMPLETED -> {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                NotificationIconType.PAYOUT_INITIATED -> {
                    Icon(
                        imageVector = Icons.Outlined.CurrencyRupee,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                NotificationIconType.KYC_UNDER_REVIEW -> {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                NotificationIconType.SUPPORT_TICKET_UPDATED -> {
                    Icon(
                        imageVector = Icons.Outlined.SupportAgent,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Content: Title + Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.relativeTime,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp
                        ),
                        color = textSecondary
                    )

                    // Blue Unread Indicator Dot
                    if (!item.isRead) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.message,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                ),
                color = textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
