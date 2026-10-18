package com.games.dominocaos.viewmodel

import androidx.lifecycle.ViewModel
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.Jugador
import com.games.dominocaos.model.Partida
import com.google.firebase.firestore.FirebaseFirestore

class PartidaViewModel : ViewModel() {

    var partidaActual = Partida()
    private val db = FirebaseFirestore.getInstance()

    // 1. Función para generar las 28 fichas clásicas del dominó
    private fun generarFichas(): MutableList<Ficha> {
        val fichasNuevas = mutableListOf<Ficha>()
        for (i in 0..6) {
            for (j in i..6) {
                fichasNuevas.add(Ficha(i, j))
            }
        }
        fichasNuevas.shuffle() // La IA hace el trabajo: las mezclamos aleatoriamente
        return fichasNuevas
    }

    // 2. Función para iniciar el juego y repartir
    fun iniciarPartida() {
        val pozoCompleto = generarFichas()

        // Creamos los jugadores de prueba
        val jugador1 = Jugador(id = "1", nombre = "Samuel")
        val jugador2 = Jugador(id = "2", nombre = "Oponente")

        // Repartimos 7 fichas a cada uno y las quitamos del pozo
        jugador1.fichas = pozoCompleto.take(7).toMutableList()
        pozoCompleto.removeAll(jugador1.fichas)

        jugador2.fichas = pozoCompleto.take(7).toMutableList()
        pozoCompleto.removeAll(jugador2.fichas)

        // Actualizamos la mesa de juego
        partidaActual = Partida(
            estado = "JUGANDO",
            jugadores = listOf(jugador1, jugador2),
            pozo = pozoCompleto,
            turnoActualId = jugador1.id // Samuel empieza
        )
        guardarPartidaEnFirebase()
    }

    // 3. Función para validar y colocar una ficha en el tablero
    fun jugarFicha(jugadorId: String, ficha: Ficha): Boolean {
        // Primero, verificamos que sí sea el turno de la persona que intenta jugar
        if (partidaActual.turnoActualId != jugadorId) return false

        val tablero = partidaActual.tablero
        val jugador = partidaActual.jugadores.find { it.id == jugadorId } ?: return false

        // Si la mesa está vacía, cualquier ficha que ponga es válida
        if (tablero.isEmpty()) {
            tablero.add(ficha)
            jugador.fichas.remove(ficha)
            pasarTurno()
            evaluarEstadoPartida() // Evaluamos si ganó
            guardarPartidaEnFirebase()
            return true
        }

        // Identificamos los números en los extremos actuales del tablero
        val extremoIzquierdo = tablero.first().izquierdo
        val extremoDerecho = tablero.last().derecho

        // Evaluamos si la ficha encaja
        when {
            ficha.derecho == extremoIzquierdo -> tablero.add(0, ficha) // Encaja por la izquierda
            ficha.izquierdo == extremoIzquierdo -> tablero.add(0, ficha.voltear()) // Se voltea y encaja por la izq
            ficha.izquierdo == extremoDerecho -> tablero.add(ficha) // Encaja por la derecha
            ficha.derecho == extremoDerecho -> tablero.add(ficha.voltear()) // Se voltea y encaja por la der
            else -> return false // La ficha no coincide por ningún lado, jugada inválida
        }

        // Si el código llega hasta aquí, la ficha se colocó con éxito
        jugador.fichas.remove(ficha)
        pasarTurno()
        evaluarEstadoPartida() // Evaluamos si alguien ya se quedó sin fichas
        guardarPartidaEnFirebase()
        return true
    }

    // 4. Función rápida para rotar el turno al otro jugador
    private fun pasarTurno() {
        val jugador1 = partidaActual.jugadores[0]
        val jugador2 = partidaActual.jugadores[1]

        partidaActual.turnoActualId = if (partidaActual.turnoActualId == jugador1.id) {
            jugador2.id
        } else {
            jugador1.id
        }
    }

    // 5. Función que sube el estado de la mesa a la nube
    private fun guardarPartidaEnFirebase() {
        db.collection("salas").document("sala_A7K3")
            .set(partidaActual)
            .addOnSuccessListener {
                println("¡Movimiento sincronizado con la nube!")
            }
            .addOnFailureListener { e ->
                println("Error al sincronizar: ${e.message}")
            }
    }

    // 6. Función para robar del pozo cuando no hay movimientos
    fun robarFicha(jugadorId: String): Boolean {
        if (partidaActual.turnoActualId != jugadorId) return false

        val jugador = partidaActual.jugadores.find { it.id == jugadorId } ?: return false

        if (partidaActual.pozo.isNotEmpty()) {
            val fichaRobada = partidaActual.pozo.removeAt(0)
            jugador.fichas.add(fichaRobada)
            guardarPartidaEnFirebase()
            return true
        } else {
            pasarTurno()
            evaluarEstadoPartida() // Si nadie puede jugar y el pozo está vacío, el juego debe evaluar si hay tranque
            guardarPartidaEnFirebase()
            return false
        }
    }

    // 7. Función para usar los poderes del caos
    fun usarPoder(jugadorId: String, tipoPoder: String): Boolean {
        if (partidaActual.turnoActualId != jugadorId) return false

        val jugador = partidaActual.jugadores.find { it.id == jugadorId } ?: return false
        var poderAplicado = false

        when (tipoPoder.uppercase()) {
            "SALTO" -> {
                if (jugador.usosSalto > 0) {
                    jugador.usosSalto -= 1
                    poderAplicado = true
                }
            }
            "INVERSION" -> {
                if (jugador.usosInversion > 0) {
                    jugador.usosInversion -= 1
                    poderAplicado = true
                }
            }
        }

        if (poderAplicado) {
            partidaActual.eventoActivo = "¡${jugador.nombre} activó $tipoPoder y repite turno!"
            guardarPartidaEnFirebase()
            return true
        }

        return false
    }

    // 8. Función para evaluar si la partida debe terminar y calcular puntajes
    fun evaluarEstadoPartida() {
        val jugador1 = partidaActual.jugadores[0]
        val jugador2 = partidaActual.jugadores[1]

        // Caso 1: Victoria por Dominó (alguien se queda sin fichas)
        val ganadorPorDomino = partidaActual.jugadores.find { it.fichas.isEmpty() }
        if (ganadorPorDomino != null) {
            partidaActual.estado = "FINALIZADA"
            partidaActual.eventoActivo = "¡Juego terminado! ${ganadorPorDomino.nombre} hizo dominó."
            return
        }

        // Caso 2: Tranque o Cierre (se acabó el pozo y no hay jugadas, gana el de menor puntaje)
        if (partidaActual.pozo.isEmpty()) {
            val puntosJ1 = jugador1.fichas.sumOf { it.izquierdo + it.derecho }
            val puntosJ2 = jugador2.fichas.sumOf { it.izquierdo + it.derecho }

            partidaActual.estado = "FINALIZADA"
            val mensaje = when {
                puntosJ1 < puntosJ2 -> "Tranque: ¡${jugador1.nombre} gana con $puntosJ1 puntos!"
                puntosJ2 < puntosJ1 -> "Tranque: ¡${jugador2.nombre} gana con $puntosJ2 puntos!"
                else -> "Tranque: ¡Empate a $puntosJ1 puntos!"
            }
            partidaActual.eventoActivo = mensaje

            jugador1.puntaje = puntosJ1
            jugador2.puntaje = puntosJ2
        }
    }
}