package com.example.recetario.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetario.data.AuthRepository
import com.example.recetario.data.findFragmentActivity
import com.example.recetario.data.showBiometricPrompt
import com.example.recetario.ui.theme.RecetarioTheme

@Composable
fun ReturningLoginScreen(
    onLoginClick: () -> Unit,
    onDifferentUserClick: () -> Unit,
    onRecoverPasswordClick: () -> Unit
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalMessage by remember { mutableStateOf<String?>(null) }

    val fullName = authRepository.getFullName()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(74.dp))

        RecetarioLogo()

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Hola, $fullName",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        TextButton(
            onClick = onDifferentUserClick
        ) {
            Text(
                text = "¿No eres $fullName?",
                color = Color.Black,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Contraseña",
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            errorMessage = passwordError,
            modifier = Modifier.widthIn(max = 360.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

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

                if (!authRepository.isBiometricEnabled()) {
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
                        authRepository.setSessionActive(true)
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
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OrangeButton(
            text = "Iniciar Sesión",
            onClick = {
                passwordError = null
                generalMessage = null

                if (password.isEmpty()) {
                    passwordError = "Ingresa tu contraseña."
                    return@OrangeButton
                }

                if (authRepository.loginWithSavedUser(password)) {
                    onLoginClick()
                } else {
                    generalMessage = "Contraseña incorrecta."
                }
            },
            modifier = Modifier.widthIn(max = 340.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReturningLoginScreenPreview() {
    RecetarioTheme {
        ReturningLoginScreen(
            onLoginClick = {},
            onDifferentUserClick = {},
            onRecoverPasswordClick = {}
        )
    }
}
