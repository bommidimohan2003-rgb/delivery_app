package com.payent.delivery.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.payent.delivery.core.AppContainer
import com.payent.delivery.feature.earnings.EarningsDetailsScreen
import com.payent.delivery.feature.earnings.EarningsScreen
import com.payent.delivery.feature.earnings.EarningsViewModel
import com.payent.delivery.feature.earnings.PayoutHistoryScreen
import com.payent.delivery.feature.home.HomeScreen
import com.payent.delivery.feature.home.HomeViewModel
import com.payent.delivery.feature.notifications.NotificationsScreen
import com.payent.delivery.feature.notifications.NotificationsViewModel
import com.payent.delivery.feature.orders.ActiveOrdersScreen
import com.payent.delivery.feature.orders.DeliveryResultScreen
import com.payent.delivery.feature.orders.DeliveryScreen
import com.payent.delivery.feature.orders.OrderDetailsScreen
import com.payent.delivery.feature.orders.OrderTab
import com.payent.delivery.feature.orders.OrdersScreen
import com.payent.delivery.feature.orders.OrdersViewModel
import com.payent.delivery.feature.orders.PickupResultScreen
import com.payent.delivery.feature.orders.PickupScreen
import com.payent.delivery.feature.orders.ProductInspectionScreen
import com.payent.delivery.feature.orders.RenterPinScreen
import com.payent.delivery.feature.orders.RenterVerificationScreen
import com.payent.delivery.feature.orders.ReportProblemScreen
import com.payent.delivery.feature.orders.VendorHandoverScreen
import com.payent.delivery.feature.profile.EditProfileScreen
import com.payent.delivery.feature.profile.KycScreen
import com.payent.delivery.feature.profile.ProfileScreen
import com.payent.delivery.feature.profile.ProfileViewModel
import com.payent.delivery.feature.settings.AboutAppScreen
import com.payent.delivery.feature.settings.PrivacyInfoScreen
import com.payent.delivery.feature.settings.SettingsScreen
import com.payent.delivery.feature.settings.SettingsViewModel
import com.payent.delivery.feature.settings.TermsConditionsScreen
import com.payent.delivery.feature.support.HelpSupportScreen
import com.payent.delivery.feature.support.SupportCategory
import com.payent.delivery.feature.support.SupportViewModel
import com.payent.delivery.feature.support.TicketDetailScreen
import com.payent.delivery.feature.support.TicketHistoryScreen

