package com.games.dominocaos.model

enum class EventoCaos(val titulo: String, val descripcion: String, val duracionTurnos: Int) {
    // Afectan el puntaje (temporales)
    PUNTOS_DOBLES("Puntos dobles", "Las fichas colocadas valen el doble.", 2),
    SIN_PUNTOS("Sequía", "Las fichas colocadas no puntúan.", 2),

    // Modifica las reglas (temporal)
    DOBLES_LIBRES("Dobles libres", "Los dobles se pueden colocar en cualquier extremo.", 2),

    // Afectan fichas / pozo / turno (inmediatos)
    ROBO_FORZADO("Robo forzado", "El jugador del turno roba una ficha del pozo.", 0),
    TURNO_EXTRA("Turno extra", "El jugador del turno juega dos veces seguidas.", 0),
    DESCARTE_AL_POZO("Descarte", "El jugador del turno devuelve una ficha al azar al pozo.", 0);

    val esTemporal: Boolean get() = duracionTurnos > 0
}
