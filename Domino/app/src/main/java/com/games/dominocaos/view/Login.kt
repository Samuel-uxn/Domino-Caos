package com.games.dominocaos.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.games.dominocaos.viewmodel.AuthViewModel // Importamos tu nuevo cerebro

@Composable
fun Login(
    authViewModel: AuthViewModel, // Recibe el cerebro por parámetro
    onLoginExitoso: (String) -> Unit,
    onIrARegistro: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current // Para mostrar mensajitos de error emergentes

    val gameboyFondo = Color(0xFF9BBC0F)
    val gameboyOscuro = Color(0xFF0F380F)
    val gameboyBoton = Color(0xFF8BAC0F)

    Column(
        modifier = Modifier.fillMaxSize().background(gameboyFondo).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DOMINÓ CAOS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = gameboyOscuro, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("CORREO ELECTRÓNICO", fontFamily = FontFamily.Monospace, color = gameboyOscuro) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().border(3.dp, gameboyOscuro, RectangleShape),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("CONTRASEÑA", fontFamily = FontFamily.Monospace, color = gameboyOscuro) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().border(3.dp, gameboyOscuro, RectangleShape),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (correo.isNotBlank() && password.isNotBlank()) {
                    // Llamamos a Firebase para verificar
                    authViewModel.iniciarSesion(correo, password) { exito, mensaje ->
                        if (exito) {
                            onLoginExitoso(correo.substringBefore("@")) // Usa la primera parte del correo como nombre
                        } else {
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = gameboyBoton),
            modifier = Modifier.fillMaxWidth().height(50.dp).border(3.dp, gameboyOscuro, RectangleShape)
        ) {
            Text("> INICIAR SESIÓN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = gameboyOscuro)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onIrARegistro,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = gameboyFondo),
            modifier = Modifier.fillMaxWidth().height(50.dp).border(3.dp, gameboyOscuro, RectangleShape)
        ) {
            Text("> CREAR CUENTA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = gameboyOscuro)
        }
    }
}