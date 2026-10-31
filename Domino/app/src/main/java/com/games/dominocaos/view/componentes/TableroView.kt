package com.games.dominocaos.view.componentes

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.games.dominocaos.model.Ficha

/** Fichas colocadas en la mesa, de izquierda a derecha, con los dos extremos abiertos. */
@Composable
fun TableroView(tablero: List<Ficha>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(3.dp, Gameboy.Oscuro)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (tablero.isEmpty()) {
            TextoGameboy("MESA VACÍA: PON CUALQUIER FICHA")
        } else {
            TextoGameboy("EXTREMOS  IZQ: ${tablero.first().izquierdo}   DER: ${tablero.last().derecho}", tamano = 12)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tablero.forEach { FichaView(it) }
            }
        }
    }
}
