package com.payent.delivery.feature.notifications

enum class NotificationFilter {
    ALL,
    ORDERS,
    PAYMENTS,
    SYSTEM
}

enum class NotificationGroup {
    TODAY,
    YESTERDAY,
    THIS_WEEK
}

enum class NotificationIconType {
    ORDER_ASSIGNED,
    PICKUP_VERIFIED,
    DELIVERY_COMPLETED,
    PAYOUT_INITIATED,
    KYC_UNDER_REVIEW,
    SUPPORT_TICKET_UPDATED
}

enum class NotificationCategory {
    ORDER_UPDATE,
    PICKUP_REMINDER,
    DELIVERY_REMINDER,
    EARNINGS_PAYOUT,
    KYC_STATUS,
    ANNOUNCEMENT
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val filterCategory: NotificationFilter = NotificationFilter.ORDERS,
    val group: NotificationGroup = NotificationGroup.TODAY,
    val category: NotificationCategory = NotificationCategory.ORDER_UPDATE,
    val relativeTime: String,
    val isRead: Boolean = false,
    val iconType: NotificationIconType = NotificationIconType.ORDER_ASSIGNED,
    val targetRoute: String? = null
)
