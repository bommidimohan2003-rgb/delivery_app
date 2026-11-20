package com.payent.delivery.feature.auth

import com.payent.delivery.domain.model.AuthState

data class AuthUiState(
    // Delivery Partner ID / Login Fields
    val partnerId: String = "",
    val partnerIdError: String? = null,
    val backendCompatibilityNotice: String? = null,

    // Registration Fields
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,

    // Inline Validation Error Messages
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneNumberError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    // OTP Fields & State
    val otp: String = "",
    val maskedPhoneNumber: String = "",
    val resendCooldownSeconds: Int = 0,
    val isOtpSent: Boolean = false,

    // General Screen State
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val authState: AuthState = AuthState.Unauthenticated
)
