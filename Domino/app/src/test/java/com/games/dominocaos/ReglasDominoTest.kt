package com.games.dominocaos

import com.games.dominocaos.domain.ReglasDomino
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.Lado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasDominoTest {

    private val tablero = listOf(Ficha(2, 3), Ficha(3, 5))

    @Test
    fun ficha_que_encaja_por_la_derecha_no_se_voltea() {
        assertEquals(Ficha(5, 1), ReglasDomino.orientar(Ficha(5, 1), Lado.DERECHO, tablero))
    }

    @Test
    fun ficha_se_voltea_para_encajar_por_la_derecha() {
        assertEquals(Ficha(5, 6), ReglasDomino.orientar(Ficha(6, 5), Lado.DERECHO, tablero))
    }

    @Test
    fun ficha_encaja_por_la_izquierda_con_su_lado_derecho() {
        assertEquals(Ficha(4, 2), ReglasDomino.orientar(Ficha(4, 2), Lado.IZQUIERDO, tablero))
    }

    @Test
    fun ficha_se_voltea_para_encajar_por_la_izquierda() {
        assertEquals(Ficha(4, 2), ReglasDomino.orientar(Ficha(2, 4), Lado.IZQUIERDO, tablero))
    }

    @Test
    fun ficha_que_no_encaja_devuelve_null() {
        assertNull(ReglasDomino.orientar(Ficha(0, 1), Lado.DERECHO, tablero))
    }

    @Test
    fun dobles_libres_permiten_colocar_cualquier_doble() {
        assertNull(ReglasDomino.orientar(Ficha(4, 4), Lado.DERECHO, tablero))
        assertEquals(Ficha(4, 4), ReglasDomino.orientar(Ficha(4, 4), Lado.DERECHO, tablero, doblesLibres = true))
    }

    @Test
    fun con_tablero_vacio_cualquier_ficha_se_puede_colocar() {
        assertTrue(ReglasDomino.puedeColocar(Ficha(6, 0), emptyList()))
    }

    @Test
    fun tiene_jugada_detecta_si_alguna_ficha_encaja() {
        assertTrue(ReglasDomino.tieneJugada(listOf(Ficha(0, 1), Ficha(2, 6)), tablero))
        assertFalse(ReglasDomino.tieneJugada(listOf(Ficha(0, 1), Ficha(4, 6)), tablero))
    }

    @Test
    fun lados_validos_lista_solo_los_extremos_donde_encaja() {
        assertEquals(listOf(Lado.IZQUIERDO), ReglasDomino.ladosValidos(Ficha(2, 0), tablero))
        assertEquals(listOf(Lado.IZQUIERDO, Lado.DERECHO), ReglasDomino.ladosValidos(Ficha(2, 5), tablero))
        assertEquals(listOf(Lado.DERECHO), ReglasDomino.ladosValidos(Ficha(1, 1), emptyList()))
    }
}
