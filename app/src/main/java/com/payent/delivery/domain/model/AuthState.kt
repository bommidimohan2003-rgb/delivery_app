package com.payent.delivery.domain.model

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data class RegistrationPendingVerification(
        val phoneNumber: String,
        val maskedPhoneNumber: String
    ) : AuthState()
    data class Authenticated(
        val partnerId: String,
        val name: String,
        val phoneNumber: String,
        val isDemo: Boolean = false
    ) : AuthState()
    data class SessionExpired(val reason: String) : AuthState()
}
