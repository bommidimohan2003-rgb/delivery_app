package com.payent.delivery.data.remote.dto

data class RegisterRequestDto(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class OtpRequestDto(
    val phoneNumber: String
)

data class OtpVerifyRequestDto(
    val phoneNumber: String,
    val otp: String
)

data class AuthResponseDto(
    val token: String? = null,
    val partnerId: String? = null,
    val name: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val isOnline: Boolean = false,
    val kycStatus: String = "PENDING",
    val requiresOtp: Boolean = false,
    val maskedPhoneNumber: String? = null
)

data class PartnerProfileDto(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val isOnline: Boolean = false,
    val kycStatus: String = "PENDING",
    val vehicleType: String? = null
)
