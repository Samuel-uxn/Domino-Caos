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
import com.games.dominocaos.viewmodel.AuthViewModel

@Composable
fun Registro(
    authViewModel: AuthViewModel,
    onRegistroExitoso: (String) -> Unit,
    onVolver: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val gameboyFondo = Color(0xFF9BBC0F)
    val gameboyOscuro = Color(0xFF0F380F)
    val gameboyBoton = Color(0xFF8BAC0F)

    Column(
        modifier = Modifier.fillMaxSize().background(gameboyFondo).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("CREAR CUENTA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = gameboyOscuro, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(32.dp))

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
            label = { Text("CONTRASEÑA (Min 6 letras)", fontFamily = FontFamily.Monospace, color = gameboyOscuro) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().border(3.dp, gameboyOscuro, RectangleShape),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (correo.isNotBlank() && password.length >= 6) {
                    authViewModel.registrarUsuario(correo, password) { exito, mensaje ->
                        if (exito) {
                            onRegistroExitoso(correo.substringBefore("@"))
                        } else {
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (password.length < 6) {
                    Toast.makeText(context, "La contraseña debe tener 6 caracteres", Toast.LENGTH_SHORT).show()
                }
            },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = gameboyBoton),
            modifier = Modifier.fillMaxWidth().height(50.dp).border(3.dp, gameboyOscuro, RectangleShape)
        ) {
            Text("> REGISTRARSE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = gameboyOscuro)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onVolver) {
            Text("VOLVER AL LOGIN", fontFamily = FontFamily.Monospace, color = gameboyOscuro)
        }
    }
}