@Composable
fun PayentNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    // Scoped shared ViewModels across the NavHost graph for consistent state & argument sharing
    val sharedOrdersViewModel: OrdersViewModel = viewModel(
        factory = OrdersViewModel.Factory(
            ordersRepository = appContainer.ordersRepository,
            orderAlertManager = appContainer.orderAlertManager
        )
    )

    val sharedEarningsViewModel: EarningsViewModel = viewModel(
        factory = EarningsViewModel.Factory(
            earningsRepository = appContainer.earningsRepository
        )
    )

    val sharedProfileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(
            authRepository = appContainer.authRepository,
            sessionManager = appContainer.sessionManager
        )
    )

    val sharedSupportViewModel: SupportViewModel = viewModel()
    val sharedNotificationsViewModel: NotificationsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(
                    authRepository = appContainer.authRepository,
                    ordersRepository = appContainer.ordersRepository,
                    sessionManager = appContainer.sessionManager,
                    ordersViewModel = sharedOrdersViewModel
                )
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToActiveOrders = {
                    navController.navigate(Screen.ActiveOrders.route)
                },
                onNavigateToOrderDetails = { order ->
                    sharedOrdersViewModel.openOrderDetails(order)
                    navController.navigate(Screen.OrderDetails.route)
                },
                onAcceptOrder = { order ->
                    sharedOrdersViewModel.openOrderDetails(order)
                    sharedOrdersViewModel.requestAcceptOrder(order)
                    navController.navigate(Screen.OrderDetails.route)
                },
                onNavigateToEarnings = {
                    navController.navigate(Screen.Earnings.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToKyc = {
                    navController.navigate(Screen.Kyc.route)
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                viewModel = sharedNotificationsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToRoute = { route ->
                    when (route) {
                        "orders" -> {
                            navController.navigate(Screen.Orders.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        "earnings" -> {
                            navController.navigate(Screen.Earnings.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        "kyc" -> navController.navigate(Screen.Kyc.route)
                        "help_support" -> navController.navigate(Screen.HelpSupport.route)
                        else -> navController.navigate(route)
                    }
                }
            )
        }

        composable(Screen.HelpSupport.route) {
            HelpSupportScreen(
                viewModel = sharedSupportViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTicketHistory = { navController.navigate(Screen.TicketHistory.route) }
            )
        }

        composable(Screen.TicketHistory.route) {
            TicketHistoryScreen(
                viewModel = sharedSupportViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTicketDetail = {
                    navController.navigate(Screen.TicketDetail.route)
                }
            )
        }

        composable(Screen.TicketDetail.route) {
            TicketDetailScreen(
                viewModel = sharedSupportViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    sessionManager = appContainer.sessionManager,
                    authRepository = appContainer.authRepository
                )
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacyInfo = { navController.navigate(Screen.PrivacyInfo.route) },
                onNavigateToTerms = { navController.navigate(Screen.TermsConditions.route) },
                onNavigateToAbout = { navController.navigate(Screen.AboutApp.route) },
                onLoggedOut = {}
            )
        }

        composable(Screen.PrivacyInfo.route) {
            PrivacyInfoScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.TermsConditions.route) {
            TermsConditionsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AboutApp.route) {
            AboutAppScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Orders.route) {
            OrdersScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateToOrderDetails = { order ->
                    sharedOrdersViewModel.openOrderDetails(order)
                    navController.navigate(Screen.OrderDetails.route)
                },
                onNavigateToKyc = {
                    navController.navigate(Screen.Kyc.route)
                },
                onNavigateToHelp = {
                    sharedSupportViewModel.setPageScope(SupportCategory.ORDERS)
                    navController.navigate(Screen.HelpSupport.route)
                }
            )
        }

        composable(Screen.ActiveOrders.route) {
            ActiveOrdersScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOrderDetails = { order ->
                    sharedOrdersViewModel.openOrderDetails(order)
                    navController.navigate(Screen.OrderDetails.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToHelp = {
                    sharedSupportViewModel.setPageScope(SupportCategory.ORDERS)
                    navController.navigate(Screen.HelpSupport.route)
                }
            )
        }

        composable(Screen.OrderDetails.route) {
            OrderDetailsScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPickupMap = { navController.navigate(Screen.PickupMap.route) }
            )
        }

        composable(Screen.PickupMap.route) {
            PickupScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHandover = { navController.navigate(Screen.VendorHandover.route) },
                onNavigateToHelp = {
                    sharedSupportViewModel.setPageScope(SupportCategory.PICKUP)
                    navController.navigate(Screen.HelpSupport.route)
                }
            )
        }

        composable(Screen.VendorHandover.route) {
            VendorHandoverScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate(Screen.PickupResult.route) },
                onNavigateToHelp = {
                    sharedSupportViewModel.setPageScope(SupportCategory.PICKUP)
                    navController.navigate(Screen.HelpSupport.route)
                }
            )
        }

        composable(Screen.PickupResult.route) {
            PickupResultScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToDelivery = {
                    navController.navigate(Screen.DeliveryMap.route)
                }
            )
        }

        composable(Screen.DeliveryMap.route) {
            DeliveryScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRenterVerification = { navController.navigate(Screen.RenterVerification.route) },
                onNavigateToHelp = {
                    sharedSupportViewModel.setPageScope(SupportCategory.DELIVERY)
                    navController.navigate(Screen.HelpSupport.route)
                }
            )
        }

        composable(Screen.RenterVerification.route) {
            RenterVerificationScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate(Screen.DeliveryResult.route) },
                onNavigateToInspection = { navController.navigate(Screen.ProductInspection.route) },
                onNavigateToHelp = {
                    sharedSupportViewModel.setPageScope(SupportCategory.DELIVERY)
                    navController.navigate(Screen.HelpSupport.route)
                }
            )
        }

        composable(Screen.ProductInspection.route) {
            ProductInspectionScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReportProblem = { navController.navigate(Screen.ReportProblem.route) },
                onNavigateToPin = { navController.navigate(Screen.RenterPin.route) }
            )
        }

        composable(Screen.ReportProblem.route) {
            ReportProblemScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RenterPin.route) {
            RenterPinScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate(Screen.DeliveryResult.route) },
                onNavigateToHelp = {
                    sharedSupportViewModel.setPageScope(SupportCategory.DELIVERY)
                    navController.navigate(Screen.HelpSupport.route)
                }
            )
        }

        composable(Screen.DeliveryResult.route) {
            DeliveryResultScreen(
                viewModel = sharedOrdersViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Earnings.route) {
            EarningsScreen(
                viewModel = sharedEarningsViewModel,
                onNavigateToDetails = { navController.navigate(Screen.EarningsDetails.route) },
                onNavigateToPayoutHistory = { navController.navigate(Screen.PayoutHistory.route) },
                onNavigateToHelp = {
                    sharedSupportViewModel.setPageScope(SupportCategory.EARNINGS_PAYMENTS)
                    navController.navigate(Screen.HelpSupport.route)
                }
            )
        }

        composable(Screen.EarningsDetails.route) {
            EarningsDetailsScreen(
                viewModel = sharedEarningsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PayoutHistory.route) {
            PayoutHistoryScreen(
                viewModel = sharedEarningsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = sharedProfileViewModel,
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToKyc = { navController.navigate(Screen.Kyc.route) },
                onNavigateToOrders = {
                    sharedOrdersViewModel.selectTab(OrderTab.AVAILABLE)
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToEarnings = {
                    navController.navigate(Screen.Earnings.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToSupport = {
                    sharedSupportViewModel.setPageScope(SupportCategory.ACCOUNT)
                    navController.navigate(Screen.HelpSupport.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onLoggedOut = {}
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = sharedProfileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Kyc.route) {
            KycScreen(
                viewModel = sharedProfileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
