package com.payent.delivery

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.data.remote.dto.AuthResponseDto
import com.payent.delivery.domain.model.AuthState
import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.model.KycDocumentStatus
import com.payent.delivery.domain.model.KycDocumentType
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.repository.AuthRepository
import com.payent.delivery.feature.notifications.NotificationFilter
import com.payent.delivery.feature.notifications.NotificationGroup
import com.payent.delivery.feature.notifications.NotificationsViewModel
import com.payent.delivery.feature.profile.ProfileViewModel
import com.payent.delivery.feature.support.SupportCategory
import com.payent.delivery.feature.support.SupportTab
import com.payent.delivery.feature.support.SupportViewModel
import com.payent.delivery.feature.support.TicketStatus
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileAccountRedesignTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val samplePartner = DeliveryPartner(
        id = "demo_partner",
        name = "Mohan B",
        phoneNumber = "+91 98765 43210",
        email = "mohanb@gmail.com",
        address = "123, 4th Street, Gachibowli",
        pincode = "500032",
        isOnline = true,
        kycStatus = KycStatus.NOT_STARTED
    )

    private val fakeAuthRepository = object : AuthRepository {
        override val authStateFlow: Flow<AuthState> = MutableStateFlow(
            AuthState.Authenticated(
                partnerId = samplePartner.id,
                name = samplePartner.name,
                phoneNumber = samplePartner.phoneNumber
            )
        )
        override suspend fun register(fullName: String, email: String, phoneNumber: String, password: String) =
            NetworkResult.Success(AuthResponseDto("1", fullName))
        override suspend fun login(email: String, password: String) = NetworkResult.Success(samplePartner)
        override suspend fun startDemoLogin() = NetworkResult.Success(samplePartner)
        override suspend fun requestOtp(phoneNumber: String) = NetworkResult.Success(true)
        override suspend fun verifyOtp(phoneNumber: String, otp: String) = NetworkResult.Success(samplePartner)
        override suspend fun getPartnerProfile() = NetworkResult.Success(samplePartner)
        override suspend fun logout() = NetworkResult.Success(true)
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
    fun testProfileScreen1_initialDataMatchesReferenceImage() = runTest {
        val viewModel = ProfileViewModel(fakeAuthRepository)
        val state = viewModel.uiState.value

        assertEquals("Mohan B", state.partner?.name)
        assertEquals("+91 98765 43210", state.partner?.phoneNumber)
        assertEquals("mohanb@gmail.com", state.partner?.email)
        assertEquals("123, 4th Street, Gachibowli", state.partner?.address)
        assertEquals("500032", state.partner?.pincode)

        // Toggle online status
        viewModel.toggleOnlineStatus(false)
        assertFalse(viewModel.uiState.value.partner?.isOnline ?: true)
        viewModel.toggleOnlineStatus(true)
        assertTrue(viewModel.uiState.value.partner?.isOnline ?: false)
    }

    @Test
    fun testEditProfileScreen2_validationRules() = runTest {
        val viewModel = ProfileViewModel(fakeAuthRepository)
        viewModel.initEditProfileForm()

        // Missing full name
        viewModel.onEditNameChanged("")
        assertFalse(viewModel.saveProfileChanges())
        assertNotNull(viewModel.uiState.value.editNameError)

        // Invalid email
        viewModel.onEditNameChanged("Mohan B")
        viewModel.onEditEmailChanged("not-an-email")
        assertFalse(viewModel.saveProfileChanges())
        assertNotNull(viewModel.uiState.value.editEmailError)

        // Invalid pincode (less than 6 digits)
        viewModel.onEditEmailChanged("mohanb@gmail.com")
        viewModel.onEditPincodeChanged("5000")
        assertFalse(viewModel.saveProfileChanges())
        assertNotNull(viewModel.uiState.value.editPincodeError)

        // Valid submission
        viewModel.onEditPincodeChanged("500032")
        viewModel.onEditAddressChanged("123, 4th Street, Gachibowli")
        assertTrue(viewModel.saveProfileChanges())
        assertNull(viewModel.uiState.value.editNameError)
        assertNull(viewModel.uiState.value.editEmailError)
        assertNull(viewModel.uiState.value.editPincodeError)
        assertEquals("Profile updated successfully.", viewModel.uiState.value.isProfileSaveSuccessMessage)
    }

    @Test
    fun testKycScreen3_documentSelectionAndSubmission() = runTest {
        val viewModel = ProfileViewModel(fakeAuthRepository)

        // Select Driving Licence
        viewModel.selectKycDocument(KycDocumentType.DRIVING_LICENCE, "content://dl_uri", "dl_front.jpg")
        val dl = viewModel.uiState.value.kycDocuments.find { it.type == KycDocumentType.DRIVING_LICENCE }
        assertEquals(KycDocumentStatus.SELECTED_LOCALLY, dl?.status)

        // Submitting with missing documents triggers validation error
        viewModel.submitKycForDemo()
        assertNotNull(viewModel.uiState.value.kycValidationMessage)

        // Select all remaining documents
        viewModel.selectKycDocument(KycDocumentType.AADHAAR_CARD, "content://aadhaar_uri", "aadhaar_front.jpg")
        viewModel.selectKycDocument(KycDocumentType.BANK_PASSBOOK, "content://bank_uri", "passbook_front.jpg")

        viewModel.submitKycForDemo()
        assertNull(viewModel.uiState.value.kycValidationMessage)
        assertEquals(KycDocumentStatus.SUBMITTED_DEMO, viewModel.uiState.value.kycOverallStatus)
        assertEquals(KycStatus.SUBMITTED_DEMO, viewModel.uiState.value.partner?.kycStatus)
    }

    @Test
    fun testNotificationsScreen4_filtersAndGroupsMatchReference() {
        val viewModel = NotificationsViewModel()
        val allNotifications = viewModel.uiState.value.notifications
        assertEquals(6, allNotifications.size)

        // Verify Today items
        val todayItems = allNotifications.filter { it.group == NotificationGroup.TODAY }
        assertEquals(3, todayItems.size)
        assertEquals("Order Assigned", todayItems[0].title)
        assertEquals("Pickup Verified", todayItems[1].title)
        assertEquals("Delivery Completed", todayItems[2].title)

        // Verify Yesterday items
        val yesterdayItems = allNotifications.filter { it.group == NotificationGroup.YESTERDAY }
        assertEquals(2, yesterdayItems.size)
        assertEquals("Payout Initiated", yesterdayItems[0].title)
        assertEquals("KYC Under Review", yesterdayItems[1].title)

        // Verify Filter Switching
        viewModel.selectFilter(NotificationFilter.ORDERS)
        assertEquals(NotificationFilter.ORDERS, viewModel.uiState.value.selectedFilter)
        val ordersOnly = allNotifications.filter { it.filterCategory == NotificationFilter.ORDERS }
        assertEquals(3, ordersOnly.size)

        viewModel.selectFilter(NotificationFilter.PAYMENTS)
        val paymentsOnly = allNotifications.filter { it.filterCategory == NotificationFilter.PAYMENTS }
        assertEquals(1, paymentsOnly.size)
        assertEquals("Payout Initiated", paymentsOnly[0].title)
    }

    @Test
    fun testHelpSupportScreen5_tabsAndTicketCreation() {
        val viewModel = SupportViewModel()
        val state = viewModel.uiState.value

        // Verify default tab is Support Tickets
        assertEquals(SupportTab.TICKETS, state.selectedTab)

        // Verify reference tickets exist
        val tickets = state.supportTickets
        assertEquals(3, tickets.size)
        assertEquals("#TKT1001", tickets[0].reference)
        assertEquals(TicketStatus.OPEN, tickets[0].status)
        assertEquals("Payout not received", tickets[0].subject)

        assertEquals("#TKT10987", tickets[1].reference)
        assertEquals(TicketStatus.IN_PROGRESS, tickets[1].status)
        assertEquals("Document verification issue", tickets[1].subject)

        assertEquals("#TKT0954", tickets[2].reference)
        assertEquals(TicketStatus.RESOLVED, tickets[2].status)
        assertEquals("App navigation problem", tickets[2].subject)

        // Switch tab to FAQ
        viewModel.selectTab(SupportTab.FAQ)
        assertEquals(SupportTab.FAQ, viewModel.uiState.value.selectedTab)

        // Create new ticket flow
        viewModel.openCreateTicketDialog()
        assertTrue(viewModel.uiState.value.isCreateTicketDialogOpen)

        viewModel.onSubjectChanged("Payment delay inquiry")
        viewModel.selectTicketCategory(SupportCategory.EARNINGS_PAYMENTS)
        viewModel.onDescriptionChanged("My payment for Friday orders has not arrived yet.")

        assertTrue(viewModel.submitDemoTicket())
        assertFalse(viewModel.uiState.value.isCreateTicketDialogOpen)
        assertEquals(4, viewModel.uiState.value.supportTickets.size)
        assertEquals("Payment delay inquiry", viewModel.uiState.value.supportTickets[0].subject)
    }
}
