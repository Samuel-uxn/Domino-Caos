package com.games.dominocaos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.games.dominocaos.domain.ReglasConfig
import com.games.dominocaos.view.componentes.BotonGameboy
import com.games.dominocaos.view.componentes.Gameboy
import com.games.dominocaos.view.componentes.TextoGameboy
import com.games.dominocaos.viewmodel.PartidaViewModel

/** Sala de espera: muestra el código para compartir y los jugadores que ya entraron. */
@Composable
fun SalaEspera(partidaViewModel: PartidaViewModel) {
    val estado by partidaViewModel.estado.collectAsState()
    val sala = estado ?: return
    val soyCreador = sala.activos.firstOrNull()?.id == partidaViewModel.miId
    val puedeIniciar = soyCreador && sala.activos.size >= ReglasConfig.MIN_JUGADORES

    Column(
        modifier = Modifier.fillMaxSize().background(Gameboy.Fondo).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextoGameboy("SALA DE ESPERA", tamano = 24)
        TextoGameboy("CÓDIGO (compártelo):")
        TextoGameboy(sala.idSala, tamano = 40)

        Column(
            modifier = Modifier.fillMaxWidth().border(3.dp, Gameboy.Oscuro).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextoGameboy("JUGADORES (${sala.activos.size}/${ReglasConfig.MAX_JUGADORES})")
            sala.activos.forEachIndexed { i, jugador ->
                val etiqueta = if (i == 0) " [CREADOR]" else ""
                val yo = if (jugador.id == partidaViewModel.miId) " (TÚ)" else ""
                TextoGameboy("${i + 1}. ${jugador.nombre}$etiqueta$yo", negrita = false)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (soyCreador) {
            if (!puedeIniciar) {
                TextoGameboy("Esperando al menos ${ReglasConfig.MIN_JUGADORES} jugadores...", tamano = 12, negrita = false)
            }
            BotonGameboy(
                texto = "> INICIAR PARTIDA",
                onClick = { partidaViewModel.iniciar() },
                habilitado = puedeIniciar,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            TextoGameboy("Esperando que el creador inicie la partida...", tamano = 12, negrita = false)
        }
        BotonGameboy(
            texto = "> SALIR DE LA SALA",
            onClick = { partidaViewModel.abandonar() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
