package com.payent.delivery.feature.profile

import com.payent.delivery.domain.model.DeliveryPartner
import com.payent.delivery.domain.model.KycDocument
import com.payent.delivery.domain.model.KycDocumentStatus
import com.payent.delivery.domain.model.KycDocumentType

data class ProfileUiState(
    val partner: DeliveryPartner? = null,
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val appVersion: String = "1.0.0 (Phase 3)",

    // Edit Profile Form State
    val editName: String = "",
    val editEmail: String = "",
    val editPhone: String = "",
    val editAddress: String = "",
    val editPincode: String = "",
    val editVehicleType: String = "",
    val editNameError: String? = null,
    val editEmailError: String? = null,
    val editPhoneError: String? = null,
    val editAddressError: String? = null,
    val editPincodeError: String? = null,
    val isProfileSaveSuccessMessage: String? = null,

    // KYC Form State
    val kycDocuments: List<KycDocument> = listOf(
        KycDocument(
            type = KycDocumentType.DRIVING_LICENCE,
            title = "Driving Licence",
            description = "Valid commercial or standard driver's licence"
        ),
        KycDocument(
            type = KycDocumentType.AADHAAR_CARD,
            title = "Aadhaar Card",
            description = "Government issued identity proof (Sample document recommended)"
        ),
        KycDocument(
            type = KycDocumentType.BANK_PASSBOOK,
            title = "Bank Account Details",
            description = "First page of bank passbook or cancelled cheque showing account details"
        )
    ),
    val kycOverallStatus: KycDocumentStatus = KycDocumentStatus.NOT_STARTED,
    val kycValidationMessage: String? = null,
    val kycNoticeMessage: String? = null,
    val kycSubmissionDate: String? = null,
    val kycRejectionReason: String? = null,
    val submittedDocuments: List<String> = emptyList()
)
