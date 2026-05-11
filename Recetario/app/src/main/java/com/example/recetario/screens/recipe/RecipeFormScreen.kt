package com.example.recetario.screens.recipe

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetario.data.saveBitmapToInternalStorage
import com.example.recetario.model.Recipe
import com.example.recetario.screens.auth.AuthTextField
import com.example.recetario.screens.auth.OrangeButton
import com.example.recetario.screens.auth.RecetarioOrange
import com.example.recetario.ui.theme.RecetarioTheme
import com.example.recetario.viewmodel.AuthViewModel
import com.example.recetario.viewmodel.RecipeViewModel

@Composable
fun RecipeFormScreen(
    authViewModel: AuthViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel(),
    recipeId: String? = null,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val userState by authViewModel.userState.collectAsState()
    val recipeUiState by recipeViewModel.uiState.collectAsState()

    val existingRecipe = remember(recipeId, recipeUiState.allRecipes) {
        recipeId?.let { recipeViewModel.findRecipeById(it) }
    }

    val isEditing = existingRecipe != null

    var name by remember(existingRecipe) { mutableStateOf(existingRecipe?.name.orEmpty()) }
    var description by remember(existingRecipe) { mutableStateOf(existingRecipe?.description.orEmpty()) }
    var category by remember(existingRecipe) { mutableStateOf(existingRecipe?.category.orEmpty()) }
    var ingredientInput by remember { mutableStateOf("") }
    var stepInput by remember { mutableStateOf("") }
    var tagsInput by remember(existingRecipe) {
        mutableStateOf(existingRecipe?.tags?.joinToString(", ").orEmpty())
    }
    var referenceUrl by remember(existingRecipe) {
        mutableStateOf(existingRecipe?.referenceUrl.orEmpty())
    }
    var imageUri by remember(existingRecipe) {
        mutableStateOf(existingRecipe?.imageUri)
    }
    var shareRecipe by remember(existingRecipe) {
        mutableStateOf(existingRecipe?.isPublic ?: false)
    }
    var secretRecipe by remember(existingRecipe) {
        mutableStateOf(existingRecipe?.isSecret ?: false)
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val ingredients = remember(existingRecipe) {
        mutableStateListOf<String>().apply {
            addAll(existingRecipe?.ingredients ?: emptyList())
        }
    }

    val preparationSteps = remember(existingRecipe) {
        mutableStateListOf<String>().apply {
            addAll(existingRecipe?.preparationSteps ?: emptyList())
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            imageUri = uri.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap == null) {
            errorMessage = "No se pudo capturar la fotografía."
        } else {
            val savedImageUri = saveBitmapToInternalStorage(
                context = context,
                bitmap = bitmap,
                filePrefix = "recipe"
            )

            if (savedImageUri == null) {
                errorMessage = "No se pudo guardar la fotografía capturada."
            } else {
                imageUri = savedImageUri
                errorMessage = null
            }
        }
    }

    fun validateForm(): Boolean {
        errorMessage = null

        val tags = tagsInput
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (name.trim().isEmpty()) {
            errorMessage = "Ingresa el nombre de la receta."
            return false
        }

        if (description.trim().isEmpty()) {
            errorMessage = "Ingresa una descripción breve."
            return false
        }

        if (category.trim().isEmpty()) {
            errorMessage = "Ingresa una categoría."
            return false
        }

        if (ingredients.isEmpty()) {
            errorMessage = "Agrega al menos un ingrediente."
            return false
        }

        if (preparationSteps.isEmpty()) {
            errorMessage = "Agrega al menos un paso de preparación."
            return false
        }

        if (tags.isEmpty()) {
            errorMessage = "Agrega al menos una etiqueta."
            return false
        }

        if (
            referenceUrl.isNotBlank() &&
            !referenceUrl.startsWith("https://") &&
            !referenceUrl.startsWith("http://")
        ) {
            errorMessage = "El enlace debe iniciar con http:// o https://."
            return false
        }

        if (shareRecipe && imageUri.isNullOrBlank()) {
            errorMessage = "Para publicar una receta en Explorar recetas, agrega una fotografía."
            return false
        }

        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (isEditing) "Editar Receta" else "Nueva Receta",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Para que una receta aparezca en Explorar recetas, activa Compartir receta y completa los campos obligatorios.",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        AuthTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Nombre de la receta"
        )

        Spacer(modifier = Modifier.height(12.dp))

        FormTextArea(
            value = description,
            onValueChange = { description = it },
            label = "Descripción",
            placeholder = "Descripción breve de la receta"
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = category,
            onValueChange = { category = it },
            placeholder = "Categoría, por ejemplo: Postres"
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Ingredientes")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuthTextField(
                value = ingredientInput,
                onValueChange = { ingredientInput = it },
                placeholder = "Ejemplo: 2 tazas de harina",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    if (ingredientInput.trim().isNotEmpty()) {
                        ingredients.add(ingredientInput.trim())
                        ingredientInput = ""
                    }
                }
            ) {
                Text(
                    text = "Agregar",
                    color = RecetarioOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ingredients.forEachIndexed { index, ingredient ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "• $ingredient",
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        ingredients.removeAt(index)
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = Color.Red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Preparación")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuthTextField(
                value = stepInput,
                onValueChange = { stepInput = it },
                placeholder = "Ejemplo: Mezclar los ingredientes",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = {
                    if (stepInput.trim().isNotEmpty()) {
                        preparationSteps.add(stepInput.trim())
                        stepInput = ""
                    }
                }
            ) {
                Text(
                    text = "Agregar",
                    color = RecetarioOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        preparationSteps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${index + 1}. $step",
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        preparationSteps.removeAt(index)
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = Color.Red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle(title = "Fotografía")

        RecipeImagePreview(
            imageUri = imageUri,
            placeholderText = "Sin fotografía",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                imagePickerLauncher.launch(arrayOf("image/*"))
            }
        ) {
            Text(
                text = "Seleccionar fotografía",
                color = RecetarioOrange,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                cameraLauncher.launch(null)
            }
        ) {
            Text(
                text = "Tomar foto con cámara",
                color = RecetarioOrange,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        AuthTextField(
            value = tagsInput,
            onValueChange = { tagsInput = it },
            placeholder = "Etiquetas separadas por coma"
        )

        Spacer(modifier = Modifier.height(12.dp))

        AuthTextField(
            value = referenceUrl,
            onValueChange = { referenceUrl = it },
            placeholder = "URL opcional de referencia",
            keyboardType = KeyboardType.Uri
        )

        Spacer(modifier = Modifier.height(22.dp))

        SwitchOption(
            title = "Compartir receta",
            description = "La receta será visible en Explorar recetas.",
            checked = shareRecipe,
            onCheckedChange = {
                shareRecipe = it

                if (it) {
                    secretRecipe = false
                }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        SwitchOption(
            title = "Receta secreta",
            description = "La receta se ocultará de la lista principal. Después se accederá desde el perfil con huella digital.",
            checked = secretRecipe,
            onCheckedChange = {
                secretRecipe = it

                if (it) {
                    shareRecipe = false
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        OrangeButton(
            text = if (isEditing) "Guardar cambios" else "Guardar receta",
            onClick = {
                if (validateForm()) {
                    val tags = tagsInput
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    val recipeToSave = Recipe(
                        id = existingRecipe?.id ?: "own_${System.currentTimeMillis()}",
                        name = name.trim(),
                        description = description.trim(),
                        category = category.trim(),
                        ingredients = ingredients.toList(),
                        preparationSteps = preparationSteps.toList(),
                        tags = tags,
                        imageUri = imageUri,
                        referenceUrl = referenceUrl.trim().ifBlank { null },
                        authorName = userState.fullName,
                        isOwnRecipe = true,
                        isPublic = shareRecipe,
                        isSecret = secretRecipe,
                        ratingAverage = existingRecipe?.ratingAverage ?: 0.0,
                        ratingCount = existingRecipe?.ratingCount ?: 0
                    )

                    recipeViewModel.saveRecipe(recipeToSave, onSaved)
                }
            },
            modifier = Modifier.widthIn(max = 360.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Cancelar",
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun FormTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = Color.DarkGray
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = Color.Gray
            )
        },
        minLines = 4,
        keyboardOptions = KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = RecetarioOrange,
            focusedBorderColor = RecetarioOrange,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = RecetarioOrange,
            unfocusedLabelColor = Color.DarkGray,
            focusedPlaceholderColor = Color.Gray,
            unfocusedPlaceholderColor = Color.Gray
        ),
        modifier = Modifier.fillMaxWidth()
    )
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
private fun SwitchOption(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecipeFormScreenPreview() {
    RecetarioTheme {
        RecipeFormScreen(
            onSaved = {},
            onCancel = {}
        )
    }
}
