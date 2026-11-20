package com.payent.delivery.core.alert

import android.content.Context
import android.util.Log
import com.payent.delivery.core.common.DispatcherProvider
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.core.security.SessionManager
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.model.OrderStatus
import com.payent.delivery.domain.repository.OrdersRepository
import com.payent.delivery.feature.orders.OrdersViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Central coordinator for the PayentDelivery New Order Assignment Alert System.
 * Manages event deduplication, alert lifecycle, audio/vibration loops, system notifications,
 * and stop alert conditions.
 */
class OrderAlertManager(
    private val sessionManager: SessionManager,
    private val ordersRepository: OrdersRepository,
    private val dispatchers: DispatcherProvider,
    private val deduplicator: AssignmentEventDeduplicator = AssignmentEventDeduplicator(),
    private val player: AlertPlayer,
    private val notificationHelper: OrderAlertNotifier
) {
    constructor(
        context: Context,
        sessionManager: SessionManager,
        ordersRepository: OrdersRepository,
        dispatchers: DispatcherProvider,
        deduplicator: AssignmentEventDeduplicator = AssignmentEventDeduplicator()
    ) : this(
        sessionManager = sessionManager,
        ordersRepository = ordersRepository,
        dispatchers = dispatchers,
        deduplicator = deduplicator,
        player = AlertSoundVibrationPlayer(context),
        notificationHelper = OrderAlertNotificationHelper(context)
    )

    companion object {
        private const val TAG = "OrderAlertManager"

        const val ACTION_VIEW_ASSIGNMENT = OrderAlertNotificationHelper.ACTION_VIEW_ASSIGNMENT
        const val EXTRA_ORDER_ID = OrderAlertNotificationHelper.EXTRA_ORDER_ID
        const val EXTRA_EVENT_ID = OrderAlertNotificationHelper.EXTRA_EVENT_ID
    }

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)

    private val _alertState = MutableStateFlow<AlertState>(AlertState.Idle)
    val alertState: StateFlow<AlertState> = _alertState.asStateFlow()

    private var isAppInForeground: Boolean = false

    private fun log(msg: String) {
        try {
            Log.d(TAG, msg)
        } catch (_: Throwable) {
            // JVM test fallback
        }
    }

    /**
     * Handles an incoming order assignment event (e.g. from FCM push, background worker, or backend poll).
     */
    suspend fun handleAssignmentEvent(event: OrderAssignmentEvent) {
        log("Incoming assignment event: eventId=${event.eventId}, orderId=${event.orderId}, type=${event.eventType}")

        // 1. Check event type
        when (event.eventType) {
            AssignmentEventType.CANCELLED -> {
                onOrderCancelled(event.orderId, event.reason)
                return
            }
            AssignmentEventType.REASSIGNED -> {
                onOrderReassigned(event.orderId)
                return
            }
            AssignmentEventType.EXPIRED -> {
                onAssignmentExpired(event.orderId)
                return
            }
            AssignmentEventType.ASSIGNED -> {
                // Proceed to assignment alert flow
            }
        }

        // 2. Duplicate and race condition protection
        if (!deduplicator.shouldProcessAssignment(event)) {
            log("Duplicate or already active event ignored: eventId=${event.eventId}, orderId=${event.orderId}")
            return
        }

        // 3. Check user preference master toggle
        val orderUpdatesEnabled = sessionManager.orderUpdatesEnabledFlow.first()
        if (!orderUpdatesEnabled) {
            log("Order updates are disabled in user settings, ignoring alert.")
            return
        }

        // 4. Resolve the full order model
        val resolvedOrder = resolveOrder(event)
        if (resolvedOrder == null || resolvedOrder.status == OrderStatus.CANCELLED || resolvedOrder.status == OrderStatus.DELIVERED) {
            log("Order is not available for assignment or is already handled: ${event.orderId}")
            return
        }

        // 5. If an existing alert is active, stop it cleanly first before activating new alert
        stopActiveAlertInternal(isSuperceded = true)

        // 6. Mark alert as active in deduplicator
        deduplicator.markAlertActive(event)

        // 7. Post high-importance system notification
        notificationHelper.postOrderAlertNotification(event, resolvedOrder)

        // 8. Update state
        val activeState = AlertState.Active(event = event, order = resolvedOrder)
        _alertState.value = activeState

        // 9. If app is currently in foreground, start looping audio & vibration
        if (isAppInForeground) {
            val soundEnabled = sessionManager.orderAlertSoundEnabledFlow.first()
            val vibrationEnabled = sessionManager.orderAlertVibrationEnabledFlow.first()
            withContext(dispatchers.main) {
                player.startAlert(soundEnabled = soundEnabled, vibrationEnabled = vibrationEnabled)
            }
        }
    }

    fun handleAssignmentEventAsync(event: OrderAssignmentEvent) {
        scope.launch {
            handleAssignmentEvent(event)
        }
    }

    /**
     * Stop Condition 1: Rider opens and acknowledges the assigned order.
     */
    fun acknowledgeAlert(orderId: String) {
        val current = _alertState.value
        if (current is AlertState.Active && current.order.id == orderId) {
            log("Alert acknowledged by rider for orderId=$orderId")
            deduplicator.markAlertHandled(orderId, current.event.eventId)
            stopActiveAlertInternal()
        }
    }

    /**
     * Stop Condition 2: Rider accepts the order successfully.
     */
    fun onOrderAccepted(orderId: String) {
        val current = _alertState.value
        if (current is AlertState.Active && current.order.id == orderId) {
            log("Order $orderId accepted successfully. Stopping alert.")
            deduplicator.markAlertHandled(orderId, current.event.eventId)
            notificationHelper.cancelOrderAlertNotification(orderId)
            stopActiveAlertInternal()
        }
    }

    /**
     * Accepts the order directly from the alert action.
     */
    suspend fun acceptOrder(orderId: String, onAccepted: (Order) -> Unit = {}, onError: (String) -> Unit = {}) {
        val current = _alertState.value
        if (current !is AlertState.Active || current.order.id != orderId) {
            onError("No active alert found for order $orderId")
            return
        }

        val order = current.order
        // Stop sound & vibration immediately
        withContext(dispatchers.main) {
            player.stopAlert()
        }
        notificationHelper.cancelOrderAlertNotification(orderId)
        deduplicator.markAlertHandled(orderId, current.event.eventId)
        _alertState.value = AlertState.Idle

        // Execute atomic accept against backend or demo repository
        if (order.id.startsWith("demo_")) {
            val acceptedOrder = order.copy(status = OrderStatus.ACCEPTED)
            withContext(dispatchers.main) {
                onAccepted(acceptedOrder)
            }
        } else {
            when (val result = ordersRepository.acceptOrder(orderId)) {
                is NetworkResult.Success -> {
                    withContext(dispatchers.main) {
                        onAccepted(result.data)
                    }
                }
                is NetworkResult.ApiError -> {
                    withContext(dispatchers.main) {
                        onError(result.message ?: "Failed to accept order (${result.code})")
                    }
                }
                else -> {
                    withContext(dispatchers.main) {
                        onError("Network error accepting order.")
                    }
                }
            }
        }
    }

    fun acceptOrderAsync(orderId: String, onAccepted: (Order) -> Unit = {}, onError: (String) -> Unit = {}) {
        scope.launch {
            acceptOrder(orderId, onAccepted, onError)
        }
    }

    /**
     * Stop Condition 3: Rider explicitly dismisses the alert without accepting.
     */
    fun dismissAlert(orderId: String) {
        val current = _alertState.value
        if (current is AlertState.Active && current.order.id == orderId) {
            log("Alert explicitly dismissed for orderId=$orderId")
            deduplicator.markAlertHandled(orderId, current.event.eventId)
            notificationHelper.cancelOrderAlertNotification(orderId)
            stopActiveAlertInternal()
        }
    }

    /**
     * Stop Condition 4: The order is cancelled.
     */
    fun onOrderCancelled(orderId: String, reason: String? = null) {
        val current = _alertState.value
        if (current is AlertState.Active && current.order.id == orderId) {
            log("Order $orderId was cancelled: $reason. Stopping alert.")
            deduplicator.markAlertHandled(orderId, current.event.eventId)
            notificationHelper.cancelOrderAlertNotification(orderId)
            stopActiveAlertInternal()
        }
    }

    /**
     * Stop Condition 5: The order is no longer assigned to this rider / reassigned.
     */
    fun onOrderReassigned(orderId: String) {
        val current = _alertState.value
        if (current is AlertState.Active && current.order.id == orderId) {
            log("Order $orderId was reassigned to another rider. Stopping alert.")
            deduplicator.markAlertHandled(orderId, current.event.eventId)
            notificationHelper.cancelOrderAlertNotification(orderId)
            stopActiveAlertInternal()
        }
    }

    /**
     * Stop Condition 6: The assignment expires or is superseded.
     */
    fun onAssignmentExpired(orderId: String) {
        val current = _alertState.value
        if (current is AlertState.Active && current.order.id == orderId) {
            log("Assignment for order $orderId expired. Stopping alert.")
            deduplicator.markAlertHandled(orderId, current.event.eventId)
            notificationHelper.cancelOrderAlertNotification(orderId)
            stopActiveAlertInternal()
        }
    }

    /**
     * Lifecycle Callback: Invoked when MainActivity enters the foreground.
     */
    fun onAppForegrounded() {
        isAppInForeground = true
        val current = _alertState.value
        if (current is AlertState.Active) {
            scope.launch {
                val soundEnabled = sessionManager.orderAlertSoundEnabledFlow.first()
                val vibrationEnabled = sessionManager.orderAlertVibrationEnabledFlow.first()
                withContext(dispatchers.main) {
                    player.startAlert(soundEnabled = soundEnabled, vibrationEnabled = vibrationEnabled)
                }
            }
        }
    }

    /**
     * Lifecycle Callback: Invoked when MainActivity goes into the background.
     */
    fun onAppBackgrounded() {
        isAppInForeground = false
        // In background, stop foreground MediaPlayer and Vibrator so Android notification handles it cleanly
        scope.launch(dispatchers.main) {
            player.stopAlert()
        }
    }

    private fun stopActiveAlertInternal(isSuperceded: Boolean = false) {
        player.stopAlert()
        if (!isSuperceded) {
            _alertState.value = AlertState.Idle
        }
    }

    private suspend fun resolveOrder(event: OrderAssignmentEvent): Order? {
        if (event.order != null) return event.order

        // Check sample demo orders if matching demo id
        val sample = OrdersViewModel.sampleDemoOrders.firstOrNull { it.id == event.orderId || it.orderNumber == event.orderNumber }
        if (sample != null) return sample

        // Otherwise query available orders from repository
        return when (val result = ordersRepository.getAvailableOrders()) {
            is NetworkResult.Success -> {
                result.data.firstOrNull { it.id == event.orderId || it.orderNumber == event.orderNumber }
                    ?: Order(
                        id = event.orderId,
                        orderNumber = event.orderNumber,
                        vendorName = "Assigned Vendor Hub",
                        vendorAddress = "Scheduled Vendor Address",
                        deliveryAddress = "Customer Destination Address",
                        rentalPeriod = "Rental Order Dispatch",
                        payoutAmount = 55.00,
                        status = OrderStatus.AVAILABLE
                    )
            }
            else -> {
                Order(
                    id = event.orderId,
                    orderNumber = event.orderNumber,
                    vendorName = "Assigned Vendor Hub",
                    vendorAddress = "Scheduled Vendor Address",
                    deliveryAddress = "Customer Destination Address",
                    rentalPeriod = "Rental Order Dispatch",
                    payoutAmount = 55.00,
                    status = OrderStatus.AVAILABLE
                )
            }
        }
    }

    fun destroy() {
        player.stopAlert()
        _alertState.value = AlertState.Idle
    }
}
