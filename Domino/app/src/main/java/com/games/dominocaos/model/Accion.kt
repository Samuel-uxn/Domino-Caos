package com.games.dominocaos.model


/** Todo lo que un jugador puede pedirle al motor. */
sealed interface Accion {
    val jugadorId: String

    /**
     * Coloca una ficha. Si [poder] no es null se consume antes del movimiento:
     *  - SALTO: el siguiente jugador pierde el turno.
     */
    data class JugarFicha(
        override val jugadorId: String,
        val ficha: Ficha,
        val lado: Lado = Lado.DERECHO,
        val poder: Poder? = null
    ) : Accion

    data class Robar(override val jugadorId: String) : Accion

    /** Solo válido si no hay jugada y el pozo está vacío. */
    data class Pasar(override val jugadorId: String) : Accion

    data class Abandonar(override val jugadorId: String) : Accion
}

sealed interface Resultado {
    data class Exito(val estado: EstadoPartida) : Resultado
    data class Error(val mensaje: String) : Resultado
}
