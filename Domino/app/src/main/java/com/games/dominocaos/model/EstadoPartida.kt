package com.games.dominocaos.model

import com.games.dominocaos.domain.ReglasConfig

enum class FasePartida { ESPERANDO, JUGANDO, FINALIZADA }

enum class MotivoFin { DOMINO, BLOQUEO, RONDAS, ABANDONO }

data class JugadorEstado(
    val id: String,
    val nombre: String,
    val fichas: List<Ficha> = emptyList(),
    val puntaje: Int = 0,
    val usosRestantes: Map<Poder, Int> = ReglasConfig.usosIniciales(),
    val poderesUsados: Int = 0,
    val abandono: Boolean = false
) {
    fun usosDe(poder: Poder): Int = usosRestantes[poder] ?: 0
    val puntosEnMano: Int get() = fichas.sumOf { it.izquierdo + it.derecho }
}

/**
 * Estado completo e inmutable de una partida. El motor siempre devuelve una copia nueva,
 * así que se puede exponer tal cual en un StateFlow.
 *
 * tablero: de izquierda a derecha. Extremo izquierdo = tablero.first().izquierdo,
 * extremo derecho = tablero.last().derecho.
 */
data class EstadoPartida(
    val idSala: String,
    val fase: FasePartida = FasePartida.ESPERANDO,
    val jugadores: List<JugadorEstado> = emptyList(),
    val tablero: List<Ficha> = emptyList(),
    val pozo: List<Ficha> = emptyList(),
    val turnoActualId: String = "",
    val saltarSiguiente: Boolean = false,
    val turnoExtra: Boolean = false,
    val ronda: Int = 1,
    val maxRondas: Int = ReglasConfig.MAX_RONDAS,
    val turnosEnRonda: Int = 0,
    val eventoActivo: EventoCaos? = null,
    val turnosEventoRestantes: Int = 0,
    val ultimoMensaje: String = "",
    val ganadorId: String? = null,              // null + FINALIZADA = empate
    val motivoFin: MotivoFin? = null
) {
    fun jugador(id: String): JugadorEstado? = jugadores.firstOrNull { it.id == id }
    val jugadorActual: JugadorEstado? get() = jugador(turnoActualId)
    val activos: List<JugadorEstado> get() = jugadores.filterNot { it.abandono }
}
