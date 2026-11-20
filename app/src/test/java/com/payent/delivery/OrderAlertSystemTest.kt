package com.payent.delivery

import com.payent.delivery.core.alert.AlertPlayer
import com.payent.delivery.core.alert.AlertState
import com.payent.delivery.core.alert.AssignmentEventDeduplicator
import com.payent.delivery.core.alert.AssignmentEventType
import com.payent.delivery.core.alert.OrderAlertManager
import com.payent.delivery.core.alert.OrderAlertNotifier
import com.payent.delivery.core.alert.OrderAssignmentEvent
import com.payent.delivery.core.alert.PayentPushMessageProcessor
import com.payent.delivery.core.common.DispatcherProvider
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.core.security.SessionManager
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.model.OrderStatus
import com.payent.delivery.domain.repository.OrdersRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderAlertSystemTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeDispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
        override val unconfined: CoroutineDispatcher = testDispatcher
    }

    private val fakeOrderUpdatesFlow = MutableStateFlow(true)
    private val fakeOrderAlertSoundFlow = MutableStateFlow(true)
    private val fakeOrderAlertVibrationFlow = MutableStateFlow(true)

    private val fakeSessionManager = object : SessionManager {
        override val authTokenFlow: Flow<String?> = MutableStateFlow("token")
        override val partnerIdFlow: Flow<String?> = MutableStateFlow("partner_101")
        override val partnerNameFlow: Flow<String?> = MutableStateFlow("Test Partner")
        override val isDemoSessionFlow: Flow<Boolean> = MutableStateFlow(true)
        override val isOnlineStatusFlow: Flow<Boolean> = MutableStateFlow(true)
        override val themeModeFlow: Flow<String> = MutableStateFlow("DARK")
        override val languageCodeFlow: Flow<String> = MutableStateFlow("en")
        override val orderUpdatesEnabledFlow: Flow<Boolean> = fakeOrderUpdatesFlow
        override val orderAlertSoundEnabledFlow: Flow<Boolean> = fakeOrderAlertSoundFlow
        override val orderAlertVibrationEnabledFlow: Flow<Boolean> = fakeOrderAlertVibrationFlow

        override fun getAuthTokenSync(): String = "token"
        override suspend fun saveAuthToken(token: String) {}
        override suspend fun savePartnerSession(partnerId: String, partnerName: String, token: String) {}
        override suspend fun saveDemoSession() {}
        override suspend fun saveOnlineStatus(isOnline: Boolean) {}
        override suspend fun saveThemeMode(mode: String) {}
        override suspend fun saveLanguageCode(code: String) {}
        override suspend fun saveOrderUpdatesEnabled(enabled: Boolean) { fakeOrderUpdatesFlow.value = enabled }
        override suspend fun saveOrderAlertSoundEnabled(enabled: Boolean) { fakeOrderAlertSoundFlow.value = enabled }
        override suspend fun saveOrderAlertVibrationEnabled(enabled: Boolean) { fakeOrderAlertVibrationFlow.value = enabled }
        override suspend fun clearSession() {}
    }

    private val sampleOrder = Order(
        id = "ord_test_001",
        orderNumber = "PAYENT-ORD-9001",
        vendorName = "Pro Camera Gear Hub",
        vendorAddress = "120 Main Road, Sector 5",
        deliveryAddress = "789 Park View Heights, Apt 4B",
        rentalPeriod = "2-Day DSLR Kit",
        payoutAmount = 65.00,
        status = OrderStatus.AVAILABLE
    )

    private val fakeOrdersRepository = object : OrdersRepository {
        var acceptedOrderId: String? = null

        override suspend fun getAvailableOrders(): NetworkResult<List<Order>> {
            return NetworkResult.Success(listOf(sampleOrder))
        }

        override suspend fun getActiveOrders(): NetworkResult<List<Order>> {
            return NetworkResult.Success(emptyList())
        }

        override suspend fun getOrderHistory(): NetworkResult<List<Order>> {
            return NetworkResult.Success(emptyList())
        }

        override suspend fun acceptOrder(orderId: String): NetworkResult<Order> {
            acceptedOrderId = orderId
            return NetworkResult.Success(sampleOrder.copy(status = OrderStatus.ACCEPTED))
        }

        override suspend fun updateAvailabilityStatus(isOnline: Boolean): NetworkResult<Boolean> {
            return NetworkResult.Success(true)
        }
    }

    private class FakeAlertSoundPlayer : AlertPlayer {
        var isPlayingAlert: Boolean = false
        var lastSoundEnabled: Boolean? = null
        var lastVibrationEnabled: Boolean? = null
        var stopCount: Int = 0

        override fun startAlert(soundEnabled: Boolean, vibrationEnabled: Boolean) {
            isPlayingAlert = true
            lastSoundEnabled = soundEnabled
            lastVibrationEnabled = vibrationEnabled
        }

        override fun stopAlert() {
            isPlayingAlert = false
            stopCount++
        }

        override fun isAlertPlaying(): Boolean = isPlayingAlert
    }

    private class FakeNotificationHelper : OrderAlertNotifier {
        val postedEvents = mutableListOf<Pair<OrderAssignmentEvent, Order>>()
        val cancelledOrderIds = mutableListOf<String>()

        override fun createNotificationChannel() {}

        override fun postOrderAlertNotification(event: OrderAssignmentEvent, order: Order) {
            postedEvents.add(event to order)
        }

        override fun cancelOrderAlertNotification(orderId: String) {
            cancelledOrderIds.add(orderId)
        }

        override fun cancelAllNotifications() {
            cancelledOrderIds.add("ALL")
        }
    }

    private lateinit var deduplicator: AssignmentEventDeduplicator
    private lateinit var fakePlayer: FakeAlertSoundPlayer
    private lateinit var fakeNotificationHelper: FakeNotificationHelper
    private lateinit var alertManager: OrderAlertManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeOrderUpdatesFlow.value = true
        fakeOrderAlertSoundFlow.value = true
        fakeOrderAlertVibrationFlow.value = true

        deduplicator = AssignmentEventDeduplicator()
        fakePlayer = FakeAlertSoundPlayer()
        fakeNotificationHelper = FakeNotificationHelper()

        alertManager = OrderAlertManager(
            sessionManager = fakeSessionManager,
            ordersRepository = fakeOrdersRepository,
            dispatchers = fakeDispatchers,
            deduplicator = deduplicator,
            player = fakePlayer,
            notificationHelper = fakeNotificationHelper
        )
    }

    @After
    fun tearDown() {
        alertManager.destroy()
        Dispatchers.resetMain()
    }

    @Test
    fun newAssignmentEvent_triggersAlertAndNotification() = runTest {
        alertManager.onAppForegrounded()

        val event = OrderAssignmentEvent(
            eventId = "evt_001",
            orderId = sampleOrder.id,
            orderNumber = sampleOrder.orderNumber,
            order = sampleOrder
        )

        alertManager.handleAssignmentEvent(event)

        val state = alertManager.alertState.value
        assertTrue("AlertState should be Active", state is AlertState.Active)
        assertEquals(sampleOrder.id, (state as AlertState.Active).order.id)

        // Verify audio/vibration player started
        assertTrue("Player should be playing", fakePlayer.isPlayingAlert)
        assertEquals(true, fakePlayer.lastSoundEnabled)
        assertEquals(true, fakePlayer.lastVibrationEnabled)

        // Verify notification was posted
        assertEquals(1, fakeNotificationHelper.postedEvents.size)
        assertEquals(sampleOrder.id, fakeNotificationHelper.postedEvents.first().second.id)
    }

    @Test
    fun duplicateAssignment_doesNotTriggerDuplicateAlert() = runTest {
        alertManager.onAppForegrounded()

        val event = OrderAssignmentEvent(
            eventId = "evt_dup_001",
            orderId = sampleOrder.id,
            orderNumber = sampleOrder.orderNumber,
            order = sampleOrder
        )

        // First delivery
        alertManager.handleAssignmentEvent(event)
        assertEquals(1, fakeNotificationHelper.postedEvents.size)

        // Re-delivery of identical event
        alertManager.handleAssignmentEvent(event)
        assertEquals("Duplicate event should be rejected", 1, fakeNotificationHelper.postedEvents.size)
    }

    @Test
    fun acknowledgement_stopsAlert() = runTest {
        alertManager.onAppForegrounded()

        val event = OrderAssignmentEvent(
            eventId = "evt_ack_001",
            orderId = sampleOrder.id,
            orderNumber = sampleOrder.orderNumber,
            order = sampleOrder
        )

        alertManager.handleAssignmentEvent(event)
        assertTrue(alertManager.alertState.value is AlertState.Active)
        assertTrue(fakePlayer.isPlayingAlert)

        // Rider opens/acknowledges the assigned order
        alertManager.acknowledgeAlert(sampleOrder.id)

        assertTrue("AlertState should be Idle after acknowledgement", alertManager.alertState.value is AlertState.Idle)
        assertFalse("Player should be stopped", fakePlayer.isPlayingAlert)
    }

    @Test
    fun successfulAcceptance_stopsAlert() = runTest {
        alertManager.onAppForegrounded()

        val event = OrderAssignmentEvent(
            eventId = "evt_acc_001",
            orderId = sampleOrder.id,
            orderNumber = sampleOrder.orderNumber,
            order = sampleOrder
        )

        alertManager.handleAssignmentEvent(event)
        assertTrue(alertManager.alertState.value is AlertState.Active)

        // Rider accepts the order successfully
        alertManager.onOrderAccepted(sampleOrder.id)

        assertTrue("AlertState should be Idle after acceptance", alertManager.alertState.value is AlertState.Idle)
        assertFalse("Player should be stopped", fakePlayer.isPlayingAlert)
        assertTrue("Notification should be cancelled", fakeNotificationHelper.cancelledOrderIds.contains(sampleOrder.id))
    }

    @Test
    fun cancellation_stopsAlert() = runTest {
        alertManager.onAppForegrounded()

        val event = OrderAssignmentEvent(
            eventId = "evt_cancel_001",
            orderId = sampleOrder.id,
            orderNumber = sampleOrder.orderNumber,
            order = sampleOrder
        )

        alertManager.handleAssignmentEvent(event)
        assertTrue(alertManager.alertState.value is AlertState.Active)

        // Backend notifies that the order was cancelled
        alertManager.onOrderCancelled(sampleOrder.id, reason = "Renter cancelled order")

        assertTrue("AlertState should be Idle after cancellation", alertManager.alertState.value is AlertState.Idle)
        assertFalse("Player should be stopped", fakePlayer.isPlayingAlert)
        assertTrue("Notification should be cancelled", fakeNotificationHelper.cancelledOrderIds.contains(sampleOrder.id))
    }

    @Test
    fun reassignmentToAnotherRider_stopsOldRiderAlert() = runTest {
        alertManager.onAppForegrounded()

        val event = OrderAssignmentEvent(
            eventId = "evt_reassign_001",
            orderId = sampleOrder.id,
            orderNumber = sampleOrder.orderNumber,
            order = sampleOrder
        )

        alertManager.handleAssignmentEvent(event)
        assertTrue(alertManager.alertState.value is AlertState.Active)

        // Order reassigned to another delivery partner
        alertManager.onOrderReassigned(sampleOrder.id)

        assertTrue("AlertState should be Idle after reassignment", alertManager.alertState.value is AlertState.Idle)
        assertFalse("Player should be stopped", fakePlayer.isPlayingAlert)
        assertTrue("Notification should be cancelled", fakeNotificationHelper.cancelledOrderIds.contains(sampleOrder.id))
    }

    @Test
    fun multipleOrders_doNotCreateOverlappingAlertLoops() = runTest {
        alertManager.onAppForegrounded()

        val order1 = sampleOrder.copy(id = "order_101", orderNumber = "PAYENT-ORD-101")
        val order2 = sampleOrder.copy(id = "order_102", orderNumber = "PAYENT-ORD-102")

        val event1 = OrderAssignmentEvent(
            eventId = "evt_multi_1",
            orderId = order1.id,
            orderNumber = order1.orderNumber,
            order = order1
        )
        val event2 = OrderAssignmentEvent(
            eventId = "evt_multi_2",
            orderId = order2.id,
            orderNumber = order2.orderNumber,
            order = order2
        )

        // First assignment triggers
        alertManager.handleAssignmentEvent(event1)
        assertEquals("order_101", (alertManager.alertState.value as AlertState.Active).order.id)
        assertTrue(fakePlayer.isPlayingAlert)
        val stopCountBefore = fakePlayer.stopCount

        // Second assignment arrives while first is active
        alertManager.handleAssignmentEvent(event2)

        // Old alert must be stopped cleanly without overlapping loop
        assertTrue("Previous alert player must be stopped before new one starts", fakePlayer.stopCount > stopCountBefore)
        assertTrue("Player should still be running for new order", fakePlayer.isPlayingAlert)
        assertEquals("order_102", (alertManager.alertState.value as AlertState.Active).order.id)
    }

    @Test
    fun lifecycleCleanup_worksCorrectly() = runTest {
        alertManager.onAppForegrounded()

        val event = OrderAssignmentEvent(
            eventId = "evt_life_001",
            orderId = sampleOrder.id,
            orderNumber = sampleOrder.orderNumber,
            order = sampleOrder
        )
        alertManager.handleAssignmentEvent(event)
        assertTrue(fakePlayer.isPlayingAlert)

        // When app goes into the background
        alertManager.onAppBackgrounded()

        // Foreground player must stop so background notification handles ring/vibrate
        assertFalse("Foreground MediaPlayer should stop in background", fakePlayer.isPlayingAlert)

        // When app returns to foreground
        alertManager.onAppForegrounded()
        assertTrue("Foreground player resumes when app returns to foreground", fakePlayer.isPlayingAlert)

        // Complete destroy
        alertManager.destroy()
        assertFalse(fakePlayer.isPlayingAlert)
        assertTrue(alertManager.alertState.value is AlertState.Idle)
    }

    @Test
    fun userSettingDisabled_doesNotTriggerAlert() = runTest {
        alertManager.onAppForegrounded()
        fakeOrderUpdatesFlow.value = false // User turned off order updates in Settings

        val event = OrderAssignmentEvent(
            eventId = "evt_disabled_001",
            orderId = sampleOrder.id,
            orderNumber = sampleOrder.orderNumber,
            order = sampleOrder
        )

        alertManager.handleAssignmentEvent(event)

        assertTrue("Alert should remain Idle when user disabled updates", alertManager.alertState.value is AlertState.Idle)
        assertFalse(fakePlayer.isPlayingAlert)
        assertEquals(0, fakeNotificationHelper.postedEvents.size)
    }

    @Test
    fun pushPayloadProcessor_correctlyParsesAndDispatches() = runTest {
        val payload = mapOf(
            PayentPushMessageProcessor.KEY_EVENT_ID to "push_evt_99",
            PayentPushMessageProcessor.KEY_ORDER_ID to "ord_push_99",
            PayentPushMessageProcessor.KEY_ORDER_NUMBER to "PAYENT-ORD-9999",
            PayentPushMessageProcessor.KEY_ACTION to "ASSIGNED",
            PayentPushMessageProcessor.KEY_VENDOR_NAME to "Push Hub",
            PayentPushMessageProcessor.KEY_VENDOR_ADDRESS to "99 Push Street",
            PayentPushMessageProcessor.KEY_DELIVERY_ADDRESS to "88 Push Avenue",
            PayentPushMessageProcessor.KEY_RENTAL_PERIOD to "Camera Kit",
            PayentPushMessageProcessor.KEY_PAYOUT_AMOUNT to "75.00"
        )

        val processed = PayentPushMessageProcessor.processPayload(payload, alertManager)
        assertTrue(processed)

        val active = alertManager.alertState.value as AlertState.Active
        assertEquals("ord_push_99", active.order.id)
        assertEquals("PAYENT-ORD-9999", active.order.orderNumber)
        assertEquals(75.00, active.order.payoutAmount, 0.01)
    }
}
