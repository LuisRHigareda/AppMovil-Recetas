package com.example.recetario.data.firebase

import com.example.recetario.data.local.FavoriteRecipeEntity

data class FavoriteRecipeFirebase(
    val ownerEmail: String = "",
    val recipeId: String = "",
    val savedYearMonth: String = "",
)

fun FavoriteRecipeEntity.toFirebase(): FavoriteRecipeFirebase{
    return FavoriteRecipeFirebase(
        ownerEmail = ownerEmail,
        recipeId = recipeId,
        savedYearMonth = savedYearMonth
    )
}
fun FavoriteRecipeFirebase.toEntity(): FavoriteRecipeEntity{
    return FavoriteRecipeEntity(
        ownerEmail = ownerEmail,
        recipeId = recipeId,
        savedYearMonth = savedYearMonth,
        isSynced = true
    )
}