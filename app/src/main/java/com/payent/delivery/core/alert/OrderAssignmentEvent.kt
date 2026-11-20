package com.payent.delivery.core.alert

import com.payent.delivery.domain.model.Order

enum class AssignmentEventType {
    ASSIGNED,
    CANCELLED,
    REASSIGNED,
    EXPIRED
}

/**
 * Unique assignment event representing a new, updated, or cancelled order assignment.
 * Every incoming event has a unique eventId and orderId to enforce idempotency and deduplication.
 */
data class OrderAssignmentEvent(
    val eventId: String,
    val orderId: String,
    val orderNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val order: Order? = null,
    val eventType: AssignmentEventType = AssignmentEventType.ASSIGNED,
    val reason: String? = null
)

