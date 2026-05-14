package com.example.recetario.model

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val ingredients: List<String>,
    val preparationSteps: List<String>,
    val tags: List<String>,
    val imageUri: String? = null,
    val referenceUrl: String? = null,
    val authorName: String,
    val ownerEmail: String = "",
    val isOwnRecipe: Boolean,
    val isPublic: Boolean,
    val isSecret: Boolean,
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val isSynced: Boolean = true
)