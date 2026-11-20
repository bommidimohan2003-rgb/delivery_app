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
class OnboardingAuthRedesignTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val samplePartner = DeliveryPartner(
        id = "partner_123",
        name = "Mohan B",
        phoneNumber = "+91 98765 43210",
        email = "mohanb@gmail.com"
    )

    private val fakeAuthRepository = object : AuthRepository {
        override val authStateFlow: Flow<AuthState> = MutableStateFlow(AuthState.Unauthenticated)

        override suspend fun register(
            fullName: String,
            email: String,
            phoneNumber: String,
            password: String
        ): NetworkResult<AuthResponseDto> =
            NetworkResult.Success(AuthResponseDto(partnerId = "partner_123", name = fullName))

        override suspend fun login(email: String, password: String): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(samplePartner)

        override suspend fun startDemoLogin(): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(samplePartner)

        override suspend fun requestOtp(phoneNumber: String): NetworkResult<Boolean> =
            NetworkResult.Success(true)

        override suspend fun verifyOtp(phoneNumber: String, otp: String): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(samplePartner)

        override suspend fun getPartnerProfile(): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(samplePartner)

        override suspend fun logout(): NetworkResult<Boolean> =
            NetworkResult.Success(true)
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
    fun testScreen2_loginValidation_emailAndPassword() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)

        // Attempt login with empty fields
        viewModel.login()
        val errorState = viewModel.uiState.value
        assertNotNull(errorState.emailError)
        assertNotNull(errorState.passwordError)

        // Invalid email format
        viewModel.onEmailChanged("not-an-email")
        viewModel.login()
        assertEquals("Please enter a valid email address", viewModel.uiState.value.emailError)

        // Valid credentials
        viewModel.onEmailChanged("partner@payent.com")
        viewModel.onPasswordChanged("SecurePass123!")
        viewModel.login()
        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun testScreen2_mobileLogin_phoneValidationAndOtpRequest() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)

        // Short invalid phone number
        viewModel.onPhoneNumberChanged("98765")
        viewModel.requestOtpForLogin()
        assertEquals("Please enter valid 10-digit mobile number", viewModel.uiState.value.phoneNumberError)
        assertFalse(viewModel.uiState.value.isOtpSent)

        // Valid 10-digit phone number
        viewModel.onPhoneNumberChanged("9876543210")
        viewModel.requestOtpForLogin()
        assertNull(viewModel.uiState.value.phoneNumberError)
        assertTrue(viewModel.uiState.value.isOtpSent)
        assertNotNull(viewModel.uiState.value.maskedPhoneNumber)
    }

    @Test
    fun testScreen2_demoLogin_succeeds() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)
        viewModel.startDemoLogin()
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun testScreen3_registration_allFieldsValidation() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)

        // Submit empty form
        viewModel.register()
        var state = viewModel.uiState.value
        assertNotNull(state.fullNameError)
        assertNotNull(state.emailError)
        assertNotNull(state.phoneNumberError)
        assertNotNull(state.passwordError)

        // Fix name & email
        viewModel.onFullNameChanged("Mohan B")
        viewModel.onEmailChanged("mohanb@gmail.com")
        viewModel.register()
        state = viewModel.uiState.value
        assertNull(state.fullNameError)
        assertNull(state.emailError)
        assertNotNull(state.phoneNumberError)

        // Fix phone
        viewModel.onPhoneNumberChanged("9876543210")

        // Password shorter than 8 chars
        viewModel.onPasswordChanged("short")
        viewModel.onConfirmPasswordChanged("short")
        viewModel.register()
        assertEquals("Password must be at least 8 characters long", viewModel.uiState.value.passwordError)

        // Password mismatch
        viewModel.onPasswordChanged("Password123!")
        viewModel.onConfirmPasswordChanged("PasswordMismatch!")
        viewModel.register()
        assertNull(viewModel.uiState.value.passwordError)
        assertEquals("Passwords do not match", viewModel.uiState.value.confirmPasswordError)

        // Full valid form
        viewModel.onConfirmPasswordChanged("Password123!")
        viewModel.register()
        state = viewModel.uiState.value
        assertNull(state.fullNameError)
        assertNull(state.emailError)
        assertNull(state.phoneNumberError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertTrue(state.isOtpSent)
    }

    @Test
    fun testScreen3_passwordVisibilityToggles() = runTest {
        val viewModel = AuthViewModel(fakeAuthRepository)

        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertFalse(viewModel.uiState.value.isPasswordVisible)

        assertFalse(viewModel.uiState.value.isConfirmPasswordVisible)
        viewModel.toggleConfirmPasswordVisibility()
        assertTrue(viewModel.uiState.value.isConfirmPasswordVisible)
        viewModel.toggleConfirmPasswordVisibility()
        assertFalse(viewModel.uiState.value.isConfirmPasswordVisible)
    }
}
