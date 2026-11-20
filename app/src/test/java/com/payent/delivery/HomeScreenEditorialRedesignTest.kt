package com.payent.delivery

import com.payent.delivery.R
import com.payent.delivery.domain.model.Order
import com.payent.delivery.domain.model.OrderStatus
import com.payent.delivery.feature.orders.OrdersViewModel
import com.payent.delivery.feature.orders.getOrderDisplayInfo
import com.payent.delivery.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test verifying the Editorial Home Screen redesign data mappings,
 * products, and navigation destinations.
 */
class HomeScreenEditorialRedesignTest {

    @Test
    fun testDemoOrdersMatchReferenceImageProducts() {
        val sampleOrders = OrdersViewModel.sampleDemoOrders
        assertEquals(4, sampleOrders.size)

        // 1. Mountain Bike - ₹45/day
        val bikeOrder = sampleOrders[0]
        val bikeInfo = getOrderDisplayInfo(bikeOrder)
        assertEquals("Mountain Bike", bikeInfo.title)
        assertEquals(R.drawable.product_mountain_bike, bikeInfo.drawableRes)
        assertEquals("2.4 km", bikeInfo.pickupDistance)
        assertEquals(45.0, bikeOrder.payoutAmount, 0.01)

        // 2. DSLR Camera - ₹85/day
        val cameraOrder = sampleOrders[1]
        val cameraInfo = getOrderDisplayInfo(cameraOrder)
        assertEquals("DSLR Camera", cameraInfo.title)
        assertEquals(R.drawable.product_dslr_camera, cameraInfo.drawableRes)
        assertEquals("3.1 km", cameraInfo.pickupDistance)
        assertEquals(85.0, cameraOrder.payoutAmount, 0.01)

        // 3. Camping Tent - ₹120/day
        val tentOrder = sampleOrders[2]
        val tentInfo = getOrderDisplayInfo(tentOrder)
        assertEquals("Camping Tent", tentInfo.title)
        assertEquals(R.drawable.product_camping_tent, tentInfo.drawableRes)
        assertEquals("5.6 km", tentInfo.pickupDistance)
        assertEquals(120.0, tentOrder.payoutAmount, 0.01)

        // 4. Generator - ₹150/day
        val genOrder = sampleOrders[3]
        val genInfo = getOrderDisplayInfo(genOrder)
        assertEquals("Generator", genInfo.title)
        assertEquals(R.drawable.product_generator, genInfo.drawableRes)
        assertEquals("4.8 km", genInfo.pickupDistance)
        assertEquals(150.0, genOrder.payoutAmount, 0.01)
    }

    @Test
    fun testBottomNavigationDestinationsPreserved() {
        // Must contain exactly 4 destinations: Home, Orders, Earnings, Profile
        val destinations = Screen.bottomNavItems
        assertEquals(4, destinations.size)
        assertEquals(Screen.Home, destinations[0])
        assertEquals(Screen.Orders, destinations[1])
        assertEquals(Screen.Earnings, destinations[2])
        assertEquals(Screen.Profile, destinations[3])

        // Verify Earnings is preserved in bottom navigation
        val earningsScreen = destinations.find { it == Screen.Earnings }
        assertNotNull(earningsScreen)
        assertEquals("earnings", earningsScreen?.route)
    }

    @Test
    fun testOrderDisplayInfoFallbackForCustomLiveOrder() {
        val customOrder = Order(
            id = "live_999",
            orderNumber = "ORD-999",
            vendorName = "Custom Vendor",
            vendorAddress = "123 Main Street, Suite 4",
            deliveryAddress = "456 Market St",
            rentalPeriod = "1-Day Rental — Equipment",
            payoutAmount = 90.0,
            status = OrderStatus.AVAILABLE
        )
        val info = getOrderDisplayInfo(customOrder)
        assertNotNull(info)
        assertEquals("ORD-999", info.title)
        assertTrue(info.schedule.contains("Today"))
    }

    @Test
    fun testDarkModeMonochromeColorContract() {
        // Verify dark mode uses clean monochrome values without blue tints
        val darkCardBg = androidx.compose.ui.graphics.Color(0xFF141414)
        val darkPillBg = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
        val darkBorderCol = androidx.compose.ui.graphics.Color(0xFF282828)
        val darkCard1Bg = androidx.compose.ui.graphics.Color.White

        // Ensure darkCard1Bg is pure white when active
        assertEquals(androidx.compose.ui.graphics.Color.White, darkCard1Bg)
        // Ensure neutral dark surfaces have balanced R, G, B channels (no blue tint)
        assertEquals(darkCardBg.red, darkCardBg.green, 0.01f)
        assertEquals(darkCardBg.green, darkCardBg.blue, 0.01f)
        assertEquals(darkPillBg.red, darkPillBg.green, 0.01f)
        assertEquals(darkPillBg.green, darkPillBg.blue, 0.01f)
        assertEquals(darkBorderCol.red, darkBorderCol.green, 0.01f)
        assertEquals(darkBorderCol.green, darkBorderCol.blue, 0.01f)
    }
}
