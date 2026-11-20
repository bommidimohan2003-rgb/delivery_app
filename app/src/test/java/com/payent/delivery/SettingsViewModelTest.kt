package com.payent.delivery

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.core.security.SessionManager
import com.payent.delivery.data.remote.dto.AuthResponseDto
import com.payent.delivery.domain.model.AuthState
import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.repository.AuthRepository
import com.payent.delivery.feature.settings.SettingsViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeThemeFlow = MutableStateFlow("DARK")
    private val fakeLangFlow = MutableStateFlow("en")
    private val fakeOrderUpdatesFlow = MutableStateFlow(true)
    private val fakeOrderAlertSoundFlow = MutableStateFlow(true)
    private val fakeOrderAlertVibrationFlow = MutableStateFlow(true)

    private val testPartner = DeliveryPartner(
        id = "1",
        name = "Test Partner",
        phoneNumber = "+1234567890",
        email = "test@example.com"
    )

    private val fakeSessionManager = object : SessionManager {
        override val authTokenFlow: Flow<String?> = MutableStateFlow("token")
        override val partnerIdFlow: Flow<String?> = MutableStateFlow("id")
        override val partnerNameFlow: Flow<String?> = MutableStateFlow("partner")
        override val isDemoSessionFlow: Flow<Boolean> = MutableStateFlow(true)
        override val isOnlineStatusFlow: Flow<Boolean> = MutableStateFlow(false)
        override val themeModeFlow: Flow<String> = fakeThemeFlow
        override val languageCodeFlow: Flow<String> = fakeLangFlow
        override val orderUpdatesEnabledFlow: Flow<Boolean> = fakeOrderUpdatesFlow
        override val orderAlertSoundEnabledFlow: Flow<Boolean> = fakeOrderAlertSoundFlow
        override val orderAlertVibrationEnabledFlow: Flow<Boolean> = fakeOrderAlertVibrationFlow

        override fun getAuthTokenSync(): String = "token"
        override suspend fun saveAuthToken(token: String) {}
        override suspend fun savePartnerSession(partnerId: String, partnerName: String, token: String) {}
        override suspend fun saveDemoSession() {}
        override suspend fun saveOnlineStatus(isOnline: Boolean) {}
        override suspend fun saveThemeMode(mode: String) { fakeThemeFlow.value = mode }
        override suspend fun saveLanguageCode(code: String) { fakeLangFlow.value = code }
        override suspend fun saveOrderUpdatesEnabled(enabled: Boolean) { fakeOrderUpdatesFlow.value = enabled }
        override suspend fun saveOrderAlertSoundEnabled(enabled: Boolean) { fakeOrderAlertSoundFlow.value = enabled }
        override suspend fun saveOrderAlertVibrationEnabled(enabled: Boolean) { fakeOrderAlertVibrationFlow.value = enabled }
        override suspend fun clearSession() {}
    }

    private val fakeAuthRepository = object : AuthRepository {
        override val authStateFlow: Flow<AuthState> = MutableStateFlow(
            AuthState.Authenticated(
                partnerId = "1",
                name = "Test Partner",
                phoneNumber = "+1234567890"
            )
        )
        override suspend fun register(fullName: String, email: String, phoneNumber: String, password: String): NetworkResult<AuthResponseDto> =
            NetworkResult.Success(AuthResponseDto("1", fullName))
        override suspend fun login(email: String, password: String): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(testPartner)
        override suspend fun startDemoLogin(): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(testPartner)
        override suspend fun requestOtp(phoneNumber: String): NetworkResult<Boolean> =
            NetworkResult.Success(true)
        override suspend fun verifyOtp(phoneNumber: String, otp: String): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(testPartner)
        override suspend fun getPartnerProfile(): NetworkResult<DeliveryPartner> =
            NetworkResult.Success(testPartner)
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
    fun themeMode_updatesAndPersistsInSessionManager() = runTest {
        val viewModel = SettingsViewModel(fakeSessionManager, fakeAuthRepository)

        assertEquals("DARK", viewModel.uiState.value.themeMode)

        viewModel.setThemeMode("LIGHT")
        assertEquals("LIGHT", viewModel.uiState.value.themeMode)
        assertEquals("LIGHT", fakeThemeFlow.value)
    }

    @Test
    fun languageSelection_updatesStateAndSessionManager() = runTest {
        val viewModel = SettingsViewModel(fakeSessionManager, fakeAuthRepository)

        viewModel.setLanguage("te")
        assertEquals("te", viewModel.uiState.value.selectedLanguage)
        assertEquals("te", fakeLangFlow.value)
    }

    @Test
    fun notificationToggles_updateLocally() = runTest {
        val viewModel = SettingsViewModel(fakeSessionManager, fakeAuthRepository)

        assertTrue(viewModel.uiState.value.isOrderUpdatesEnabled)

        viewModel.toggleNotificationPreference("orders")
        assertFalse(viewModel.uiState.value.isOrderUpdatesEnabled)
    }

    @Test
    fun logoutConfirmation_clearsSessionOnConfirm() = runTest {
        val viewModel = SettingsViewModel(fakeSessionManager, fakeAuthRepository)

        viewModel.requestLogoutConfirmation()
        assertTrue(viewModel.uiState.value.isLogoutConfirmDialogOpen)

        var loggedOutCalled = false
        viewModel.confirmLogout { loggedOutCalled = true }

        assertTrue(loggedOutCalled)
        assertFalse(viewModel.uiState.value.isLogoutConfirmDialogOpen)
    }
}
