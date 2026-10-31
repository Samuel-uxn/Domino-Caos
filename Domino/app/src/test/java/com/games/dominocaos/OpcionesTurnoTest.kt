package com.games.dominocaos

import com.games.dominocaos.domain.OpcionesTurno
import com.games.dominocaos.model.EstadoPartida
import com.games.dominocaos.model.FasePartida
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.JugadorEstado
import com.games.dominocaos.model.Lado
import com.games.dominocaos.model.Poder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpcionesTurnoTest {

    private fun estado(manoA: List<Ficha>, tablero: List<Ficha>, pozo: List<Ficha> = emptyList()) = EstadoPartida(
        idSala = "T", fase = FasePartida.JUGANDO,
        jugadores = listOf(JugadorEstado("a", "A", manoA), JugadorEstado("b", "B", listOf(Ficha(6, 6)))),
        tablero = tablero, pozo = pozo, turnoActualId = "a"
    )

    @Test
    fun fuera_de_turno_no_hay_opciones() {
        val o = OpcionesTurno.calcular(estado(listOf(Ficha(1, 3)), listOf(Ficha(1, 3))), "b", Ficha(6, 6), null)
        assertFalse(o.esMiTurno)
        assertTrue(o.lados.isEmpty())
    }

    @Test
    fun lados_habilitados_segun_donde_encaja_la_ficha() {
        val o = OpcionesTurno.calcular(estado(listOf(Ficha(1, 5)), listOf(Ficha(1, 3))), "a", Ficha(1, 5), null)
        assertEquals(listOf(Lado.IZQUIERDO), o.lados)
    }

    @Test
    fun robar_solo_si_no_hay_jugada_y_hay_pozo() {
        val sinJugada = estado(listOf(Ficha(0, 0)), listOf(Ficha(1, 3)), pozo = listOf(Ficha(2, 2)))
        val o = OpcionesTurno.calcular(sinJugada, "a", null, null)
        assertTrue(o.puedeRobar)
        assertFalse(o.puedePasar)
    }

    @Test
    fun pasar_solo_si_no_hay_jugada_ni_pozo() {
        val o = OpcionesTurno.calcular(estado(listOf(Ficha(0, 0)), listOf(Ficha(1, 3))), "a", null, null)
        assertTrue(o.puedePasar)
        assertFalse(o.puedeRobar)
    }

    @Test
    fun poder_sin_usos_deshabilita_todos_los_lados() {
        val sinUsos = JugadorEstado("a", "A", listOf(Ficha(1, 5)), usosRestantes = mapOf(Poder.SALTO to 0))
        val e = estado(listOf(Ficha(1, 5)), listOf(Ficha(1, 3))).copy(
            jugadores = listOf(sinUsos, JugadorEstado("b", "B", listOf(Ficha(6, 6))))
        )
        val o = OpcionesTurno.calcular(e, "a", Ficha(1, 5), Poder.SALTO)
        assertTrue(o.lados.isEmpty())
    }
}
