package com.payent.delivery.feature.orders

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PedalBike
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import com.payent.delivery.R
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.model.OrderStatus
import java.util.Locale

/**
 * Extension property defining whether an order status is active (in-progress deliveries).
 * Excludes unaccepted AVAILABLE, completed DELIVERED / DELIVERY_DEMO_COMPLETED, and CANCELLED.
 */
val OrderStatus.isActive: Boolean
    get() = this !in setOf(
        OrderStatus.AVAILABLE,
        OrderStatus.DELIVERED,
        OrderStatus.DELIVERY_DEMO_COMPLETED,
        OrderStatus.CANCELLED
    )

/**
 * Extension property defining whether an order status is available for acceptance.
 */
val OrderStatus.isAvailable: Boolean
    get() = this == OrderStatus.AVAILABLE

/**
 * Formatted display text for order statuses.
 */
fun OrderStatus.toDisplayLabel(): String = when (this) {
    OrderStatus.AVAILABLE -> "AVAILABLE"
    OrderStatus.ACCEPTED -> "ACCEPTED"
    OrderStatus.JOURNEY_STARTED -> "ON THE WAY TO PICKUP"
    OrderStatus.ARRIVED_AT_VENDOR -> "ARRIVED AT VENDOR"
    OrderStatus.VENDOR_PIN_SIMULATED -> "VENDOR PIN VERIFIED"
    OrderStatus.DEMO_PICKUP_COMPLETED -> "PICKUP COMPLETED"
    OrderStatus.DELIVERY_JOURNEY_STARTED -> "ON THE WAY TO RENTER"
    OrderStatus.ARRIVED_AT_RENTER -> "ARRIVED AT RENTER"
    OrderStatus.RENTER_OTP_DEMO_COMPLETED -> "RENTER OTP VERIFIED"
    OrderStatus.INSPECTION_IN_PROGRESS -> "INSPECTION COMPLETED"
    OrderStatus.ISSUE_REPORTED_DEMO -> "ISSUE REPORTED"
    OrderStatus.DELIVERY_DEMO_COMPLETED -> "DELIVERED"
    OrderStatus.PICKED_UP -> "PICKED UP"
    OrderStatus.DELIVERED -> "DELIVERED"
    OrderStatus.CANCELLED -> "CANCELLED"
}

/**
 * Helper data class to dynamically format title, category, icon, distances, schedule and product drawable from an Order
 */
data class OrderDisplayInfo(
    val title: String,
    val category: String,
    val icon: ImageVector,
    val pickupDistance: String,
    val deliveryDistance: String,
    val schedule: String,
    val drawableRes: Int? = null
)

fun getOrderDisplayInfo(order: Order): OrderDisplayInfo {
    val searchContent = "${order.vendorName} ${order.rentalPeriod} ${order.orderNumber}"
    return when {
        searchContent.contains("Bike", ignoreCase = true) || searchContent.contains("Cycle", ignoreCase = true) || searchContent.contains("8821", ignoreCase = true) -> {
            OrderDisplayInfo(
                title = "Mountain Bike",
                category = "Sports & Fitness",
                icon = Icons.Outlined.PedalBike,
                pickupDistance = "2.4 km",
                deliveryDistance = "6.8 km",
                schedule = "Today, 2:00 PM - 3:00 PM",
                drawableRes = R.drawable.product_mountain_bike
            )
        }
        searchContent.contains("Camera", ignoreCase = true) || searchContent.contains("Lens", ignoreCase = true) || searchContent.contains("8822", ignoreCase = true) -> {
            OrderDisplayInfo(
                title = "DSLR Camera",
                category = "Electronics",
                icon = Icons.Outlined.PhotoCamera,
                pickupDistance = "3.1 km",
                deliveryDistance = "7.5 km",
                schedule = "Today, 3:30 PM - 5:30 PM",
                drawableRes = R.drawable.product_dslr_camera
            )
        }
        searchContent.contains("Tent", ignoreCase = true) || searchContent.contains("Camp", ignoreCase = true) || searchContent.contains("8823", ignoreCase = true) -> {
            OrderDisplayInfo(
                title = "Camping Tent",
                category = "Outdoor & Gear",
                icon = Icons.Outlined.Inventory2,
                pickupDistance = "5.6 km",
                deliveryDistance = "8.2 km",
                schedule = "Today, 4:00 PM - 6:00 PM",
                drawableRes = R.drawable.product_camping_tent
            )
        }
        searchContent.contains("Generator", ignoreCase = true) || searchContent.contains("Power", ignoreCase = true) || searchContent.contains("8824", ignoreCase = true) -> {
            OrderDisplayInfo(
                title = "Generator",
                category = "Machinery & Power",
                icon = Icons.Outlined.Inventory2,
                pickupDistance = "4.8 km",
                deliveryDistance = "9.5 km",
                schedule = "Today, 5:00 PM - 7:00 PM",
                drawableRes = R.drawable.product_generator
            )
        }
        searchContent.contains("Audio", ignoreCase = true) || searchContent.contains("DJ", ignoreCase = true) -> {
            OrderDisplayInfo(
                title = "Audio Pro Gear",
                category = "Audio & Electronics",
                icon = Icons.Outlined.Inventory2,
                pickupDistance = "1.9 km",
                deliveryDistance = "5.4 km",
                schedule = "Today, 5:00 PM - 6:00 PM",
                drawableRes = null
            )
        }
        else -> {
            val dist = if (order.vendorLatitude != 0.0 && order.deliveryLatitude != 0.0) {
                val d = calculateDistance(
                    order.vendorLatitude, order.vendorLongitude,
                    order.deliveryLatitude, order.deliveryLongitude
                )
                String.format(Locale.getDefault(), "%.1f km", d)
            } else {
                "4.2 km"
            }
            OrderDisplayInfo(
                title = order.orderNumber,
                category = order.vendorName,
                icon = Icons.Outlined.Inventory2,
                pickupDistance = "2.1 km",
                deliveryDistance = dist,
                schedule = if (order.rentalPeriod.isNotBlank()) "Today, ${order.rentalPeriod.substringBefore("—").trim()}" else "Today, Scheduled Delivery",
                drawableRes = null
            )
        }
    }
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}
