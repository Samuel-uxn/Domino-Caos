package com.games.dominocaos.domain

import com.games.dominocaos.model.Poder

object ReglasConfig {
    const val FICHAS_INICIALES = 7
    const val MIN_JUGADORES = 2
    const val MAX_JUGADORES = 4
    const val MAX_RONDAS = 20
    const val PROBABILIDAD_EVENTO = 0.25
    const val BONUS_DOBLE = 5
    const val USOS_POR_PODER = 2

    fun usosIniciales(): Map<Poder, Int> = Poder.entries.associateWith { USOS_POR_PODER }
}
