package com.games.dominocaos.model


data class Partida(
    val idSala: String = "",
    var estado: String = "ESPERANDO", // Puede ser: ESPERANDO, JUGANDO o FINALIZADA
    var jugadores: List<Jugador> = listOf(),
    var tablero: MutableList<Ficha> = mutableListOf(),
    var pozo: MutableList<Ficha> = mutableListOf(),
    var turnoActualId: String = "", // Para llevar el control del turno actual durante toda la partida
    var eventoActivo: String? = null // Para modificar temporalmente las reglas de la partida
)