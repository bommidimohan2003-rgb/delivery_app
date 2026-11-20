package com.payent.delivery.domain.model

data class Order(
    val id: String,
    val orderNumber: String,
    val vendorName: String,
    val vendorAddress: String,
    val deliveryAddress: String,
    val rentalPeriod: String,
    val payoutAmount: Double,
    val status: OrderStatus,
    val vendorLatitude: Double = 12.9716, // Bengaluru Commercial Hub / SF Hub default demo coordinate
    val vendorLongitude: Double = 77.5946,
    val deliveryLatitude: Double = 12.9352, // Renter Residence demo coordinate
    val deliveryLongitude: Double = 77.6245
)

enum class OrderStatus {
    AVAILABLE,
    ACCEPTED,
    JOURNEY_STARTED,
    ARRIVED_AT_VENDOR,
    VENDOR_PIN_SIMULATED,
    DEMO_PICKUP_COMPLETED,
    DELIVERY_JOURNEY_STARTED,
    ARRIVED_AT_RENTER,
    RENTER_OTP_DEMO_COMPLETED,
    INSPECTION_IN_PROGRESS,
    ISSUE_REPORTED_DEMO,
    DELIVERY_DEMO_COMPLETED,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}
