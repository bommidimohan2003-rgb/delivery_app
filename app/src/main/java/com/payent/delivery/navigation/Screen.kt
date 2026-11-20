package com.payent.delivery.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // Auth Routes
    data object Splash : Screen(route = "splash", title = "Splash")
    data object Welcome : Screen(route = "welcome", title = "Welcome")
    data object Register : Screen(route = "register", title = "Register")
    data object OtpVerify : Screen(route = "otp_verify", title = "OTP Verification")
    data object Login : Screen(route = "login", title = "Sign In")

    // Profile & KYC Sub-Routes
    data object EditProfile : Screen(route = "edit_profile", title = "Edit Profile")
    data object Kyc : Screen(route = "kyc", title = "KYC Verification")

    // Order & Pickup Navigation Sub-Routes
    data object ActiveOrders : Screen(route = "active_orders", title = "Active Orders")
    data object OrderDetails : Screen(route = "order_details", title = "Order Details")
    data object PickupMap : Screen(route = "pickup_map", title = "Pickup Map")
    data object VendorHandover : Screen(route = "vendor_handover", title = "Vendor Handover")
    data object PickupResult : Screen(route = "pickup_result", title = "Pickup Completion")

    // Phase 6 Delivery & Renter Handover Sub-Routes
    data object DeliveryMap : Screen(route = "delivery_map", title = "Delivery Navigation")
    data object RenterVerification : Screen(route = "renter_verification", title = "Renter Verification")
    data object ProductInspection : Screen(route = "product_inspection", title = "Product Inspection")
    data object ReportProblem : Screen(route = "report_problem", title = "Report a Problem")
    data object RenterPin : Screen(route = "renter_pin", title = "Renter Handover PIN")
    data object DeliveryResult : Screen(route = "delivery_result", title = "Delivery Completion")

    // Phase 7 Earnings & Payout Sub-Routes
    data object EarningsDetails : Screen(route = "earnings_details", title = "Earnings Details")
    data object PayoutHistory : Screen(route = "payout_history", title = "Payout History")

    // Phase 8 Notifications & Support Sub-Routes
    data object Notifications : Screen(route = "notifications", title = "Notifications")
    data object HelpSupport : Screen(route = "help_support", title = "Help & Support")
    data object TicketHistory : Screen(route = "ticket_history", title = "Ticket History")
    data object TicketDetail : Screen(route = "ticket_detail", title = "Ticket Details")

    // Phase 9 Settings & Info Sub-Routes
    data object Settings : Screen(route = "settings", title = "Settings")
    data object PrivacyInfo : Screen(route = "privacy_info", title = "Privacy Information")
    data object TermsConditions : Screen(route = "terms_conditions", title = "Terms and Conditions")
    data object AboutApp : Screen(route = "about_app", title = "About App")

    // Authenticated Bottom Nav Routes
    data object Home : Screen(
        route = "home",
        title = "Home",
        icon = Icons.Outlined.Home
    )

    data object Orders : Screen(
        route = "orders",
        title = "Orders",
        icon = Icons.Outlined.ShoppingBag
    )

    data object Earnings : Screen(
        route = "earnings",
        title = "My Earnings",
        icon = Icons.Outlined.Payments
    )

    data object Profile : Screen(
        route = "profile",
        title = "Profile",
        icon = Icons.Outlined.Person
    )

    companion object {
        val bottomNavItems = listOf(Home, Orders, Earnings, Profile)
    }
}
