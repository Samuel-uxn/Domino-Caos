package com.games.dominocaos

import com.games.dominocaos.domain.Puntuacion
import com.games.dominocaos.domain.ReglasConfig
import com.games.dominocaos.model.EventoCaos
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.JugadorEstado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PuntuacionTest {

    private fun jugador(id: String, puntaje: Int, poderes: Int = 0, abandono: Boolean = false) =
        JugadorEstado(id = id, nombre = id, puntaje = puntaje, poderesUsados = poderes, abandono = abandono)

    @Test
    fun ficha_normal_suma_sus_dos_lados() {
        assertEquals(7, Puntuacion.puntosPorColocar(Ficha(3, 4), null))
    }

    @Test
    fun doble_suma_bonus() {
        assertEquals(6 + ReglasConfig.BONUS_DOBLE, Puntuacion.puntosPorColocar(Ficha(3, 3), null))
    }

    @Test
    fun puntos_dobles_duplica() {
        assertEquals(14, Puntuacion.puntosPorColocar(Ficha(3, 4), EventoCaos.PUNTOS_DOBLES))
    }

    @Test
    fun sequia_no_da_puntos() {
        assertEquals(0, Puntuacion.puntosPorColocar(Ficha(6, 6), EventoCaos.SIN_PUNTOS))
    }

    @Test
    fun otros_eventos_no_cambian_los_puntos() {
        assertEquals(7, Puntuacion.puntosPorColocar(Ficha(3, 4), EventoCaos.TURNO_EXTRA))
    }

    @Test
    fun bono_domino_suma_las_manos_de_los_rivales() {
        val rivales = listOf(
            JugadorEstado("a", "a", fichas = listOf(Ficha(1, 2), Ficha(6, 6))),
            JugadorEstado("b", "b", fichas = listOf(Ficha(0, 3)))
        )
        assertEquals(3 + 12 + 3, Puntuacion.bonoDomino(rivales))
    }

    @Test
    fun gana_el_mayor_puntaje() {
        assertEquals("b", Puntuacion.determinarGanador(listOf(jugador("a", 10), jugador("b", 20))))
    }

    @Test
    fun en_empate_gana_quien_uso_menos_poderes() {
        val ganador = Puntuacion.determinarGanador(listOf(jugador("a", 10, poderes = 3), jugador("b", 10, poderes = 1)))
        assertEquals("b", ganador)
    }

    @Test
    fun empate_total_devuelve_null() {
        assertNull(Puntuacion.determinarGanador(listOf(jugador("a", 10, 1), jugador("b", 10, 1))))
    }

    @Test
    fun quien_abandona_no_puede_ganar() {
        val ganador = Puntuacion.determinarGanador(listOf(jugador("a", 99, abandono = true), jugador("b", 5)))
        assertEquals("b", ganador)
    }
}
