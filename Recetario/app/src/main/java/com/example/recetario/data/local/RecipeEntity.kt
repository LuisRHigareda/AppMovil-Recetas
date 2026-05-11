package com.example.recetario.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.recetario.model.Recipe

@Entity(tableName = "own_recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val ownerEmail: String,
    val name: String,
    val description: String,
    val category: String,
    val ingredients: List<String>,
    val preparationSteps: List<String>,
    val tags: List<String>,
    val imageUri: String?,
    val referenceUrl: String?,
    val authorName: String,
    val isOwnRecipe: Boolean,
    val isPublic: Boolean,
    val isSecret: Boolean,
    val ratingAverage: Double,
    val ratingCount: Int
)

fun RecipeEntity.toDomain(): Recipe {
    return Recipe(
        id = id,
        name = name,
        description = description,
        category = category,
        ingredients = ingredients,
        preparationSteps = preparationSteps,
        tags = tags,
        imageUri = imageUri,
        referenceUrl = referenceUrl,
        authorName = authorName,
        ownerEmail = ownerEmail,
        isOwnRecipe = isOwnRecipe,
        isPublic = isPublic,
        isSecret = isSecret,
        ratingAverage = ratingAverage,
        ratingCount = ratingCount
    )
}

fun Recipe.toEntity(ownerEmailOverride: String = ownerEmail): RecipeEntity {
    return RecipeEntity(
        id = id,
        ownerEmail = ownerEmailOverride,
        name = name,
        description = description,
        category = category,
        ingredients = ingredients,
        preparationSteps = preparationSteps,
        tags = tags,
        imageUri = imageUri,
        referenceUrl = referenceUrl,
        authorName = authorName,
        isOwnRecipe = isOwnRecipe,
        isPublic = isPublic,
        isSecret = isSecret,
        ratingAverage = ratingAverage,
        ratingCount = ratingCount
    )
}
