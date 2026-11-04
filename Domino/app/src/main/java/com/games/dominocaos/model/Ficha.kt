package com.games.dominocaos.model

data class Ficha(
    val izquierdo: Int,
    val derecho: Int
) {
    fun esDoble(): Boolean = izquierdo == derecho
    fun voltear(): Ficha = Ficha(derecho, izquierdo)
}