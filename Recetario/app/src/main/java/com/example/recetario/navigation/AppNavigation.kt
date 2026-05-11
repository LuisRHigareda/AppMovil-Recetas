package com.example.recetario.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recetario.screens.auth.BiometricSetupScreen
import com.example.recetario.screens.auth.LoginScreen
import com.example.recetario.screens.auth.RecoverPasswordScreen
import com.example.recetario.screens.auth.RegisterScreen
import com.example.recetario.screens.auth.ReturningLoginScreen
import com.example.recetario.screens.home.HomeScreen
import com.example.recetario.screens.profile.ProfileScreen
import com.example.recetario.screens.recipe.RecipeDetailScreen
import com.example.recetario.screens.recipe.RecipeFormScreen
import com.example.recetario.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val userState by authViewModel.userState.collectAsState()

    if (!userState.isLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        )
        return
    }

    val startDestination = if (userState.sessionActive) {
        Routes.ReturningLogin.route
    } else {
        Routes.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginClick = {
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
                authViewModel = authViewModel,
                onLoginClick = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.ReturningLogin.route) {
                            inclusive = true
                        }
                    }
                },
                onDifferentUserClick = {
                    authViewModel.logout()

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
                authViewModel = authViewModel,
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
                authViewModel = authViewModel,
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
            HomeScreen(
                authViewModel = authViewModel,
                onProfileClick = {
                    navController.navigate(Routes.Profile.route)
                },
                onRecipeClick = { recipeId ->
                    navController.navigate(Routes.RecipeDetail.createRoute(recipeId))
                },
                onAddRecipeClick = {
                    navController.navigate(Routes.RecipeForm.route)
                }
            )
        }

        composable(Routes.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    authViewModel.logout()

                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Profile.route) {
                            inclusive = true
                        }
                    }
                },
                onEditRecipeClick = { recipeId ->
                    navController.navigate(Routes.RecipeEdit.createRoute(recipeId))
                },
                onViewRecipeClick = { recipeId ->
                    navController.navigate(Routes.RecipeDetail.createRoute(recipeId))
                }
            )
        }

        composable(Routes.RecipeForm.route) {
            RecipeFormScreen(
                authViewModel = authViewModel,
                recipeId = null,
                onSaved = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) {
                            inclusive = true
                        }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.RecipeEdit.route,
            arguments = listOf(
                navArgument(Routes.RecipeEdit.ARG_RECIPE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments
                ?.getString(Routes.RecipeEdit.ARG_RECIPE_ID)
                .orEmpty()

            RecipeFormScreen(
                authViewModel = authViewModel,
                recipeId = recipeId,
                onSaved = {
                    navController.navigate(Routes.Profile.route) {
                        popUpTo(Routes.Profile.route) {
                            inclusive = true
                        }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.RecipeDetail.route,
            arguments = listOf(
                navArgument(Routes.RecipeDetail.ARG_RECIPE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments
                ?.getString(Routes.RecipeDetail.ARG_RECIPE_ID)
                .orEmpty()

            RecipeDetailScreen(
                recipeId = recipeId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
