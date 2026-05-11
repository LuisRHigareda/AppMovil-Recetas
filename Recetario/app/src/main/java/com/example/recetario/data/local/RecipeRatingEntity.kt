package com.example.recetario.data.local

import androidx.room.Entity

@Entity(
    tableName = "recipe_ratings",
    primaryKeys = ["ownerEmail", "recipeId"]
)
data class RecipeRatingEntity(
    val ownerEmail: String,
    val recipeId: String,
    val rating: Int
)
