package com.example.recetario.navigation

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object ReturningLogin : Routes("returning_login")
    data object Register : Routes("register")
    data object RecoverPassword : Routes("recover_password")
    data object BiometricSetup : Routes("biometric_setup")
    data object Home : Routes("home")
    data object Profile : Routes("profile")
    data object RecipeForm : Routes("recipe_form")

    data object RecipeEdit : Routes("recipe_form/{recipeId}") {
        const val ARG_RECIPE_ID = "recipeId"

        fun createRoute(recipeId: String): String {
            return "recipe_form/$recipeId"
        }
    }

    data object RecipeDetail : Routes("recipe_detail/{recipeId}") {
        const val ARG_RECIPE_ID = "recipeId"

        fun createRoute(recipeId: String): String {
            return "recipe_detail/$recipeId"
        }
    }
}