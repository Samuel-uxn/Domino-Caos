package com.games.dominocaos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.games.dominocaos.model.MotivoFin
import com.games.dominocaos.view.componentes.BotonGameboy
import com.games.dominocaos.view.componentes.Gameboy
import com.games.dominocaos.view.componentes.TextoGameboy
import com.games.dominocaos.viewmodel.PartidaViewModel

/** Resultado final: ganador, motivo del fin y tabla de puntajes. */
@Composable
fun FinPartida(partidaViewModel: PartidaViewModel) {
    val estado by partidaViewModel.estado.collectAsState()
    val partida = estado ?: return
    val miId = partidaViewModel.miId
    val ganador = partida.jugador(partida.ganadorId.orEmpty())
    val soyCreador = partida.activos.firstOrNull()?.id == miId

    val motivo = when (partida.motivoFin) {
        MotivoFin.DOMINO -> "Alguien se quedó sin fichas"
        MotivoFin.BLOQUEO -> "Juego bloqueado"
        MotivoFin.RONDAS -> "Se alcanzó el máximo de rondas"
        MotivoFin.ABANDONO -> "Los demás jugadores abandonaron"
        null -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gameboy.Fondo)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextoGameboy("FIN DE LA PARTIDA", tamano = 24)
        TextoGameboy(
            when {
                ganador == null -> "EMPATE"
                ganador.id == miId -> "¡GANASTE!"
                else -> "GANA ${ganador.nombre}"
            },
            tamano = 28
        )
        TextoGameboy(motivo, tamano = 12, negrita = false)

        Column(
            modifier = Modifier.fillMaxWidth().border(3.dp, Gameboy.Oscuro).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextoGameboy("PUNTAJES")
            partida.jugadores.sortedByDescending { it.puntaje }.forEach { j ->
                val extra = if (j.abandono) " (abandonó)" else ""
                TextoGameboy("${j.nombre}: ${j.puntaje} pts, ${j.poderesUsados} poderes$extra", tamano = 12, negrita = false)
            }
        }

        if (soyCreador) {
            BotonGameboy("> JUGAR DE NUEVO", { partidaViewModel.reiniciar() }, modifier = Modifier.fillMaxWidth())
        } else {
            TextoGameboy("Esperando que el creador reinicie...", tamano = 12, negrita = false)
        }
        BotonGameboy("> VOLVER AL MENÚ", { partidaViewModel.abandonar() }, modifier = Modifier.fillMaxWidth())
    }
}
