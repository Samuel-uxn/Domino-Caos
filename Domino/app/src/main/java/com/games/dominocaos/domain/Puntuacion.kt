package com.games.dominocaos.domain

import com.games.dominocaos.model.EventoCaos
import com.games.dominocaos.model.JugadorEstado
import com.games.dominocaos.model.Ficha

object Puntuacion {
    fun puntosPorColocar(ficha: Ficha, evento: EventoCaos?): Int {
        val base = ficha.izquierdo + ficha.derecho + if (ficha.esDoble()) ReglasConfig.BONUS_DOBLE else 0
        return when (evento) {
            EventoCaos.PUNTOS_DOBLES -> base * 2
            EventoCaos.SIN_PUNTOS -> 0
            else -> base
        }
    }

    fun bonoDomino(rivales: List<JugadorEstado>): Int = rivales.sumOf { it.puntosEnMano }

    fun determinarGanador(jugadores: List<JugadorEstado>): String? {
        val candidatos = jugadores.filterNot { it.abandono }
        if (candidatos.isEmpty()) return null
        val maxPuntaje = candidatos.maxOf { it.puntaje }
        val empatados = candidatos.filter { it.puntaje == maxPuntaje }
        if (empatados.size == 1) return empatados.first().id
        val minPoderes = empatados.minOf { it.poderesUsados }
        val finalistas = empatados.filter { it.poderesUsados == minPoderes }
        return if (finalistas.size == 1) finalistas.first().id else null
    }
}
