package com.example.recetario.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM own_recipes WHERE ownerEmail = :ownerEmail ORDER BY name COLLATE NOCASE ASC")
    fun observeOwnRecipes(ownerEmail: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM own_recipes WHERE isPublic = 1 AND isSecret = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observePublicUserRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM own_recipes WHERE ownerEmail = :ownerEmail AND id = :recipeId LIMIT 1")
    suspend fun getOwnRecipeById(ownerEmail: String, recipeId: String): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM own_recipes WHERE ownerEmail = :ownerEmail AND id = :recipeId")
    suspend fun deleteRecipe(ownerEmail: String, recipeId: String)

    @Query("SELECT * FROM favorite_recipes WHERE ownerEmail = :ownerEmail")
    fun observeFavorites(ownerEmail: String): Flow<List<FavoriteRecipeEntity>>

    @Query("SELECT COUNT(*) FROM favorite_recipes WHERE ownerEmail = :ownerEmail AND recipeId = :recipeId")
    suspend fun countFavorite(ownerEmail: String, recipeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteRecipeEntity)

    @Query("DELETE FROM favorite_recipes WHERE ownerEmail = :ownerEmail AND recipeId = :recipeId")
    suspend fun deleteFavorite(ownerEmail: String, recipeId: String)

    @Query("DELETE FROM favorite_recipes WHERE recipeId = :recipeId")
    suspend fun deleteFavoriteForAllUsers(recipeId: String)

    @Query("SELECT * FROM recipe_ratings WHERE ownerEmail = :ownerEmail")
    fun observeRatings(ownerEmail: String): Flow<List<RecipeRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRating(rating: RecipeRatingEntity)

    @Query("DELETE FROM recipe_ratings WHERE ownerEmail = :ownerEmail AND recipeId = :recipeId")
    suspend fun deleteRating(ownerEmail: String, recipeId: String)

    @Query("DELETE FROM recipe_ratings WHERE recipeId = :recipeId")
    suspend fun deleteRatingForAllUsers(recipeId: String)
}
