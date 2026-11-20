package com.payent.delivery.data.repository

import com.payent.delivery.core.common.DispatcherProvider
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.core.network.safeApiCall
import com.payent.delivery.core.security.SessionManager
import com.payent.delivery.data.remote.AuthApiService
import com.payent.delivery.data.remote.dto.AuthResponseDto
import com.payent.delivery.data.remote.dto.LoginRequestDto
import com.payent.delivery.data.remote.dto.OtpRequestDto
import com.payent.delivery.data.remote.dto.OtpVerifyRequestDto
import com.payent.delivery.data.remote.dto.RegisterRequestDto
import com.payent.delivery.domain.model.AuthState
import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val dispatchers: DispatcherProvider
) : AuthRepository {

    override val authStateFlow: Flow<AuthState> = combine(
        sessionManager.partnerIdFlow,
        sessionManager.partnerNameFlow,
        sessionManager.authTokenFlow,
        sessionManager.isDemoSessionFlow
    ) { partnerId, partnerName, token, isDemo ->
        if (!token.isNullOrBlank() && !partnerId.isNullOrBlank()) {
            AuthState.Authenticated(
                partnerId = partnerId,
                name = partnerName ?: if (isDemo) "Demo Delivery Partner" else "Delivery Partner",
                phoneNumber = if (isDemo) "+1 000 000 0000" else "",
                isDemo = isDemo
            )
        } else {
            AuthState.Unauthenticated
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String
    ): NetworkResult<AuthResponseDto> {
        return when (val result = safeApiCall(dispatchers.io) {
            authApiService.register(RegisterRequestDto(fullName, email, phoneNumber, password))
        }) {
            is NetworkResult.Success -> {
                val dto = result.data
                if (!dto.token.isNullOrBlank() && !dto.partnerId.isNullOrBlank()) {
                    sessionManager.savePartnerSession(
                        partnerId = dto.partnerId,
                        partnerName = dto.name ?: fullName,
                        token = dto.token
                    )
                }
                NetworkResult.Success(dto)
            }
            is NetworkResult.ApiError -> NetworkResult.ApiError(result.code, result.message)
            is NetworkResult.NetworkError -> NetworkResult.NetworkError(result.exception)
            is NetworkResult.UnknownError -> NetworkResult.UnknownError(result.exception)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): NetworkResult<DeliveryPartner> {
        return when (val result = safeApiCall(dispatchers.io) {
            authApiService.login(LoginRequestDto(email, password))
        }) {
            is NetworkResult.Success -> {
                val dto = result.data
                val token = dto.token ?: ""
                val partnerId = dto.partnerId ?: ""
                if (token.isNotBlank() && partnerId.isNotBlank()) {
                    sessionManager.savePartnerSession(
                        partnerId = partnerId,
                        partnerName = dto.name ?: "Delivery Partner",
                        token = token
                    )
                }
                NetworkResult.Success(
                    DeliveryPartner(
                        id = partnerId,
                        name = dto.name ?: "Delivery Partner",
                        phoneNumber = dto.phoneNumber ?: "",
                        email = dto.email ?: email,
                        isOnline = dto.isOnline,
                        kycStatus = parseKycStatus(dto.kycStatus)
                    )
                )
            }
            is NetworkResult.ApiError -> NetworkResult.ApiError(result.code, result.message)
            is NetworkResult.NetworkError -> NetworkResult.NetworkError(result.exception)
            is NetworkResult.UnknownError -> NetworkResult.UnknownError(result.exception)
        }
    }

    override suspend fun startDemoLogin(): NetworkResult<DeliveryPartner> {
        sessionManager.saveDemoSession()
        return NetworkResult.Success(
            DeliveryPartner(
                id = "demo_partner",
                name = "Demo Delivery Partner",
                phoneNumber = "+1 000 000 0000",
                email = "demo.delivery@example.com",
                isOnline = false,
                kycStatus = KycStatus.NOT_SUBMITTED,
                vehicleType = "Electric Scooter (Demo)"
            )
        )
    }

    override suspend fun requestOtp(phoneNumber: String): NetworkResult<Boolean> {
        return safeApiCall(dispatchers.io) {
            authApiService.requestOtp(OtpRequestDto(phoneNumber))
            true
        }
    }

    override suspend fun verifyOtp(
        phoneNumber: String,
        otp: String
    ): NetworkResult<DeliveryPartner> {
        return when (val result = safeApiCall(dispatchers.io) {
            authApiService.verifyOtp(OtpVerifyRequestDto(phoneNumber, otp))
        }) {
            is NetworkResult.Success -> {
                val dto = result.data
                val token = dto.token ?: ""
                val partnerId = dto.partnerId ?: ""
                if (token.isNotBlank() && partnerId.isNotBlank()) {
                    sessionManager.savePartnerSession(
                        partnerId = partnerId,
                        partnerName = dto.name ?: "Delivery Partner",
                        token = token
                    )
                }
                NetworkResult.Success(
                    DeliveryPartner(
                        id = partnerId,
                        name = dto.name ?: "Delivery Partner",
                        phoneNumber = dto.phoneNumber ?: phoneNumber,
                        email = dto.email,
                        isOnline = dto.isOnline,
                        kycStatus = parseKycStatus(dto.kycStatus)
                    )
                )
            }
            is NetworkResult.ApiError -> NetworkResult.ApiError(result.code, result.message)
            is NetworkResult.NetworkError -> NetworkResult.NetworkError(result.exception)
            is NetworkResult.UnknownError -> NetworkResult.UnknownError(result.exception)
        }
    }

    override suspend fun getPartnerProfile(): NetworkResult<DeliveryPartner> {
        val currentToken = sessionManager.getAuthTokenSync()
        if (currentToken == "demo_token_session_active") {
            return NetworkResult.Success(
                DeliveryPartner(
                    id = "demo_partner",
                    name = "Demo Delivery Partner",
                    phoneNumber = "+1 000 000 0000",
                    email = "demo.delivery@example.com",
                    isOnline = false,
                    kycStatus = KycStatus.NOT_SUBMITTED,
                    vehicleType = "Electric Scooter (Demo)"
                )
            )
        }

        return when (val result = safeApiCall(dispatchers.io) {
            authApiService.getProfile()
        }) {
            is NetworkResult.Success -> {
                val dto = result.data
                NetworkResult.Success(
                    DeliveryPartner(
                        id = dto.id,
                        name = dto.name,
                        phoneNumber = dto.phoneNumber,
                        email = dto.email,
                        isOnline = dto.isOnline,
                        kycStatus = parseKycStatus(dto.kycStatus),
                        vehicleType = dto.vehicleType
                    )
                )
            }
            is NetworkResult.ApiError -> NetworkResult.ApiError(result.code, result.message)
            is NetworkResult.NetworkError -> NetworkResult.NetworkError(result.exception)
            is NetworkResult.UnknownError -> NetworkResult.UnknownError(result.exception)
        }
    }

    override suspend fun logout(): NetworkResult<Boolean> {
        sessionManager.clearSession()
        return safeApiCall(dispatchers.io) {
            try {
                authApiService.logout()
            } catch (_: Exception) {}
            true
        }
    }

    private fun parseKycStatus(statusStr: String): KycStatus {
        return try {
            KycStatus.valueOf(statusStr.uppercase())
        } catch (_: Exception) {
            KycStatus.PENDING
        }
    }
}
