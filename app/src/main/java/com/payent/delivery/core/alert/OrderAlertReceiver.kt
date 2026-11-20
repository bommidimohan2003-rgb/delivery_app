package com.payent.delivery.core.alert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.payent.delivery.PayentApplication

/**
 * Handles broadcast intents dispatched from notifications, push listeners, or system actions.
 */
class OrderAlertReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "OrderAlertReceiver"

        const val ACTION_ACCEPT_ASSIGNMENT = OrderAlertNotificationHelper.ACTION_ACCEPT_ASSIGNMENT
        const val ACTION_DISMISS_ASSIGNMENT = OrderAlertNotificationHelper.ACTION_DISMISS_ASSIGNMENT
        const val ACTION_TRIGGER_ASSIGNMENT = "com.payent.delivery.action.TRIGGER_ASSIGNMENT"

        const val EXTRA_ORDER_ID = OrderAlertNotificationHelper.EXTRA_ORDER_ID
        const val EXTRA_EVENT_ID = OrderAlertNotificationHelper.EXTRA_EVENT_ID
        const val EXTRA_ORDER_NUMBER = "com.payent.delivery.extra.ORDER_NUMBER"
        const val EXTRA_EVENT_TYPE = "com.payent.delivery.extra.EVENT_TYPE"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val app = context.applicationContext as? PayentApplication ?: return
        val alertManager = app.appContainer.orderAlertManager

        val action = intent.action
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: "event_${System.currentTimeMillis()}"

        Log.d(TAG, "Received broadcast action=$action for orderId=$orderId")

        when (action) {
            ACTION_DISMISS_ASSIGNMENT -> {
                if (orderId.isNotBlank()) {
                    alertManager.dismissAlert(orderId)
                }
            }
            ACTION_ACCEPT_ASSIGNMENT -> {
                if (orderId.isNotBlank()) {
                    alertManager.acceptOrderAsync(
                        orderId = orderId,
                        onAccepted = { acceptedOrder ->
                            Log.d(TAG, "Order ${acceptedOrder.orderNumber} successfully accepted via notification action")
                        },
                        onError = { error ->
                            Log.e(TAG, "Error accepting order $orderId: $error")
                        }
                    )
                }
            }
            ACTION_TRIGGER_ASSIGNMENT -> {
                val orderNumber = intent.getStringExtra(EXTRA_ORDER_NUMBER) ?: "PAYENT-ORD-$orderId"
                val eventTypeStr = intent.getStringExtra(EXTRA_EVENT_TYPE) ?: "ASSIGNED"
                val eventType = try {
                    AssignmentEventType.valueOf(eventTypeStr)
                } catch (_: Exception) {
                    AssignmentEventType.ASSIGNED
                }

                val event = OrderAssignmentEvent(
                    eventId = eventId,
                    orderId = orderId.ifBlank { "order_${System.currentTimeMillis()}" },
                    orderNumber = orderNumber,
                    eventType = eventType
                )
                alertManager.handleAssignmentEventAsync(event)
            }
        }
    }
}
