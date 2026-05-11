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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetario.data.findFragmentActivity
import com.example.recetario.data.showBiometricPrompt
import com.example.recetario.ui.theme.RecetarioTheme
import com.example.recetario.viewmodel.AuthViewModel

@Composable
fun BiometricSetupScreen(
    authViewModel: AuthViewModel = viewModel(),
    onAcceptClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val context = LocalContext.current

    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(110.dp))

        RecetarioLogo()

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Acceso seguro y rápido",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Puedes vincular la huella digital de tu teléfono para acceder al recetario de forma más rápida en el futuro.",
            fontSize = 18.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        if (message != null) {
            Text(
                text = message ?: "",
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        OrangeButton(
            text = "Vincular huella digital",
            onClick = {
                message = null

                val activity = context.findFragmentActivity()

                if (activity == null) {
                    message = "No se pudo iniciar la autenticación con huella digital."
                    return@OrangeButton
                }

                showBiometricPrompt(
                    activity = activity,
                    title = "Vincular huella digital",
                    subtitle = "Confirma tu huella digital para activar el acceso.",
                    onSuccess = {
                        authViewModel.setBiometricEnabled(true)
                        authViewModel.setSessionActive(true)
                        onAcceptClick()
                    },
                    onError = { error ->
                        message = error
                    },
                    onFailed = {
                        message = "No se reconoció la huella digital. Intenta nuevamente."
                    }
                )
            },
            modifier = Modifier.widthIn(max = 320.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                authViewModel.setBiometricEnabled(false)
                authViewModel.setSessionActive(true)
                onSkipClick()
            }
        ) {
            Text(
                text = "Ahora no",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BiometricSetupScreenPreview() {
    RecetarioTheme {
        BiometricSetupScreen(
            onAcceptClick = {},
            onSkipClick = {}
        )
    }
}
