package com.example.recetario.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetario.data.RecipeRepository
import com.example.recetario.model.Recipe
import com.example.recetario.screens.auth.OrangeButton
import com.example.recetario.screens.auth.RecetarioOrange
import java.util.Locale

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val recipeRepository = remember { RecipeRepository(context) }
    var refreshCounter by remember { mutableIntStateOf(0) }

    val recipe = remember(recipeId, refreshCounter) {
        recipeRepository.findRecipeById(recipeId)
    }

    if (recipe == null) {
        RecipeNotFoundScreen(onBackClick = onBackClick)
    } else {
        RecipeDetailContent(
            recipe = recipe,
            recipeRepository = recipeRepository,
            onBackClick = onBackClick,
            onRefresh = {
                refreshCounter++
            }
        )
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    recipeRepository: RecipeRepository,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val displayRating = recipeRepository.getDisplayRating(recipe)
    val userRating = recipeRepository.getUserRating(recipe.id)
    val isFavorite = recipeRepository.isFavorite(recipe.id)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onBackClick) {
                Text(
                    text = "Volver",
                    color = RecetarioOrange,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            RecipeImagePreview(
                imageUri = recipe.imageUri,
                placeholderText = "Sin fotografía",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = recipe.name,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = if (recipe.isOwnRecipe) {
                    "Receta propia"
                } else {
                    "por ${recipe.authorName}"
                },
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Categoría: ${recipe.category}",
                fontSize = 16.sp,
                color = Color.Black
            )

            if (!recipe.isOwnRecipe) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Calificación promedio: ${
                        String.format(Locale.getDefault(), "%.1f", displayRating)
                    }/5",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RecipeTagRow(tags = recipe.tags)

            Spacer(modifier = Modifier.height(20.dp))

            if (!recipe.isOwnRecipe) {
                OrangeButton(
                    text = if (isFavorite) "Quitar de favoritas" else "Guardar en favoritas",
                    onClick = {
                        recipeRepository.toggleFavorite(recipe.id)
                        onRefresh()
                    },
                    modifier = Modifier.widthIn(max = 320.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Calificar receta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { rating ->
                        OutlinedButton(
                            onClick = {
                                recipeRepository.rateRecipe(recipe.id, rating)
                                onRefresh()
                            }
                        ) {
                            Text(
                                text = rating.toString(),
                                color = if (userRating == rating) {
                                    RecetarioOrange
                                } else {
                                    Color.Black
                                },
                                fontWeight = if (userRating == rating) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }
                }

                if (userRating > 0) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tu calificación: $userRating/5",
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            SectionTitle(title = "Descripción")

            Text(
                text = recipe.description,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(22.dp))

            SectionTitle(title = "Ingredientes")

            recipe.ingredients.forEach { ingredient ->
                Text(
                    text = "• $ingredient",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            SectionTitle(title = "Preparación")

            recipe.preparationSteps.forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }

            if (!recipe.referenceUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        uriHandler.openUri(recipe.referenceUrl)
                    }
                ) {
                    Text(
                        text = "Abrir enlace de referencia",
                        color = RecetarioOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun RecipeNotFoundScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(30.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No se encontró la receta.",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        OrangeButton(
            text = "Volver",
            onClick = onBackClick
        )
    }
}