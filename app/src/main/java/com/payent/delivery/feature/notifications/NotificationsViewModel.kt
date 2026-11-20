package com.payent.delivery.feature.notifications

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    companion object {
        val sampleDemoNotifications = listOf(
            NotificationItem(
                id = "notif_101",
                title = "Order Assigned",
                message = "New delivery order assigned to you.",
                filterCategory = NotificationFilter.ORDERS,
                group = NotificationGroup.TODAY,
                category = NotificationCategory.ORDER_UPDATE,
                relativeTime = "5 min ago",
                isRead = false,
                iconType = NotificationIconType.ORDER_ASSIGNED,
                targetRoute = "orders"
            ),
            NotificationItem(
                id = "notif_102",
                title = "Pickup Verified",
                message = "Vendor verification completed.",
                filterCategory = NotificationFilter.ORDERS,
                group = NotificationGroup.TODAY,
                category = NotificationCategory.PICKUP_REMINDER,
                relativeTime = "25 min ago",
                isRead = false,
                iconType = NotificationIconType.PICKUP_VERIFIED,
                targetRoute = "orders"
            ),
            NotificationItem(
                id = "notif_103",
                title = "Delivery Completed",
                message = "Order delivered successfully.",
                filterCategory = NotificationFilter.ORDERS,
                group = NotificationGroup.TODAY,
                category = NotificationCategory.DELIVERY_REMINDER,
                relativeTime = "1 hour ago",
                isRead = false,
                iconType = NotificationIconType.DELIVERY_COMPLETED,
                targetRoute = "orders"
            ),
            NotificationItem(
                id = "notif_104",
                title = "Payout Initiated",
                message = "₹2,850 payout has been transferred to your bank account.",
                filterCategory = NotificationFilter.PAYMENTS,
                group = NotificationGroup.YESTERDAY,
                category = NotificationCategory.EARNINGS_PAYOUT,
                relativeTime = "Yesterday, 6:45 PM",
                isRead = true,
                iconType = NotificationIconType.PAYOUT_INITIATED,
                targetRoute = "earnings"
            ),
            NotificationItem(
                id = "notif_105",
                title = "KYC Under Review",
                message = "Your documents are under review.",
                filterCategory = NotificationFilter.SYSTEM,
                group = NotificationGroup.YESTERDAY,
                category = NotificationCategory.KYC_STATUS,
                relativeTime = "Yesterday, 2:10 PM",
                isRead = true,
                iconType = NotificationIconType.KYC_UNDER_REVIEW,
                targetRoute = "kyc"
            ),
            NotificationItem(
                id = "notif_106",
                title = "Support Ticket Updated",
                message = "Your ticket #TKT10987 has been updated.",
                filterCategory = NotificationFilter.SYSTEM,
                group = NotificationGroup.THIS_WEEK,
                category = NotificationCategory.ANNOUNCEMENT,
                relativeTime = "3 Sep 2026",
                isRead = true,
                iconType = NotificationIconType.SUPPORT_TICKET_UPDATED,
                targetRoute = "help_support"
            )
        )
    }

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        _uiState.update {
            it.copy(
                notifications = sampleDemoNotifications,
                isAllRead = sampleDemoNotifications.all { n -> n.isRead }
            )
        }
    }

    fun selectFilter(filter: NotificationFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun markAsRead(id: String) {
        _uiState.update { state ->
            val updated = state.notifications.map {
                if (it.id == id) it.copy(isRead = true) else it
            }
            state.copy(
                notifications = updated,
                isAllRead = updated.all { it.isRead }
            )
        }
    }

    fun markAllAsRead() {
        _uiState.update { state ->
            val updated = state.notifications.map { it.copy(isRead = true) }
            state.copy(
                notifications = updated,
                isAllRead = true,
                noticeMessage = "All notifications marked as read."
            )
        }
    }

    fun requestClearAll() {
        _uiState.update { it.copy(showClearConfirmationDialog = true) }
    }

    fun dismissClearConfirmation() {
        _uiState.update { it.copy(showClearConfirmationDialog = false) }
    }

    fun confirmClearAll() {
        _uiState.update {
            it.copy(
                notifications = emptyList(),
                showClearConfirmationDialog = false,
                isAllRead = true,
                noticeMessage = "Notification list cleared."
            )
        }
    }

    fun clearNoticeMessage() {
        _uiState.update { it.copy(noticeMessage = null) }
    }
}
