package com.example.recetario.screens.profile

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetario.data.MonthlyFavoriteActivity
import com.example.recetario.data.findFragmentActivity
import com.example.recetario.data.showBiometricPrompt
import com.example.recetario.model.Recipe
import com.example.recetario.screens.auth.AuthTextField
import com.example.recetario.screens.auth.GenderDropdown
import com.example.recetario.screens.auth.OrangeButton
import com.example.recetario.screens.auth.RecetarioLightGray
import com.example.recetario.screens.auth.RecetarioOrange
import com.example.recetario.screens.recipe.RecipeImagePreview
import com.example.recetario.ui.theme.RecetarioTheme
import com.example.recetario.viewmodel.AuthViewModel
import com.example.recetario.viewmodel.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel(),
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditRecipeClick: (String) -> Unit,
    onViewRecipeClick: (String) -> Unit
) {
    val context = LocalContext.current
    val userState by authViewModel.userState.collectAsState()
    val recipeUiState by recipeViewModel.uiState.collectAsState()

    var isEditingProfile by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSecretFolderUnlocked by remember { mutableStateOf(false) }
    var secretFolderMessage by remember { mutableStateOf<String?>(null) }

    var firstName by remember(userState.firstName) { mutableStateOf(userState.firstName) }
    var lastName by remember(userState.lastName) { mutableStateOf(userState.lastName) }
    var birthDate by remember(userState.birthDate) { mutableStateOf(userState.birthDate) }
    var gender by remember(userState.gender) { mutableStateOf(userState.gender) }

    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }

    val publicRecipes = recipeUiState.publicRecipes
    val secretRecipes = recipeUiState.secretRecipes
    val favoriteActivity = recipeUiState.favoriteActivity
    val publicRecipeCount = recipeUiState.publicRecipeCount
    val privateRecipeCount = recipeUiState.privateRecipeCount
    val fullName = userState.fullName
    val email = userState.email
    val profileImageUri = userState.profileImageUri
    val initials = userState.initials

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

            authViewModel.setProfileImageUri(uri.toString())
        }
    }

    fun validateProfile(): Boolean {
        firstNameError = null
        lastNameError = null
        birthDateError = null
        genderError = null
        message = null

        var isValid = true

        if (firstName.trim().length < 2) {
            firstNameError = "Ingresa tu nombre."
            isValid = false
        }

        if (lastName.trim().length < 2) {
            lastNameError = "Ingresa tus apellidos."
            isValid = false
        }

        if (!isValidBirthDate(birthDate)) {
            birthDateError = "Usa el formato DD/MM/AAAA."
            isValid = false
        }

        if (gender.trim().isEmpty()) {
            genderError = "Selecciona una opción."
            isValid = false
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(26.dp))

        TextButton(onClick = onBackClick) {
            Text(
                text = "Volver",
                color = RecetarioOrange,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            UserAvatar(
                initials = initials,
                imageUri = profileImageUri,
                modifier = Modifier.size(86.dp)
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fullName,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = {
                imagePickerLauncher.launch(arrayOf("image/*"))
            }
        ) {
            Text(
                text = "Cambiar foto de perfil",
                color = RecetarioOrange,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Información personal",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (message != null) {
            Text(
                text = message ?: "",
                color = RecetarioOrange,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isEditingProfile) {
            AuthTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = "Nombre(s)",
                isError = firstNameError != null,
                errorMessage = firstNameError
            )

            Spacer(modifier = Modifier.height(10.dp))

            AuthTextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = "Apellidos",
                isError = lastNameError != null,
                errorMessage = lastNameError
            )

            Spacer(modifier = Modifier.height(10.dp))

            AuthTextField(
                value = birthDate,
                onValueChange = {
                    birthDate = formatBirthDateInput(it)
                },
                label = "Fecha de nacimiento",
                placeholder = "DD/MM/AAAA",
                keyboardType = KeyboardType.Number,
                isError = birthDateError != null,
                errorMessage = birthDateError
            )

            Spacer(modifier = Modifier.height(10.dp))

            GenderDropdown(
                value = gender,
                onValueChange = { gender = it },
                isError = genderError != null,
                errorMessage = genderError
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Correo electrónico: $email",
                color = Color.DarkGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            OrangeButton(
                text = "Guardar cambios",
                onClick = {
                    if (validateProfile()) {
                        authViewModel.updateUserProfile(
                            firstName = firstName,
                            lastName = lastName,
                            birthDate = birthDate,
                            gender = gender
                        ) {
                            isEditingProfile = false
                            message = "Información actualizada correctamente."
                        }
                    }
                },
                modifier = Modifier.widthIn(max = 340.dp)
            )

            TextButton(
                onClick = {
                    isEditingProfile = false
                    firstName = userState.firstName
                    lastName = userState.lastName
                    birthDate = userState.birthDate
                    gender = userState.gender
                }
            ) {
                Text(
                    text = "Cancelar",
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            ProfileInfoRow(label = "Nombre", value = userState.firstName)
            ProfileInfoRow(label = "Apellidos", value = userState.lastName)
            ProfileInfoRow(label = "Fecha de nacimiento", value = userState.birthDate)
            ProfileInfoRow(label = "Género", value = userState.gender)
            ProfileInfoRow(label = "Correo", value = email)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    isEditingProfile = true
                    message = null
                }
            ) {
                Text(
                    text = "Editar información",
                    color = RecetarioOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Actividad",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        ActivityChartCard(
            title = "Recetas guardadas por mes",
            entries = favoriteActivity,
            emptyMessage = "Todavía no tienes recetas guardadas en favoritos."
        )

        Spacer(modifier = Modifier.height(18.dp))

        VisibilityChartCard(
            publicCount = publicRecipeCount,
            privateCount = privateRecipeCount
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Mis recetas publicadas",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (publicRecipes.isEmpty()) {
            Text(
                text = "Aún no tienes recetas publicadas. Para publicar una receta, actívale la opción Compartir receta al guardarla.",
                color = Color.DarkGray,
                fontSize = 15.sp
            )
        } else {
            publicRecipes.forEach { recipe ->
                PublicRecipeCard(
                    recipe = recipe,
                    onEditClick = {
                        onEditRecipeClick(recipe.id)
                    },
                    onDeleteClick = {
                        recipeViewModel.deleteRecipe(recipe.id)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Folder secreto",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tus recetas secretas solo se muestran aquí y requieren huella digital para desbloquearse.",
            color = Color.DarkGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (secretFolderMessage != null) {
            Text(
                text = secretFolderMessage ?: "",
                color = if (isSecretFolderUnlocked) RecetarioOrange else Color.Red,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (secretRecipes.isEmpty()) {
            Text(
                text = "Todavía no has marcado recetas como secretas.",
                color = Color.DarkGray,
                fontSize = 15.sp
            )
        } else if (!isSecretFolderUnlocked) {
            OutlinedButton(
                onClick = {
                    secretFolderMessage = null

                    val activity = context.findFragmentActivity()

                    if (activity == null) {
                        secretFolderMessage = "No se pudo iniciar la autenticación con huella digital."
                        return@OutlinedButton
                    }

                    showBiometricPrompt(
                        activity = activity,
                        title = "Folder secreto",
                        subtitle = "Confirma tu huella para ver tus recetas secretas.",
                        onSuccess = {
                            isSecretFolderUnlocked = true
                            secretFolderMessage = "Folder desbloqueado correctamente."
                        },
                        onError = { error ->
                            secretFolderMessage = error
                        },
                        onFailed = {
                            secretFolderMessage = "No se reconoció la huella digital. Intenta nuevamente."
                        }
                    )
                }
            ) {
                Text(
                    text = "Desbloquear con huella",
                    color = RecetarioOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            TextButton(
                onClick = {
                    isSecretFolderUnlocked = false
                    secretFolderMessage = "Folder bloqueado."
                }
            ) {
                Text(
                    text = "Ocultar recetas secretas",
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            secretRecipes.forEach { recipe ->
                SecretRecipeCard(
                    recipe = recipe,
                    onViewClick = {
                        onViewRecipeClick(recipe.id)
                    },
                    onEditClick = {
                        onEditRecipeClick(recipe.id)
                    },
                    onDeleteClick = {
                        recipeViewModel.deleteRecipe(recipe.id)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        OrangeButton(
            text = "Cerrar sesión",
            onClick = onLogoutClick,
            modifier = Modifier.widthIn(max = 320.dp)
        )

        Spacer(modifier = Modifier.height(34.dp))
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.DarkGray,
            fontSize = 13.sp
        )

        Text(
            text = value.ifBlank { "Sin información" },
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActivityChartCard(
    title: String,
    entries: List<MonthlyFavoriteActivity>,
    emptyMessage: String
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            val hasActivity = entries.any { it.savedCount > 0 }

            if (!hasActivity) {
                Text(
                    text = emptyMessage,
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            } else {
                val maxValue = entries.maxOf { it.savedCount }.coerceAtLeast(1)

                entries.forEach { entry ->
                    ChartBarRow(
                        label = entry.monthLabel,
                        value = entry.savedCount,
                        maxValue = maxValue,
                        barColor = RecetarioOrange
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun VisibilityChartCard(
    publicCount: Int,
    privateCount: Int
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recetas públicas vs privadas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            val maxValue = maxOf(publicCount, privateCount).coerceAtLeast(1)

            ChartBarRow(
                label = "Públicas",
                value = publicCount,
                maxValue = maxValue,
                barColor = RecetarioOrange
            )

            Spacer(modifier = Modifier.height(10.dp))

            ChartBarRow(
                label = "Privadas",
                value = privateCount,
                maxValue = maxValue,
                barColor = Color(0xFF5C6BC0)
            )
        }
    }
}

@Composable
private fun ChartBarRow(
    label: String,
    value: Int,
    maxValue: Int,
    barColor: Color
) {
    val progress = if (maxValue == 0) {
        0f
    } else {
        value.toFloat() / maxValue.toFloat()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.width(76.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(50))
                .background(RecetarioLightGray)
        ) {
            if (value > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(barColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value.toString(),
            color = Color.DarkGray,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(20.dp)
        )
    }
}

@Composable
private fun PublicRecipeCard(
    recipe: Recipe,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RecipeImagePreview(
                imageUri = recipe.imageUri,
                placeholderText = "Imagen",
                modifier = Modifier.size(78.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.name,
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = recipe.category,
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    TextButton(onClick = onEditClick) {
                        Text(
                            text = "Editar",
                            color = RecetarioOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onDeleteClick) {
                        Text(
                            text = "Eliminar",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecretRecipeCard(
    recipe: Recipe,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RecipeImagePreview(
                imageUri = recipe.imageUri,
                placeholderText = "Secreta",
                modifier = Modifier.size(78.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.name,
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Acceso protegido con huella",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    TextButton(onClick = onViewClick) {
                        Text(
                            text = "Ver",
                            color = RecetarioOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onEditClick) {
                        Text(
                            text = "Editar",
                            color = RecetarioOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = onDeleteClick) {
                        Text(
                            text = "Eliminar",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatBirthDateInput(input: String): String {
    val digits = input.filter { it.isDigit() }.take(8)

    return buildString {
        digits.forEachIndexed { index, char ->
            append(char)

            if (index == 1 || index == 3) {
                append("/")
            }
        }
    }
}

private fun isValidBirthDate(date: String): Boolean {
    if (date.length != 10) return false

    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatter.isLenient = false

        val parsedDate = formatter.parse(date) ?: return false
        !parsedDate.after(Date())
    } catch (exception: Exception) {
        false
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    RecetarioTheme {
        ProfileScreen(
            onBackClick = {},
            onLogoutClick = {},
            onEditRecipeClick = {},
            onViewRecipeClick = {}
        )
    }
}
