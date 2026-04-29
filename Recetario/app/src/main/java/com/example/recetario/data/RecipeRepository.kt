package com.example.recetario.data

import android.content.Context
import com.example.recetario.model.Recipe
import org.json.JSONArray
import org.json.JSONObject
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MonthlyFavoriteActivity(
    val monthLabel: String,
    val savedCount: Int
)

class RecipeRepository(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        "recetario_recipes_preferences",
        Context.MODE_PRIVATE
    )

    fun getOwnRecipes(): List<Recipe> {
        val rawJson = preferences.getString(KEY_OWN_RECIPES, "[]") ?: "[]"

        return try {
            val array = JSONArray(rawJson)

            List(array.length()) { index ->
                parseRecipe(array.getJSONObject(index))
            }
        } catch (exception: Exception) {
            emptyList()
        }
    }

    fun getVisibleOwnRecipes(): List<Recipe> {
        return getOwnRecipes().filter { !it.isSecret }
    }

    fun getOwnPublicRecipes(): List<Recipe> {
        return getOwnRecipes().filter { it.isPublic && !it.isSecret }
    }

    fun getOwnSecretRecipes(): List<Recipe> {
        return getOwnRecipes().filter { it.isSecret }
    }

    fun getExploreRecipes(): List<Recipe> {
        val defaultPublicRecipes = getDefaultPublicRecipes()
        val userPublicRecipes = getOwnPublicRecipes()

        return defaultPublicRecipes + userPublicRecipes
    }

    fun getFavoriteRecipes(): List<Recipe> {
        return getExploreRecipes().filter { isFavorite(it.id) }
    }

    fun findRecipeById(recipeId: String): Recipe? {
        return getOwnRecipes().find { it.id == recipeId }
            ?: getDefaultPublicRecipes().find { it.id == recipeId }
    }

    fun saveUserRecipe(recipe: Recipe) {
        val currentRecipes = getOwnRecipes().toMutableList()
        val index = currentRecipes.indexOfFirst { it.id == recipe.id }

        if (index >= 0) {
            currentRecipes[index] = recipe
        } else {
            currentRecipes.add(recipe)
        }

        saveOwnRecipes(currentRecipes)
    }

    fun deleteUserRecipe(recipeId: String) {
        val updatedRecipes = getOwnRecipes().filter { it.id != recipeId }
        saveOwnRecipes(updatedRecipes)

        val favorites = preferences.getStringSet(KEY_FAVORITES, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()

        if (favorites.contains(recipeId)) {
            favorites.remove(recipeId)

            preferences.edit()
                .putStringSet(KEY_FAVORITES, favorites)
                .apply()
        }
    }

    fun isFavorite(recipeId: String): Boolean {
        val favorites = preferences.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
        return favorites.contains(recipeId)
    }

    fun toggleFavorite(recipeId: String) {
        val favorites = preferences.getStringSet(KEY_FAVORITES, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()

        if (favorites.contains(recipeId)) {
            favorites.remove(recipeId)
        } else {
            favorites.add(recipeId)
            registerFavoriteSave()
        }

        preferences.edit()
            .putStringSet(KEY_FAVORITES, favorites)
            .apply()
    }

    fun rateRecipe(recipeId: String, rating: Int) {
        if (rating in 1..5) {
            preferences.edit()
                .putInt("$KEY_RATING_PREFIX$recipeId", rating)
                .apply()
        }
    }

    fun getUserRating(recipeId: String): Int {
        return preferences.getInt("$KEY_RATING_PREFIX$recipeId", 0)
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

    fun getPublicRecipeCount(): Int {
        return getOwnRecipes().count { it.isPublic }
    }

    fun getPrivateRecipeCount(): Int {
        return getOwnRecipes().count { !it.isPublic }
    }

    fun getFavoriteActivityLastSixMonths(): List<MonthlyFavoriteActivity> {
        val formatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
        val currentMonth = YearMonth.now()
        val activityByMonth = getFavoriteActivityMap()

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
                savedCount = activityByMonth.optInt(yearMonth.toString(), 0)
            )
        }
    }

    private fun saveOwnRecipes(recipes: List<Recipe>) {
        val array = JSONArray()

        recipes.forEach { recipe ->
            array.put(recipeToJson(recipe))
        }

        preferences.edit()
            .putString(KEY_OWN_RECIPES, array.toString())
            .apply()
    }

    private fun registerFavoriteSave() {
        val activityByMonth = getFavoriteActivityMap()
        val currentMonth = YearMonth.now().toString()
        val currentCount = activityByMonth.optInt(currentMonth, 0)

        activityByMonth.put(currentMonth, currentCount + 1)

        preferences.edit()
            .putString(KEY_FAVORITE_ACTIVITY, activityByMonth.toString())
            .apply()
    }

    private fun getFavoriteActivityMap(): JSONObject {
        val rawJson = preferences.getString(KEY_FAVORITE_ACTIVITY, "{}") ?: "{}"

        return runCatching {
            JSONObject(rawJson)
        }.getOrElse {
            JSONObject()
        }
    }

    private fun recipeToJson(recipe: Recipe): JSONObject {
        return JSONObject().apply {
            put("id", recipe.id)
            put("name", recipe.name)
            put("description", recipe.description)
            put("category", recipe.category)
            put("ingredients", recipe.ingredients.toJsonArray())
            put("preparationSteps", recipe.preparationSteps.toJsonArray())
            put("tags", recipe.tags.toJsonArray())
            put("imageUri", recipe.imageUri ?: "")
            put("referenceUrl", recipe.referenceUrl ?: "")
            put("authorName", recipe.authorName)
            put("isOwnRecipe", recipe.isOwnRecipe)
            put("isPublic", recipe.isPublic)
            put("isSecret", recipe.isSecret)
            put("ratingAverage", recipe.ratingAverage)
            put("ratingCount", recipe.ratingCount)
        }
    }

    private fun parseRecipe(jsonObject: JSONObject): Recipe {
        return Recipe(
            id = jsonObject.optString("id"),
            name = jsonObject.optString("name"),
            description = jsonObject.optString("description"),
            category = jsonObject.optString("category"),
            ingredients = jsonObject.optJSONArray("ingredients").toStringList(),
            preparationSteps = jsonObject.optJSONArray("preparationSteps").toStringList(),
            tags = jsonObject.optJSONArray("tags").toStringList(),
            imageUri = jsonObject.optString("imageUri").ifBlank { null },
            referenceUrl = jsonObject.optString("referenceUrl").ifBlank { null },
            authorName = jsonObject.optString("authorName"),
            isOwnRecipe = jsonObject.optBoolean("isOwnRecipe"),
            isPublic = jsonObject.optBoolean("isPublic"),
            isSecret = jsonObject.optBoolean("isSecret"),
            ratingAverage = jsonObject.optDouble("ratingAverage", 0.0),
            ratingCount = jsonObject.optInt("ratingCount", 0)
        )
    }

    private fun List<String>.toJsonArray(): JSONArray {
        val array = JSONArray()

        this.forEach { value ->
            array.put(value)
        }

        return array
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()

        return List(length()) { index ->
            optString(index)
        }.filter { it.isNotBlank() }
    }

    private fun getDefaultPublicRecipes(): List<Recipe> {
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
                isOwnRecipe = false,
                isPublic = true,
                isSecret = false,
                ratingAverage = 4.7,
                ratingCount = 22
            )
        )
    }

    companion object {
        private const val KEY_OWN_RECIPES = "own_recipes"
        private const val KEY_FAVORITES = "favorite_recipe_ids"
        private const val KEY_FAVORITE_ACTIVITY = "favorite_activity"
        private const val KEY_RATING_PREFIX = "rating_"
    }
}
