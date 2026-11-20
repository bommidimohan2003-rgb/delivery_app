package com.payent.delivery.feature.notifications

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val showClearConfirmationDialog: Boolean = false,
    val isAllRead: Boolean = false,
    val isLoading: Boolean = false,
    val noticeMessage: String? = null
)
