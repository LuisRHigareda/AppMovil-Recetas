package com.example.recetario.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetario.data.findFragmentActivity
import com.example.recetario.data.showBiometricPrompt
import com.example.recetario.ui.theme.RecetarioTheme
import com.example.recetario.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginClick: () -> Unit,
    onBiometricSetupRequired: () -> Unit,
    onRegisterClick: () -> Unit,
    onRecoverPasswordClick: () -> Unit
) {
    val context = LocalContext.current
    val userState by authViewModel.userState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalMessage by remember { mutableStateOf<String?>(null) }

    fun validateLogin(): Boolean {
        emailError = null
        passwordError = null
        generalMessage = null

        var isValid = true

        if (email.trim().isEmpty()) {
            emailError = "Ingresa tu correo."
            isValid = false
        }

        if (password.isEmpty()) {
            passwordError = "Ingresa tu contraseña."
            isValid = false
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(70.dp))

        RecetarioLogo()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Iniciar Sesión",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(34.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.widthIn(max = 360.dp)
        ) {
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
        }

        TextButton(
            onClick = onRecoverPasswordClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = Color.Black
            )
        }

        TextButton(
            onClick = {
                generalMessage = null

                if (!userState.hasAnyRegisteredUser) {
                    generalMessage = "Primero debes crear una cuenta."
                    return@TextButton
                }

                if (!userState.biometricEnabled) {
                    generalMessage = "La huella digital todavía no está vinculada a tu cuenta."
                    return@TextButton
                }

                val activity = context.findFragmentActivity()

                if (activity == null) {
                    generalMessage = "No se pudo iniciar la autenticación con huella digital."
                    return@TextButton
                }

                showBiometricPrompt(
                    activity = activity,
                    title = "Huella digital",
                    subtitle = "Usa tu huella digital para entrar al recetario.",
                    onSuccess = {
                        authViewModel.setSessionActive(true)
                        onLoginClick()
                    },
                    onError = { message ->
                        generalMessage = message
                    },
                    onFailed = {
                        generalMessage = "No se reconoció la huella digital. Intenta nuevamente."
                    }
                )
            }
        ) {
            Text(
                text = "Iniciar sesión con huella digital",
                color = Color.Black
            )
        }

        if (generalMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = generalMessage ?: "",
                color = Color.Red,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OrangeButton(
            text = "Iniciar Sesión",
            onClick = {
                if (validateLogin()) {
                    if (!userState.hasAnyRegisteredUser) {
                        generalMessage = "Primero debes crear una cuenta."
                    } else {
                        authViewModel.login(email, password) { result ->
                            if (!result.isValid) {
                                generalMessage = "Correo o contraseña incorrectos."
                            } else if (result.requiresBiometricSetup) {
                                onBiometricSetupRequired()
                            } else {
                                onLoginClick()
                            }
                        }
                    }
                }
            },
            modifier = Modifier.widthIn(max = 340.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomAuthText(
            normalText = "¿No tienes una cuenta?",
            actionText = "Regístrate aquí",
            onClick = onRegisterClick
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    RecetarioTheme {
        LoginScreen(
            onLoginClick = {},
            onBiometricSetupRequired = {},
            onRegisterClick = {},
            onRecoverPasswordClick = {}
        )
    }
}
