package com.payent.delivery

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.model.EarningsSummary
import com.payent.delivery.domain.model.EarningsTimeRange
import com.payent.delivery.domain.model.KycDocumentType
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.model.OrderStatus
import com.payent.delivery.domain.repository.AuthRepository
import com.payent.delivery.domain.repository.EarningsRepository
import com.payent.delivery.domain.repository.OrdersRepository
import com.payent.delivery.feature.auth.AuthViewModel
import com.payent.delivery.feature.earnings.EarningsViewModel
import com.payent.delivery.feature.orders.OrdersViewModel
import com.payent.delivery.feature.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class KycAndEarningsRedesignTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeDeliveryPartner = DeliveryPartner(
        id = "DP-10042",
        name = "Test Partner",
        phoneNumber = "+91 98765 43210",
        email = "partner@payent.com",
        kycStatus = KycStatus.NOT_STARTED
    )

    private val fakeAuthRepository = object : AuthRepository {
        override val authStateFlow = flowOf(com.payent.delivery.domain.model.AuthState.Unauthenticated)
        override suspend fun register(fullName: String, email: String, phoneNumber: String, password: String) =
            NetworkResult.Success(com.payent.delivery.data.remote.dto.AuthResponseDto(token = "tok", partnerId = "DP-10042"))
        override suspend fun login(email: String, password: String) =
            NetworkResult.Success(fakeDeliveryPartner)
        override suspend fun startDemoLogin() = NetworkResult.Success(fakeDeliveryPartner)
        override suspend fun requestOtp(phoneNumber: String) = NetworkResult.Success(true)
        override suspend fun verifyOtp(phoneNumber: String, otp: String) = NetworkResult.Success(fakeDeliveryPartner)
        override suspend fun getPartnerProfile() = NetworkResult.Success(fakeDeliveryPartner)
        override suspend fun logout() = NetworkResult.Success(true)
    }

    private val fakeEarningsSummary = EarningsSummary(
        totalEarnings = 5400.0,
        todayEarnings = 480.0,
        weeklyEarnings = 2100.0,
        monthlyEarnings = 8200.0,
        completedDeliveriesCount = 7,
        nextPayoutDate = "Thursday, Nov 12, 2026",
        payoutStatus = "SCHEDULED"
    )

    private val fakeEarningsRepository = object : EarningsRepository {
        override suspend fun getEarningsSummary(): NetworkResult<EarningsSummary> =
            NetworkResult.Success(fakeEarningsSummary)
    }

    private val fakeOrdersRepository = object : OrdersRepository {
        override suspend fun getAvailableOrders(): NetworkResult<List<Order>> = NetworkResult.Success(emptyList())
        override suspend fun getActiveOrders(): NetworkResult<List<Order>> = NetworkResult.Success(emptyList())
        override suspend fun getOrderHistory(): NetworkResult<List<Order>> = NetworkResult.Success(emptyList())
        override suspend fun acceptOrder(orderId: String): NetworkResult<Order> =
            NetworkResult.Success(Order("ord_1", "ORD-1", "V", "VA", "DA", "1d", 250.0, OrderStatus.ACCEPTED))
        override suspend fun updateAvailabilityStatus(isOnline: Boolean): NetworkResult<Boolean> = NetworkResult.Success(true)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // 1. KYC VERIFICATION & ADMIN APPROVAL FLOW TESTS
    // =========================================================================

    @Test
    fun kycSubmission_validatesMissingDocuments() = runTest {
        val viewModel = ProfileViewModel(fakeAuthRepository)

        // Initial state is NOT_STARTED
        assertEquals(KycStatus.NOT_STARTED, viewModel.uiState.value.partner?.kycStatus)

        // Attempting to submit without selecting documents produces validation message
        viewModel.submitKycDocuments()
        assertNotNull(viewModel.uiState.value.kycValidationMessage)
        assertTrue(viewModel.uiState.value.kycValidationMessage!!.contains("Please select files for:"))
        assertEquals(KycStatus.NOT_STARTED, viewModel.uiState.value.partner?.kycStatus)
    }

    @Test
    fun kycSubmission_transitionsToDocumentsSubmittedAndStoresDate() = runTest {
        val viewModel = ProfileViewModel(fakeAuthRepository)

        // Select all 3 required documents: DL, Aadhaar, Bank Details
        viewModel.selectKycDocument(KycDocumentType.DRIVING_LICENCE, "file://dl.jpg", "dl_photo.jpg")
        viewModel.selectKycDocument(KycDocumentType.AADHAAR_CARD, "file://aadhaar.jpg", "aadhaar_photo.jpg")
        viewModel.selectKycDocument(KycDocumentType.BANK_PASSBOOK, "file://passbook.jpg", "passbook_photo.jpg")

        // Submit documents
        viewModel.submitKycDocuments()

        val state = viewModel.uiState.value
        assertEquals(KycStatus.DOCUMENTS_SUBMITTED, state.partner?.kycStatus)
        assertNotNull(state.kycSubmissionDate)
        assertEquals(3, state.submittedDocuments.size)
        assertNull(state.kycValidationMessage)
    }

    @Test
    fun kycApproval_updatesStatusAndEnablesOrderAcceptance() = runTest {
        val profileViewModel = ProfileViewModel(fakeAuthRepository)
        val ordersViewModel = OrdersViewModel(fakeOrdersRepository)

        // Initially NOT_STARTED: ordersViewModel should block acceptance
        ordersViewModel.updatePartnerKycStatus(KycStatus.NOT_STARTED)
        assertFalse(ordersViewModel.uiState.value.partnerKycStatus.isApproved)

        val sampleOrder = Order("o1", "PAY-1", "V", "VA", "DA", "1d", 200.0, OrderStatus.AVAILABLE)
        ordersViewModel.updateOnlineStatus(true)
        ordersViewModel.requestAcceptOrder(sampleOrder)
        assertTrue(ordersViewModel.uiState.value.showKycRequiredDialog)

        // Admin updates status to APPROVED
        profileViewModel.updateKycStatusForTesting(KycStatus.APPROVED)
        assertEquals(KycStatus.APPROVED, profileViewModel.uiState.value.partner?.kycStatus)
        assertTrue(profileViewModel.uiState.value.partner?.kycStatus?.isApproved == true)

        // Sync with orders ViewModel
        ordersViewModel.dismissKycRequiredDialog()
        ordersViewModel.updatePartnerKycStatus(KycStatus.APPROVED)
        assertTrue(ordersViewModel.uiState.value.partnerKycStatus.isApproved)

        // Now order acceptance is allowed (confirmation dialog shown instead of KYC required dialog)
        ordersViewModel.requestAcceptOrder(sampleOrder)
        assertFalse(ordersViewModel.uiState.value.showKycRequiredDialog)
        assertTrue(ordersViewModel.uiState.value.showAcceptConfirmationDialog)
    }

    @Test
    fun kycRejection_displaysReasonAndAllowsResubmission() = runTest {
        val viewModel = ProfileViewModel(fakeAuthRepository)

        // Admin reports REJECTED with specific reason
        val reasonText = "Aadhaar Card photo is blurry. Please provide clear scanned image."
        viewModel.updateKycStatusForTesting(KycStatus.REJECTED, reasonText)

        assertEquals(KycStatus.REJECTED, viewModel.uiState.value.partner?.kycStatus)
        assertEquals(reasonText, viewModel.uiState.value.kycRejectionReason)

        // Rider taps "Review & Resubmit Documents"
        viewModel.resubmitKycDocuments()

        // Status resets back to NOT_STARTED, files cleared
        val resetState = viewModel.uiState.value
        assertEquals(KycStatus.NOT_STARTED, resetState.partner?.kycStatus)
        assertNull(resetState.kycRejectionReason)
        assertTrue(resetState.kycDocuments.all { it.selectedUri == null })
    }

    // =========================================================================
    // 2. DELIVERY PARTNER ID LOGIN COMPATIBILITY TESTS
    // =========================================================================

    @Test
    fun login_validatesBlankFields() = runTest {
        val authViewModel = AuthViewModel(fakeAuthRepository)

        authViewModel.onPartnerIdChanged("")
        authViewModel.onPasswordChanged("")
        authViewModel.login()

        val state = authViewModel.uiState.value
        assertNotNull(state.partnerIdError)
        assertNotNull(state.passwordError)
    }

    @Test
    fun login_supportsPartnerIdInput() = runTest {
        val authViewModel = AuthViewModel(fakeAuthRepository)

        authViewModel.onPartnerIdChanged("DP-8821")
        authViewModel.onPasswordChanged("SecurePass123!")

        assertEquals("DP-8821", authViewModel.uiState.value.partnerId)
        authViewModel.login()

        // Auth succeeds via repository contract
        assertNull(authViewModel.uiState.value.partnerIdError)
    }

    // =========================================================================
    // 3. EARNINGS MATHEMATICAL INTEGRITY & BREAKDOWN TESTS
    // =========================================================================

    @Test
    fun dailyEarnings_sumOfOrdersEqualsDailyTotal() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)
        viewModel.selectTimeRange(EarningsTimeRange.DAILY)

        val daily = viewModel.uiState.value.dailyBreakdown
        val expectedSum = daily.orders.sumOf { it.earningsRupees }

        assertEquals(expectedSum, daily.totalEarningsRupees, 0.001)
        assertEquals(daily.orders.size, daily.completedDeliveriesCount)
        assertEquals(daily.totalEarningsRupees, viewModel.uiState.value.periodEarningsRupees, 0.001)
    }

    @Test
    fun weeklyEarnings_sumOf7DaysEqualsWeeklyTotal() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)
        viewModel.selectTimeRange(EarningsTimeRange.WEEKLY)

        val weekly = viewModel.uiState.value.weeklyBreakdown
        assertEquals(7, weekly.days.size)

        val expectedWeeklySum = weekly.days.sumOf { it.totalEarningsRupees }
        val expectedDeliveries = weekly.days.sumOf { it.completedDeliveriesCount }

        assertEquals(expectedWeeklySum, weekly.totalEarningsRupees, 0.001)
        assertEquals(expectedDeliveries, weekly.completedDeliveriesCount)
        assertEquals(weekly.totalEarningsRupees, viewModel.uiState.value.periodEarningsRupees, 0.001)
    }

    @Test
    fun monthlyEarnings_sumOfCalendarWeeksEqualsMonthlyTotal() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)
        viewModel.selectTimeRange(EarningsTimeRange.MONTHLY)

        val monthly = viewModel.uiState.value.monthlyBreakdown
        assertTrue("Month must have between 4 and 6 calendar weeks", monthly.weeks.size in 4..6)

        val expectedMonthlySum = monthly.weeks.sumOf { it.totalEarningsRupees }
        val expectedDeliveries = monthly.weeks.sumOf { it.completedDeliveriesCount }

        assertEquals(expectedMonthlySum, monthly.totalEarningsRupees, 0.001)
        assertEquals(expectedDeliveries, monthly.completedDeliveriesCount)
        assertEquals(monthly.totalEarningsRupees, viewModel.uiState.value.periodEarningsRupees, 0.001)

        val expectedAvgWeekly = if (monthly.weeks.isNotEmpty()) expectedMonthlySum / monthly.weeks.size else 0.0
        assertEquals(expectedAvgWeekly, monthly.averageWeeklyEarningsRupees, 0.001)
    }

    @Test
    fun dateNavigation_preventsNavigatingToFutureDates() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)
        viewModel.selectTimeRange(EarningsTimeRange.DAILY)

        // Today is selected by default; nextDay must be disabled
        assertTrue(viewModel.uiState.value.isDailyNextDayDisabled)

        // Navigate back one day
        viewModel.previousDay()
        assertFalse(viewModel.uiState.value.isDailyNextDayDisabled)

        // Navigate forward back to today
        viewModel.nextDay()
        assertTrue(viewModel.uiState.value.isDailyNextDayDisabled)
    }

    @Test
    fun weeklyToDailyNavigation_switchesTabAndSelectsDate() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)
        viewModel.selectTimeRange(EarningsTimeRange.WEEKLY)

        val targetDay = viewModel.uiState.value.weeklyBreakdown.days.first()
        viewModel.navigateToDailyForDate(targetDay.dateIso)

        assertEquals(EarningsTimeRange.DAILY, viewModel.uiState.value.selectedTimeRange)
        assertEquals(targetDay.dateIso, viewModel.uiState.value.dailyBreakdown.dateIso)
    }

    @Test
    fun november2026_calendarWeeks_hasSixWeeksAndMatchesReferenceData() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)
        viewModel.selectTimeRange(EarningsTimeRange.MONTHLY)

        val monthly = viewModel.uiState.value.monthlyBreakdown
        // November 2026 spans 6 calendar weeks
        assertEquals(6, monthly.weeks.size)

        // W1: 1 Nov
        val w1 = monthly.weeks[0]
        assertEquals("W1", w1.weekLabel)
        assertEquals("Week 1: 1 Nov", w1.dateRangeText)
        assertEquals(1010.0, w1.totalEarningsRupees, 0.001)
        assertEquals(3, w1.completedDeliveriesCount)

        // W2: 2 Nov – 8 Nov
        val w2 = monthly.weeks[1]
        assertEquals("W2", w2.weekLabel)
        assertEquals("Week 2: 2 Nov – 8 Nov", w2.dateRangeText)
        assertEquals(2690.0, w2.totalEarningsRupees, 0.001)

        // Month total equals ₹3,700.00
        assertEquals(3700.0, monthly.totalEarningsRupees, 0.001)
    }

    @Test
    fun dailyOrderTimeline_hasOrderBarsWithExactTimes() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)
        viewModel.navigateToDailyForDate("2026-11-01")

        val daily = viewModel.uiState.value.dailyBreakdown
        assertEquals(3, daily.orders.size)

        val times = daily.orders.map { it.completedTimeText }
        assertEquals(listOf("09:30 AM", "01:15 PM", "05:50 PM"), times)

        val total = daily.orders.sumOf { it.earningsRupees }
        assertEquals(1010.0, total, 0.001)

        // Test order selection
        val secondOrder = daily.orders[1]
        viewModel.selectOrder(secondOrder)
        assertEquals(secondOrder.orderId, viewModel.uiState.value.selectedOrderInDaily?.orderId)
    }
}

