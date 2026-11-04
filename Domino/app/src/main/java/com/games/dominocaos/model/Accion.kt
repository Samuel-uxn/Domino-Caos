package com.games.dominocaos.model

sealed interface Accion {
    val jugadorId: String


    data class JugarFicha(
        override val jugadorId: String,
        val ficha: Ficha,
        val lado: Lado = Lado.DERECHO,
        val poder: Poder? = null
    ) : Accion

    data class Robar(override val jugadorId: String) : Accion

    data class Pasar(override val jugadorId: String) : Accion

    data class Abandonar(override val jugadorId: String) : Accion
}

sealed interface Resultado {
    data class Exito(val estado: EstadoPartida) : Resultado
    data class Error(val mensaje: String) : Resultado
}
