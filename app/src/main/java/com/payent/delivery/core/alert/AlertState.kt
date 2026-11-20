package com.payent.delivery.core.alert

import com.payent.delivery.domain.model.Order

sealed class AlertState {
    data object Idle : AlertState()

    data class Active(
        val event: OrderAssignmentEvent,
        val order: Order,
        val triggeredAt: Long = System.currentTimeMillis()
    ) : AlertState()
}

