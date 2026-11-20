package com.payent.delivery

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.data.remote.dto.AuthResponseDto
import com.payent.delivery.domain.model.AuthState
import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.model.KycDocumentStatus
import com.payent.delivery.domain.model.KycDocumentType
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.repository.AuthRepository
import com.payent.delivery.feature.profile.ProfileViewModel
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
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testPartner = DeliveryPartner(
        id = "demo_partner",
        name = "Demo Partner",
        phoneNumber = "+1234567890",
        email = "demo@example.com",
        kycStatus = KycStatus.NOT_STARTED
    )

    private val fakeAuthRepository = object : AuthRepository {
        override val authStateFlow: Flow<AuthState> = MutableStateFlow(
            AuthState.Authenticated(
                partnerId = "demo_partner",
                name = "Demo Partner",
                phoneNumber = "+1234567890"
            )
        )
        override suspend fun register(fullName: String, email: String, phoneNumber: String, password: String) =
            NetworkResult.Success(AuthResponseDto("1", fullName))
        override suspend fun login(email: String, password: String) = NetworkResult.Success(testPartner)
        override suspend fun startDemoLogin() = NetworkResult.Success(testPartner)
        override suspend fun requestOtp(phoneNumber: String) = NetworkResult.Success(true)
        override suspend fun verifyOtp(phoneNumber: String, otp: String) = NetworkResult.Success(testPartner)
        override suspend fun getPartnerProfile() = NetworkResult.Success(testPartner)
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
    fun editProfileValidation_failsWhenEmpty() = runTest {
        val viewModel = ProfileViewModel(fakeAuthRepository)

        viewModel.onEditNameChanged("")
        viewModel.onEditEmailChanged("invalid-email")
        viewModel.onEditPhoneChanged("123")

        val result = viewModel.saveProfileChanges()

        assertFalse(result)
        assertNotNull(viewModel.uiState.value.editNameError)
        assertNotNull(viewModel.uiState.value.editEmailError)
        assertNotNull(viewModel.uiState.value.editPhoneError)
    }

    @Test
    fun saveProfileChanges_updatesLocallyWhenValid() = runTest {
        val viewModel = ProfileViewModel(fakeAuthRepository)

        viewModel.onEditNameChanged("Jane Partner")
        viewModel.onEditEmailChanged("jane@example.com")
        viewModel.onEditPhoneChanged("+12345678901")

        val result = viewModel.saveProfileChanges()

        assertTrue(result)
        assertEquals("Jane Partner", viewModel.uiState.value.partner?.name)
        assertNotNull(viewModel.uiState.value.isProfileSaveSuccessMessage)
    }

    @Test
    fun kycDocumentSelection_andDemoSubmission() = runTest {
        val viewModel = ProfileViewModel(fakeAuthRepository)

        viewModel.selectKycDocument(KycDocumentType.DRIVING_LICENCE, "content://uri_dl", "dl.jpg")
        viewModel.selectKycDocument(KycDocumentType.AADHAAR_CARD, "content://uri_aadhaar", "aadhaar.jpg")
        viewModel.selectKycDocument(KycDocumentType.BANK_PASSBOOK, "content://uri_passbook", "passbook.jpg")

        viewModel.submitKycForDemo()

        assertEquals(KycDocumentStatus.SUBMITTED_DEMO, viewModel.uiState.value.kycOverallStatus)
        assertEquals(KycStatus.SUBMITTED_DEMO, viewModel.uiState.value.partner?.kycStatus)
    }
}
