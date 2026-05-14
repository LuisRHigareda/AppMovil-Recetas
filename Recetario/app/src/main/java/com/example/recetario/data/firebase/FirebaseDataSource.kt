package com.example.recetario.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseDataSource {
    // Instancia principal de Firestore
    private val db = FirebaseFirestore.getInstance()

    // Referencias a nuestras 3 colecciones principales en la base de datos
    private val recipesCollection = db.collection("recetas")
    private val ratingsCollection = db.collection("calificaciones")
    private val favoritesCollection = db.collection("favoritos")

    // ====================================================================
    // GUARDADO (SUBIR A LA NUBE)
    // Todas devuelven Boolean: true si hubo éxito, false si falló (ej. sin internet)
    // ====================================================================

    suspend fun saveRecipe(recipe: RecipeFirebase): Boolean {
        return try {
            // Usamos el ID de la receta como nombre del documento en Firebase.
            // .set() crea o actualiza el documento.
            recipesCollection.document(recipe.id).set(recipe).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun saveRating(rating: RecipeRatingFirebase): Boolean {
        return try {
            // Para las calificaciones, el documento debe ser único por usuario y receta.
            // Usamos la misma lógica de llave compuesta de Room.
            val docId = "${rating.ownerEmail}_${rating.recipeId}"
            ratingsCollection.document(docId).set(rating).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun saveFavorite(favorite: FavoriteRecipeFirebase): Boolean {
        return try {
            // Misma lógica que los ratings: ID único por usuario y receta
            val docId = "${favorite.ownerEmail}_${favorite.recipeId}"
            favoritesCollection.document(docId).set(favorite).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ====================================================================
    // LECTURA (DESCARGAR DE LA NUBE)
    // ====================================================================

    // Ejemplo: Descargar todas las recetas públicas de todos los usuarios
    suspend fun getPublicRecipes(): List<RecipeFirebase> {
        return try {
            // Hacemos una consulta (query) a Firestore
            val snapshot = recipesCollection
                .whereEqualTo("isPublic", true)
                .whereEqualTo("isSecret", false)
                .get()
                .await()

            // Convertimos la respuesta cruda (snapshot) a nuestra data class
            snapshot.toObjects(RecipeFirebase::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Ejemplo: Descargar las recetas de un usuario en específico
    suspend fun getUserRecipes(ownerEmail: String): List<RecipeFirebase> {
        return try {
            val snapshot = recipesCollection
                .whereEqualTo("ownerEmail", ownerEmail)
                .get()
                .await()
            snapshot.toObjects(RecipeFirebase::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteRecipe(recipeId: String): Boolean {
        return try {
            // Buscamos el documento por su ID y lo eliminamos físicamente de Firebase
            recipesCollection.document(recipeId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteFavorite(ownerEmail: String, recipeId: String): Boolean {
        return try {
            // Recreamos el ID compuesto para encontrar el documento exacto
            val docId = "${ownerEmail}_${recipeId}"
            favoritesCollection.document(docId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteRating(ownerEmail: String, recipeId: String): Boolean {
        return try {
            val docId = "${ownerEmail}_${recipeId}"
            ratingsCollection.document(docId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserRatings(ownerEmail: String): List<RecipeRatingFirebase> {
        return try {
            val snapshot = ratingsCollection
                .whereEqualTo("ownerEmail", ownerEmail)
                .get()
                .await()
            snapshot.toObjects(RecipeRatingFirebase::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserFavorites(ownerEmail: String): List<FavoriteRecipeFirebase> {
        return try {
            val snapshot = favoritesCollection
                .whereEqualTo("ownerEmail", ownerEmail)
                .get()
                .await()
            snapshot.toObjects(FavoriteRecipeFirebase::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}