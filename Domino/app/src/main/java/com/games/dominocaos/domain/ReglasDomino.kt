package com.games.dominocaos.domain

import com.games.dominocaos.model.Lado
import com.games.dominocaos.model.Ficha

object ReglasDomino {
    fun orientar(
        ficha: Ficha,
        lado: Lado,
        tablero: List<Ficha>,
        doblesLibres: Boolean = false
    ): Ficha? {
        if (doblesLibres && ficha.esDoble()) return ficha
        return when (lado) {
            Lado.IZQUIERDO -> {
                val extremo = tablero.first().izquierdo
                when {
                    ficha.derecho == extremo -> ficha
                    ficha.izquierdo == extremo -> ficha.voltear()
                    else -> null
                }
            }
            Lado.DERECHO -> {
                val extremo = tablero.last().derecho
                when {
                    ficha.izquierdo == extremo -> ficha
                    ficha.derecho == extremo -> ficha.voltear()
                    else -> null
                }
            }
        }
    }

    fun puedeColocar(ficha: Ficha, tablero: List<Ficha>, doblesLibres: Boolean = false): Boolean =
        tablero.isEmpty() || Lado.entries.any { orientar(ficha, it, tablero, doblesLibres) != null }

    fun tieneJugada(mano: List<Ficha>, tablero: List<Ficha>, doblesLibres: Boolean = false): Boolean =
        mano.any { puedeColocar(it, tablero, doblesLibres) }

    fun ladosValidos(ficha: Ficha, tablero: List<Ficha>, doblesLibres: Boolean = false): List<Lado> =
        if (tablero.isEmpty()) listOf(Lado.DERECHO)
        else Lado.entries.filter { orientar(ficha, it, tablero, doblesLibres) != null }
}
