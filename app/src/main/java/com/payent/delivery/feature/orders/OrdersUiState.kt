package com.payent.delivery.feature.orders

import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.model.Order

data class OrdersUiState(
    val selectedTab: OrderTab = OrderTab.AVAILABLE,
    val availableOrders: List<Order> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val completedOrders: List<Order> = emptyList(),
    val selectedOrderForDetails: Order? = null,
    val showAcceptConfirmationDialog: Boolean = false,
    val orderToAccept: Order? = null,
    val acceptNoticeMessage: String? = null,

    // Online / Acceptance Eligibility State
    val isOnline: Boolean = true,
    val showOfflineWarningDialog: Boolean = false,
    val isAcceptingOrder: Boolean = false,

    // KYC Verification Order Acceptance Blocking State
    val partnerKycStatus: KycStatus = KycStatus.VERIFIED,
    val showKycRequiredDialog: Boolean = false,

    // Live GPS and Navigation State
    val partnerLatitude: Double? = null,
    val partnerLongitude: Double? = null,
    val partnerResolvedAddress: String? = null,
    val isLocationPermissionGranted: Boolean = false,
    val isGpsEnabled: Boolean = true,
    val isResolvingLocation: Boolean = false,
    val locationErrorMessage: String? = null,

    // Phase 3 & 4 Pickup & Vendor Handover State (4-Digit Vendor PIN & OTP)
    val isJourneyStarted: Boolean = false,
    val isArrivedAtVendor: Boolean = false,
    val isVendorPinRequestSent: Boolean = false,
    val isWaitingForVendorPin: Boolean = false,
    val vendorPinInput: String = "",
    val isVendorPinMasked: Boolean = true,
    val isVerifyingVendorPin: Boolean = false,
    val vendorPinError: String? = null,
    val isVendorPinSimulated: Boolean = false,
    val isOtpRequested: Boolean = false,
    val isVendorOtpSent: Boolean = false,
    val vendorOtpInput: String = "",
    val vendorOtpError: String? = null,
    val isVerifyingVendorOtp: Boolean = false,
    val isPickupCompleted: Boolean = false,
    val pickupNoticeMessage: String? = null,

    // Phase 5 & 6 Delivery, Inspection & Problem Reporting State
    val isDeliveryJourneyStarted: Boolean = false,
    val isArrivedAtRenter: Boolean = false,
    val isRenterOtpRequested: Boolean = false,
    val isRenterOtpSent: Boolean = false,
    val renterOtpInput: String = "",
    val renterOtpError: String? = null,
    val isVerifyingRenterOtp: Boolean = false,
    val isRenterOtpVerified: Boolean = false,

    // Inspection Checklist
    val isCheckItemProductCorrect: Boolean = false,
    val isCheckItemConditionInspected: Boolean = false,
    val isCheckItemAccessoriesPresent: Boolean = false,
    val isCheckItemMatchesOrder: Boolean = false,
    val isCheckItemRenterInspected: Boolean = false,
    val isInspectionMarkedComplete: Boolean = false,

    // Problem Report State
    val selectedProblemCategory: String? = null,
    val problemDescription: String = "",
    val problemPhotoUri: String? = null,
    val problemPhotoFileName: String? = null,
    val hasOpenProblemReport: Boolean = false,
    val problemReportSummary: String? = null,

    // Delivery Completion State (4-Digit Customer PIN)
    val customerPinInput: String = "",
    val isCustomerPinMasked: Boolean = true,
    val isVerifyingCustomerPin: Boolean = false,
    val customerPinError: String? = null,
    val isRenterPinSimulated: Boolean = false,
    val isDeliveryCompleted: Boolean = false,
    val deliveryNoticeMessage: String? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class OrderTab {
    AVAILABLE,
    ACTIVE,
    HISTORY
}
