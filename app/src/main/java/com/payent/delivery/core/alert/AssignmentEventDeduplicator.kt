package com.payent.delivery.core.alert

import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe deduplicator preventing duplicate alerts for the same assignment event or order ID.
 * Tracks processed event IDs and actively alerted/handled order IDs with timestamp expiration.
 */
class AssignmentEventDeduplicator(
    private val eventRetentionMillis: Long = 1000L * 60 * 60 * 24 // 24 hours
) {
    // Stores eventId -> timestamp
    private val processedEventIds = ConcurrentHashMap<String, Long>()

    // Stores orderId -> latest eventId
    private val handledOrderAssignments = ConcurrentHashMap<String, String>()

    // Stores currently active alerted orderId -> eventId
    private val activeAlertOrders = ConcurrentHashMap<String, String>()

    /**
     * Determines whether an incoming assignment event should be processed.
     * Returns false if the eventId has already been processed or if this exact order assignment is already active.
     */
    @Synchronized
    fun shouldProcessAssignment(event: OrderAssignmentEvent): Boolean {
        cleanupExpiredEvents()

        // 1. Check if this exact event ID has already been recorded
        if (processedEventIds.containsKey(event.eventId)) {
            return false
        }

        // 2. If the event is an ASSIGNED event, check if order is already actively alerting or already handled for this event
        if (event.eventType == AssignmentEventType.ASSIGNED) {
            val activeEventId = activeAlertOrders[event.orderId]
            if (activeEventId != null) {
                // Already actively alerting this order
                return false
            }

            val handledEventId = handledOrderAssignments[event.orderId]
            if (handledEventId != null && handledEventId == event.eventId) {
                return false
            }
        }

        return true
    }

    /**
     * Marks an assignment event as actively alerting.
     */
    @Synchronized
    fun markAlertActive(event: OrderAssignmentEvent) {
        val now = System.currentTimeMillis()
        processedEventIds[event.eventId] = now
        activeAlertOrders[event.orderId] = event.eventId
    }

    /**
     * Marks an alert for the given order as handled (e.g. accepted, acknowledged, dismissed, or cancelled).
     */
    @Synchronized
    fun markAlertHandled(orderId: String, eventId: String? = null) {
        val activeEventId = activeAlertOrders.remove(orderId)
        val finalEventId = eventId ?: activeEventId
        if (finalEventId != null) {
            handledOrderAssignments[orderId] = finalEventId
            processedEventIds[finalEventId] = System.currentTimeMillis()
        }
    }

    /**
     * Checks if a specific order currently has an active alert.
     */
    fun isOrderAlertActive(orderId: String): Boolean {
        return activeAlertOrders.containsKey(orderId)
    }

    /**
     * Clears all tracking state. Useful for test isolation.
     */
    @Synchronized
    fun reset() {
        processedEventIds.clear()
        handledOrderAssignments.clear()
        activeAlertOrders.clear()
    }

    private fun cleanupExpiredEvents() {
        val now = System.currentTimeMillis()
        val iterator = processedEventIds.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value > eventRetentionMillis) {
                iterator.remove()
            }
        }
    }
}

