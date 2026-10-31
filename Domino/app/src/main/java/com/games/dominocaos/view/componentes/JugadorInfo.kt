package com.games.dominocaos.view.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.games.dominocaos.model.JugadorEstado

/** Tarjeta con nombre, puntaje, fichas y poderes usados de un jugador. Resalta al del turno. */
@Composable
fun JugadorInfo(
    jugador: JugadorEstado,
    esTurno: Boolean,
    esYo: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(if (esTurno) Gameboy.Boton else Gameboy.Fondo)
            .border(if (esTurno) 4.dp else 2.dp, Gameboy.Oscuro)
            .padding(6.dp)
    ) {
        val marca = if (esTurno) "> " else ""
        val yo = if (esYo) " (TÚ)" else ""
        TextoGameboy("$marca${jugador.nombre}$yo", tamano = 12)
        TextoGameboy("PTS: ${jugador.puntaje}", tamano = 12)
        TextoGameboy("FICHAS: ${jugador.fichas.size}", tamano = 11, negrita = false)
        TextoGameboy(
            if (jugador.abandono) "ABANDONÓ" else "PODERES: ${jugador.poderesUsados}",
            tamano = 11,
            negrita = false
        )
    }
}
