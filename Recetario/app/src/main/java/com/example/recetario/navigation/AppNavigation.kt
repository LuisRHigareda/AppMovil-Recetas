package com.example.recetario.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recetario.data.AuthRepository
import com.example.recetario.screens.auth.BiometricSetupScreen
import com.example.recetario.screens.auth.LoginScreen
import com.example.recetario.screens.auth.RecoverPasswordScreen
import com.example.recetario.screens.auth.RegisterScreen
import com.example.recetario.screens.auth.ReturningLoginScreen
import com.example.recetario.screens.home.HomePlaceholderScreen

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val navController = rememberNavController()

    val startDestination = remember {
        if (authRepository.isSessionActive()) {
            Routes.ReturningLogin.route
        } else {
            Routes.Login.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginClick = {
                    authRepository.setSessionActive(true)

                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.Register.route)
                },
                onRecoverPasswordClick = {
                    navController.navigate(Routes.RecoverPassword.route)
                }
            )
        }

        composable(Routes.ReturningLogin.route) {
            ReturningLoginScreen(
                onLoginClick = {
                    authRepository.setSessionActive(true)

                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.ReturningLogin.route) {
                            inclusive = true
                        }
                    }
                },
                onDifferentUserClick = {
                    authRepository.logout()

                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.ReturningLogin.route) {
                            inclusive = true
                        }
                    }
                },
                onRecoverPasswordClick = {
                    navController.navigate(Routes.RecoverPassword.route)
                }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                onRegisterClick = {
                    navController.navigate(Routes.BiometricSetup.route)
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.RecoverPassword.route) {
            RecoverPasswordScreen(
                onSendInstructionsClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.BiometricSetup.route) {
            BiometricSetupScreen(
                onAcceptClick = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onSkipClick = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomePlaceholderScreen(
                onLogoutClick = {
                    authRepository.logout()

                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}