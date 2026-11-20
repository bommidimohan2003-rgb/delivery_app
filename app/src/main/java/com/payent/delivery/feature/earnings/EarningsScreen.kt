package com.payent.delivery.feature.earnings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.payent.delivery.domain.model.DailyOrderEarning
import com.payent.delivery.domain.model.DayEarningsBreakdown
import com.payent.delivery.domain.model.EarningsTimeRange
import com.payent.delivery.domain.model.WeekEarningsBreakdown
import java.util.Locale

/**
 * Editorial Redesign of PayentDelivery Earnings Screen
 *
 * Implements strict visual corrections:
 * 1. Single-line currency amounts with zero vertical line-wrapping.
 * 2. Non-wrapping Active status badge.
 * 3. Evenly aligned summary metric columns.
 * 4. Monday-first calendar grouping (W1 to W6 for 6-week months like Nov 2026).
 * 5. Horizontal chart scrolling for multiple daily orders to avoid label overlap.
 * 6. Responsive order detail and completed orders cards.
 * 7. Bottom navigation overlap fix ensuring the final card is completely scroll-accessible.
 */
@Composable
fun EarningsScreen(
    viewModel: EarningsViewModel,
    onNavigateToDetails: () -> Unit = {},
    onNavigateToPayoutHistory: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Editorial White Palette
    val screenBg = Color.White
    val cardBg = Color.White
    val surfaceLightGrey = Color(0xFFF9FAFB)
    val cardBorder = Color(0xFFE5E7EB)
    val textPrimary = Color(0xFF111111)
    val textSecondary = Color(0xFF6B7280)
    val textMuted = Color(0xFF9CA3AF)
    val accentGreen = Color(0xFF16A34A)
    val primaryBlack = Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Header: "My Earnings" + Payout link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Earnings",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Financial performance & payout ledger",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                        color = textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceLightGrey)
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                        .clickable(onClick = onNavigateToPayoutHistory)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalance,
                            contentDescription = "Payouts",
                            tint = textPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Payouts",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Summary Card (Responsive, non-wrapping, evenly aligned)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceLightGrey)
                    .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    // Header Row: Title & Active Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (uiState.selectedTimeRange) {
                                EarningsTimeRange.DAILY -> "TOTAL EARNINGS TODAY"
                                EarningsTimeRange.WEEKLY -> "TOTAL EARNINGS THIS WEEK"
                                EarningsTimeRange.MONTHLY -> "TOTAL EARNINGS THIS MONTH"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Active Badge - strictly wrapped in one horizontal line
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                    contentDescription = null,
                                    tint = accentGreen,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Active",
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = accentGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Prominent Total Amount - Guaranteed Single Line
                    Text(
                        text = formatRupees(uiState.periodEarningsRupees),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = cardBorder, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 KPI Summary Columns - Evenly distributed with weight(1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Completed Deliveries",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${uiState.completedDeliveriesCount}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = textPrimary,
                                maxLines = 1
                            )
                        }

                        Column(
                            modifier = Modifier.weight(0.9f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Transactions",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${uiState.transactionCount}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = textPrimary,
                                maxLines = 1
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1.1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = when (uiState.selectedTimeRange) {
                                    EarningsTimeRange.DAILY -> "Avg per Order"
                                    EarningsTimeRange.WEEKLY -> "Average Daily"
                                    EarningsTimeRange.MONTHLY -> "Average Weekly"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = formatRupees(uiState.averageMetricRupees),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Daily / Weekly / Monthly Segmented Tabs
            EarningsSegmentedTabs(
                selectedRange = uiState.selectedTimeRange,
                onSelectRange = { range -> viewModel.selectTimeRange(range) },
                primaryBlack = primaryBlack,
                surfaceGrey = surfaceLightGrey,
                cardBorder = cardBorder,
                textSecondary = textSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Tab Content
            AnimatedContent(
                targetState = uiState.selectedTimeRange,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "EarningsTabContent"
            ) { targetRange ->
                when (targetRange) {
                    EarningsTimeRange.DAILY -> {
                        DailyEarningsView(
                            uiState = uiState,
                            viewModel = viewModel,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            textMuted = textMuted,
                            cardBg = cardBg,
                            surfaceGrey = surfaceLightGrey,
                            cardBorder = cardBorder,
                            accentGreen = accentGreen,
                            primaryBlack = primaryBlack
                        )
                    }
                    EarningsTimeRange.WEEKLY -> {
                        WeeklyEarningsView(
                            uiState = uiState,
                            viewModel = viewModel,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            textMuted = textMuted,
                            cardBg = cardBg,
                            surfaceGrey = surfaceLightGrey,
                            cardBorder = cardBorder,
                            accentGreen = accentGreen,
                            primaryBlack = primaryBlack
                        )
                    }
                    EarningsTimeRange.MONTHLY -> {
                        MonthlyEarningsView(
                            uiState = uiState,
                            viewModel = viewModel,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            textMuted = textMuted,
                            cardBg = cardBg,
                            surfaceGrey = surfaceLightGrey,
                            cardBorder = cardBorder,
                            accentGreen = accentGreen,
                            primaryBlack = primaryBlack
                        )
                    }
                }
            }

            // Generous bottom scroll clearance ensuring the final card is 100% accessible above the floating bottom navigation bar
            Spacer(modifier = Modifier.navigationBarsPadding().height(160.dp))
        }
    }
}

// =====================================================================
// SEGMENTED TAB SWITCHER
// =====================================================================

@Composable
private fun EarningsSegmentedTabs(
    selectedRange: EarningsTimeRange,
    onSelectRange: (EarningsTimeRange) -> Unit,
    primaryBlack: Color,
    surfaceGrey: Color,
    cardBorder: Color,
    textSecondary: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(surfaceGrey)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EarningsTabItem(
                title = "Daily",
                isSelected = selectedRange == EarningsTimeRange.DAILY,
                onClick = { onSelectRange(EarningsTimeRange.DAILY) },
                primaryBlack = primaryBlack,
                textSecondary = textSecondary,
                modifier = Modifier.weight(1f)
            )

            EarningsTabItem(
                title = "Weekly",
                isSelected = selectedRange == EarningsTimeRange.WEEKLY,
                onClick = { onSelectRange(EarningsTimeRange.WEEKLY) },
                primaryBlack = primaryBlack,
                textSecondary = textSecondary,
                modifier = Modifier.weight(1f)
            )

            EarningsTabItem(
                title = "Monthly",
                isSelected = selectedRange == EarningsTimeRange.MONTHLY,
                onClick = { onSelectRange(EarningsTimeRange.MONTHLY) },
                primaryBlack = primaryBlack,
                textSecondary = textSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EarningsTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryBlack: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) primaryBlack else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 13.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (isSelected) Color.White else textSecondary
        )
    }
}

