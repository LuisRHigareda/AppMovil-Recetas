package com.example.recetario.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM own_recipes ORDER BY name COLLATE NOCASE ASC")
    fun observeOwnRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM own_recipes WHERE id = :recipeId LIMIT 1")
    suspend fun getOwnRecipeById(recipeId: String): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM own_recipes WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: String)

    @Query("SELECT * FROM favorite_recipes")
    fun observeFavorites(): Flow<List<FavoriteRecipeEntity>>

    @Query("SELECT COUNT(*) FROM favorite_recipes WHERE recipeId = :recipeId")
    suspend fun countFavorite(recipeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteRecipeEntity)

    @Query("DELETE FROM favorite_recipes WHERE recipeId = :recipeId")
    suspend fun deleteFavorite(recipeId: String)

    @Query("SELECT * FROM recipe_ratings")
    fun observeRatings(): Flow<List<RecipeRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRating(rating: RecipeRatingEntity)

    @Query("DELETE FROM recipe_ratings WHERE recipeId = :recipeId")
    suspend fun deleteRating(recipeId: String)
}
