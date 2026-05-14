package com.example.recetario.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    // MODIFICADO: Agregamos "AND isDeleted = 0" para que la UI no muestre lo que se está borrando
    @Query("SELECT * FROM own_recipes WHERE ownerEmail = :ownerEmail AND isDeleted = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeOwnRecipes(ownerEmail: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM own_recipes WHERE isPublic = 1 AND isSecret = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observePublicUserRecipes(): Flow<List<RecipeEntity>>

    // NUEVO: Función para "borrado lógico" (Soft Delete)
    @Query("UPDATE own_recipes SET isDeleted = 1, isSynced = 0 WHERE ownerEmail = :ownerEmail AND id = :recipeId")
    suspend fun markRecipeAsDeleted(ownerEmail: String, recipeId: String)

    // NUEVO: Buscar lo que hay que borrar físicamente en Firebase
    @Query("SELECT * FROM own_recipes WHERE isDeleted = 1 AND isSynced = 0")
    suspend fun getRecipesPendingDeletion(): List<RecipeEntity>

    // NUEVO: Borrado físico (solo se usa DESPUÉS de que Firebase confirme el borrado)
    @Query("DELETE FROM own_recipes WHERE id = :recipeId")
    suspend fun hardDeleteRecipe(recipeId: String)

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

    // --- RECETAS ---
    // Busca todas las recetas que se crearon offline y aún no suben
    @Query("SELECT * FROM own_recipes WHERE isSynced = 0")
    suspend fun getUnsyncedRecipes(): List<RecipeEntity>

    // Actualiza la bandera de una receta cuando Firebase confirma que se guardó
    @Query("UPDATE own_recipes SET isSynced = :isSynced WHERE id = :recipeId")
    suspend fun updateRecipeSyncStatus(recipeId: String, isSynced: Boolean)

    // --- CALIFICACIONES ---
    @Query("SELECT * FROM recipe_ratings WHERE isSynced = 0")
    suspend fun getUnsyncedRatings(): List<RecipeRatingEntity>

    // Como tiene llave compuesta, necesitamos ambos datos para actualizar el correcto
    @Query("UPDATE recipe_ratings SET isSynced = :isSynced WHERE ownerEmail = :ownerEmail AND recipeId = :recipeId")
    suspend fun updateRatingSyncStatus(ownerEmail: String, recipeId: String, isSynced: Boolean)

    // --- FAVORITOS ---
    @Query("SELECT * FROM favorite_recipes WHERE isSynced = 0")
    suspend fun getUnsyncedFavorites(): List<FavoriteRecipeEntity>

    @Query("UPDATE favorite_recipes SET isSynced = :isSynced WHERE ownerEmail = :ownerEmail AND recipeId = :recipeId")
    suspend fun updateFavoriteSyncStatus(ownerEmail: String, recipeId: String, isSynced: Boolean)
}
