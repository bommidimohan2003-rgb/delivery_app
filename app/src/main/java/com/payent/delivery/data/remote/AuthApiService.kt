package com.payent.delivery.data.remote

import com.payent.delivery.data.remote.dto.AuthResponseDto
import com.payent.delivery.data.remote.dto.LoginRequestDto
import com.payent.delivery.data.remote.dto.OtpRequestDto
import com.payent.delivery.data.remote.dto.OtpVerifyRequestDto
import com.payent.delivery.data.remote.dto.PartnerProfileDto
import com.payent.delivery.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/otp/request")
    suspend fun requestOtp(@Body request: OtpRequestDto)

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequestDto): AuthResponseDto

    @GET("partner/profile")
    suspend fun getProfile(): PartnerProfileDto

    @POST("auth/logout")
    suspend fun logout()
}
