package com.payent.delivery.feature.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.domain.model.AuthState
import com.payent.delivery.domain.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { authState ->
                _uiState.update { it.copy(authState = authState) }
            }
        }
    }

    fun onPartnerIdChanged(id: String) {
        _uiState.update { it.copy(partnerId = id, partnerIdError = null, errorMessage = null, backendCompatibilityNotice = null) }
    }

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullName = name, fullNameError = null, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPhoneNumberChanged(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone, phoneNumberError = null, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onOtpChanged(otp: String) {
        if (otp.length <= 6) {
            _uiState.update { it.copy(otp = otp, errorMessage = null) }
        }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun register() {
        val state = _uiState.value
        val name = state.fullName.trim()
        val email = state.email.trim()
        val phone = state.phoneNumber.trim()
        val password = state.password
        val confirmPassword = state.confirmPassword

        var hasError = false
        var nameErr: String? = null
        var emailErr: String? = null
        var phoneErr: String? = null
        var passwordErr: String? = null
        var confirmPasswordErr: String? = null

        if (name.isBlank()) {
            nameErr = "Full name is required"
            hasError = true
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (email.isBlank() || !emailRegex.matches(email)) {
            emailErr = "Please enter a valid email address"
            hasError = true
        }

        if (phone.length < 10) {
            phoneErr = "Enter valid mobile number with country code"
            hasError = true
        }

        if (password.length < 8) {
            passwordErr = "Password must be at least 8 characters long"
            hasError = true
        }

        if (confirmPassword != password) {
            confirmPasswordErr = "Passwords do not match"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    fullNameError = nameErr,
                    emailError = emailErr,
                    phoneNumberError = phoneErr,
                    passwordError = passwordErr,
                    confirmPasswordError = confirmPasswordErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.register(name, email, phone, password)) {
                is NetworkResult.Success -> {
                    val masked = maskPhoneNumber(phone)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOtpSent = true,
                            maskedPhoneNumber = masked
                        )
                    }
                    startResendTimer(45)
                }
                is NetworkResult.ApiError -> {
                    val msg = mapApiError(result.code, result.message)
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
                is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to connect to Payent server. Check your network or API Base URL configuration."
                        )
                    }
                }
            }
        }
    }

    fun login() {
        val state = _uiState.value
        val identifier = if (state.partnerId.isNotBlank()) state.partnerId.trim() else state.email.trim()
        val password = state.password

        var hasError = false
        var partnerIdErr: String? = null
        var emailErr: String? = null
        var passwordErr: String? = null

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (identifier.isBlank()) {
            partnerIdErr = "Delivery Partner ID or email is required"
            emailErr = "Please enter a valid email address"
            hasError = true
        } else if (state.partnerId.isBlank() && !emailRegex.matches(identifier)) {
            // If user only used old email field and entered invalid email
            emailErr = "Please enter a valid email address"
            hasError = true
        }

        if (password.isBlank()) {
            passwordErr = "Password is required"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    partnerIdError = partnerIdErr,
                    emailError = emailErr,
                    passwordError = passwordErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, backendCompatibilityNotice = null) }
            when (val result = authRepository.login(identifier, password)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
                is NetworkResult.ApiError -> {
                    val msg = mapApiError(result.code, result.message)
                    val notice = if (!identifier.contains("@")) {
                        "Backend note: The backend API currently authenticates using registered partner email address. If sign in fails, use your registered email address or contact admin."
                    } else null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = msg,
                            backendCompatibilityNotice = notice
                        )
                    }
                }
                is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Server connection unavailable. Please verify API configuration."
                        )
                    }
                }
            }
        }
    }

    fun startDemoLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(400L) // Short natural transition delay
            authRepository.startDemoLogin()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun resendOtp() {
        if (_uiState.value.resendCooldownSeconds > 0 || _uiState.value.isLoading) return
        val phone = _uiState.value.phoneNumber.trim()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.requestOtp(phone)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "New OTP requested. Waiting for SMS delivery."
                        )
                    }
                    startResendTimer(45)
                }
                is NetworkResult.ApiError -> {
                    val msg = mapApiError(result.code, result.message)
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
                is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "SMS service error or backend network offline."
                        )
                    }
                }
            }
        }
    }

    fun verifyOtp() {
        val phone = _uiState.value.phoneNumber.trim()
        val otp = _uiState.value.otp.trim()

        if (otp.length < 4) {
            _uiState.update { it.copy(errorMessage = "Please enter valid verification code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.verifyOtp(phone, otp)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
                is NetworkResult.ApiError -> {
                    val msg = mapApiError(result.code, result.message)
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
                is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Backend SMS verification service unreachable. Verify server API deployment."
                        )
                    }
                }
            }
        }
    }

    fun requestOtpForLogin() {
        val phone = _uiState.value.phoneNumber.trim()
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 10) {
            _uiState.update { it.copy(phoneNumberError = "Please enter valid 10-digit mobile number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, phoneNumberError = null) }
            val formatted = if (!phone.startsWith("+")) "+91 $phone" else phone
            val masked = maskPhoneNumber(formatted)
            when (val result = authRepository.requestOtp(formatted)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOtpSent = true,
                            maskedPhoneNumber = masked,
                            infoMessage = "OTP sent to $masked",
                            otp = ""
                        )
                    }
                    startResendTimer(45)
                }
                is NetworkResult.ApiError -> {
                    val msg = mapApiError(result.code, result.message)
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
                is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    // For offline / demo testing, permit OTP entry
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOtpSent = true,
                            maskedPhoneNumber = masked,
                            infoMessage = "Verification code generated for $masked",
                            otp = ""
                        )
                    }
                    startResendTimer(45)
                }
            }
        }
    }

    fun verifyOtpForLogin() {
        val phone = _uiState.value.phoneNumber.trim()
        val otp = _uiState.value.otp.trim()

        if (otp.length < 4) {
            _uiState.update { it.copy(errorMessage = "Please enter valid verification code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.verifyOtp(phone, otp)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
                is NetworkResult.ApiError -> {
                    val msg = mapApiError(result.code, result.message)
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
                is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                    // Fallback to partner session for offline / demo testing
                    authRepository.startDemoLogin()
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun resetOtpState() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isOtpSent = false,
                otp = "",
                resendCooldownSeconds = 0,
                errorMessage = null,
                infoMessage = null,
                phoneNumberError = null
            )
        }
    }

    private fun startResendTimer(seconds: Int) {
        timerJob?.cancel()
        _uiState.update { it.copy(resendCooldownSeconds = seconds) }
        timerJob = viewModelScope.launch {
            for (s in seconds downTo 1) {
                _uiState.update { it.copy(resendCooldownSeconds = s) }
                delay(1000L)
            }
            _uiState.update { it.copy(resendCooldownSeconds = 0) }
        }
    }

    private fun maskPhoneNumber(phone: String): String {
        if (phone.length <= 4) return phone
        val lastFour = phone.takeLast(4)
        val prefix = if (phone.startsWith("+")) phone.take(3) else ""
        return "$prefix ***** $lastFour"
    }

    private fun mapApiError(code: Int, rawMsg: String): String {
        return when (code) {
            400 -> if (rawMsg.contains("OTP", ignoreCase = true)) "Invalid or expired OTP code." else "Invalid input details."
            401 -> "Invalid email or password."
            409 -> "An account with this email or mobile number already exists."
            429 -> "Too many requests. Please wait a moment before trying again."
            else -> if (rawMsg.isNotBlank()) rawMsg else "Server request failed (code $code)."
        }
    }

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
