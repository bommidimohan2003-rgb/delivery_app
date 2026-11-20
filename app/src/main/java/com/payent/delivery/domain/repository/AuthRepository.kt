package com.payent.delivery.domain.repository

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.data.remote.dto.AuthResponseDto
import com.payent.delivery.domain.model.AuthState
import com.payent.delivery.domain.model.DeliveryPartner
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authStateFlow: Flow<AuthState>
    suspend fun register(fullName: String, email: String, phoneNumber: String, password: String): NetworkResult<AuthResponseDto>
    suspend fun login(email: String, password: String): NetworkResult<DeliveryPartner>
    suspend fun startDemoLogin(): NetworkResult<DeliveryPartner>
    suspend fun requestOtp(phoneNumber: String): NetworkResult<Boolean>
    suspend fun verifyOtp(phoneNumber: String, otp: String): NetworkResult<DeliveryPartner>
    suspend fun getPartnerProfile(): NetworkResult<DeliveryPartner>
    suspend fun logout(): NetworkResult<Boolean>
}
