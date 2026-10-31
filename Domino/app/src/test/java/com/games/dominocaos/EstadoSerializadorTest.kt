package com.games.dominocaos

import com.games.dominocaos.data.EstadoSerializador
import com.games.dominocaos.domain.MotorPartida
import com.games.dominocaos.model.EstadoPartida
import com.games.dominocaos.model.EventoCaos
import com.games.dominocaos.model.FasePartida
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.JugadorEstado
import com.games.dominocaos.model.MotivoFin
import com.games.dominocaos.model.Poder
import com.games.dominocaos.model.Resultado
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class EstadoSerializadorTest {

    /** Imita a Firestore, que devuelve todos los enteros como Long. */
    private fun comoFirestore(valor: Any?): Any? = when (valor) {
        is Int -> valor.toLong()
        is List<*> -> valor.map { comoFirestore(it) }
        is Map<*, *> -> valor.entries.associate { it.key to comoFirestore(it.value) }
        else -> valor
    }

    @Test
    fun ida_y_vuelta_conserva_un_estado_completo() {
        val estado = EstadoPartida(
            idSala = "A7K3",
            fase = FasePartida.FINALIZADA,
            jugadores = listOf(
                JugadorEstado("a", "Ana", listOf(Ficha(1, 2)), 15, mapOf(Poder.SALTO to 1), 3, false),
                JugadorEstado("b", "Beto", emptyList(), 9, abandono = true)
            ),
            tablero = listOf(Ficha(0, 3), Ficha(3, 6)),
            pozo = listOf(Ficha(4, 4)),
            turnoActualId = "b",
            saltarSiguiente = true,
            turnoExtra = true,
            ronda = 4,
            maxRondas = 10,
            turnosEnRonda = 1,
            eventoActivo = EventoCaos.DOBLES_LIBRES,
            turnosEventoRestantes = 2,
            ultimoMensaje = "Ana colocó [3|6] (+9)",
            ganadorId = "a",
            motivoFin = MotivoFin.RONDAS
        )
        assertEquals(estado, EstadoSerializador.desdeMapa(EstadoSerializador.aMapa(estado)))
    }

    @Test
    fun campos_nulos_se_conservan() {
        val estado = EstadoPartida(idSala = "X", jugadores = listOf(JugadorEstado("a", "Ana")))
        assertEquals(estado, EstadoSerializador.desdeMapa(EstadoSerializador.aMapa(estado)))
    }

    @Test
    fun lee_correctamente_los_enteros_que_llegan_como_long() {
        val motor = MotorPartida(Random(7), 0.0)
        var e = motor.crearSala("Q1W2", "a", "Ana")
        e = (motor.unirse(e, "b", "Beto") as Resultado.Exito).estado
        e = (motor.iniciar(e, "a") as Resultado.Exito).estado
        @Suppress("UNCHECKED_CAST")
        val mapaFirestore = comoFirestore(EstadoSerializador.aMapa(e)) as Map<String, Any?>
        assertEquals(e, EstadoSerializador.desdeMapa(mapaFirestore))
    }
}
