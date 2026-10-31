package com.games.dominocaos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuPrincipal(
    jugadorLogueado: String,
    onCrearSalaClick: () -> Unit,
    onUnirseSalaClick: (String) -> Unit
) {
    var codigoSala by remember { mutableStateOf("") }

    val gameboyFondo = Color(0xFF9BBC0F)
    val gameboyOscuro = Color(0xFF0F380F)
    val gameboyBoton = Color(0xFF8BAC0F)

    Column(
        modifier = Modifier.fillMaxSize().background(gameboyFondo).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().border(3.dp, gameboyOscuro, RectangleShape).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("JUGADOR:\n$jugadorLogueado", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = gameboyOscuro)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onCrearSalaClick,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = gameboyBoton),
            modifier = Modifier.fillMaxWidth().height(60.dp).border(3.dp, gameboyOscuro, RectangleShape)
        ) {
            Text("> NUEVA PARTIDA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = gameboyOscuro)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("--- O INGRESA CÓDIGO ---", fontFamily = FontFamily.Monospace, color = gameboyOscuro, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = codigoSala, onValueChange = { codigoSala = it.uppercase() },
            placeholder = { Text("CÓDIGO (EJ: A7K3)", fontFamily = FontFamily.Monospace, color = gameboyOscuro) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().border(3.dp, gameboyOscuro, RectangleShape),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { if (codigoSala.isNotBlank()) onUnirseSalaClick(codigoSala) },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = gameboyBoton),
            modifier = Modifier.fillMaxWidth().height(60.dp).border(3.dp, gameboyOscuro, RectangleShape)
        ) {
            Text("> UNIRSE A PARTIDA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = gameboyOscuro)
        }
    }
}