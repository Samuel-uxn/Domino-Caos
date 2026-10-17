package com.games.dominocaos.model
import kotlin.collections.isNotEmpty
data class Jugador(
    val id: String = "",
    val nombre: String = "",
    var fichas: MutableList<Ficha> = mutableListOf(),
    var puntaje: Int = 0,
    // Los poderes tienen un número limitado de usos; iniciamos con 1 por defecto
    var usosComodin: Int = 1,
    var usosSalto: Int = 1,
    var usosIntercambio: Int = 1,
    var usosInversion: Int = 1
) {
    // Función rápida para saber si el jugador aún tiene fichas o si ya terminó
    fun tieneFichas(): Boolean = fichas.isNotEmpty()
}