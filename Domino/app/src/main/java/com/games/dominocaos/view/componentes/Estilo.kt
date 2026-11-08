package com.games.dominocaos.view.componentes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Paleta estilo Game Boy que ya usan las pantallas de login, registro y menú. */
object Gameboy {
    val Fondo = Color(0xFF9BBC0F)
    val Oscuro = Color(0xFF0F380F)
    val Boton = Color(0xFF8BAC0F)
    val Resaltado = Color(0xFFCADC9F)
    val Ficha = Color(0xFFE8F0C8)
}

@Composable
fun TextoGameboy(
    texto: String,
    modifier: Modifier = Modifier,
    tamano: Int = 14,
    negrita: Boolean = true
) {
    Text(
        text = texto,
        modifier = modifier,
        fontFamily = FontFamily.Monospace,
        fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
        fontSize = tamano.sp,
        color = Gameboy.Oscuro
    )
}
@Composable
fun BotonGameboy(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    marcado: Boolean = false,
    tamano: Int = 14
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        shape = RectangleShape,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (marcado) Gameboy.Oscuro else Gameboy.Boton,
            contentColor = if (marcado) Gameboy.Boton else Gameboy.Oscuro,
            disabledContainerColor = Gameboy.Fondo,
            disabledContentColor = Gameboy.Oscuro.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .border(3.dp, Gameboy.Oscuro.copy(alpha = if (habilitado) 1f else 0.4f), RectangleShape)
    ) {
        Text(texto, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = tamano.sp)
    }
}
