package com.example.recetario.data.local

import androidx.room.Entity

@Entity(
    tableName = "favorite_recipes",
    primaryKeys = ["ownerEmail", "recipeId"]
)
data class FavoriteRecipeEntity(
    val ownerEmail: String,
    val recipeId: String,
    val savedYearMonth: String
)
