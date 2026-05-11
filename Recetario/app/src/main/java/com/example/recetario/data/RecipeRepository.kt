package com.example.recetario.data

import android.content.Context
import com.example.recetario.data.local.AppDatabase
import com.example.recetario.data.local.FavoriteRecipeEntity
import com.example.recetario.data.local.RecipeRatingEntity
import com.example.recetario.data.local.toDomain
import com.example.recetario.data.local.toEntity
import com.example.recetario.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthlyFavoriteActivity(
    val monthLabel: String,
    val savedCount: Int
)

class RecipeRepository(context: Context) {

    private val recipeDao = AppDatabase.getInstance(context).recipeDao()

    fun observeOwnRecipes(ownerEmail: String): Flow<List<Recipe>> {
        return recipeDao.observeOwnRecipes(ownerEmail.normalizeEmail()).map { recipes ->
            recipes.map { it.toDomain() }
        }
    }

    fun observePublicUserRecipes(): Flow<List<Recipe>> {
        return recipeDao.observePublicUserRecipes().map { recipes ->
            recipes.map { it.toDomain() }
        }
    }

    fun observeFavorites(ownerEmail: String): Flow<List<FavoriteRecipeEntity>> {
        return recipeDao.observeFavorites(ownerEmail.normalizeEmail())
    }

    fun observeRatings(ownerEmail: String): Flow<List<RecipeRatingEntity>> {
        return recipeDao.observeRatings(ownerEmail.normalizeEmail())
    }

    suspend fun findOwnRecipeById(ownerEmail: String, recipeId: String): Recipe? {
        return recipeDao.getOwnRecipeById(ownerEmail.normalizeEmail(), recipeId)?.toDomain()
    }

    suspend fun saveUserRecipe(ownerEmail: String, recipe: Recipe) {
        val normalizedOwnerEmail = ownerEmail.normalizeEmail()
        recipeDao.upsertRecipe(
            recipe
                .copy(ownerEmail = normalizedOwnerEmail, isOwnRecipe = true)
                .toEntity(ownerEmailOverride = normalizedOwnerEmail)
        )
    }

    suspend fun deleteUserRecipe(ownerEmail: String, recipeId: String) {
        val normalizedOwnerEmail = ownerEmail.normalizeEmail()
        recipeDao.deleteRecipe(normalizedOwnerEmail, recipeId)
        recipeDao.deleteFavoriteForAllUsers(recipeId)
        recipeDao.deleteRatingForAllUsers(recipeId)
    }

    suspend fun toggleFavorite(ownerEmail: String, recipeId: String) {
        val normalizedOwnerEmail = ownerEmail.normalizeEmail()
        val isFavorite = recipeDao.countFavorite(normalizedOwnerEmail, recipeId) > 0

        if (isFavorite) {
            recipeDao.deleteFavorite(normalizedOwnerEmail, recipeId)
        } else {
            recipeDao.insertFavorite(
                FavoriteRecipeEntity(
                    ownerEmail = normalizedOwnerEmail,
                    recipeId = recipeId,
                    savedYearMonth = YearMonth.now().toString()
                )
            )
        }
    }

    suspend fun rateRecipe(ownerEmail: String, recipeId: String, rating: Int) {
        val normalizedOwnerEmail = ownerEmail.normalizeEmail()

        if (rating in 1..5) {
            recipeDao.upsertRating(
                RecipeRatingEntity(
                    ownerEmail = normalizedOwnerEmail,
                    recipeId = recipeId,
                    rating = rating
                )
            )
        }
    }

    fun getFavoriteActivityLastSixMonths(
        favorites: List<FavoriteRecipeEntity>
    ): List<MonthlyFavoriteActivity> {
        val formatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
        val currentMonth = YearMonth.now()
        val activityByMonth = favorites.groupingBy { it.savedYearMonth }.eachCount()

        return (5 downTo 0).map { monthsBack ->
            val yearMonth = currentMonth.minusMonths(monthsBack.toLong())
            val label = yearMonth.format(formatter)
                .replaceFirstChar { char ->
                    if (char.isLowerCase()) {
                        char.titlecase(Locale.getDefault())
                    } else {
                        char.toString()
                    }
                }

            MonthlyFavoriteActivity(
                monthLabel = label,
                savedCount = activityByMonth[yearMonth.toString()] ?: 0
            )
        }
    }

