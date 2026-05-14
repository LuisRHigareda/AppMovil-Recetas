package com.example.recetario.data.firebase

import com.example.recetario.data.local.RecipeEntity

data class RecipeFirebase(
    val id: String = "",
    val ownerEmail: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val ingredients: List<String> = emptyList(),
    val preparationSteps: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val imageUri: String? = null,
    val referenceUrl: String? = null,
    val authorName: String = "",
    val isOwnRecipe: Boolean = false,
    val isPublic: Boolean = false,
    val isSecret: Boolean = false,
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0
)

// De Room a Firebase (Para cuando vayas a subir los datos a la nube)
fun RecipeEntity.toFirebase(): RecipeFirebase {
    return RecipeFirebase(
        id = id,
        ownerEmail = ownerEmail,
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

// De Firebase a Room (Para cuando descargues recetas de la nube y las guardes local)
fun RecipeFirebase.toEntity(): RecipeEntity {
    return RecipeEntity(
        id = id,
        ownerEmail = ownerEmail,
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
        ratingCount = ratingCount,
        isSynced = true
    )
}