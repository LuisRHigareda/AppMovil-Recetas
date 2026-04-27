package com.example.recetario.navigation

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object ReturningLogin : Routes("returning_login")
    data object Register : Routes("register")
    data object RecoverPassword : Routes("recover_password")
    data object BiometricSetup : Routes("biometric_setup")
    data object Home : Routes("home")
}