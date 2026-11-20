package com.payent.delivery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.payent.delivery.core.AppContainer
import com.payent.delivery.core.alert.AlertState
import com.payent.delivery.core.alert.ui.NewOrderAlertDialog
import com.payent.delivery.domain.model.AuthState
import com.payent.delivery.feature.auth.SplashScreen
import com.payent.delivery.navigation.AuthNavHost
import com.payent.delivery.navigation.PayentBottomNavigation
import com.payent.delivery.navigation.PayentNavHost
import com.payent.delivery.navigation.Screen
import com.payent.delivery.ui.theme.PayentDeliveryTheme
import kotlinx.coroutines.launch

@Composable
fun PayentApp(appContainer: AppContainer) {
    val themeMode by appContainer.sessionManager.themeModeFlow.collectAsState(initial = "DARK")
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    PayentDeliveryTheme(darkTheme = isDark) {
        val authState by appContainer.authRepository.authStateFlow.collectAsState(initial = AuthState.Unauthenticated)
        val coroutineScope = rememberCoroutineScope()
        // Pre-warm session in background during splash so Home is ready with data upon finish
        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (authState !is AuthState.Authenticated) {
                appContainer.authRepository.startDemoLogin()
            }
        }

        var isSplashFinished by remember { mutableStateOf(false) }
        if (!isSplashFinished) {
            SplashScreen(
                onSplashFinished = {
                    isSplashFinished = true
                }
            )
        } else {
            if (authState is AuthState.Authenticated) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute != Screen.VendorHandover.route &&
                        currentRoute != Screen.PickupMap.route &&
                        currentRoute != Screen.DeliveryMap.route &&
                        currentRoute != Screen.RenterVerification.route &&
                        currentRoute != Screen.ProductInspection.route &&
                        currentRoute != Screen.ReportProblem.route &&
                        currentRoute != Screen.RenterPin.route &&
                        currentRoute != Screen.PickupResult.route &&
                        currentRoute != Screen.DeliveryResult.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        PayentNavHost(
                            navController = navController,
                            appContainer = appContainer,
                            modifier = Modifier.fillMaxSize()
                        )

                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                                animationSpec = tween(250),
                                initialOffsetY = { it }
                            ),
                            exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
                                animationSpec = tween(200),
                                targetOffsetY = { it }
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            PayentBottomNavigation(navController = navController)
                        }

                        // High-Priority Foreground New Order Alert Dialog Overlay
                        val alertState by appContainer.orderAlertManager.alertState.collectAsState()
                        (alertState as? AlertState.Active)?.let { active ->
                            NewOrderAlertDialog(
                                order = active.order,
                                onViewOrder = { order ->
                                    appContainer.orderAlertManager.acknowledgeAlert(order.id)
                                    navController.navigate(Screen.Orders.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onAcceptOrder = { order ->
                                    appContainer.orderAlertManager.acceptOrderAsync(
                                        orderId = order.id,
                                        onAccepted = { _ ->
                                            navController.navigate(Screen.Orders.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                },
                                onDismissAlert = { order ->
                                    appContainer.orderAlertManager.dismissAlert(order.id)
                                }
                            )
                        }
                    }
                }
            } else {
                val authNavController = rememberNavController()
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.White
                ) {
                    AuthNavHost(
                        navController = authNavController,
                        appContainer = appContainer,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
