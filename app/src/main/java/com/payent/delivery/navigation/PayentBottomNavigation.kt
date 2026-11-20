package com.payent.delivery.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkSurface

/**
 * Editorial Floating Bottom Navigation Bar matching the reference design.
 *
 * Characteristics:
 * - Solid black dock background with elevated depth and subtle border.
 * - Exactly 4 destinations: Home, Orders, Earnings, Profile.
 * - The selected destination (Home) features a refined white rounded-rectangle outline.
 * - White icons and crisp typography across both Light and Dark themes.
 */
@Composable
fun PayentBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val isDark = MaterialTheme.colorScheme.background == DarkBackground ||
            MaterialTheme.colorScheme.surface == DarkSurface

    // Black dock background matching reference image
    val containerBg = if (isDark) Color(0xFF0A0A0A) else Color(0xFF000000)
    val borderCol = if (isDark) Color(0xFF262626) else Color(0xFF1F1F1F)
    val shadowCol = Color(0x66000000)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = shadowCol,
                spotColor = shadowCol
            ),
        shape = CircleShape,
        color = containerBg,
        border = BorderStroke(1.dp, borderCol),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Screen.bottomNavItems.forEach { screen ->
                val selected = isRouteSelected(screen, currentRoute)

                FloatingNavItem(
                    screen = screen,
                    selected = selected,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (screen == Screen.Home) {
                            if (currentRoute != Screen.Home.route) {
                                val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
                                if (!popped) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        } else {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    screen: Screen,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.95f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "nav_item_scale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFFB0B0B8),
        animationSpec = tween(durationMillis = 180),
        label = "nav_item_content_color"
    )

    // Select icons matching the reference design:
    // Home: House
    // Orders: Clipboard / Assignment
    // Earnings: Bar chart
    // Profile: Person
    val icon: ImageVector = when (screen) {
        Screen.Home -> Icons.Outlined.Home
        Screen.Orders -> Icons.AutoMirrored.Outlined.Assignment
        Screen.Earnings -> Icons.Outlined.BarChart
        Screen.Profile -> Icons.Outlined.Person
        else -> screen.icon ?: Icons.Outlined.Home
    }

    val title = when (screen) {
        Screen.Home -> "Home"
        Screen.Orders -> "Orders"
        Screen.Earnings -> "Earnings"
        Screen.Profile -> "Profile"
        else -> screen.title
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Selected item is framed by a thin white rounded outline rectangle
        Box(
            modifier = Modifier
                .then(
                    if (selected) {
                        Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.2.dp, Color.White, RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    } else {
                        Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = contentColor,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Checks whether a given bottom-nav destination is currently active,
 * maintaining selected synchronization even when on sub-routes.
 */
private fun isRouteSelected(screen: Screen, currentRoute: String?): Boolean {
    if (currentRoute == null) return screen == Screen.Home
    return when (screen) {
        Screen.Home -> currentRoute == Screen.Home.route
        Screen.Orders -> currentRoute in listOf(
            Screen.Orders.route,
            Screen.ActiveOrders.route,
            Screen.OrderDetails.route,
            Screen.PickupMap.route,
            Screen.VendorHandover.route,
            Screen.PickupResult.route,
            Screen.DeliveryMap.route,
            Screen.RenterVerification.route,
            Screen.ProductInspection.route,
            Screen.ReportProblem.route,
            Screen.RenterPin.route,
            Screen.DeliveryResult.route
        )
        Screen.Earnings -> currentRoute in listOf(
            Screen.Earnings.route,
            Screen.EarningsDetails.route,
            Screen.PayoutHistory.route
        )
        Screen.Profile -> currentRoute in listOf(
            Screen.Profile.route,
            Screen.EditProfile.route,
            Screen.Kyc.route,
            Screen.Settings.route,
            Screen.PrivacyInfo.route,
            Screen.TermsConditions.route,
            Screen.AboutApp.route
        )
        else -> currentRoute == screen.route
    }
}
