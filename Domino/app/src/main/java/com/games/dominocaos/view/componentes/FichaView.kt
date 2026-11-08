package com.games.dominocaos.view.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.games.dominocaos.model.Ficha

private fun puntos(valor: Int): List<Pair<Int, Int>> = when (valor) {
    1 -> listOf(1 to 1)
    2 -> listOf(0 to 0, 2 to 2)
    3 -> listOf(0 to 0, 1 to 1, 2 to 2)
    4 -> listOf(0 to 0, 2 to 0, 0 to 2, 2 to 2)
    5 -> listOf(0 to 0, 2 to 0, 1 to 1, 0 to 2, 2 to 2)
    6 -> listOf(0 to 0, 0 to 1, 0 to 2, 2 to 0, 2 to 1, 2 to 2)
    else -> emptyList()
}

@Composable
fun FichaView(
    ficha: Ficha?,
    modifier: Modifier = Modifier,
    seleccionada: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val fondo = if (ficha == null) Gameboy.Oscuro else if (seleccionada) Gameboy.Resaltado else Gameboy.Ficha
    var base = modifier
        .size(width = 64.dp, height = 34.dp)
        .background(fondo)
        .border(if (seleccionada) 4.dp else 2.dp, Gameboy.Oscuro)
    if (onClick != null) base = base.clickable(onClick = onClick)

    Canvas(modifier = base) {
        if (ficha == null) return@Canvas
        val mitad = size.width / 2f
        drawLine(Gameboy.Oscuro, Offset(mitad, 5f), Offset(mitad, size.height - 5f), strokeWidth = 3f)
        dibujarMitad(ficha.izquierdo, 0f, mitad)
        dibujarMitad(ficha.derecho, mitad, mitad)
    }
}

private fun DrawScope.dibujarMitad(valor: Int, inicioX: Float, ancho: Float) {
    puntos(valor).forEach { (col, fila) ->
        val x = inicioX + ancho * (0.2f + 0.3f * col)
        val y = size.height * (0.2f + 0.3f * fila)
        drawCircle(Gameboy.Oscuro, radius = size.height * 0.09f, center = Offset(x, y))
    }
}
