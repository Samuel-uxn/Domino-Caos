package com.games.dominocaos.model

data class Ficha(
    val izquierdo: Int,
    val derecho: Int
) {
    // Nos servirá para identificar fácilmente los dobles
    fun esDoble(): Boolean = izquierdo == derecho

    // Función útil para cuando el jugador necesite rotar la ficha al colocarla en el tablero
    fun voltear(): Ficha = Ficha(derecho, izquierdo)
}