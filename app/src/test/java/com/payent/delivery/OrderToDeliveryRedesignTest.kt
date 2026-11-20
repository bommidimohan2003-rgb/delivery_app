package com.payent.delivery

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.model.OrderStatus
import com.payent.delivery.domain.repository.OrdersRepository
import com.payent.delivery.feature.orders.OrdersViewModel
import com.payent.delivery.feature.orders.getOrderDisplayInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderToDeliveryRedesignTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockOrdersRepository = object : OrdersRepository {
        override suspend fun getAvailableOrders(): NetworkResult<List<Order>> =
            NetworkResult.Success(OrdersViewModel.sampleDemoOrders)
        override suspend fun getActiveOrders(): NetworkResult<List<Order>> =
            NetworkResult.Success(emptyList())
        override suspend fun getOrderHistory(): NetworkResult<List<Order>> =
            NetworkResult.Success(emptyList())
        override suspend fun acceptOrder(orderId: String): NetworkResult<Order> =
            NetworkResult.Success(OrdersViewModel.sampleDemoOrders.first().copy(status = OrderStatus.ACCEPTED))
        override suspend fun updateAvailabilityStatus(isOnline: Boolean): NetworkResult<Boolean> =
            NetworkResult.Success(true)
    }

    private lateinit var viewModel: OrdersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OrdersViewModel(mockOrdersRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDynamicOrderDetailsDisplayInfoForAllProducts() {
        // Mountain Bike
        val bikeOrder = OrdersViewModel.sampleDemoOrders[0]
        val bikeInfo = getOrderDisplayInfo(bikeOrder)
        assertEquals("Mountain Bike", bikeInfo.title)
        assertEquals("Sports & Fitness", bikeInfo.category)
        assertNotNull(bikeInfo.drawableRes)

        // DSLR Camera
        val cameraOrder = OrdersViewModel.sampleDemoOrders[1]
        val cameraInfo = getOrderDisplayInfo(cameraOrder)
        assertEquals("DSLR Camera", cameraInfo.title)
        assertEquals("Electronics", cameraInfo.category)
        assertNotNull(cameraInfo.drawableRes)

        // Camping Tent
        val tentOrder = OrdersViewModel.sampleDemoOrders[2]
        val tentInfo = getOrderDisplayInfo(tentOrder)
        assertEquals("Camping Tent", tentInfo.title)
        assertEquals("Outdoor & Gear", tentInfo.category)
        assertNotNull(tentInfo.drawableRes)

        // Generator
        val genOrder = OrdersViewModel.sampleDemoOrders[3]
        val genInfo = getOrderDisplayInfo(genOrder)
        assertEquals("Generator", genInfo.title)
        assertEquals("Machinery & Power", genInfo.category)
        assertNotNull(genInfo.drawableRes)
    }

    @Test
    fun testAtomicOrderAcceptanceRequiresKycAndOnline() {
        val order = OrdersViewModel.sampleDemoOrders.first()

        // 1. Offline partner cannot accept
        viewModel.updateOnlineStatus(false)
        viewModel.requestAcceptOrder(order)
        assertTrue(viewModel.uiState.value.showOfflineWarningDialog)
        assertFalse(viewModel.uiState.value.showAcceptConfirmationDialog)

        // 2. Unverified KYC partner cannot accept
        viewModel.dismissOfflineWarningDialog()
        viewModel.updateOnlineStatus(true)
        viewModel.updatePartnerKycStatus(KycStatus.NOT_VERIFIED)
        viewModel.requestAcceptOrder(order)
        assertTrue(viewModel.uiState.value.showKycRequiredDialog)
        assertFalse(viewModel.uiState.value.showAcceptConfirmationDialog)

        // 3. Online and Verified KYC partner triggers confirmation dialog
        viewModel.dismissKycRequiredDialog()
        viewModel.updatePartnerKycStatus(KycStatus.VERIFIED)
        viewModel.requestAcceptOrder(order)
        assertTrue(viewModel.uiState.value.showAcceptConfirmationDialog)

        // 4. Confirm acceptance adds order to active and sets status to ACCEPTED
        var acceptedCallbackOrder: Order? = null
        viewModel.confirmAcceptOrder { acceptedCallbackOrder = it }
        assertNotNull(acceptedCallbackOrder)
        assertEquals(OrderStatus.ACCEPTED, acceptedCallbackOrder?.status)
        assertTrue(viewModel.uiState.value.activeOrders.any { it.id == order.id })
    }

    @Test
    fun testVendorVerificationSequenceAndExternalPinResponsibility() {
        val order = OrdersViewModel.sampleDemoOrders.first()
        viewModel.requestAcceptOrder(order)
        viewModel.confirmAcceptOrder()

        // Step 1: Partner marks arrived at vendor
        viewModel.markArrivedAtVendor(order)
        assertTrue(viewModel.uiState.value.isArrivedAtVendor)

        // Step 2: Secret PIN must be verified before vendor OTP
        viewModel.sendOtpToVendor()
        // OTP cannot be sent if Vendor PIN is not verified
        assertFalse(viewModel.uiState.value.isVendorOtpSent)
        assertNotNull(viewModel.uiState.value.vendorOtpError)

        // Partner requests PIN from vendor app (vendor enters PIN externally in vendor app)
        viewModel.sendPinRequestToVendor(order)
        assertTrue(viewModel.uiState.value.isVendorPinRequestSent)

        // External vendor app simulates entering secret PIN
        val pinSuccess = viewModel.onVendorPinVerifiedExternally(order.id, "0429")
        assertTrue(pinSuccess)
        assertTrue(viewModel.uiState.value.isVendorPinSimulated)

        // Now Step 2 (Request OTP) is unlocked
        viewModel.sendOtpToVendor()
        assertTrue(viewModel.uiState.value.isVendorOtpSent)

        // Delivery partner enters vendor OTP to complete pickup
        viewModel.onVendorOtpChanged("982143")
        val pickupSuccess = viewModel.verifyVendorOtp(order)
        assertTrue(pickupSuccess)
        assertTrue(viewModel.uiState.value.isPickupCompleted)
    }

    @Test
    fun testCustomerVerificationSequenceAndCompletion() {
        val order = OrdersViewModel.sampleDemoOrders.first()
        viewModel.requestAcceptOrder(order)
        viewModel.confirmAcceptOrder()
        viewModel.onVendorPinVerifiedExternally(order.id, "0429")
        viewModel.onVendorOtpChanged("982143")
        viewModel.verifyVendorOtp(order)

        // Step 1: Start delivery journey (Vendor -> Renter)
        viewModel.startDeliveryJourney(order)
        assertTrue(viewModel.uiState.value.isDeliveryJourneyStarted)

        // Step 2: Partner marks arrived at renter
        viewModel.markArrivedAtRenter(order)
        assertTrue(viewModel.uiState.value.isArrivedAtRenter)

        // Step 3: In Customer Verification, OTP is requested first
        viewModel.sendOtpToRenter()
        assertTrue(viewModel.uiState.value.isRenterOtpSent)

        // Secret PIN is blocked before OTP verification
        val blockedPinAttempt = viewModel.verifyCustomerPin(order, "0821")
        assertFalse(blockedPinAttempt)

        // Partner verifies Customer OTP
        viewModel.onRenterOtpChanged("654321")
        val otpSuccess = viewModel.verifyRenterOtp(order)
        assertTrue(otpSuccess)
        assertTrue(viewModel.uiState.value.isRenterOtpVerified)

        // Step 4: Customer enters secret PIN in customer app
        viewModel.markInspectionComplete(order)
        val customerPinSuccess = viewModel.simulateRenterPinVerified(order)
        assertTrue(customerPinSuccess)
        assertTrue(viewModel.uiState.value.isDeliveryCompleted)
    }
}
