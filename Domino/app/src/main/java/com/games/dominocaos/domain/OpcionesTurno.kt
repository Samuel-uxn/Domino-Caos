package com.games.dominocaos.domain

import com.games.dominocaos.model.EstadoPartida
import com.games.dominocaos.model.EventoCaos
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.FasePartida
import com.games.dominocaos.model.Lado
import com.games.dominocaos.model.Poder

/**
 * Qué puede hacer un jugador en este momento. La interfaz lo usa para habilitar o
 * deshabilitar botones sin duplicar las reglas del juego.
 */
data class OpcionesTurno(
    val esMiTurno: Boolean,
    val lados: List<Lado>,
    val puedeRobar: Boolean,
    val puedePasar: Boolean
) {
    companion object {
        private val SIN_OPCIONES = OpcionesTurno(false, emptyList(), false, false)

        fun calcular(estado: EstadoPartida, jugadorId: String, seleccion: Ficha?, poder: Poder?): OpcionesTurno {
            val yo = estado.jugador(jugadorId) ?: return SIN_OPCIONES
            val miTurno = estado.fase == FasePartida.JUGANDO && !yo.abandono && estado.turnoActualId == jugadorId
            if (!miTurno) return SIN_OPCIONES

            val doblesLibres = estado.eventoActivo == EventoCaos.DOBLES_LIBRES
            val hayJugada = ReglasDomino.tieneJugada(yo.fichas, estado.tablero, doblesLibres)
            val poderSinUsos = poder != null && yo.usosDe(poder) <= 0

            val lados = when {
                seleccion == null || poderSinUsos -> emptyList()
                else -> ReglasDomino.ladosValidos(seleccion, estado.tablero, doblesLibres)
            }
            return OpcionesTurno(
                esMiTurno = true,
                lados = lados,
                puedeRobar = estado.pozo.isNotEmpty() && !hayJugada,
                puedePasar = estado.pozo.isEmpty() && !hayJugada
            )
        }
    }
}