// =====================================================================
// SECTION 1: DAILY EARNINGS VIEW (Time-Based Bar Graph with Horiz Scroll)
// =====================================================================

@Composable
private fun DailyEarningsView(
    uiState: EarningsUiState,
    viewModel: EarningsViewModel,
    textPrimary: Color,
    textSecondary: Color,
    textMuted: Color,
    cardBg: Color,
    surfaceGrey: Color,
    cardBorder: Color,
    accentGreen: Color,
    primaryBlack: Color
) {
    val daily = uiState.dailyBreakdown

    Column(modifier = Modifier.fillMaxWidth()) {
        // Date Navigation Selector
        DateNavigationHeader(
            title = daily.fullDateText,
            onPrevious = { viewModel.previousDay() },
            onNext = { viewModel.nextDay() },
            isNextDisabled = uiState.isDailyNextDayDisabled,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            surfaceGrey = surfaceGrey,
            cardBorder = cardBorder
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Time-Based Bar Graph Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "ORDER EARNINGS TIMELINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (daily.orders.isNotEmpty()) "${daily.orders.size} deliveries completed" else "0 deliveries",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = textPrimary,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatRupees(daily.totalEarningsRupees),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (daily.totalEarningsRupees > 0) accentGreen else textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (daily.orders.isEmpty()) {
                    DailyZeroEarningsPlaceholder(
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        surfaceGrey = surfaceGrey,
                        cardBorder = cardBorder
                    )
                } else {
                    // Time-based Bar Chart with Horizontal Scroll for multi-order support
                    DailyOrderTimeBarGraph(
                        orders = daily.orders,
                        selectedOrder = uiState.selectedOrderInDaily,
                        onSelectOrder = { viewModel.selectOrder(it) },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        surfaceGrey = surfaceGrey,
                        primaryBlack = primaryBlack
                    )

                    // Selected Order Inspection Detail Card
                    uiState.selectedOrderInDaily?.let { selected ->
                        Spacer(modifier = Modifier.height(16.dp))
                        DailyOrderDetailCard(
                            order = selected,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            surfaceGrey = surfaceGrey,
                            cardBorder = cardBorder,
                            accentGreen = accentGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chronological Completed Orders List
        Text(
            text = "Completed Orders (${daily.orders.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            color = textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (daily.orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(surfaceGrey)
                    .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = textMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Orders Delivered",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You did not complete any delivery orders on this date.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                daily.orders.forEach { order ->
                    val isSelected = uiState.selectedOrderInDaily?.orderId == order.orderId
                    CompletedOrderItemCard(
                        order = order,
                        isSelected = isSelected,
                        onClick = { viewModel.selectOrder(order) },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBg = cardBg,
                        surfaceGrey = surfaceGrey,
                        cardBorder = cardBorder,
                        accentGreen = accentGreen,
                        primaryBlack = primaryBlack
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyOrderTimeBarGraph(
    orders: List<DailyOrderEarning>,
    selectedOrder: DailyOrderEarning?,
    onSelectOrder: (DailyOrderEarning) -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    surfaceGrey: Color,
    primaryBlack: Color
) {
    val maxEarnings = (orders.maxOfOrNull { it.earningsRupees } ?: 1.0).coerceAtLeast(100.0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceGrey)
            .padding(vertical = 10.dp)
    ) {
        // Horizontally scrollable row prevents overlapping when many orders exist
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            horizontalArrangement = if (orders.size <= 4) Arrangement.SpaceEvenly else Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            orders.forEach { order ->
                val isSelected = selectedOrder?.orderId == order.orderId
                val heightFraction = (order.earningsRupees / maxEarnings).toFloat().coerceIn(0.12f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectOrder(order) }
                        .padding(horizontal = 4.dp)
                ) {
                    // Amount Label above bar
                    Text(
                        text = "₹${order.earningsRupees.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) primaryBlack else textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar Column
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height((100 * heightFraction).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                when {
                                    isSelected -> primaryBlack
                                    else -> Color(0xFFCBD5E1)
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Time Label
                    Text(
                        text = order.completedTimeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) primaryBlack else textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyOrderDetailCard(
    order: DailyOrderEarning,
    textPrimary: Color,
    textSecondary: Color,
    surfaceGrey: Color,
    cardBorder: Color,
    accentGreen: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceGrey)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = accentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Delivered at ${order.completedTimeText}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "+${formatRupees(order.earningsRupees)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = accentGreen,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${order.pickupLocation} → ${order.deliveryLocation}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DailyZeroEarningsPlaceholder(
    textPrimary: Color,
    textSecondary: Color,
    surfaceGrey: Color,
    cardBorder: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceGrey)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "₹0.00",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textSecondary,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No deliveries were completed on this date.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CompletedOrderItemCard(
    order: DailyOrderEarning,
    isSelected: Boolean,
    onClick: () -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    surfaceGrey: Color,
    cardBorder: Color,
    accentGreen: Color,
    primaryBlack: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFFF1F5F9) else cardBg)
            .border(
                1.dp,
                if (isSelected) primaryBlack else cardBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) primaryBlack else surfaceGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeliveryDining,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = order.orderNumber,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = order.completedTimeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                color = textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.wrapContentWidth(Alignment.End),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "+${formatRupees(order.earningsRupees)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = accentGreen,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Route summary
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${order.pickupLocation} → ${order.deliveryLocation}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// =====================================================================
// SECTION 2: WEEKLY EARNINGS VIEW (7-Day Breakdown)
// =====================================================================

@Composable
private fun WeeklyEarningsView(
    uiState: EarningsUiState,
    viewModel: EarningsViewModel,
    textPrimary: Color,
    textSecondary: Color,
    textMuted: Color,
    cardBg: Color,
    surfaceGrey: Color,
    cardBorder: Color,
    accentGreen: Color,
    primaryBlack: Color
) {
    val weekly = uiState.weeklyBreakdown

    Column(modifier = Modifier.fillMaxWidth()) {
        // Date Navigation Selector
        DateNavigationHeader(
            title = weekly.dateRangeText,
            onPrevious = { viewModel.previousWeek() },
            onNext = { viewModel.nextWeek() },
            isNextDisabled = uiState.isWeeklyNextWeekDisabled,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            surfaceGrey = surfaceGrey,
            cardBorder = cardBorder
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7-Bar Chart Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "7-DAY WEEKLY DISTRIBUTION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap a bar to view day details",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = textPrimary,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatRupees(weekly.totalEarningsRupees),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (weekly.totalEarningsRupees > 0) accentGreen else textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 7-Bar Chart Component
                Weekly7BarGraph(
                    days = weekly.days,
                    selectedDay = uiState.selectedDayInWeekly,
                    onSelectDay = { viewModel.selectDayInWeekly(it) },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    surfaceGrey = surfaceGrey,
                    accentGreen = accentGreen,
                    primaryBlack = primaryBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 7-Day Breakdown List
        Text(
            text = "Day-Wise Breakdown",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            color = textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            weekly.days.forEach { day ->
                val isSelected = uiState.selectedDayInWeekly?.dateIso == day.dateIso
                WeeklyDayRowItem(
                    day = day,
                    isSelected = isSelected,
                    onClick = {
                        viewModel.selectDayInWeekly(day)
                        viewModel.navigateToDailyForDate(day.dateIso)
                    },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    cardBg = cardBg,
                    surfaceGrey = surfaceGrey,
                    cardBorder = cardBorder,
                    accentGreen = accentGreen,
                    primaryBlack = primaryBlack
                )
            }
        }
    }
}

@Composable
private fun Weekly7BarGraph(
    days: List<DayEarningsBreakdown>,
    selectedDay: DayEarningsBreakdown?,
    onSelectDay: (DayEarningsBreakdown) -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    surfaceGrey: Color,
    accentGreen: Color,
    primaryBlack: Color
) {
    val maxEarnings = (days.maxOfOrNull { it.totalEarningsRupees } ?: 1.0).coerceAtLeast(100.0)
    val highestDayDateIso = days.maxByOrNull { it.totalEarningsRupees }?.takeIf { it.totalEarningsRupees > 0 }?.dateIso

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceGrey)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { day ->
                val isSelected = selectedDay?.dateIso == day.dateIso
                val isHighest = day.dateIso == highestDayDateIso
                val heightFraction = if (day.totalEarningsRupees > 0) {
                    (day.totalEarningsRupees / maxEarnings).toFloat().coerceIn(0.12f, 1f)
                } else 0.04f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectDay(day) }
                        .padding(horizontal = 2.dp)
                ) {
                    // Amount / Highest Indicator
                    Text(
                        text = if (day.totalEarningsRupees > 0) "₹${day.totalEarningsRupees.toInt()}" else "-",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected || isHighest) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = when {
                            isSelected -> primaryBlack
                            isHighest -> accentGreen
                            else -> textSecondary
                        },
                        maxLines = 1,
                        softWrap = false
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar Column
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height((100 * heightFraction).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                when {
                                    isSelected -> primaryBlack
                                    isHighest -> Color(0xFF15803D)
                                    day.totalEarningsRupees > 0 -> Color(0xFFCBD5E1)
                                    else -> Color(0xFFE2E8F0)
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = day.dayOfWeek,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = if (isSelected) primaryBlack else textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        text = "${day.dayOfMonth}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = if (isSelected) primaryBlack else textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyDayRowItem(
    day: DayEarningsBreakdown,
    isSelected: Boolean,
    onClick: () -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    surfaceGrey: Color,
    cardBorder: Color,
    accentGreen: Color,
    primaryBlack: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFF1F5F9) else cardBg)
            .border(
                1.dp,
                if (isSelected) primaryBlack else cardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) primaryBlack else surfaceGrey),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayOfWeek.take(2).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) Color.White else textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${day.dayOfWeek}, ${day.dayOfMonth}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (day.completedDeliveriesCount > 0) "${day.completedDeliveriesCount} deliveries completed" else "Rest day (0 deliveries)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier.wrapContentWidth(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatRupees(day.totalEarningsRupees),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (day.totalEarningsRupees > 0) accentGreen else textSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "View Day",
                    tint = textSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// =====================================================================
// SECTION 3: MONTHLY EARNINGS VIEW (Calendar-Week W1–W6 Breakdown)
// =====================================================================

@Composable
private fun MonthlyEarningsView(
    uiState: EarningsUiState,
    viewModel: EarningsViewModel,
    textPrimary: Color,
    textSecondary: Color,
    textMuted: Color,
    cardBg: Color,
    surfaceGrey: Color,
    cardBorder: Color,
    accentGreen: Color,
    primaryBlack: Color
) {
    val monthly = uiState.monthlyBreakdown

    Column(modifier = Modifier.fillMaxWidth()) {
        // Month Selector Header
        DateNavigationHeader(
            title = monthly.monthYearText,
            onPrevious = { viewModel.previousMonth() },
            onNext = { viewModel.nextMonth() },
            isNextDisabled = uiState.isMonthlyNextMonthDisabled,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            surfaceGrey = surfaceGrey,
            cardBorder = cardBorder
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar-Week Bar Chart Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "CALENDAR-WEEK BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${monthly.weeks.size} calendar weeks in ${monthly.monthName}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = textPrimary,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatRupees(monthly.totalEarningsRupees),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (monthly.totalEarningsRupees > 0) accentGreen else textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Monthly Calendar-Week Bar Graph (W1..W6)
                MonthlyCalendarWeekBarGraph(
                    weeks = monthly.weeks,
                    selectedWeek = uiState.selectedWeekInMonthly,
                    onSelectWeek = { viewModel.selectWeekInMonthly(it) },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    surfaceGrey = surfaceGrey,
                    primaryBlack = primaryBlack
                )

                // Selected Week Detail Card below graph
                uiState.selectedWeekInMonthly?.let { selected ->
                    Spacer(modifier = Modifier.height(14.dp))
                    SelectedWeekTooltipCard(
                        week = selected,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        surfaceGrey = surfaceGrey,
                        cardBorder = cardBorder,
                        accentGreen = accentGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Week Breakdown List
        Text(
            text = "Weeks in ${monthly.monthName}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            color = textPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            monthly.weeks.forEach { week ->
                val isSelected = uiState.selectedWeekInMonthly?.weekNumber == week.weekNumber
                MonthlyWeekRowItem(
                    week = week,
                    isSelected = isSelected,
                    onClick = {
                        viewModel.selectWeekInMonthly(week)
                        viewModel.navigateToWeeklyForWeek(week.weekNumber)
                    },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    cardBg = cardBg,
                    surfaceGrey = surfaceGrey,
                    cardBorder = cardBorder,
                    accentGreen = accentGreen,
                    primaryBlack = primaryBlack
                )
            }
        }
    }
}

@Composable
private fun MonthlyCalendarWeekBarGraph(
    weeks: List<WeekEarningsBreakdown>,
    selectedWeek: WeekEarningsBreakdown?,
    onSelectWeek: (WeekEarningsBreakdown) -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    surfaceGrey: Color,
    primaryBlack: Color
) {
    val maxEarnings = (weeks.maxOfOrNull { it.totalEarningsRupees } ?: 1.0).coerceAtLeast(100.0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceGrey)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            weeks.forEach { week ->
                val isSelected = selectedWeek?.weekNumber == week.weekNumber
                val heightFraction = if (week.totalEarningsRupees > 0) {
                    (week.totalEarningsRupees / maxEarnings).toFloat().coerceIn(0.12f, 1f)
                } else 0.04f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectWeek(week) }
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = if (week.totalEarningsRupees > 0) "₹${week.totalEarningsRupees.toInt()}" else "-",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) primaryBlack else textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height((100 * heightFraction).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                when {
                                    isSelected -> primaryBlack
                                    week.totalEarningsRupees > 0 -> Color(0xFFCBD5E1)
                                    else -> Color(0xFFE2E8F0)
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = week.weekLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = if (isSelected) primaryBlack else textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedWeekTooltipCard(
    week: WeekEarningsBreakdown,
    textPrimary: Color,
    textSecondary: Color,
    surfaceGrey: Color,
    cardBorder: Color,
    accentGreen: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceGrey)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = accentGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = week.dateRangeText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${week.completedDeliveriesCount} completed deliveries",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatRupees(week.totalEarningsRupees),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (week.totalEarningsRupees > 0) accentGreen else textSecondary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun MonthlyWeekRowItem(
    week: WeekEarningsBreakdown,
    isSelected: Boolean,
    onClick: () -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    surfaceGrey: Color,
    cardBorder: Color,
    accentGreen: Color,
    primaryBlack: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFF1F5F9) else cardBg)
            .border(
                1.dp,
                if (isSelected) primaryBlack else cardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) primaryBlack else surfaceGrey),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "W${week.weekNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) Color.White else textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = week.dateRangeText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${week.completedDeliveriesCount} completed deliveries",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier.wrapContentWidth(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatRupees(week.totalEarningsRupees),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (week.totalEarningsRupees > 0) accentGreen else textSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "View Week",
                    tint = textSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// =====================================================================
// SHARED UI COMPONENTS & HELPERS
// =====================================================================

@Composable
private fun DateNavigationHeader(
    title: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isNextDisabled: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    surfaceGrey: Color,
    cardBorder: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(surfaceGrey)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = "Previous",
                tint = textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onNext,
            enabled = !isNextDisabled,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Next",
                tint = if (isNextDisabled) Color(0xFFD1D5DB) else textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatRupees(amount: Double): String {
    return String.format(Locale.US, "₹%,.2f", amount)
}
