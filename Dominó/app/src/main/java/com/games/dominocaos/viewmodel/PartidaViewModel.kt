package com.games.dominocaos.viewmodel

import androidx.lifecycle.ViewModel
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.Jugador
import com.games.dominocaos.model.Partida

class PartidaViewModel : ViewModel() {

    var partidaActual = Partida()

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
    }
}