package com.payent.delivery.domain.model

data class DeliveryPartner(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val address: String? = "123, 4th Street, Gachibowli",
    val pincode: String? = "500032",
    val isOnline: Boolean = false,
    val kycStatus: KycStatus = KycStatus.NOT_STARTED,
    val vehicleType: String? = "Electric Scooter (Demo)",
    val kycRejectionReason: String? = null,
    val kycSubmissionDate: String? = null
)

enum class KycStatus {
    NOT_STARTED,
    DOCUMENTS_SUBMITTED,
    PENDING,
    APPROVED,
    REJECTED,
    // Preserved for backward compatibility with existing tests and logic
    NOT_SUBMITTED,
    SELECTED_LOCALLY,
    SUBMITTED_DEMO,
    NOT_VERIFIED,
    VERIFIED;

    val isApproved: Boolean
        get() = this == APPROVED || this == VERIFIED
}

data class KycDocument(
    val type: KycDocumentType,
    val title: String,
    val description: String,
    val selectedUri: String? = null,
    val selectedFileName: String? = null,
    val status: KycDocumentStatus = KycDocumentStatus.NOT_STARTED
)

enum class KycDocumentType {
    DRIVING_LICENCE,
    AADHAAR_CARD,
    BANK_PASSBOOK
}

enum class KycDocumentStatus {
    NOT_STARTED,
    SELECTED_LOCALLY,
    SUBMITTED_DEMO,
    NOT_VERIFIED
}
