package com.example.recetario.data.firebase

import com.example.recetario.data.local.RecipeRatingEntity

data class RecipeRatingFirebase(
    val ownerEmail: String = "",
    val recipeId: String = "",
    val rating: Int = 0
)

// --- MAPPERS ---

// De Room a Firebase (Para subir la calificación)
fun RecipeRatingEntity.toFirebase(): RecipeRatingFirebase {
    return RecipeRatingFirebase(
        ownerEmail = ownerEmail,
        recipeId = recipeId,
        rating = rating
    )
}

// De Firebase a Room (Para descargar calificaciones de otros o las tuyas propias)
fun RecipeRatingFirebase.toEntity(): RecipeRatingEntity {
    return RecipeRatingEntity(
        ownerEmail = ownerEmail,
        recipeId = recipeId,
        rating = rating,
        isSynced = true
    )
}