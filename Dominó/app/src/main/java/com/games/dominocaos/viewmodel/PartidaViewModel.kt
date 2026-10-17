package com.games.dominocaos.viewmodel

import androidx.lifecycle.ViewModel
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.Jugador
import com.games.dominocaos.model.Partida
import com.google.firebase.firestore.FirebaseFirestore

class PartidaViewModel : ViewModel() {

    var partidaActual = Partida()
    private val db = FirebaseFirestore.getInstance();

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
            guardarPartidaEnFirebase()
            return true
        }

        // Identificamos los números en los extremos actuales del tablero
        val extremoIzquierdo = tablero.first().izquierdo
        val extremoDerecho = tablero.last().derecho

        // Evaluamos si la ficha encaja (la IA calcula y voltea la ficha automáticamente si es necesario)
        when {
            ficha.derecho == extremoIzquierdo -> tablero.add(0, ficha) // Encaja por la izquierda
            ficha.izquierdo == extremoIzquierdo -> tablero.add(0, ficha.voltear()) // Se voltea y encaja por la izquierda
            ficha.izquierdo == extremoDerecho -> tablero.add(ficha) // Encaja por la derecha
            ficha.derecho == extremoDerecho -> tablero.add(ficha.voltear()) // Se voltea y encaja por la derecha
            else -> return false // La ficha no coincide por ningún lado, jugada inválida
        }

        // Si el código llega hasta aquí, la ficha se colocó con éxito
        jugador.fichas.remove(ficha)
        pasarTurno()
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
        // Guardamos toda la partida en la colección "salas" bajo el documento "sala_A7K3"
        db.collection("salas").document("sala_A7K3")
            .set(partidaActual)
            .addOnSuccessListener {
                println("¡Movimiento sincronizado con la nube!")
            }
            .addOnFailureListener { e ->
                println("Error al sincronizar: ${e.message}")
            }
    }
}