package com.games.dominocaos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.games.dominocaos.domain.OpcionesTurno
import com.games.dominocaos.view.componentes.Controles
import com.games.dominocaos.view.componentes.FichaView
import com.games.dominocaos.view.componentes.Gameboy
import com.games.dominocaos.view.componentes.JugadorInfo
import com.games.dominocaos.view.componentes.PanelPoderes
import com.games.dominocaos.view.componentes.TableroView
import com.games.dominocaos.view.componentes.TextoGameboy
import com.games.dominocaos.viewmodel.PartidaViewModel

/** Pantalla de la partida en curso. Solo dibuja el estado y reenvía los toques al ViewModel. */
@Composable
fun Juego(partidaViewModel: PartidaViewModel) {
    val estado by partidaViewModel.estado.collectAsState()
    val seleccion by partidaViewModel.seleccion.collectAsState()
    val poderArmado by partidaViewModel.poderArmado.collectAsState()
    var confirmarSalida by remember { mutableStateOf(false) }

    val partida = estado ?: return
    val miId = partidaViewModel.miId
    val yo = partida.jugador(miId) ?: return
    val opciones = OpcionesTurno.calcular(partida, miId, seleccion, poderArmado)
    val turnoDe = partida.jugadorActual?.nombre.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gameboy.Fondo)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextoGameboy("SALA ${partida.idSala}  RONDA ${partida.ronda}/${partida.maxRondas}", tamano = 12)
        TextoGameboy(
            if (opciones.esMiTurno) "ES TU TURNO" else "TURNO DE: $turnoDe",
            tamano = 20
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            partida.jugadores.forEach { jugador ->
                JugadorInfo(
                    jugador = jugador,
                    esTurno = jugador.id == partida.turnoActualId,
                    esYo = jugador.id == miId,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        partida.eventoActivo?.let { evento ->
            Column(Modifier.fillMaxWidth().border(3.dp, Gameboy.Oscuro).background(Gameboy.Resaltado).padding(8.dp)) {
                TextoGameboy("EVENTO: ${evento.titulo} (${partida.turnosEventoRestantes} turnos)", tamano = 12)
                TextoGameboy(evento.descripcion, tamano = 11, negrita = false)
            }
        }
        if (partida.ultimoMensaje.isNotBlank()) {
            TextoGameboy(partida.ultimoMensaje, tamano = 12, negrita = false)
        }

        TableroView(partida.tablero)
        TextoGameboy("POZO: ${partida.pozo.size} fichas", tamano = 12)

        PanelPoderes(
            jugador = yo,
            poderArmado = poderArmado,
            esMiTurno = opciones.esMiTurno,
            onPoder = { partidaViewModel.armarPoder(it) }
        )

        TextoGameboy("TUS FICHAS (toca una para elegirla)", tamano = 11)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            yo.fichas.forEach { ficha ->
                FichaView(
                    ficha = ficha,
                    seleccionada = ficha == seleccion,
                    onClick = if (opciones.esMiTurno) ({ partidaViewModel.seleccionar(ficha) }) else null
                )
            }
        }

        Controles(
            opciones = opciones,
            onLado = { partidaViewModel.jugarFicha(it) },
            onRobar = { partidaViewModel.robar() },
            onPasar = { partidaViewModel.pasar() },
            onAbandonar = { confirmarSalida = true }
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    partidaViewModel.abandonar()
                }) { TextoGameboy("SÍ, ABANDONAR") }
            },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { TextoGameboy("CANCELAR") } },
            title = { TextoGameboy("ABANDONAR PARTIDA", tamano = 18) },
            text = { TextoGameboy("Perderás la partida. ¿Seguro?", tamano = 12, negrita = false) }
        )
    }
}
