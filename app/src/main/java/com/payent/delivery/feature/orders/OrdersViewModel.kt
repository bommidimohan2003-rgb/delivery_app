package com.payent.delivery.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.payent.delivery.core.alert.OrderAlertManager
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.model.OrderStatus
import com.payent.delivery.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val ordersRepository: OrdersRepository,
    private val orderAlertManager: OrderAlertManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    companion object {
        val sampleDemoOrders = listOf(
            Order(
                id = "demo_order_101",
                orderNumber = "PAYENT-ORD-8821",
                vendorName = "Apex Bike Rentals (Downtown)",
                vendorAddress = "742 Evergreen Ave, Sector 4",
                deliveryAddress = "104 West 4th Street, Metro Heights, Apt 12B",
                rentalPeriod = "2-Day Rental — Mountain Bike",
                payoutAmount = 45.00,
                status = OrderStatus.AVAILABLE
            ),
            Order(
                id = "demo_order_102",
                orderNumber = "PAYENT-ORD-8822",
                vendorName = "Payent Camera Studio",
                vendorAddress = "104 West 4th Street, Bay 9",
                deliveryAddress = "310 Broad Street, Creative Hub #301",
                rentalPeriod = "3-Day Rental — DSLR 4K Camera",
                payoutAmount = 85.00,
                status = OrderStatus.AVAILABLE
            ),
            Order(
                id = "demo_order_103",
                orderNumber = "PAYENT-ORD-8823",
                vendorName = "CampCraft Gear Co.",
                vendorAddress = "21 Park Avenue, Green Ridge",
                deliveryAddress = "450 University Ave, Event Hall 2",
                rentalPeriod = "4-Day Rental — Camping Tent",
                payoutAmount = 120.00,
                status = OrderStatus.AVAILABLE
            ),
            Order(
                id = "demo_order_104",
                orderNumber = "PAYENT-ORD-8824",
                vendorName = "PowerPro Rentals",
                vendorAddress = "56 Lake View St, Sector 12",
                deliveryAddress = "Tower 7, DLF Cyber City",
                rentalPeriod = "2-Day Rental — Generator",
                payoutAmount = 150.00,
                status = OrderStatus.AVAILABLE
            )
        )
    }

    init {
        fetchOrders()
    }

    fun updatePartnerKycStatus(status: KycStatus) {
        _uiState.update { it.copy(partnerKycStatus = status) }
    }

    fun updateOnlineStatus(isOnline: Boolean) {
        _uiState.update { it.copy(isOnline = isOnline) }
    }

    fun dismissOfflineWarningDialog() {
        _uiState.update { it.copy(showOfflineWarningDialog = false, orderToAccept = null) }
    }

    fun selectTab(tab: OrderTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun fetchOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val availableResult = ordersRepository.getAvailableOrders()
            val activeResult = ordersRepository.getActiveOrders()

            _uiState.update { state ->
                val availList = (availableResult as? NetworkResult.Success)?.data
                val activeList = (activeResult as? NetworkResult.Success)?.data

                val finalAvailable = if (!availList.isNullOrEmpty()) availList else if (state.availableOrders.isEmpty() && state.activeOrders.isEmpty()) sampleDemoOrders else state.availableOrders
                val finalActive = activeList ?: state.activeOrders

                state.copy(
                    isLoading = false,
                    availableOrders = finalAvailable,
                    activeOrders = finalActive
                )
            }
        }
    }

    fun openOrderDetails(order: Order) {
        orderAlertManager?.acknowledgeAlert(order.id)
        _uiState.update { it.copy(selectedOrderForDetails = order) }
    }

    fun closeOrderDetails() {
        _uiState.update { it.copy(selectedOrderForDetails = null) }
    }

    fun requestAcceptOrder(order: Order) {
        if (!_uiState.value.isOnline) {
            _uiState.update {
                it.copy(
                    showOfflineWarningDialog = true,
                    orderToAccept = order
                )
            }
            return
        }

        val kycStatus = _uiState.value.partnerKycStatus
        if (!kycStatus.isApproved) {
            _uiState.update {
                it.copy(
                    showKycRequiredDialog = true,
                    orderToAccept = order
                )
            }
            return
        }

        if (_uiState.value.activeOrders.any { it.id == order.id }) {
            _uiState.update {
                it.copy(
                    errorMessage = "Order ${order.orderNumber} is already in your active deliveries."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                showAcceptConfirmationDialog = true,
                orderToAccept = order
            )
        }
    }

    fun dismissKycRequiredDialog() {
        _uiState.update {
            it.copy(
                showKycRequiredDialog = false,
                orderToAccept = null
            )
        }
    }

    fun cancelAcceptOrder() {
        _uiState.update {
            it.copy(
                showAcceptConfirmationDialog = false,
                orderToAccept = null
            )
        }
    }

    fun confirmAcceptOrder(onSuccess: (Order) -> Unit = {}) {
        val order = _uiState.value.orderToAccept ?: return
        if (!_uiState.value.isOnline) {
            _uiState.update {
                it.copy(
                    showAcceptConfirmationDialog = false,
                    showOfflineWarningDialog = true
                )
            }
            return
        }

        val kycStatus = _uiState.value.partnerKycStatus
        if (!kycStatus.isApproved) {
            _uiState.update {
                it.copy(
                    showKycRequiredDialog = true,
                    showAcceptConfirmationDialog = false
                )
            }
            return
        }

        if (order.id.startsWith("demo_")) {
            // Local / Demo order acceptance flow
            _uiState.update { state ->
                val updatedAvailable = state.availableOrders.filter { it.id != order.id }
                val acceptedOrder = order.copy(status = OrderStatus.ACCEPTED)
                val updatedActive = state.activeOrders.filter { it.id != acceptedOrder.id } + acceptedOrder

                state.copy(
                    availableOrders = updatedAvailable,
                    activeOrders = updatedActive,
                    showAcceptConfirmationDialog = false,
                    orderToAccept = null,
                    selectedOrderForDetails = acceptedOrder,
                    selectedTab = OrderTab.ACTIVE,
                    acceptNoticeMessage = "Demo Order ${order.orderNumber} accepted! Assigned to active deliveries."
                )
            }
            orderAlertManager?.onOrderAccepted(order.id)
            onSuccess(order.copy(status = OrderStatus.ACCEPTED))
        } else {
            // Live / Real backend acceptance flow
            viewModelScope.launch {
                _uiState.update { it.copy(isAcceptingOrder = true, errorMessage = null) }
                when (val result = ordersRepository.acceptOrder(order.id)) {
                    is NetworkResult.Success -> {
                        val acceptedOrder = result.data
                        _uiState.update { state ->
                            val updatedAvailable = state.availableOrders.filter { it.id != order.id }
                            val updatedActive = state.activeOrders.filter { it.id != acceptedOrder.id } + acceptedOrder

                            state.copy(
                                isAcceptingOrder = false,
                                showAcceptConfirmationDialog = false,
                                orderToAccept = null,
                                availableOrders = updatedAvailable,
                                activeOrders = updatedActive,
                                selectedOrderForDetails = acceptedOrder,
                                selectedTab = OrderTab.ACTIVE,
                                acceptNoticeMessage = "Order ${acceptedOrder.orderNumber} accepted successfully!"
                            )
                        }
                        orderAlertManager?.onOrderAccepted(acceptedOrder.id)
                        onSuccess(acceptedOrder)
                    }
                    is NetworkResult.ApiError -> {
                        // API error - do NOT claim success or add to active list
                        _uiState.update {
                            it.copy(
                                isAcceptingOrder = false,
                                showAcceptConfirmationDialog = false,
                                errorMessage = result.message ?: "Failed to accept order (${result.code})"
                            )
                        }
                    }
                    is NetworkResult.NetworkError, is NetworkResult.UnknownError -> {
                        _uiState.update {
                            it.copy(
                                isAcceptingOrder = false,
                                showAcceptConfirmationDialog = false,
                                errorMessage = "Network error communicating with dispatch server. Order was not accepted."
                            )
                        }
                    }
                }
            }
        }
    }

    // Phase 5 Pickup Functions
    fun startDemoJourney(order: Order) {
        _uiState.update { state ->
            val updatedActive = state.activeOrders.map {
                if (it.id == order.id) it.copy(status = OrderStatus.JOURNEY_STARTED) else it
            }
            val currentSelected = state.selectedOrderForDetails
            val updatedSelected = if (currentSelected?.id == order.id) currentSelected.copy(status = OrderStatus.JOURNEY_STARTED) else currentSelected

            state.copy(
                activeOrders = updatedActive,
                selectedOrderForDetails = updatedSelected,
                isJourneyStarted = true,
                pickupNoticeMessage = "Demo journey started. Navigating to vendor location."
            )
        }
    }

    fun markArrivedAtVendor(order: Order) {
        _uiState.update { state ->
            val updatedActive = state.activeOrders.map {
                if (it.id == order.id) it.copy(status = OrderStatus.ARRIVED_AT_VENDOR) else it
            }
            val currentSelected = state.selectedOrderForDetails
            val updatedSelected = if (currentSelected?.id == order.id) currentSelected.copy(status = OrderStatus.ARRIVED_AT_VENDOR) else currentSelected

            state.copy(
                activeOrders = updatedActive,
                selectedOrderForDetails = updatedSelected,
                isArrivedAtVendor = true,
                pickupNoticeMessage = "Arrived at vendor location. Proceed with vendor handover verification."
            )
        }
    }

    fun updateLocationState(
        lat: Double?,
        lon: Double?,
        address: String?,
        hasPermission: Boolean,
        isGpsEnabled: Boolean,
        errorMessage: String? = null
    ) {
        _uiState.update {
            it.copy(
                partnerLatitude = lat,
                partnerLongitude = lon,
                partnerResolvedAddress = address,
                isLocationPermissionGranted = hasPermission,
                isGpsEnabled = isGpsEnabled,
                isResolvingLocation = false,
                locationErrorMessage = errorMessage
            )
        }
    }

    fun onVendorPinChanged(pin: String) {
        val sanitized = pin.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(vendorPinInput = sanitized, vendorPinError = null) }
    }

    fun toggleVendorPinMask() {
        _uiState.update { it.copy(isVendorPinMasked = !it.isVendorPinMasked) }
    }

    fun quickFillDemoVendorPin() {
        // Preserves leading zero: "0429" (exactly 4 numeric digits)
        _uiState.update { it.copy(vendorPinInput = "0429", vendorPinError = null) }
    }

    /**
     * Step 1: Delivery partner requests the vendor to enter their 4-digit secret PIN in the vendor app.
     * Crucial: This request does NOT immediately mark the step completed. It waits for the vendor's app.
     */
    fun sendPinRequestToVendor(order: Order) {
        if (_uiState.value.isVendorPinSimulated || order.status == OrderStatus.VENDOR_PIN_SIMULATED) return
        _uiState.update {
            it.copy(
                isVendorPinRequestSent = true,
                isWaitingForVendorPin = true,
                vendorPinError = null,
                pickupNoticeMessage = "PIN request sent to vendor's Payent app. Awaiting vendor's 4-digit PIN."
            )
        }
    }

    /**
     * Handles authoritative external verification from the vendor app.
     * Safely ignores wrong-order events.
     */
    fun onVendorPinVerifiedExternally(orderId: String, pin: String = "0429"): Boolean {
        val currentSelected = _uiState.value.selectedOrderForDetails
        val matchingActive = _uiState.value.activeOrders.find { it.id == orderId }

        // Discard events for wrong or mismatched orders
        if (matchingActive == null && currentSelected?.id != orderId) {
            return false
        }

        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            return false
        }

        _uiState.update { state ->
            val updatedActive = state.activeOrders.map {
                if (it.id == orderId) it.copy(status = OrderStatus.VENDOR_PIN_SIMULATED) else it
            }
            val updatedSelected = if (state.selectedOrderForDetails?.id == orderId) {
                state.selectedOrderForDetails.copy(status = OrderStatus.VENDOR_PIN_SIMULATED)
            } else {
                state.selectedOrderForDetails
            }

            state.copy(
                activeOrders = updatedActive,
                selectedOrderForDetails = updatedSelected,
                isVendorPinSimulated = true,
                isWaitingForVendorPin = false,
                isVendorPinRequestSent = true,
                vendorPinError = null,
                pickupNoticeMessage = "Vendor 4-digit secret PIN verified! Step 2 'Send OTP to Vendor' is unlocked."
            )
        }
        return true
    }

    fun simulateVendorPinEnteredInVendorApp(orderId: String = "") {
        val targetId = if (orderId.isNotBlank()) orderId else (_uiState.value.selectedOrderForDetails?.id ?: sampleDemoOrders.first().id)
        onVendorPinVerifiedExternally(targetId, "0429")
    }

    fun verifyVendorPin(order: Order, pinOverride: String? = null, onSuccess: () -> Unit = {}): Boolean {
        if (_uiState.value.isVerifyingVendorPin) return false

        val pin = pinOverride ?: _uiState.value.vendorPinInput
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            _uiState.update {
                it.copy(vendorPinError = "Vendor secret PIN must be exactly 4 digits.")
            }
            return false
        }

        // State check: order must be in an accepted / arrived pickup state
        if (order.status != OrderStatus.ACCEPTED &&
            order.status != OrderStatus.JOURNEY_STARTED &&
            order.status != OrderStatus.ARRIVED_AT_VENDOR &&
            order.status != OrderStatus.VENDOR_PIN_SIMULATED) {
            _uiState.update {
                it.copy(vendorPinError = "Invalid order state for vendor PIN verification.")
            }
            return false
        }

        val success = onVendorPinVerifiedExternally(order.id, pin)
        if (success) {
            onSuccess()
        }
        return success
    }

    fun simulateVendorPinVerified() {
        val order = _uiState.value.selectedOrderForDetails ?: sampleDemoOrders.first()
        simulateVendorPinEnteredInVendorApp(order.id)
    }

    fun sendOtpToVendor() {
        if (!_uiState.value.isVendorPinSimulated && _uiState.value.selectedOrderForDetails?.status != OrderStatus.VENDOR_PIN_SIMULATED) {
            _uiState.update {
                it.copy(vendorOtpError = "Vendor 4-digit secret PIN must be verified before sending OTP.")
            }
            return
        }
        _uiState.update {
            it.copy(
                isOtpRequested = true,
                isVendorOtpSent = true,
                vendorOtpError = null,
                pickupNoticeMessage = "OTP sent to vendor's registered mobile number (+91 98840 XXXXX) [Simulated]."
            )
        }
    }

    fun requestVendorOtp() {
        sendOtpToVendor()
    }

    fun onVendorOtpChanged(otp: String) {
        val sanitized = otp.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(vendorOtpInput = sanitized, vendorOtpError = null) }
    }

    fun simulateVendorOtpFilled() {
        _uiState.update {
            it.copy(
                vendorOtpInput = "982143",
                vendorOtpError = null,
                pickupNoticeMessage = "Simulated vendor OTP '982143' entered."
            )
        }
    }

    fun verifyVendorOtp(order: Order, onSuccess: () -> Unit = {}): Boolean {
        // Prevent duplicate execution if already completed
        if (_uiState.value.isPickupCompleted) {
            return true
        }

        if (!_uiState.value.isVendorPinSimulated && order.status != OrderStatus.VENDOR_PIN_SIMULATED) {
            _uiState.update {
                it.copy(vendorOtpError = "Vendor 4-digit PIN must be verified before completing OTP handover.")
            }
            return false
        }

        val otp = _uiState.value.vendorOtpInput
        if (otp.length != 6 || !otp.all { it.isDigit() }) {
            _uiState.update {
                it.copy(vendorOtpError = "Please enter the 6-digit OTP shared by the vendor.")
            }
            return false
        }

        // Demo check: valid OTP is "982143"
        if (otp != "982143") {
            _uiState.update {
                it.copy(vendorOtpError = "Invalid OTP. Please check the 6-digit OTP shared by the vendor.")
            }
            return false
        }

        _uiState.update { state ->
            val updatedActive = state.activeOrders.map {
                if (it.id == order.id) it.copy(status = OrderStatus.DEMO_PICKUP_COMPLETED) else it
            }
            val currentSelected = state.selectedOrderForDetails
            val updatedSelected = if (currentSelected?.id == order.id) currentSelected.copy(status = OrderStatus.DEMO_PICKUP_COMPLETED) else currentSelected

            state.copy(
                activeOrders = updatedActive,
                selectedOrderForDetails = updatedSelected,
                isPickupCompleted = true,
                vendorOtpError = null,
                pickupNoticeMessage = "DEMO PICKUP FLOW COMPLETED — Handover verified locally."
            )
        }
        onSuccess()
        return true
    }

    // Phase 6 Delivery Functions
    fun startDeliveryJourney(order: Order) {
        _uiState.update { state ->
            val updatedActive = state.activeOrders.map {
                if (it.id == order.id) it.copy(status = OrderStatus.DELIVERY_JOURNEY_STARTED) else it
            }
            val currentSelected = state.selectedOrderForDetails
            val updatedSelected = if (currentSelected?.id == order.id) currentSelected.copy(status = OrderStatus.DELIVERY_JOURNEY_STARTED) else currentSelected

            state.copy(
                activeOrders = updatedActive,
                selectedOrderForDetails = updatedSelected,
                isDeliveryJourneyStarted = true,
                deliveryNoticeMessage = "Demo delivery journey started. Navigating to renter destination."
            )
        }
    }

    fun markArrivedAtRenter(order: Order) {
        _uiState.update { state ->
            val updatedActive = state.activeOrders.map {
                if (it.id == order.id) it.copy(status = OrderStatus.ARRIVED_AT_RENTER) else it
            }
            val currentSelected = state.selectedOrderForDetails
            val updatedSelected = if (currentSelected?.id == order.id) currentSelected.copy(status = OrderStatus.ARRIVED_AT_RENTER) else currentSelected

            state.copy(
                activeOrders = updatedActive,
                selectedOrderForDetails = updatedSelected,
                isArrivedAtRenter = true,
                deliveryNoticeMessage = "Arrived at renter location. Proceed to renter verification."
            )
        }
    }

    fun sendOtpToRenter() {
        _uiState.update {
            it.copy(
                isRenterOtpRequested = true,
                isRenterOtpSent = true,
                deliveryNoticeMessage = "OTP sent to renter's registered mobile number (+91 97721 XXXXX) [Simulated]."
            )
        }
    }

    fun requestRenterOtp() {
        sendOtpToRenter()
    }

    fun onRenterOtpChanged(otp: String) {
        val sanitized = otp.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(renterOtpInput = sanitized, renterOtpError = null) }
    }

    fun simulateRenterOtpFilled() {
        _uiState.update {
            it.copy(
                renterOtpInput = "654321",
                renterOtpError = null,
                deliveryNoticeMessage = "Simulated renter OTP '654321' received."
            )
        }
    }

    fun verifyRenterOtp(order: Order, onSuccess: () -> Unit = {}): Boolean {
        val otp = _uiState.value.renterOtpInput
        if (otp.length < 4 || !otp.all { it.isDigit() }) {
            _uiState.update {
                it.copy(renterOtpError = "Please enter a valid customer verification OTP.")
            }
            return false
        }

        _uiState.update { state ->
            val updatedActive = state.activeOrders.map {
                if (it.id == order.id) it.copy(status = OrderStatus.RENTER_OTP_DEMO_COMPLETED) else it
            }
            val currentSelected = state.selectedOrderForDetails
            val updatedSelected = if (currentSelected?.id == order.id) currentSelected.copy(status = OrderStatus.RENTER_OTP_DEMO_COMPLETED) else currentSelected

            state.copy(
                activeOrders = updatedActive,
                selectedOrderForDetails = updatedSelected,
                isRenterOtpVerified = true,
                renterOtpError = null,
                deliveryNoticeMessage = "Customer OTP verified in local simulation. Proceed to product inspection."
            )
        }
        onSuccess()
        return true
    }

    fun toggleCheckItem(index: Int) {
        _uiState.update { state ->
            when (index) {
                1 -> state.copy(isCheckItemProductCorrect = !state.isCheckItemProductCorrect)
                2 -> state.copy(isCheckItemConditionInspected = !state.isCheckItemConditionInspected)
                3 -> state.copy(isCheckItemAccessoriesPresent = !state.isCheckItemAccessoriesPresent)
                4 -> state.copy(isCheckItemMatchesOrder = !state.isCheckItemMatchesOrder)
                5 -> state.copy(isCheckItemRenterInspected = !state.isCheckItemRenterInspected)
                else -> state
            }
        }
    }

    fun markInspectionComplete(order: Order, onSuccess: () -> Unit = {}): Boolean {
        val state = _uiState.value
        if (state.hasOpenProblemReport) {
            _uiState.update {
                it.copy(errorMessage = "Inspection cannot be completed while a delivery problem is reported.")
            }
            return false
        }

        if (!state.isRenterOtpVerified && order.status != OrderStatus.RENTER_OTP_DEMO_COMPLETED) {
            _uiState.update {
                it.copy(errorMessage = "Customer OTP verification must be completed before inspection.")
            }
            return false
        }

        _uiState.update { s ->
            val updatedActive = s.activeOrders.map {
                if (it.id == order.id) it.copy(status = OrderStatus.INSPECTION_IN_PROGRESS) else it
            }
            val currentSelected = s.selectedOrderForDetails
            val updatedSelected = if (currentSelected?.id == order.id) currentSelected.copy(status = OrderStatus.INSPECTION_IN_PROGRESS) else currentSelected

            s.copy(
                activeOrders = updatedActive,
                selectedOrderForDetails = updatedSelected,
                isInspectionMarkedComplete = true,
                deliveryNoticeMessage = "Product inspection marked complete locally. Proceed to final customer PIN verification."
            )
        }
        onSuccess()
        return true
    }

    fun selectProblemCategory(category: String) {
        _uiState.update { it.copy(selectedProblemCategory = category, errorMessage = null) }
    }

    fun onProblemDescriptionChanged(desc: String) {
        _uiState.update { it.copy(problemDescription = desc) }
    }

    fun attachProblemPhoto(uri: String?, fileName: String?) {
        _uiState.update {
            it.copy(
                problemPhotoUri = uri ?: "sample_issue_photo.jpg",
                problemPhotoFileName = fileName ?: "issue_photo_sample.jpg"
            )
        }
    }

    fun submitProblemReport(order: Order): Boolean {
        val category = _uiState.value.selectedProblemCategory
        if (category.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select a problem category before submitting.") }
            return false
        }

        _uiState.update { state ->
            val updatedActive = state.activeOrders.map {
                if (it.id == order.id) it.copy(status = OrderStatus.ISSUE_REPORTED_DEMO) else it
            }
            val currentSelected = state.selectedOrderForDetails
            val updatedSelected = if (currentSelected?.id == order.id) currentSelected.copy(status = OrderStatus.ISSUE_REPORTED_DEMO) else currentSelected

            state.copy(
                activeOrders = updatedActive,
                selectedOrderForDetails = updatedSelected,
                hasOpenProblemReport = true,
                problemReportSummary = "ISSUE REPORTED — DEMO DELIVERY BLOCKED ($category)",
                deliveryNoticeMessage = "Problem report submitted locally. Delivery completion is blocked for this demo order."
            )
        }
        return true
    }

    fun onCustomerPinChanged(pin: String) {
        val sanitized = pin.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(customerPinInput = sanitized, customerPinError = null) }
    }

    fun toggleCustomerPinMask() {
        _uiState.update { it.copy(isCustomerPinMasked = !it.isCustomerPinMasked) }
    }

    fun quickFillDemoCustomerPin() {
        // Preserves leading zero: "0821" (exactly 4 numeric digits)
        _uiState.update { it.copy(customerPinInput = "0821", customerPinError = null) }
    }

    fun verifyCustomerPin(order: Order, pinOverride: String? = null, onSuccess: () -> Unit = {}): Boolean {
        val state = _uiState.value
        if (state.isVerifyingCustomerPin) return false

        // 1. Block if problem reported
        if (state.hasOpenProblemReport || order.status == OrderStatus.ISSUE_REPORTED_DEMO) {
            _uiState.update {
                it.copy(
                    customerPinError = "Delivery completion is blocked due to an unresolved issue report.",
                    errorMessage = "Delivery completion is blocked due to an unresolved issue report."
                )
            }
            return false
        }

        // 2. Block if customer OTP is not verified
        if (!state.isRenterOtpVerified && order.status != OrderStatus.RENTER_OTP_DEMO_COMPLETED && order.status != OrderStatus.INSPECTION_IN_PROGRESS) {
            _uiState.update {
                it.copy(customerPinError = "Customer OTP verification must be completed first.")
            }
            return false
        }

        // 3. Block if inspection is not complete
        if (!state.isInspectionMarkedComplete && order.status != OrderStatus.INSPECTION_IN_PROGRESS) {
            _uiState.update {
                it.copy(customerPinError = "Product inspection checklist must be marked complete first.")
            }
            return false
        }

        // 4. Strict 4-digit numeric validation preserving leading zeros
        val pin = pinOverride ?: state.customerPinInput
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            _uiState.update {
                it.copy(customerPinError = "Customer secret PIN must be exactly 4 digits.")
            }
            return false
        }

        _uiState.update { s ->
            val updatedActive = s.activeOrders.filter { it.id != order.id }
            val completedOrder = order.copy(status = OrderStatus.DELIVERY_DEMO_COMPLETED)
            val updatedCompleted = s.completedOrders.filter { it.id != completedOrder.id } + completedOrder

            s.copy(
                activeOrders = updatedActive,
                completedOrders = updatedCompleted,
                selectedOrderForDetails = completedOrder,
                isRenterPinSimulated = true,
                isDeliveryCompleted = true,
                isVerifyingCustomerPin = false,
                customerPinError = null,
                deliveryNoticeMessage = "Order ${order.orderNumber} fulfilled successfully!"
            )
        }
        onSuccess()
        return true
    }

    fun simulateRenterPinVerified(order: Order): Boolean {
        quickFillDemoCustomerPin()
        return verifyCustomerPin(order)
    }

    fun clearNoticeMessage() {
        _uiState.update {
            it.copy(
                acceptNoticeMessage = null,
                pickupNoticeMessage = null,
                deliveryNoticeMessage = null,
                errorMessage = null
            )
        }
    }

    class Factory(
        private val ordersRepository: OrdersRepository,
        private val orderAlertManager: OrderAlertManager? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrdersViewModel(ordersRepository, orderAlertManager) as T
        }
    }
}
