package com.example.recetario.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_ratings")
data class RecipeRatingEntity(
    @PrimaryKey val recipeId: String,
    val rating: Int
)
