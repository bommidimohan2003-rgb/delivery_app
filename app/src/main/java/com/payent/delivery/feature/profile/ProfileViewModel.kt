package com.payent.delivery.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.core.security.SessionManager
import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.model.KycDocumentStatus
import com.payent.delivery.domain.model.KycDocumentType
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager? = null
) : ViewModel() {

    private val defaultPartner = DeliveryPartner(
        id = "demo_partner",
        name = "Mohan B",
        phoneNumber = "+91 98765 43210",
        email = "mohanb@gmail.com",
        address = "123, 4th Street, Gachibowli",
        pincode = "500032",
        isOnline = true,
        kycStatus = KycStatus.NOT_STARTED,
        vehicleType = "Electric Scooter (Demo)"
    )

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            isLoading = false,
            partner = defaultPartner,
            editName = defaultPartner.name,
            editEmail = defaultPartner.email ?: "mohanb@gmail.com",
            editPhone = defaultPartner.phoneNumber,
            editAddress = defaultPartner.address ?: "123, 4th Street, Gachibowli",
            editPincode = defaultPartner.pincode ?: "500032",
            editVehicleType = defaultPartner.vehicleType ?: "Electric Scooter (Demo)"
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            when (val result = authRepository.getPartnerProfile()) {
                is NetworkResult.Success -> {
                    val partner = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            partner = partner,
                            editName = partner.name.ifBlank { "Mohan B" },
                            editEmail = partner.email ?: "mohanb@gmail.com",
                            editPhone = partner.phoneNumber.ifBlank { "+91 98765 43210" },
                            editAddress = partner.address ?: "123, 4th Street, Gachibowli",
                            editPincode = partner.pincode ?: "500032",
                            editVehicleType = partner.vehicleType ?: "Electric Scooter (Demo)"
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            partner = it.partner ?: defaultPartner,
                            editName = it.partner?.name?.ifBlank { "Mohan B" } ?: defaultPartner.name,
                            editEmail = it.partner?.email ?: "mohanb@gmail.com",
                            editPhone = it.partner?.phoneNumber?.ifBlank { "+91 98765 43210" } ?: defaultPartner.phoneNumber,
                            editAddress = it.partner?.address ?: "123, 4th Street, Gachibowli",
                            editPincode = it.partner?.pincode ?: "500032",
                            editVehicleType = it.partner?.vehicleType ?: defaultPartner.vehicleType ?: ""
                        )
                    }
                }
            }
        }
    }

    fun initEditProfileForm() {
        val partner = _uiState.value.partner
        if (partner != null) {
            _uiState.update {
                it.copy(
                    editName = partner.name,
                    editEmail = partner.email ?: "mohanb@gmail.com",
                    editPhone = partner.phoneNumber,
                    editAddress = partner.address ?: "123, 4th Street, Gachibowli",
                    editPincode = partner.pincode ?: "500032",
                    editVehicleType = partner.vehicleType ?: "Electric Scooter (Demo)",
                    editNameError = null,
                    editEmailError = null,
                    editPhoneError = null,
                    editAddressError = null,
                    editPincodeError = null,
                    isProfileSaveSuccessMessage = null
                )
            }
        }
    }

    fun onEditNameChanged(name: String) {
        _uiState.update { it.copy(editName = name, editNameError = null, isProfileSaveSuccessMessage = null) }
    }

    fun onEditEmailChanged(email: String) {
        _uiState.update { it.copy(editEmail = email, editEmailError = null, isProfileSaveSuccessMessage = null) }
    }

    fun onEditPhoneChanged(phone: String) {
        _uiState.update { it.copy(editPhone = phone, editPhoneError = null, isProfileSaveSuccessMessage = null) }
    }

    fun onEditAddressChanged(address: String) {
        _uiState.update { it.copy(editAddress = address, editAddressError = null, isProfileSaveSuccessMessage = null) }
    }

    fun onEditPincodeChanged(pincode: String) {
        _uiState.update { it.copy(editPincode = pincode, editPincodeError = null, isProfileSaveSuccessMessage = null) }
    }

    fun onEditVehicleTypeChanged(vehicle: String) {
        _uiState.update { it.copy(editVehicleType = vehicle, isProfileSaveSuccessMessage = null) }
    }

    fun toggleOnlineStatus(isOnline: Boolean) {
        _uiState.update { state ->
            val updated = state.partner?.copy(isOnline = isOnline)
            state.copy(partner = updated)
        }
    }

    fun saveProfileChanges(): Boolean {
        val state = _uiState.value
        val name = state.editName.trim()
        val email = state.editEmail.trim()
        val phone = state.editPhone.trim()
        val address = state.editAddress.trim()
        val pincode = state.editPincode.trim()

        var hasError = false
        var nameErr: String? = null
        var emailErr: String? = null
        var phoneErr: String? = null
        var addressErr: String? = null
        var pincodeErr: String? = null

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
            phoneErr = "Enter valid phone number"
            hasError = true
        }

        if (address.isBlank()) {
            addressErr = "Address is required"
            hasError = true
        }

        if (pincode.length != 6 || !pincode.all { it.isDigit() }) {
            pincodeErr = "Enter a valid 6-digit pincode"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    editNameError = nameErr,
                    editEmailError = emailErr,
                    editPhoneError = phoneErr,
                    editAddressError = addressErr,
                    editPincodeError = pincodeErr
                )
            }
            return false
        }

        val currentPartner = state.partner ?: DeliveryPartner(
            id = "demo_partner",
            name = name,
            phoneNumber = phone,
            address = address,
            pincode = pincode
        )

        val updatedPartner = currentPartner.copy(
            name = name,
            email = email,
            phoneNumber = phone,
            address = address,
            pincode = pincode,
            vehicleType = state.editVehicleType.trim()
        )

        _uiState.update {
            it.copy(
                partner = updatedPartner,
                isProfileSaveSuccessMessage = "Profile updated successfully."
            )
        }
        return true
    }

    fun selectKycDocument(type: KycDocumentType, uri: String?, fileName: String?) {
        _uiState.update { state ->
            val updatedList = state.kycDocuments.map { doc ->
                if (doc.type == type) {
                    doc.copy(
                        selectedUri = uri ?: "sample_local_file.jpg",
                        selectedFileName = fileName ?: "${doc.title.lowercase().replace(" ", "_")}_sample.jpg",
                        status = KycDocumentStatus.SELECTED_LOCALLY
                    )
                } else doc
            }
            state.copy(
                kycDocuments = updatedList,
                kycValidationMessage = null
            )
        }
    }

    fun removeKycDocument(type: KycDocumentType) {
        _uiState.update { state ->
            val updatedList = state.kycDocuments.map { doc ->
                if (doc.type == type) {
                    doc.copy(
                        selectedUri = null,
                        selectedFileName = null,
                        status = KycDocumentStatus.NOT_STARTED
                    )
                } else doc
            }
            state.copy(
                kycDocuments = updatedList,
                kycNoticeMessage = null,
                kycOverallStatus = if (updatedList.all { it.status == KycDocumentStatus.NOT_STARTED }) KycDocumentStatus.NOT_STARTED else state.kycOverallStatus
            )
        }
    }

    fun submitKycDocuments(isDemo: Boolean = false) {
        val state = _uiState.value
        val unselectedDocs = state.kycDocuments.filter { it.selectedUri.isNullOrBlank() }

        if (unselectedDocs.isNotEmpty()) {
            val names = unselectedDocs.joinToString(", ") { it.title }
            _uiState.update {
                it.copy(
                    kycValidationMessage = "Please select files for: $names before submitting."
                )
            }
            return
        }

        val submittedList = state.kycDocuments.map { doc ->
            doc.copy(status = if (isDemo) KycDocumentStatus.SUBMITTED_DEMO else KycDocumentStatus.SUBMITTED_DEMO)
        }

        val now = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US).format(java.util.Date())
        val docNames = state.kycDocuments.map { doc ->
            doc.title + (doc.selectedFileName?.let { " ($it)" } ?: "")
        }

        val targetStatus = if (isDemo) KycStatus.SUBMITTED_DEMO else KycStatus.DOCUMENTS_SUBMITTED
        val updatedPartner = state.partner?.copy(
            kycStatus = targetStatus,
            kycSubmissionDate = now,
            kycRejectionReason = null
        )

        _uiState.update {
            it.copy(
                partner = updatedPartner,
                kycDocuments = submittedList,
                kycOverallStatus = KycDocumentStatus.SUBMITTED_DEMO,
                kycSubmissionDate = now,
                submittedDocuments = docNames,
                kycValidationMessage = null,
                kycNoticeMessage = "Your documents have been submitted. Our admin team will review your application."
            )
        }
    }

    fun submitKycForDemo() {
        submitKycDocuments(isDemo = true)
    }

    fun resubmitKycDocuments() {
        val resetDocs = _uiState.value.kycDocuments.map { doc ->
            doc.copy(
                selectedUri = null,
                selectedFileName = null,
                status = KycDocumentStatus.NOT_STARTED
            )
        }
        val updatedPartner = _uiState.value.partner?.copy(
            kycStatus = KycStatus.NOT_STARTED,
            kycRejectionReason = null
        )
        _uiState.update {
            it.copy(
                partner = updatedPartner,
                kycDocuments = resetDocs,
                kycOverallStatus = KycDocumentStatus.NOT_STARTED,
                kycValidationMessage = null,
                kycNoticeMessage = null,
                kycRejectionReason = null
            )
        }
    }

    fun updateKycStatusForTesting(status: KycStatus, rejectionReason: String? = null) {
        val updatedPartner = _uiState.value.partner?.copy(
            kycStatus = status,
            kycRejectionReason = rejectionReason
        )
        _uiState.update {
            it.copy(
                partner = updatedPartner,
                kycRejectionReason = rejectionReason
            )
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoggingOut = false) }
            onLoggedOut()
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val sessionManager: SessionManager? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(authRepository, sessionManager) as T
        }
    }
}
