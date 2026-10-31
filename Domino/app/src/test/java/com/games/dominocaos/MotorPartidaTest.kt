package com.games.dominocaos

import com.games.dominocaos.domain.MotorPartida
import com.games.dominocaos.model.Accion
import com.games.dominocaos.model.EstadoPartida
import com.games.dominocaos.model.EventoCaos
import com.games.dominocaos.model.FasePartida
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.JugadorEstado
import com.games.dominocaos.model.Lado
import com.games.dominocaos.model.MotivoFin
import com.games.dominocaos.model.Poder
import com.games.dominocaos.model.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MotorPartidaTest {

    /** Sin eventos aleatorios para que las pruebas sean predecibles. */
    private val motor = MotorPartida(Random(1), probabilidadEvento = 0.0)

    private fun jugador(id: String, vararg fichas: Ficha) =
        JugadorEstado(id = id, nombre = id.uppercase(), fichas = fichas.toList())

    private fun partida(
        jugadores: List<JugadorEstado>,
        tablero: List<Ficha> = emptyList(),
        pozo: List<Ficha> = emptyList(),
        turno: String = jugadores.first().id,
        maxRondas: Int = 20
    ) = EstadoPartida(
        idSala = "TEST", fase = FasePartida.JUGANDO, jugadores = jugadores, tablero = tablero,
        pozo = pozo, turnoActualId = turno, maxRondas = maxRondas
    )

    private fun exito(r: Resultado): EstadoPartida {
        assertTrue("Se esperaba éxito pero fue: $r", r is Resultado.Exito)
        return (r as Resultado.Exito).estado
    }

    private fun error(r: Resultado) = assertTrue("Se esperaba un error pero fue: $r", r is Resultado.Error)

    // ---------- sala ----------

    @Test
    fun iniciar_reparte_7_fichas_y_deja_el_resto_en_el_pozo() {
        var e = motor.crearSala("A7K3", "a", "A")
        e = exito(motor.unirse(e, "b", "B"))
        e = exito(motor.iniciar(e, "a"))
        assertEquals(FasePartida.JUGANDO, e.fase)
        e.jugadores.forEach { assertEquals(7, it.fichas.size) }
        assertEquals(14, e.pozo.size)
        assertEquals("a", e.turnoActualId)
    }

    @Test
    fun solo_el_creador_inicia_y_se_necesitan_2_jugadores() {
        var e = motor.crearSala("A7K3", "a", "A")
        error(motor.iniciar(e, "a"))
        e = exito(motor.unirse(e, "b", "B"))
        error(motor.iniciar(e, "b"))
    }

    @Test
    fun la_sala_admite_maximo_4_jugadores() {
        var e = motor.crearSala("A7K3", "a", "A")
        listOf("b", "c", "d").forEach { e = exito(motor.unirse(e, it, it.uppercase())) }
        error(motor.unirse(e, "e", "E"))
    }

    // ---------- jugar ----------

    @Test
    fun jugar_ficha_valida_actualiza_tablero_puntaje_y_turno() {
        val e = partida(
            listOf(jugador("a", Ficha(3, 4), Ficha(0, 0)), jugador("b", Ficha(5, 5))),
            tablero = listOf(Ficha(1, 3))
        )
        val n = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO)))
        assertEquals(listOf(Ficha(1, 3), Ficha(3, 4)), n.tablero)
        assertEquals(1, n.jugador("a")!!.fichas.size)
        assertEquals(7, n.jugador("a")!!.puntaje)
        assertEquals("b", n.turnoActualId)
    }

    @Test
    fun ficha_que_no_encaja_es_rechazada() {
        val e = partida(listOf(jugador("a", Ficha(6, 6), Ficha(0, 0)), jugador("b", Ficha(5, 5))), tablero = listOf(Ficha(1, 3)))
        error(motor.aplicar(e, Accion.JugarFicha("a", Ficha(6, 6), Lado.DERECHO)))
    }

    @Test
    fun no_se_puede_jugar_fuera_de_turno() {
        val e = partida(listOf(jugador("a", Ficha(1, 1), Ficha(2, 2)), jugador("b", Ficha(5, 5), Ficha(6, 6))))
        error(motor.aplicar(e, Accion.JugarFicha("b", Ficha(5, 5))))
    }

    @Test
    fun no_se_puede_jugar_una_ficha_que_no_se_tiene() {
        val e = partida(listOf(jugador("a", Ficha(1, 1), Ficha(2, 2)), jugador("b", Ficha(5, 5), Ficha(6, 6))))
        error(motor.aplicar(e, Accion.JugarFicha("a", Ficha(6, 6))))
    }

    // ---------- pozo ----------

    @Test
    fun no_se_puede_robar_si_hay_jugada() {
        val e = partida(
            listOf(jugador("a", Ficha(3, 4)), jugador("b", Ficha(5, 5))),
            tablero = listOf(Ficha(1, 3)), pozo = listOf(Ficha(0, 0))
        )
        error(motor.aplicar(e, Accion.Robar("a")))
    }

    @Test
    fun robar_sin_jugada_agrega_la_ficha_del_pozo() {
        val e = partida(
            listOf(jugador("a", Ficha(6, 6)), jugador("b", Ficha(5, 5))),
            tablero = listOf(Ficha(1, 3)), pozo = listOf(Ficha(0, 0), Ficha(2, 2))
        )
        val n = exito(motor.aplicar(e, Accion.Robar("a")))
        assertEquals(2, n.jugador("a")!!.fichas.size)
        assertEquals(1, n.pozo.size)
        assertEquals("a", n.turnoActualId)
    }

    @Test
    fun pasar_solo_se_permite_con_pozo_vacio_y_sin_jugada() {
        val conPozo = partida(
            listOf(jugador("a", Ficha(6, 6)), jugador("b", Ficha(5, 5))),
            tablero = listOf(Ficha(1, 3)), pozo = listOf(Ficha(0, 0))
        )
        error(motor.aplicar(conPozo, Accion.Pasar("a")))
    }

    // ---------- poderes ----------

    @Test
    fun poder_sin_usos_se_rechaza() {
        val sinUsos = jugador("a", Ficha(3, 4), Ficha(0, 0)).copy(usosRestantes = Poder.entries.associateWith { 0 })
        val e = partida(listOf(sinUsos, jugador("b", Ficha(5, 5))), tablero = listOf(Ficha(1, 3)))
        error(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO, Poder.SALTO)))
    }

    @Test
    fun salto_hace_que_el_siguiente_jugador_pierda_el_turno() {
        val e = partida(
            listOf(jugador("a", Ficha(3, 4), Ficha(0, 0)), jugador("b", Ficha(5, 5)), jugador("c", Ficha(6, 6))),
            tablero = listOf(Ficha(1, 3))
        )
        val n = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO, Poder.SALTO)))
        assertEquals("c", n.turnoActualId)
    }

    // ---------- eventos ----------

    @Test
    fun salto_gasta_un_uso_y_cuenta_como_poder_usado() {
        val e = partida(
            listOf(jugador("a", Ficha(3, 4), Ficha(0, 0)), jugador("b", Ficha(5, 5)), jugador("c", Ficha(6, 6))),
            tablero = listOf(Ficha(1, 3))
        )
        val n = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO, Poder.SALTO)))
        assertEquals(1, n.jugador("a")!!.usosDe(Poder.SALTO))
        assertEquals(1, n.jugador("a")!!.poderesUsados)
    }

    @Test
    fun salto_con_dos_jugadores_hace_que_el_mismo_jugador_repita() {
        val e = partida(
            listOf(jugador("a", Ficha(3, 4), Ficha(3, 3), Ficha(0, 0)), jugador("b", Ficha(5, 5))),
            tablero = listOf(Ficha(1, 3))
        )
        val n = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO, Poder.SALTO)))
        assertEquals("a", n.turnoActualId)
    }

    @Test
    fun puntos_dobles_duplica_el_puntaje_de_la_siguiente_jugada() {
        val base = partida(listOf(jugador("a", Ficha(3, 4), Ficha(0, 0)), jugador("b", Ficha(5, 5))), tablero = listOf(Ficha(1, 3)))
        val e = motor.aplicarEvento(base, EventoCaos.PUNTOS_DOBLES)
        assertEquals(EventoCaos.PUNTOS_DOBLES, e.eventoActivo)
        val n = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO)))
        assertEquals(14, n.jugador("a")!!.puntaje)
    }

    @Test
    fun robo_forzado_pasa_una_ficha_del_pozo_al_jugador_del_turno() {
        val e = partida(listOf(jugador("a", Ficha(1, 1)), jugador("b", Ficha(5, 5))), pozo = listOf(Ficha(0, 0)))
        val n = motor.aplicarEvento(e, EventoCaos.ROBO_FORZADO)
        assertEquals(2, n.jugador("a")!!.fichas.size)
        assertTrue(n.pozo.isEmpty())
    }

    @Test
    fun turno_extra_hace_que_el_jugador_repita() {
        val base = partida(listOf(jugador("a", Ficha(3, 4), Ficha(3, 3), Ficha(0, 0)), jugador("b", Ficha(5, 5))), tablero = listOf(Ficha(1, 3)))
        val e = motor.aplicarEvento(base, EventoCaos.TURNO_EXTRA)
        val n = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO)))
        assertEquals("a", n.turnoActualId)
    }

    @Test
    fun descarte_devuelve_una_ficha_al_pozo() {
        val e = partida(listOf(jugador("a", Ficha(1, 1), Ficha(2, 2)), jugador("b", Ficha(5, 5))))
        val n = motor.aplicarEvento(e, EventoCaos.DESCARTE_AL_POZO)
        assertEquals(1, n.jugador("a")!!.fichas.size)
        assertEquals(1, n.pozo.size)
    }

    @Test
    fun un_evento_temporal_expira_tras_su_duracion() {
        val base = partida(
            listOf(jugador("a", Ficha(3, 4), Ficha(4, 4), Ficha(0, 0)), jugador("b", Ficha(4, 6), Ficha(5, 5))),
            tablero = listOf(Ficha(1, 3))
        )
        var e = motor.aplicarEvento(base, EventoCaos.SIN_PUNTOS)
        e = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO)))
        assertEquals(EventoCaos.SIN_PUNTOS, e.eventoActivo)
        e = exito(motor.aplicar(e, Accion.JugarFicha("b", Ficha(4, 6), Lado.DERECHO)))
        assertNull(e.eventoActivo)
    }

    // ---------- fin de partida ----------

    @Test
    fun quedarse_sin_fichas_termina_la_partida_y_suma_las_manos_rivales() {
        val e = partida(listOf(jugador("a", Ficha(3, 4)), jugador("b", Ficha(5, 5), Ficha(1, 0))), tablero = listOf(Ficha(1, 3)))
        val n = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO)))
        assertEquals(FasePartida.FINALIZADA, n.fase)
        assertEquals(MotivoFin.DOMINO, n.motivoFin)
        assertEquals("a", n.ganadorId)
        assertEquals(7 + 11, n.jugador("a")!!.puntaje)
    }

    @Test
    fun el_juego_se_bloquea_cuando_nadie_puede_jugar() {
        val a = jugador("a", Ficha(3, 4), Ficha(0, 0))
        val b = jugador("b", Ficha(5, 5))
        val e = partida(listOf(a, b), tablero = listOf(Ficha(3, 3)))
        val n = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO)))
        assertEquals(FasePartida.FINALIZADA, n.fase)
        assertEquals(MotivoFin.BLOQUEO, n.motivoFin)
        assertEquals("a", n.ganadorId)
    }

    @Test
    fun la_partida_termina_al_superar_el_maximo_de_rondas() {
        val e = partida(
            listOf(jugador("a", Ficha(3, 4), Ficha(0, 0)), jugador("b", Ficha(4, 5), Ficha(6, 6))),
            tablero = listOf(Ficha(1, 3)), maxRondas = 1
        )
        var n = exito(motor.aplicar(e, Accion.JugarFicha("a", Ficha(3, 4), Lado.DERECHO)))
        assertEquals(FasePartida.JUGANDO, n.fase)
        n = exito(motor.aplicar(n, Accion.JugarFicha("b", Ficha(4, 5), Lado.DERECHO)))
        assertEquals(FasePartida.FINALIZADA, n.fase)
        assertEquals(MotivoFin.RONDAS, n.motivoFin)
    }

    @Test
    fun abandonar_con_dos_jugadores_da_la_victoria_al_otro() {
        val e = partida(listOf(jugador("a", Ficha(1, 1), Ficha(2, 2)), jugador("b", Ficha(5, 5), Ficha(6, 6))))
        val n = exito(motor.aplicar(e, Accion.Abandonar("a")))
        assertEquals(FasePartida.FINALIZADA, n.fase)
        assertEquals(MotivoFin.ABANDONO, n.motivoFin)
        assertEquals("b", n.ganadorId)
    }

    @Test
    fun abandonar_en_el_turno_propio_pasa_el_turno_y_devuelve_las_fichas_al_pozo() {
        val e = partida(listOf(jugador("a", Ficha(1, 1)), jugador("b", Ficha(5, 5)), jugador("c", Ficha(6, 6))))
        val n = exito(motor.aplicar(e, Accion.Abandonar("a")))
        assertEquals(FasePartida.JUGANDO, n.fase)
        assertEquals("b", n.turnoActualId)
        assertEquals(1, n.pozo.size)
        assertTrue(n.jugador("a")!!.abandono)
    }

    @Test
    fun reiniciar_solo_funciona_con_la_partida_terminada_y_vuelve_a_repartir() {
        var e = partida(listOf(jugador("a", Ficha(1, 1), Ficha(2, 2)), jugador("b", Ficha(5, 5), Ficha(6, 6))))
        error(motor.reiniciar(e, "a"))
        e = exito(motor.aplicar(e, Accion.Abandonar("b")))
        val solo = e.copy(jugadores = e.jugadores.map { it.copy(abandono = false) })
        val n = exito(motor.reiniciar(solo, "a"))
        assertEquals(FasePartida.JUGANDO, n.fase)
        n.jugadores.forEach { assertEquals(7, it.fichas.size) }
        assertFalse(n.jugadores.any { it.puntaje != 0 })
    }
}
