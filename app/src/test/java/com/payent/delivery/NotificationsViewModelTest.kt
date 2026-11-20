package com.payent.delivery

import com.payent.delivery.feature.notifications.NotificationsViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationsViewModelTest {

    @Test
    fun notifications_initialDataLoaded() {
        val viewModel = NotificationsViewModel()
        val state = viewModel.uiState.value

        assertTrue(state.notifications.isNotEmpty())
        assertFalse(state.notifications.all { it.isRead })
    }

    @Test
    fun markAsRead_updatesSingleItem() {
        val viewModel = NotificationsViewModel()
        val unreadItem = viewModel.uiState.value.notifications.first { !it.isRead }

        viewModel.markAsRead(unreadItem.id)

        val updatedItem = viewModel.uiState.value.notifications.first { it.id == unreadItem.id }
        assertTrue(updatedItem.isRead)
    }

    @Test
    fun markAllAsRead_marksAllRead() {
        val viewModel = NotificationsViewModel()
        viewModel.markAllAsRead()

        assertTrue(viewModel.uiState.value.notifications.all { it.isRead })
        assertTrue(viewModel.uiState.value.isAllRead)
        assertNotNull(viewModel.uiState.value.noticeMessage)
    }

    @Test
    fun clearAll_clearsNotificationList() {
        val viewModel = NotificationsViewModel()
        viewModel.requestClearAll()
        assertTrue(viewModel.uiState.value.showClearConfirmationDialog)

        viewModel.confirmClearAll()

        assertTrue(viewModel.uiState.value.notifications.isEmpty())
        assertFalse(viewModel.uiState.value.showClearConfirmationDialog)
    }
}
