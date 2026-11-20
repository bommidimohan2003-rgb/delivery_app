package com.payent.delivery.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.payent.delivery.core.AppContainer
import com.payent.delivery.feature.auth.AuthViewModel
import com.payent.delivery.feature.auth.LoginScreen
import com.payent.delivery.feature.auth.OtpVerifyScreen
import com.payent.delivery.feature.auth.RegisterScreen
import com.payent.delivery.feature.auth.SplashScreen
import com.payent.delivery.feature.auth.WelcomeScreen

@Composable
fun AuthNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(
            authRepository = appContainer.authRepository
        )
    )
    val uiState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val targetRoute = if (uiState.isOtpSent) Screen.OtpVerify.route else Screen.Welcome.route
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route)
                    }
                },
                onNavigateToOtpVerify = {
                    navController.navigate(Screen.OtpVerify.route)
                }
            )
        }

        composable(Screen.OtpVerify.route) {
            OtpVerifyScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Welcome.route)
                    }
                },
                onNavigateToOtpVerify = {
                    navController.navigate(Screen.OtpVerify.route)
                }
            )
        }
    }
}
