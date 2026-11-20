package com.payent.delivery

import com.payent.delivery.core.common.DispatcherProvider
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.data.remote.dto.AuthResponseDto
import com.payent.delivery.domain.model.AuthState
import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.model.EarningsSummary
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.model.OrderStatus
import com.payent.delivery.domain.repository.AuthRepository
import com.payent.delivery.domain.repository.EarningsRepository
import com.payent.delivery.domain.repository.OrdersRepository
import com.payent.delivery.feature.earnings.EarningsViewModel
import com.payent.delivery.feature.home.HomeViewModel
import com.payent.delivery.feature.orders.OrderTab
import com.payent.delivery.feature.orders.OrdersViewModel
import com.payent.delivery.feature.orders.isActive
import com.payent.delivery.feature.orders.isAvailable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class ViewModelsTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testDispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
        override val unconfined: CoroutineDispatcher = testDispatcher
    }

    private val mockAuthRepository = object : AuthRepository {
        override val authStateFlow: Flow<AuthState> = MutableStateFlow(AuthState.Unauthenticated)
        override suspend fun register(
            fullName: String,
            email: String,
            phoneNumber: String,
            password: String
        ): NetworkResult<AuthResponseDto> =
            NetworkResult.Success(AuthResponseDto(partnerId = "1", name = fullName))

        override suspend fun login(email: String, password: String): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(DeliveryPartner(id = "1", name = "Test Partner", phoneNumber = "+1234567890"))

        override suspend fun startDemoLogin(): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(DeliveryPartner(id = "demo_partner", name = "Demo Delivery Partner", phoneNumber = "+1000000000"))

        override suspend fun requestOtp(phoneNumber: String): NetworkResult<Boolean> = NetworkResult.Success(true)
        override suspend fun verifyOtp(phoneNumber: String, otp: String): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(DeliveryPartner(id = "1", name = "Test Partner", phoneNumber = phoneNumber))
        override suspend fun getPartnerProfile(): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(DeliveryPartner(id = "1", name = "Test Partner", phoneNumber = "+1234567890"))
        override suspend fun logout(): NetworkResult<Boolean> = NetworkResult.Success(true)
    }

    private val mockOrdersRepository = object : OrdersRepository {
        override suspend fun getAvailableOrders(): NetworkResult<List<Order>> = NetworkResult.Success(emptyList())
        override suspend fun getActiveOrders(): NetworkResult<List<Order>> = NetworkResult.Success(emptyList())
        override suspend fun getOrderHistory(): NetworkResult<List<Order>> = NetworkResult.Success(emptyList())
        override suspend fun acceptOrder(orderId: String): NetworkResult<Order> = NetworkResult.ApiError(404, "Not found")
        override suspend fun updateAvailabilityStatus(isOnline: Boolean): NetworkResult<Boolean> = NetworkResult.Success(true)
    }

    private val mockEarningsRepository = object : EarningsRepository {
        override suspend fun getEarningsSummary(): NetworkResult<EarningsSummary> = NetworkResult.Success(
            EarningsSummary(
                totalEarnings = 0.0,
                todayEarnings = 0.0,
                weeklyEarnings = 0.0,
                monthlyEarnings = 0.0,
                completedDeliveriesCount = 0,
                nextPayoutDate = "Thursday",
                payoutStatus = "Scheduled"
            )
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun homeViewModel_loadsPartnerDataSuccessfully() = runTest {
        val viewModel = HomeViewModel(mockAuthRepository, mockOrdersRepository)
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isBackendConnected)
        assertEquals("Test Partner", state.partner?.name)
    }

    @Test
    fun homeViewModel_togglesOnlineStatusLocally() = runTest {
        val viewModel = HomeViewModel(mockAuthRepository, mockOrdersRepository)
        viewModel.toggleOnlineStatus(true)

        assertTrue(viewModel.uiState.value.isOnline)
    }

    @Test
    fun homeViewModel_toggleOnlineStatus_transitionsOnlineToOffline() = runTest {
        val viewModel = HomeViewModel(mockAuthRepository, mockOrdersRepository)
        viewModel.toggleOnlineStatus(true)
        assertTrue(viewModel.uiState.value.isOnline)

        viewModel.toggleOnlineStatus(false)
        assertFalse(viewModel.uiState.value.isOnline)
    }

    @Test
    fun homeViewModel_toggleOnlineStatus_preventsRedundantUpdates() = runTest {
        val viewModel = HomeViewModel(mockAuthRepository, mockOrdersRepository)
        viewModel.toggleOnlineStatus(true)
        assertTrue(viewModel.uiState.value.isOnline)

        // Calling toggle with identical state should be a no-op
        viewModel.toggleOnlineStatus(true)
        assertTrue(viewModel.uiState.value.isOnline)
        assertFalse(viewModel.uiState.value.isUpdatingStatus)
    }

    @Test
    fun homeViewModel_toggleOnlineStatus_errorDoesNotReportSuccessWhenBackendConnected() = runTest {
        val failingRepo = object : OrdersRepository by mockOrdersRepository {
            override suspend fun updateAvailabilityStatus(isOnline: Boolean): NetworkResult<Boolean> =
                NetworkResult.ApiError(500, "Network failure updating availability")
        }
        val viewModel = HomeViewModel(mockAuthRepository, failingRepo)
        assertFalse(viewModel.uiState.value.isOnline)

        viewModel.toggleOnlineStatus(true)
        // Must NOT report success when update fails
        assertFalse(viewModel.uiState.value.isOnline)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun ordersViewModel_unapprovedKyc_blocksOrderAcceptance() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()

        // Set partner KYC status to NOT_STARTED to test acceptance blocking
        viewModel.updatePartnerKycStatus(KycStatus.NOT_STARTED)
        viewModel.requestAcceptOrder(sampleOrder)

        assertTrue(viewModel.uiState.value.showKycRequiredDialog)
        assertFalse(viewModel.uiState.value.showAcceptConfirmationDialog)

        viewModel.dismissKycRequiredDialog()
        assertFalse(viewModel.uiState.value.showKycRequiredDialog)

        // Now set KYC status to VERIFIED
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        viewModel.requestAcceptOrder(sampleOrder)

        assertFalse(viewModel.uiState.value.showKycRequiredDialog)
        assertTrue(viewModel.uiState.value.showAcceptConfirmationDialog)
    }

    @Test
    fun ordersViewModel_loadsSampleOrders_whenRepoReturnsEmpty() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.availableOrders.isNotEmpty())
    }

    @Test
    fun ordersViewModel_acceptOrderFlow_movesToActiveList() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()

        viewModel.requestAcceptOrder(sampleOrder)
        assertTrue(viewModel.uiState.value.showAcceptConfirmationDialog)

        viewModel.confirmAcceptOrder()

        val updatedState = viewModel.uiState.value
        assertFalse(updatedState.showAcceptConfirmationDialog)
        assertEquals(OrderTab.ACTIVE, updatedState.selectedTab)
        assertEquals(1, updatedState.activeOrders.size)
        assertEquals(OrderStatus.ACCEPTED, updatedState.activeOrders.first().status)
        assertNotNull(updatedState.acceptNoticeMessage)
    }

    @Test
    fun ordersViewModel_pickupFlowStateTransitions() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()

        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()

        val acceptedOrder = viewModel.uiState.value.activeOrders.first()

        // 1. Start Journey
        viewModel.startDemoJourney(acceptedOrder)
        assertTrue(viewModel.uiState.value.isJourneyStarted)

        // 2. Arrived at Vendor
        viewModel.markArrivedAtVendor(acceptedOrder)
        assertTrue(viewModel.uiState.value.isArrivedAtVendor)

        // 3. Simulate Vendor PIN
        viewModel.simulateVendorPinVerified()
        assertTrue(viewModel.uiState.value.isVendorPinSimulated)

        // 4. Request & Simulate OTP
        viewModel.requestVendorOtp()
        assertTrue(viewModel.uiState.value.isOtpRequested)
        viewModel.simulateVendorOtpFilled()
        assertEquals("982143", viewModel.uiState.value.vendorOtpInput)

        // 5. Complete Pickup Verification
        viewModel.verifyVendorOtp(acceptedOrder)
        assertTrue(viewModel.uiState.value.isPickupCompleted)
        assertEquals(OrderStatus.DEMO_PICKUP_COMPLETED, viewModel.uiState.value.activeOrders.first().status)
    }

    @Test
    fun ordersViewModel_deliveryAndProblemReportFlowStateTransitions() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()

        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        // 1. Start Delivery Journey
        viewModel.startDeliveryJourney(order)
        assertTrue(viewModel.uiState.value.isDeliveryJourneyStarted)

        // 2. Arrived at Renter
        viewModel.markArrivedAtRenter(order)
        assertTrue(viewModel.uiState.value.isArrivedAtRenter)

        // 3. Renter OTP
        viewModel.requestRenterOtp()
        viewModel.simulateRenterOtpFilled()
        viewModel.verifyRenterOtp(order)
        assertTrue(viewModel.uiState.value.isRenterOtpVerified)

        // 4. Inspection
        viewModel.toggleCheckItem(1)
        viewModel.toggleCheckItem(2)
        viewModel.markInspectionComplete(order)
        assertTrue(viewModel.uiState.value.isInspectionMarkedComplete)

        // 5. Submit Problem Report and verify PIN completion is blocked
        viewModel.selectProblemCategory("Product damaged")
        viewModel.onProblemDescriptionChanged("Minor scratch on lens housing")
        val isReported = viewModel.submitProblemReport(order)
        assertTrue(isReported)
        assertTrue(viewModel.uiState.value.hasOpenProblemReport)

        val pinSuccessWhenBlocked = viewModel.simulateRenterPinVerified(order)
        assertFalse(pinSuccessWhenBlocked)
    }

    @Test
    fun ordersViewModel_offlineStatus_blocksOrderAcceptance() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()

        // Partner is offline
        viewModel.updateOnlineStatus(false)
        viewModel.requestAcceptOrder(sampleOrder)

        // Acceptance dialog must not be shown, offline warning dialog must be shown
        assertTrue(viewModel.uiState.value.showOfflineWarningDialog)
        assertFalse(viewModel.uiState.value.showAcceptConfirmationDialog)

        // Dismiss warning
        viewModel.dismissOfflineWarningDialog()
        assertFalse(viewModel.uiState.value.showOfflineWarningDialog)

        // Now go online and retry
        viewModel.updateOnlineStatus(true)
        viewModel.requestAcceptOrder(sampleOrder)
        assertFalse(viewModel.uiState.value.showOfflineWarningDialog)
        assertTrue(viewModel.uiState.value.showAcceptConfirmationDialog)
    }

    @Test
    fun ordersViewModel_acceptOrder_apiError_doesNotCreateFalseSuccessState() = runTest {
        val failingRepo = object : OrdersRepository by mockOrdersRepository {
            override suspend fun acceptOrder(orderId: String): NetworkResult<Order> =
                NetworkResult.ApiError(409, "Order has already been claimed by another delivery partner")
        }
        val viewModel = OrdersViewModel(failingRepo)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        viewModel.updateOnlineStatus(true)

        // Live order (not starting with demo_)
        val liveOrder = Order(
            id = "live_order_999",
            orderNumber = "ORD-LIVE-999",
            vendorName = "Live Camera Vendor",
            vendorAddress = "456 Market St",
            deliveryAddress = "789 Pine St",
            rentalPeriod = "2 Days",
            payoutAmount = 35.0,
            status = OrderStatus.AVAILABLE
        )

        viewModel.requestAcceptOrder(liveOrder)
        assertTrue(viewModel.uiState.value.showAcceptConfirmationDialog)

        viewModel.confirmAcceptOrder()

        val state = viewModel.uiState.value
        // Must NOT add to active deliveries
        assertFalse(state.activeOrders.any { it.id == liveOrder.id })
        // Must show error message
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("claimed"))
    }

    @Test
    fun orderFiltering_availableAndActiveOrderRules() {
        // Available order rules
        assertTrue(OrderStatus.AVAILABLE.isAvailable)
        assertFalse(OrderStatus.ACCEPTED.isAvailable)
        assertFalse(OrderStatus.JOURNEY_STARTED.isAvailable)
        assertFalse(OrderStatus.DELIVERED.isAvailable)
        assertFalse(OrderStatus.CANCELLED.isAvailable)

        // Active delivery rules
        assertFalse(OrderStatus.AVAILABLE.isActive)
        assertTrue(OrderStatus.ACCEPTED.isActive)
        assertTrue(OrderStatus.JOURNEY_STARTED.isActive)
        assertTrue(OrderStatus.ARRIVED_AT_VENDOR.isActive)
        assertTrue(OrderStatus.VENDOR_PIN_SIMULATED.isActive)
        assertTrue(OrderStatus.DEMO_PICKUP_COMPLETED.isActive)
        assertTrue(OrderStatus.DELIVERY_JOURNEY_STARTED.isActive)
        assertTrue(OrderStatus.ARRIVED_AT_RENTER.isActive)
        assertFalse(OrderStatus.DELIVERED.isActive)
        assertFalse(OrderStatus.DELIVERY_DEMO_COMPLETED.isActive)
        assertFalse(OrderStatus.CANCELLED.isActive)
    }

    @Test
    fun homeViewModel_synchronizesActiveOrdersCount_withSharedOrdersViewModel() = runTest {
        val ordersViewModel = OrdersViewModel(mockOrdersRepository)
        val homeViewModel = HomeViewModel(
            authRepository = mockAuthRepository,
            ordersRepository = mockOrdersRepository,
            ordersViewModel = ordersViewModel
        )

        // Initially no active orders
        assertEquals(0, homeViewModel.uiState.value.activeOrdersCount)
        val initialAvailableCount = homeViewModel.uiState.value.availableOrdersPreview.size
        assertTrue(initialAvailableCount > 0)

        // Accept a demo order in ordersViewModel
        val sampleOrder = ordersViewModel.uiState.value.availableOrders.first()
        ordersViewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        ordersViewModel.updateOnlineStatus(true)
        ordersViewModel.requestAcceptOrder(sampleOrder)
        ordersViewModel.confirmAcceptOrder()

        // Verify HomeViewModel reactive synchronization
        val homeState = homeViewModel.uiState.value
        assertEquals(1, homeState.activeOrdersCount)
        assertNotNull(homeState.activeOrder)
        assertEquals(sampleOrder.id, homeState.activeOrder?.id)
        // Accepted order is removed from available preview
        assertEquals(initialAvailableCount - 1, homeState.availableOrdersPreview.size)
        assertFalse(homeState.availableOrdersPreview.any { it.id == sampleOrder.id })
    }

    @Test
    fun earningsViewModel_loadsSummaryData() = runTest {
        val viewModel = EarningsViewModel(mockEarningsRepository)
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.isBackendConnected)
        assertEquals(0.0, state.earningsSummary?.todayEarnings ?: -1.0, 0.01)
    }

    @Test
    fun ordersViewModel_vendorPin_mustContainExactlyFourDigits_preservesLeadingZeros() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        // Test 1: PIN with leading zero is preserved as String
        val leadingZeroPin = "0429"
        viewModel.onVendorPinChanged(leadingZeroPin)
        assertEquals("0429", viewModel.uiState.value.vendorPinInput)
        assertEquals(4, viewModel.uiState.value.vendorPinInput.length)
        assertTrue(viewModel.uiState.value.vendorPinInput.startsWith("0"))

        // Test 2: PIN with fewer than 4 digits fails validation
        viewModel.onVendorPinChanged("04")
        val shortPinSuccess = viewModel.verifyVendorPin(order)
        assertFalse(shortPinSuccess)
        assertNotNull(viewModel.uiState.value.vendorPinError)
        assertFalse(viewModel.uiState.value.isVendorPinSimulated)

        // Test 3: Valid 4-digit PIN with leading zero succeeds
        viewModel.onVendorPinChanged("0429")
        val validPinSuccess = viewModel.verifyVendorPin(order)
        assertTrue(validPinSuccess)
        assertTrue(viewModel.uiState.value.isVendorPinSimulated)
        assertEquals(OrderStatus.VENDOR_PIN_SIMULATED, viewModel.uiState.value.activeOrders.first().status)
    }

    @Test
    fun vendorHandover_1_pinRequest_doesNotImmediatelyMarkStepCompleted() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        // Partner sends PIN request to vendor app
        viewModel.sendPinRequestToVendor(order)

        // Step 1 must NOT be immediately completed
        assertTrue(viewModel.uiState.value.isVendorPinRequestSent)
        assertTrue(viewModel.uiState.value.isWaitingForVendorPin)
        assertFalse(viewModel.uiState.value.isVendorPinSimulated)
        // Step 2 must remain locked
        assertFalse(viewModel.uiState.value.isVendorOtpSent)
    }

    @Test
    fun vendorHandover_2_vendorPinVerificationSuccess_unlocksStep2() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        viewModel.sendPinRequestToVendor(order)
        // Vendor enters secret PIN in vendor app
        val verified = viewModel.onVendorPinVerifiedExternally(order.id, "0429")
        assertTrue(verified)
        assertTrue(viewModel.uiState.value.isVendorPinSimulated)
        assertFalse(viewModel.uiState.value.isWaitingForVendorPin)

        // Step 2 is now unlocked and can send OTP
        viewModel.sendOtpToVendor()
        assertTrue(viewModel.uiState.value.isVendorOtpSent)
    }

    @Test
    fun vendorHandover_3_wrongOrderVerificationEvents_areIgnored() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        viewModel.sendPinRequestToVendor(order)
        // Event for wrong order ID arrives
        val verified = viewModel.onVendorPinVerifiedExternally("WRONG_ORDER_ID_9999", "0429")
        assertFalse(verified)
        // Original order is not marked as verified
        assertFalse(viewModel.uiState.value.isVendorPinSimulated)
        assertEquals(OrderStatus.ACCEPTED, viewModel.uiState.value.activeOrders.first().status)
    }

    @Test
    fun vendorHandover_4_otpCannotBeSentBeforeVendorPinVerification() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        // Attempt to send OTP before PIN verified
        viewModel.sendOtpToVendor()
        assertFalse(viewModel.uiState.value.isVendorOtpSent)
        assertNotNull(viewModel.uiState.value.vendorOtpError)
    }

    @Test
    fun vendorHandover_5_otpInputUsesConfiguredLength() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        // Attempt entering 8 digits: should be capped at 6 digits
        viewModel.onVendorOtpChanged("98214300")
        assertEquals("982143", viewModel.uiState.value.vendorOtpInput)
        assertEquals(6, viewModel.uiState.value.vendorOtpInput.length)
    }

    @Test
    fun vendorHandover_6_invalidOtpDoesNotCompletePickup() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        viewModel.onVendorPinVerifiedExternally(order.id, "0429")
        viewModel.sendOtpToVendor()

        // 1. Incomplete OTP (<6 digits)
        viewModel.onVendorOtpChanged("982")
        val shortOtpSuccess = viewModel.verifyVendorOtp(order)
        assertFalse(shortOtpSuccess)
        assertFalse(viewModel.uiState.value.isPickupCompleted)
        assertNotNull(viewModel.uiState.value.vendorOtpError)

        // 2. Incorrect 6-digit OTP
        viewModel.onVendorOtpChanged("000000")
        val wrongOtpSuccess = viewModel.verifyVendorOtp(order)
        assertFalse(wrongOtpSuccess)
        assertFalse(viewModel.uiState.value.isPickupCompleted)
        assertNotNull(viewModel.uiState.value.vendorOtpError)
    }

    @Test
    fun vendorHandover_7_successfulOtpVerificationCompletesPickupExactlyOnce() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        viewModel.onVendorPinVerifiedExternally(order.id, "0429")
        viewModel.sendOtpToVendor()
        viewModel.onVendorOtpChanged("982143")

        val firstSuccess = viewModel.verifyVendorOtp(order)
        assertTrue(firstSuccess)
        assertTrue(viewModel.uiState.value.isPickupCompleted)
        assertEquals(OrderStatus.DEMO_PICKUP_COMPLETED, viewModel.uiState.value.activeOrders.first().status)

        // Second verification attempt is idempotent
        val secondSuccess = viewModel.verifyVendorOtp(order)
        assertTrue(secondSuccess)
        assertEquals(OrderStatus.DEMO_PICKUP_COMPLETED, viewModel.uiState.value.activeOrders.first().status)
    }

    @Test
    fun vendorHandover_8_networkErrorsNeverCreateFalseSuccessStates() = runTest {
        val failingRepo = object : OrdersRepository by mockOrdersRepository {
            override suspend fun acceptOrder(orderId: String): NetworkResult<Order> =
                NetworkResult.NetworkError(java.io.IOException("Connection refused"))
        }
        val viewModel = OrdersViewModel(failingRepo)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)

        val liveOrder = Order(
            id = "live_order_100",
            orderNumber = "ORD-LIVE-100",
            vendorName = "Vendor Inc",
            vendorAddress = "Vendor St",
            deliveryAddress = "Renter St",
            rentalPeriod = "2 days",
            payoutAmount = 100.0,
            status = OrderStatus.AVAILABLE
        )
        viewModel.requestAcceptOrder(liveOrder)
        viewModel.confirmAcceptOrder()

        assertFalse(viewModel.uiState.value.activeOrders.any { it.id == "live_order_100" })
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun vendorHandover_9_backNavigationPreservesValidProgress() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        // Advance progress: PIN verified, OTP sent, OTP partially typed
        viewModel.onVendorPinVerifiedExternally(order.id, "0429")
        viewModel.sendOtpToVendor()
        viewModel.onVendorOtpChanged("9821")

        // In Compose MVVM with sharedOrdersViewModel, navigating away and returning reads the same state:
        val preservedState = viewModel.uiState.value
        assertTrue(preservedState.isVendorPinSimulated)
        assertTrue(preservedState.isVendorOtpSent)
        assertEquals("9821", preservedState.vendorOtpInput)
    }

    @Test
    fun vendorHandover_10_successfulPickupContinuesToRenterNavigationRatherThanInspection() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        viewModel.onVendorPinVerifiedExternally(order.id, "0429")
        viewModel.sendOtpToVendor()
        viewModel.onVendorOtpChanged("982143")
        viewModel.verifyVendorOtp(order)

        val pickedUpOrder = viewModel.uiState.value.activeOrders.first()
        assertEquals(OrderStatus.DEMO_PICKUP_COMPLETED, pickedUpOrder.status)

        // Next destination must be Delivery Journey (Renter Navigation), NOT product inspection
        viewModel.startDeliveryJourney(pickedUpOrder)
        assertEquals(OrderStatus.DELIVERY_JOURNEY_STARTED, viewModel.uiState.value.activeOrders.first().status)
        assertFalse(viewModel.uiState.value.isInspectionMarkedComplete)
    }

    @Test
    fun ordersViewModel_customerPin_mustContainExactlyFourDigits_preservesLeadingZeros() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        // Perform required steps up to customer PIN:
        // Pickup
        viewModel.onVendorPinChanged("0429")
        viewModel.verifyVendorPin(order)
        viewModel.sendOtpToVendor()
        viewModel.onVendorOtpChanged("982143")
        viewModel.verifyVendorOtp(order)
        val pickedUpOrder = viewModel.uiState.value.activeOrders.first()

        // Customer OTP
        viewModel.sendOtpToRenter()
        viewModel.onRenterOtpChanged("654321")
        viewModel.verifyRenterOtp(pickedUpOrder)

        // Customer Inspection
        viewModel.markInspectionComplete(pickedUpOrder)

        // Test leading zero in customer PIN (e.g. "0821")
        viewModel.onCustomerPinChanged("0821")
        assertEquals("0821", viewModel.uiState.value.customerPinInput)
        assertEquals(4, viewModel.uiState.value.customerPinInput.length)
        assertTrue(viewModel.uiState.value.customerPinInput.startsWith("0"))

        // Test invalid customer PIN (<4 digits)
        viewModel.onCustomerPinChanged("08")
        val shortCustomerPinSuccess = viewModel.verifyCustomerPin(pickedUpOrder)
        assertFalse(shortCustomerPinSuccess)
        assertNotNull(viewModel.uiState.value.customerPinError)
        assertFalse(viewModel.uiState.value.isDeliveryCompleted)

        // Test valid 4-digit customer PIN
        viewModel.onCustomerPinChanged("0821")
        val validCustomerPinSuccess = viewModel.verifyCustomerPin(pickedUpOrder)
        assertTrue(validCustomerPinSuccess)
        assertTrue(viewModel.uiState.value.isDeliveryCompleted)
        // Fulfilled order moved to completedOrders
        assertTrue(viewModel.uiState.value.completedOrders.any { it.id == order.id })
        assertFalse(viewModel.uiState.value.activeOrders.any { it.id == order.id })
    }

    @Test
    fun ordersViewModel_customerDelivery_cannotCompleteWithoutRequiredVerifications() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        // 1. Try customer PIN before customer OTP and inspection
        viewModel.onCustomerPinChanged("0821")
        val earlyPinSuccess = viewModel.verifyCustomerPin(order)
        assertFalse(earlyPinSuccess)
        assertNotNull(viewModel.uiState.value.customerPinError)
        assertFalse(viewModel.uiState.value.isDeliveryCompleted)

        // 2. Perform customer OTP but NOT inspection
        viewModel.sendOtpToRenter()
        viewModel.onRenterOtpChanged("654321")
        viewModel.verifyRenterOtp(order)
        val preInspectionPinSuccess = viewModel.verifyCustomerPin(order)
        assertFalse(preInspectionPinSuccess)
        assertNotNull(viewModel.uiState.value.customerPinError)
        assertFalse(viewModel.uiState.value.isDeliveryCompleted)

        // 3. Mark inspection complete, now PIN succeeds
        viewModel.markInspectionComplete(order)
        val finalPinSuccess = viewModel.verifyCustomerPin(order)
        assertTrue(finalPinSuccess)
        assertTrue(viewModel.uiState.value.isDeliveryCompleted)
    }

    @Test
    fun ordersViewModel_problemReport_strictlyBlocksDeliveryPinCompletion() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        // Advance to customer handover
        viewModel.sendOtpToRenter()
        viewModel.onRenterOtpChanged("654321")
        viewModel.verifyRenterOtp(order)
        viewModel.markInspectionComplete(order)

        // Report problem
        viewModel.selectProblemCategory("Product damaged")
        viewModel.onProblemDescriptionChanged("Camera sensor cracked upon delivery")
        viewModel.submitProblemReport(order)
        assertTrue(viewModel.uiState.value.hasOpenProblemReport)

        // Delivery PIN verification MUST be rejected
        viewModel.onCustomerPinChanged("0821")
        val blockedSuccess = viewModel.verifyCustomerPin(order)
        assertFalse(blockedSuccess)
        assertFalse(viewModel.uiState.value.isDeliveryCompleted)
        assertNotNull(viewModel.uiState.value.customerPinError)
    }

    @Test
    fun ordersViewModel_repeatedTaps_cannotDuplicateCompletion() = runTest {
        val viewModel = OrdersViewModel(mockOrdersRepository)
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        val sampleOrder = viewModel.uiState.value.availableOrders.first()
        viewModel.requestAcceptOrder(sampleOrder)
        viewModel.confirmAcceptOrder()
        val order = viewModel.uiState.value.activeOrders.first()

        // Advance to delivery
        viewModel.sendOtpToRenter()
        viewModel.onRenterOtpChanged("654321")
        viewModel.verifyRenterOtp(order)
        viewModel.markInspectionComplete(order)
        viewModel.onCustomerPinChanged("0821")

        // First tap completes
        val firstCompletion = viewModel.verifyCustomerPin(order)
        assertTrue(firstCompletion)
        val completedCountAfterFirst = viewModel.uiState.value.completedOrders.size

        // Repeated tap with already completed order
        val secondCompletion = viewModel.verifyCustomerPin(order)
        // Should not duplicate entry in completedOrders
        assertEquals(completedCountAfterFirst, viewModel.uiState.value.completedOrders.size)
    }
}
