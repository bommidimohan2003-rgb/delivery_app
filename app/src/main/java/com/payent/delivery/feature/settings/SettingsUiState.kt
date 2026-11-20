package com.payent.delivery.feature.settings

data class SettingsUiState(
    val themeMode: String = "DARK",
    val selectedLanguage: String = "en",
    val isOrderUpdatesEnabled: Boolean = true,
    val isOrderAlertSoundEnabled: Boolean = true,
    val isOrderAlertVibrationEnabled: Boolean = true,
    val isPickupRemindersEnabled: Boolean = true,
    val isDeliveryUpdatesEnabled: Boolean = true,
    val isEarningsUpdatesEnabled: Boolean = true,
    val isAnnouncementsEnabled: Boolean = true,
    val isLogoutConfirmDialogOpen: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isSystemNotificationPermissionGranted: Boolean = true,
    val appVersion: String = "1.0.0 (Phase 9)",
    val noticeMessage: String? = null
)
