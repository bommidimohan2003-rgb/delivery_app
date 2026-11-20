package com.payent.delivery.core.alert

import android.util.Log
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.model.OrderStatus

/**
 * Parses and processes raw push notification payloads (from Firebase Cloud Messaging RemoteMessage or backend sockets).
 * Translates them into strongly-typed [OrderAssignmentEvent] instances for the [OrderAlertManager].
 */
object PayentPushMessageProcessor {

    private const val TAG = "PushMessageProcessor"

    const val KEY_EVENT_ID = "event_id"
    const val KEY_ORDER_ID = "order_id"
    const val KEY_ORDER_NUMBER = "order_number"
    const val KEY_ACTION = "action" // ASSIGNED, CANCELLED, REASSIGNED, EXPIRED
    const val KEY_REASON = "reason"
    const val KEY_VENDOR_NAME = "vendor_name"
    const val KEY_VENDOR_ADDRESS = "vendor_address"
    const val KEY_DELIVERY_ADDRESS = "delivery_address"
    const val KEY_RENTAL_PERIOD = "rental_period"
    const val KEY_PAYOUT_AMOUNT = "payout_amount"

    /**
     * Parses key-value data map (compatible with Firebase RemoteMessage.getData())
     * and delivers to [OrderAlertManager].
     */
    suspend fun processPayload(
        data: Map<String, String>,
        alertManager: OrderAlertManager
    ): Boolean {
        val eventId = data[KEY_EVENT_ID] ?: data["id"]
        val orderId = data[KEY_ORDER_ID] ?: data["orderId"]

        if (eventId.isNullOrBlank() || orderId.isNullOrBlank()) {
            return false
        }

        val orderNumber = data[KEY_ORDER_NUMBER] ?: "PAYENT-ORD-$orderId"
        val action = data[KEY_ACTION]?.uppercase() ?: "ASSIGNED"
        val reason = data[KEY_REASON]

        val eventType = when (action) {
            "CANCELLED", "ORDER_CANCELLED" -> AssignmentEventType.CANCELLED
            "REASSIGNED", "ORDER_REASSIGNED" -> AssignmentEventType.REASSIGNED
            "EXPIRED", "ASSIGNMENT_EXPIRED" -> AssignmentEventType.EXPIRED
            else -> AssignmentEventType.ASSIGNED
        }

        val order: Order? = if (eventType == AssignmentEventType.ASSIGNED && data.containsKey(KEY_VENDOR_NAME)) {
            Order(
                id = orderId,
                orderNumber = orderNumber,
                vendorName = data[KEY_VENDOR_NAME] ?: "Vendor Partner",
                vendorAddress = data[KEY_VENDOR_ADDRESS] ?: "Vendor Pickup Point",
                deliveryAddress = data[KEY_DELIVERY_ADDRESS] ?: "Customer Delivery Destination",
                rentalPeriod = data[KEY_RENTAL_PERIOD] ?: "Rental Item",
                payoutAmount = data[KEY_PAYOUT_AMOUNT]?.toDoubleOrNull() ?: 50.00,
                status = OrderStatus.AVAILABLE
            )
        } else {
            null
        }

        val event = OrderAssignmentEvent(
            eventId = eventId,
            orderId = orderId,
            orderNumber = orderNumber,
            eventType = eventType,
            order = order,
            reason = reason
        )

        alertManager.handleAssignmentEvent(event)
        return true
    }
}
