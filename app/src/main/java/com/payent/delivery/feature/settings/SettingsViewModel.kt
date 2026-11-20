package com.payent.delivery.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.payent.delivery.core.security.SessionManager
import com.payent.delivery.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.themeModeFlow.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            sessionManager.languageCodeFlow.collect { code ->
                _uiState.update { it.copy(selectedLanguage = code) }
            }
        }
        viewModelScope.launch {
            sessionManager.orderUpdatesEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(isOrderUpdatesEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            sessionManager.orderAlertSoundEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(isOrderAlertSoundEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            sessionManager.orderAlertVibrationEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(isOrderAlertVibrationEnabled = enabled) }
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            sessionManager.saveThemeMode(mode)
            _uiState.update {
                it.copy(
                    themeMode = mode,
                    noticeMessage = "Theme updated to ${mode.lowercase().replaceFirstChar { c -> c.uppercase() }} Mode."
                )
            }
        }
    }

    fun setLanguage(code: String) {
        val langName = when (code) {
            "te" -> "తెలుగు (Telugu)"
            "hi" -> "हिन्दी (Hindi)"
            else -> "English"
        }
        viewModelScope.launch {
            sessionManager.saveLanguageCode(code)
            _uiState.update {
                it.copy(
                    selectedLanguage = code,
                    noticeMessage = "Language set to $langName. String localization in progress."
                )
            }
        }
    }

    fun toggleNotificationPreference(preferenceKey: String) {
        when (preferenceKey) {
            "orders" -> {
                val newTarget = !_uiState.value.isOrderUpdatesEnabled
                _uiState.update { it.copy(isOrderUpdatesEnabled = newTarget) }
                viewModelScope.launch {
                    sessionManager.saveOrderUpdatesEnabled(newTarget)
                }
            }
            "order_sound" -> {
                val newTarget = !_uiState.value.isOrderAlertSoundEnabled
                _uiState.update { it.copy(isOrderAlertSoundEnabled = newTarget) }
                viewModelScope.launch {
                    sessionManager.saveOrderAlertSoundEnabled(newTarget)
                }
            }
            "order_vibration" -> {
                val newTarget = !_uiState.value.isOrderAlertVibrationEnabled
                _uiState.update { it.copy(isOrderAlertVibrationEnabled = newTarget) }
                viewModelScope.launch {
                    sessionManager.saveOrderAlertVibrationEnabled(newTarget)
                }
            }
            "pickup" -> _uiState.update { it.copy(isPickupRemindersEnabled = !it.isPickupRemindersEnabled) }
            "delivery" -> _uiState.update { it.copy(isDeliveryUpdatesEnabled = !it.isDeliveryUpdatesEnabled) }
            "earnings" -> _uiState.update { it.copy(isEarningsUpdatesEnabled = !it.isEarningsUpdatesEnabled) }
            "announcements" -> _uiState.update { it.copy(isAnnouncementsEnabled = !it.isAnnouncementsEnabled) }
        }
    }

    fun updateSystemNotificationPermission(granted: Boolean) {
        _uiState.update { it.copy(isSystemNotificationPermissionGranted = granted) }
    }

    fun requestLogoutConfirmation() {
        _uiState.update { it.copy(isLogoutConfirmDialogOpen = true) }
    }

    fun dismissLogoutConfirmation() {
        _uiState.update { it.copy(isLogoutConfirmDialogOpen = false) }
    }

    fun confirmLogout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, isLogoutConfirmDialogOpen = false) }
            authRepository.logout()
            _uiState.update { it.copy(isLoggingOut = false) }
            onLoggedOut()
        }
    }

    fun clearNoticeMessage() {
        _uiState.update { it.copy(noticeMessage = null) }
    }

    class Factory(
        private val sessionManager: SessionManager,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(sessionManager, authRepository) as T
        }
    }
}
