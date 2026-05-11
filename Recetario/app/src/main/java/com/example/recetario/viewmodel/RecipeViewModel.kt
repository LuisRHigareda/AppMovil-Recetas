package com.example.recetario.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetario.data.MonthlyFavoriteActivity
import com.example.recetario.data.RecipeRepository
import com.example.recetario.model.Recipe
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class RecipeUiState(
    val ownRecipes: List<Recipe> = emptyList(),
    val visibleOwnRecipes: List<Recipe> = emptyList(),
    val exploreRecipes: List<Recipe> = emptyList(),
    val favoriteRecipes: List<Recipe> = emptyList(),
    val publicRecipes: List<Recipe> = emptyList(),
    val secretRecipes: List<Recipe> = emptyList(),
    val favoriteRecipeIds: Set<String> = emptySet(),
    val ratingsByRecipeId: Map<String, Int> = emptyMap(),
    val favoriteActivity: List<MonthlyFavoriteActivity> = emptyList(),
    val publicRecipeCount: Int = 0,
    val privateRecipeCount: Int = 0
) {
    val allRecipes: List<Recipe>
        get() = ownRecipes + exploreRecipes.filter { recipe -> ownRecipes.none { it.id == recipe.id } }
}

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)
    private val defaultPublicRecipes = repository.getDefaultPublicRecipes()

    val uiState: StateFlow<RecipeUiState> = combine(
        repository.observeOwnRecipes(),
        repository.observeFavorites(),
        repository.observeRatings()
    ) { ownRecipes, favorites, ratings ->
        val visibleOwnRecipes = ownRecipes.filter { !it.isSecret }
        val publicRecipes = ownRecipes.filter { it.isPublic && !it.isSecret }
        val secretRecipes = ownRecipes.filter { it.isSecret }
        val exploreRecipes = defaultPublicRecipes + publicRecipes
        val favoriteIds = favorites.map { it.recipeId }.toSet()
        val favoriteRecipes = exploreRecipes.filter { it.id in favoriteIds }
        val ratingsByRecipeId = ratings.associate { it.recipeId to it.rating }

        RecipeUiState(
            ownRecipes = ownRecipes,
            visibleOwnRecipes = visibleOwnRecipes,
            exploreRecipes = exploreRecipes,
            favoriteRecipes = favoriteRecipes,
            publicRecipes = publicRecipes,
            secretRecipes = secretRecipes,
            favoriteRecipeIds = favoriteIds,
            ratingsByRecipeId = ratingsByRecipeId,
            favoriteActivity = repository.getFavoriteActivityLastSixMonths(favorites),
            publicRecipeCount = ownRecipes.count { it.isPublic },
            privateRecipeCount = ownRecipes.count { !it.isPublic }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeUiState(
            exploreRecipes = defaultPublicRecipes
        )
    )

    fun findRecipeById(recipeId: String): Recipe? {
        return uiState.value.allRecipes.find { it.id == recipeId }
    }

    fun isFavorite(recipeId: String): Boolean {
        return recipeId in uiState.value.favoriteRecipeIds
    }

    fun getUserRating(recipeId: String): Int {
        return uiState.value.ratingsByRecipeId[recipeId] ?: 0
    }

    fun getDisplayRating(recipe: Recipe): Double {
        val userRating = getUserRating(recipe.id)

        if (userRating == 0) {
            return recipe.ratingAverage
        }

        val previousTotal = recipe.ratingAverage * recipe.ratingCount
        val newCount = recipe.ratingCount + 1

        return if (newCount > 0) {
            (previousTotal + userRating) / newCount
        } else {
            0.0
        }
    }

    fun saveRecipe(recipe: Recipe, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.saveUserRecipe(recipe)
            onSaved()
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            repository.deleteUserRecipe(recipeId)
        }
    }

    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(recipeId)
        }
    }

    fun rateRecipe(recipeId: String, rating: Int) {
        viewModelScope.launch {
            repository.rateRecipe(recipeId, rating)
        }
    }
}
