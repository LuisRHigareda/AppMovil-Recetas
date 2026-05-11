package com.example.recetario.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetario.model.Recipe
import com.example.recetario.screens.auth.AuthTextField
import com.example.recetario.screens.auth.RecetarioOrange
import com.example.recetario.screens.profile.UserAvatar
import com.example.recetario.screens.recipe.RecipeImagePreview
import com.example.recetario.screens.recipe.RecipeTagRow
import com.example.recetario.ui.theme.RecetarioTheme
import com.example.recetario.viewmodel.AuthViewModel
import com.example.recetario.viewmodel.RecipeViewModel

private enum class HomeSection(val title: String) {
    MY_RECIPES("Mis recetas"),
    EXPLORE("Explorar recetas"),
    FAVORITES("Favoritas")
}

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel(),
    onProfileClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit
) {
    val recipeUiState by recipeViewModel.uiState.collectAsState()
    val userState by authViewModel.userState.collectAsState()

    var selectedSection by remember { mutableStateOf(HomeSection.MY_RECIPES) }
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }

    val baseRecipes = when (selectedSection) {
        HomeSection.MY_RECIPES -> recipeUiState.visibleOwnRecipes
        HomeSection.EXPLORE -> recipeUiState.exploreRecipes
        HomeSection.FAVORITES -> recipeUiState.favoriteRecipes
    }

    val categories = listOf("Todas") + baseRecipes
        .map { it.category }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    val filteredRecipes = baseRecipes.filter { recipe ->
        val query = searchText.trim().lowercase()

        val matchesSearch = query.isBlank() ||
                recipe.name.lowercase().contains(query) ||
                recipe.category.lowercase().contains(query) ||
                recipe.tags.any { it.lowercase().contains(query) }

        val matchesCategory = selectedCategory == "Todas" ||
                recipe.category == selectedCategory

        matchesSearch && matchesCategory
    }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecipeClick,
                containerColor = RecetarioOrange,
                contentColor = Color.White
            ) {
                Text(
                    text = "+",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Recetario",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "Organiza y consulta tus recetas",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }

                UserAvatar(
                    initials = userState.initials,
                    imageUri = userState.profileImageUri,
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { onProfileClick() }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            AuthTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = "Buscar por nombre, categoría o etiqueta"
            )

            Spacer(modifier = Modifier.height(14.dp))

            TabRow(
                selectedTabIndex = selectedSection.ordinal,
                containerColor = Color.White,
                contentColor = RecetarioOrange
            ) {
                HomeSection.values().forEach { section ->
                    Tab(
                        selected = selectedSection == section,
                        onClick = {
                            selectedSection = section
                            selectedCategory = "Todas"
                        },
                        text = {
                            Text(
                                text = section.title,
                                color = if (selectedSection == section) {
                                    RecetarioOrange
                                } else {
                                    Color.Black
                                },
                                fontWeight = if (selectedSection == section) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CategoryFilter(
                selectedCategory = selectedCategory,
                categories = categories,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredRecipes.isEmpty()) {
                EmptyRecipeState(
                    selectedSection = selectedSection,
                    onAddRecipeClick = onAddRecipeClick
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredRecipes,
                        key = { recipe -> recipe.id }
                    ) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            isFavorite = recipeViewModel.isFavorite(recipe.id),
                            onClick = {
                                onRecipeClick(recipe.id)
                            },
                            onFavoriteClick = {
                                recipeViewModel.toggleFavorite(recipe.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Categoría: $selectedCategory",
                color = Color.Black
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category,
                            color = Color.Black
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RecipeImagePreview(
                imageUri = recipe.imageUri,
                placeholderText = "Imagen",
                modifier = Modifier.size(88.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = recipe.category,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )

                if (!recipe.isOwnRecipe) {
                    Text(
                        text = "por ${recipe.authorName}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                } else if (recipe.isPublic) {
                    Text(
                        text = "Publicada por ti",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                RecipeTagRow(
                    tags = recipe.tags.take(3)
                )

                if (!recipe.isOwnRecipe) {
                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.widthIn(min = 1.dp)
                    ) {
                        Text(
                            text = if (isFavorite) "Guardada" else "Guardar",
                            color = RecetarioOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRecipeState(
    selectedSection: HomeSection,
    onAddRecipeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val message = when (selectedSection) {
            HomeSection.MY_RECIPES -> "Aún no tienes recetas registradas."
            HomeSection.EXPLORE -> "No se encontraron recetas públicas."
            HomeSection.FAVORITES -> "Aún no tienes recetas guardadas."
        }

        Text(
            text = message,
            fontSize = 18.sp,
            color = Color.DarkGray
        )

        if (selectedSection == HomeSection.MY_RECIPES) {
            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(onClick = onAddRecipeClick) {
                Text(
                    text = "Agregar primera receta",
                    color = RecetarioOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    RecetarioTheme {
        HomeScreen(
            onProfileClick = {},
            onRecipeClick = {},
            onAddRecipeClick = {}
        )
    }
}
