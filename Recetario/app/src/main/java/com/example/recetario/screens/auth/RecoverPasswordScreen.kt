package com.example.recetario.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetario.ui.theme.RecetarioTheme

@Composable
fun RecoverPasswordScreen(
    onSendInstructionsClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(66.dp))

        RecetarioLogo()

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Recuperar Contraseña",
            fontSize = 24.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Ingresa tu correo electrónico y te enviaremos instrucciones para restablecer tu contraseña",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Correo Electrónico",
            keyboardType = KeyboardType.Email,
            modifier = Modifier.widthIn(max = 320.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        OrangeButton(
            text = "Enviar Instrucciones",
            onClick = onSendInstructionsClick,
            modifier = Modifier.widthIn(max = 340.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomAuthText(
            normalText = "¿Ya tienes una cuenta?",
            actionText = "Inicia sesión aquí",
            onClick = onLoginClick
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecoverPasswordScreenPreview() {
    RecetarioTheme {
        RecoverPasswordScreen(
            onSendInstructionsClick = {},
            onLoginClick = {}
        )
    }
}
