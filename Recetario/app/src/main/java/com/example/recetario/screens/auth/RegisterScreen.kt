package com.example.recetario.screens.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetario.ui.theme.RecetarioTheme
import com.example.recetario.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel = viewModel(),
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    fun validateForm(): Boolean {
        firstNameError = null
        lastNameError = null
        birthDateError = null
        genderError = null
        emailError = null
        passwordError = null
        confirmPasswordError = null

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

        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            emailError = "Ingresa un correo válido."
            isValid = false
        }

        if (password.length < 6) {
            passwordError = "La contraseña debe tener al menos 6 caracteres."
            isValid = false
        }

        if (confirmPassword != password) {
            confirmPasswordError = "Las contraseñas no coinciden."
            isValid = false
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(42.dp))

        RecetarioLogo()

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Regístrate",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.widthIn(max = 380.dp)
        ) {
            AuthTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = "Nombre(s)",
                isError = firstNameError != null,
                errorMessage = firstNameError
            )

            AuthTextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = "Apellidos",
                isError = lastNameError != null,
                errorMessage = lastNameError
            )

            Row {
                AuthTextField(
                    value = birthDate,
                    onValueChange = {
                        birthDate = formatBirthDateInput(it)
                    },
                    label = "Fecha de nacimiento",
                    placeholder = "DD/MM/AAAA",
                    keyboardType = KeyboardType.Number,
                    isError = birthDateError != null,
                    errorMessage = birthDateError,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                GenderDropdown(
                    value = gender,
                    onValueChange = { gender = it },
                    isError = genderError != null,
                    errorMessage = genderError,
                    modifier = Modifier.weight(1f)
                )
            }

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Correo electrónico",
                keyboardType = KeyboardType.Email,
                isError = emailError != null,
                errorMessage = emailError
            )

            AuthTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                errorMessage = passwordError
            )

            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirmar contraseña",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OrangeButton(
            text = "Registrarse",
            onClick = {
                if (validateForm()) {
                    authViewModel.registerUser(
                        firstName = firstName,
                        lastName = lastName,
                        birthDate = birthDate,
                        gender = gender,
                        email = email,
                        password = password,
                        onSuccess = onRegisterClick
                    )
                }
            },
            modifier = Modifier.widthIn(max = 340.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomAuthText(
            normalText = "¿Ya tienes una cuenta?",
            actionText = "Inicia sesión aquí",
            onClick = onLoginClick
        )

        Spacer(modifier = Modifier.height(24.dp))
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
private fun RegisterScreenPreview() {
    RecetarioTheme {
        RegisterScreen(
            onRegisterClick = {},
            onLoginClick = {}
        )
    }
}