    fun getDefaultPublicRecipes(): List<Recipe> {
        return listOf(
            Recipe(
                id = "public_tacos_asada",
                name = "Tacos de Asada",
                description = "Tacos de carne asada con tortilla, salsa y guarniciones frescas.",
                category = "Comida mexicana",
                ingredients = listOf(
                    "500 g de carne asada",
                    "Tortillas de maíz",
                    "Salsa verde",
                    "Cilantro picado",
                    "Cebolla picada",
                    "Limón al gusto"
                ),
                preparationSteps = listOf(
                    "Cocinar la carne asada hasta que esté bien dorada.",
                    "Calentar las tortillas.",
                    "Picar la carne en trozos pequeños.",
                    "Servir la carne sobre las tortillas.",
                    "Agregar salsa, cilantro, cebolla y limón."
                ),
                tags = listOf("tacos", "salsa", "carne"),
                imageUri = "drawable:tacos_asada",
                referenceUrl = "https://www.google.com/search?q=receta+tacos+de+asada",
                authorName = "Usuario252",
                ownerEmail = "default_public",
                isOwnRecipe = false,
                isPublic = true,
                isSecret = false,
                ratingAverage = 4.8,
                ratingCount = 18
            ),
            Recipe(
                id = "public_pastel_limon",
                name = "Pastel de Limón",
                description = "Postre suave con sabor cítrico y glaseado de limón.",
                category = "Postres",
                ingredients = listOf(
                    "2 tazas de harina",
                    "1 taza de azúcar",
                    "3 huevos",
                    "1/2 taza de jugo de limón",
                    "Ralladura de limón",
                    "1 cucharada de polvo para hornear"
                ),
                preparationSteps = listOf(
                    "Mezclar los ingredientes secos.",
                    "Agregar huevos, jugo y ralladura de limón.",
                    "Batir hasta obtener una mezcla uniforme.",
                    "Hornear a 180 °C durante 35 minutos.",
                    "Dejar enfriar y decorar con glaseado."
                ),
                tags = listOf("pastel", "limón", "postre"),
                imageUri = "drawable:pastel_limon",
                referenceUrl = "https://www.google.com/search?q=receta+pastel+de+limon",
                authorName = "CocinaCasera",
                ownerEmail = "default_public",
                isOwnRecipe = false,
                isPublic = true,
                isSecret = false,
                ratingAverage = 4.6,
                ratingCount = 12
            ),
            Recipe(
                id = "public_galletas_chocolate",
                name = "Galletas con Chispas de Chocolate",
                description = "Galletas caseras crujientes por fuera y suaves por dentro.",
                category = "Postres",
                ingredients = listOf(
                    "2 tazas de harina",
                    "1 taza de azúcar",
                    "1 huevo",
                    "1 taza de chispas de chocolate",
                    "1/2 taza de mantequilla",
                    "1 cucharadita de vainilla"
                ),
                preparationSteps = listOf(
                    "Batir la mantequilla con el azúcar.",
                    "Agregar el huevo y la vainilla.",
                    "Incorporar la harina poco a poco.",
                    "Añadir las chispas de chocolate.",
                    "Formar las galletas y hornear a 180 °C durante 12 minutos."
                ),
                tags = listOf("galletas", "chocolate", "postre"),
                imageUri = "drawable:galletas_chocolate",
                referenceUrl = "https://www.google.com/search?q=receta+galletas+con+chispas+de+chocolate",
                authorName = "ReposteríaFácil",
                ownerEmail = "default_public",
                isOwnRecipe = false,
                isPublic = true,
                isSecret = false,
                ratingAverage = 4.7,
                ratingCount = 22
            )
        )
    }

    private fun String.normalizeEmail(): String = trim().lowercase(Locale.getDefault())
}
