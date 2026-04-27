package com.example.recetario.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recetario.data.AuthRepository
import com.example.recetario.screens.auth.BiometricSetupScreen
import com.example.recetario.screens.auth.LoginScreen
import com.example.recetario.screens.auth.RecoverPasswordScreen
import com.example.recetario.screens.auth.RegisterScreen
import com.example.recetario.screens.auth.ReturningLoginScreen
import com.example.recetario.screens.home.HomeScreen
import com.example.recetario.screens.profile.ProfileScreen
import com.example.recetario.screens.recipe.RecipeDetailScreen
import com.example.recetario.screens.recipe.RecipeFormScreen

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
            HomeScreen(
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
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    authRepository.logout()

                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Profile.route) {
                            inclusive = true
                        }
                    }
                },
                onEditRecipeClick = { recipeId ->
                    navController.navigate(Routes.RecipeEdit.createRoute(recipeId))
                }
            )
        }

        composable(Routes.RecipeForm.route) {
            RecipeFormScreen(
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