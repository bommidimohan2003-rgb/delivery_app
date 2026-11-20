package com.payent.delivery

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.data.remote.dto.AuthResponseDto
import com.payent.delivery.domain.model.AuthState
import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.repository.AuthRepository
import com.payent.delivery.feature.auth.AuthViewModel
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
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private var mockRegisterResult: NetworkResult<AuthResponseDto> =
        NetworkResult.Success(AuthResponseDto(partnerId = "p1", name = "John Doe"))
    private var mockLoginResult: NetworkResult<DeliveryPartner> =
        NetworkResult.Success(DeliveryPartner(id = "p1", name = "John Doe", phoneNumber = "+12345678901"))
    private var mockVerifyOtpResult: NetworkResult<DeliveryPartner> =
        NetworkResult.Success(DeliveryPartner(id = "p1", name = "John Doe", phoneNumber = "+12345678901"))

    private val fakeAuthRepository = object : AuthRepository {
        override val authStateFlow: Flow<AuthState> = MutableStateFlow(AuthState.Unauthenticated)

        override suspend fun register(
            fullName: String,
            email: String,
            phoneNumber: String,
            password: String
        ): NetworkResult<AuthResponseDto> = mockRegisterResult

        override suspend fun login(email: String, password: String): NetworkResult<DeliveryPartner> = mockLoginResult

        override suspend fun startDemoLogin(): NetworkResult<DeliveryPartner> = mockLoginResult

        override suspend fun requestOtp(phoneNumber: String): NetworkResult<Boolean> = NetworkResult.Success(true)

        override suspend fun verifyOtp(phoneNumber: String, otp: String): NetworkResult<DeliveryPartner> = mockVerifyOtpResult

        override suspend fun getPartnerProfile(): NetworkResult<DeliveryPartner> = mockLoginResult

        override suspend fun logout(): NetworkResult<Boolean> = NetworkResult.Success(true)
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
    fun register_showsErrors_whenFieldsAreEmpty() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.register()

        val state = viewModel.uiState.value
        assertNotNull(state.fullNameError)
        assertNotNull(state.emailError)
        assertNotNull(state.phoneNumberError)
        assertNotNull(state.passwordError)
    }

    @Test
    fun register_showsError_whenPasswordConfirmationMismatches() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.onFullNameChanged("John Doe")
        viewModel.onEmailChanged("john@example.com")
        viewModel.onPhoneNumberChanged("+12345678901")
        viewModel.onPasswordChanged("Secret123!")
        viewModel.onConfirmPasswordChanged("Different123!")

        viewModel.register()

        val state = viewModel.uiState.value
        assertNull(state.fullNameError)
        assertNull(state.emailError)
        assertNotNull(state.confirmPasswordError)
    }

    @Test
    fun register_succeeds_andMasksPhone_whenValid() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.onFullNameChanged("John Doe")
        viewModel.onEmailChanged("john@example.com")
        viewModel.onPhoneNumberChanged("+12345678901")
        viewModel.onPasswordChanged("Secret123!")
        viewModel.onConfirmPasswordChanged("Secret123!")

        viewModel.register()

        val state = viewModel.uiState.value
        assertTrue(state.isOtpSent)
        assertEquals("+12 ***** 8901", state.maskedPhoneNumber)
    }

    @Test
    fun login_showsError_whenCredentialsAreEmpty() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.login()

        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
    }

    @Test
    fun login_handlesApiError_whenCredentialsInvalid() = runTest {
        mockLoginResult = NetworkResult.ApiError(401, "Invalid email or password.")
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.onEmailChanged("test@example.com")
        viewModel.onPasswordChanged("wrongpassword")

        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals("Invalid email or password.", state.errorMessage)
    }

    @Test
    fun startDemoLogin_executesSuccessfully() = runTest {
        mockLoginResult = NetworkResult.Success(DeliveryPartner(id = "demo_partner", name = "Demo Delivery Partner", phoneNumber = "+1000000000"))
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.startDemoLogin()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun requestOtpForLogin_showsError_whenPhoneIsInvalid() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.onPhoneNumberChanged("123")
        viewModel.requestOtpForLogin()

        val state = viewModel.uiState.value
        assertNotNull(state.phoneNumberError)
        assertFalse(state.isOtpSent)
    }

    @Test
    fun requestOtpForLogin_succeeds_whenPhoneIsValid() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.onPhoneNumberChanged("9876543210")
        viewModel.requestOtpForLogin()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOtpSent)
        assertNull(state.phoneNumberError)
        assertEquals("+91 ***** 3210", state.maskedPhoneNumber)
    }

    @Test
    fun verifyOtpForLogin_showsError_whenOtpTooShort() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.onPhoneNumberChanged("9876543210")
        viewModel.onOtpChanged("12")
        viewModel.verifyOtpForLogin()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
    }

    @Test
    fun verifyOtpForLogin_succeeds_whenOtpValid() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.onPhoneNumberChanged("9876543210")
        viewModel.onOtpChanged("123456")
        viewModel.verifyOtpForLogin()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun resetOtpState_clearsOtpAndCooldown() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.onPhoneNumberChanged("9876543210")
        viewModel.requestOtpForLogin()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOtpSent)

        viewModel.resetOtpState()
        val state = viewModel.uiState.value
        assertFalse(state.isOtpSent)
        assertEquals("", state.otp)
        assertEquals(0, state.resendCooldownSeconds)
    }
}

