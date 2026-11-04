package com.games.dominocaos.data

import com.games.dominocaos.model.EstadoPartida
import com.games.dominocaos.model.EventoCaos
import com.games.dominocaos.model.FasePartida
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.JugadorEstado
import com.games.dominocaos.model.MotivoFin
import com.games.dominocaos.model.Poder

object EstadoSerializador {

    fun aMapa(e: EstadoPartida): Map<String, Any?> = mapOf(
        "idSala" to e.idSala,
        "fase" to e.fase.name,
        "jugadores" to e.jugadores.map { aMapa(it) },
        "tablero" to e.tablero.map { fichaATexto(it) },
        "pozo" to e.pozo.map { fichaATexto(it) },
        "turnoActualId" to e.turnoActualId,
        "saltarSiguiente" to e.saltarSiguiente,
        "turnoExtra" to e.turnoExtra,
        "ronda" to e.ronda,
        "maxRondas" to e.maxRondas,
        "turnosEnRonda" to e.turnosEnRonda,
        "eventoActivo" to e.eventoActivo?.name,
        "turnosEventoRestantes" to e.turnosEventoRestantes,
        "ultimoMensaje" to e.ultimoMensaje,
        "ganadorId" to e.ganadorId,
        "motivoFin" to e.motivoFin?.name
    )

    fun desdeMapa(m: Map<String, Any?>): EstadoPartida = EstadoPartida(
        idSala = m["idSala"].texto(),
        fase = m["fase"].texto().enumOrNull<FasePartida>() ?: FasePartida.ESPERANDO,
        jugadores = m["jugadores"].lista().map { jugadorDesdeMapa(it.mapa()) },
        tablero = m["tablero"].lista().map { textoAFicha(it.texto()) },
        pozo = m["pozo"].lista().map { textoAFicha(it.texto()) },
        turnoActualId = m["turnoActualId"].texto(),
        saltarSiguiente = m["saltarSiguiente"].booleano(),
        turnoExtra = m["turnoExtra"].booleano(),
        ronda = m["ronda"].entero(),
        maxRondas = m["maxRondas"].entero(),
        turnosEnRonda = m["turnosEnRonda"].entero(),
        eventoActivo = (m["eventoActivo"] as? String)?.enumOrNull<EventoCaos>(),
        turnosEventoRestantes = m["turnosEventoRestantes"].entero(),
        ultimoMensaje = m["ultimoMensaje"].texto(),
        ganadorId = m["ganadorId"] as? String,
        motivoFin = (m["motivoFin"] as? String)?.enumOrNull<MotivoFin>()
    )

    private fun aMapa(j: JugadorEstado): Map<String, Any?> = mapOf(
        "id" to j.id,
        "nombre" to j.nombre,
        "fichas" to j.fichas.map { fichaATexto(it) },
        "puntaje" to j.puntaje,
        "usosRestantes" to j.usosRestantes.entries.associate { it.key.name to it.value },
        "poderesUsados" to j.poderesUsados,
        "abandono" to j.abandono
    )

    private fun jugadorDesdeMapa(m: Map<String, Any?>): JugadorEstado {
        val usos = m["usosRestantes"].mapa()
        return JugadorEstado(
            id = m["id"].texto(),
            nombre = m["nombre"].texto(),
            fichas = m["fichas"].lista().map { textoAFicha(it.texto()) },
            puntaje = m["puntaje"].entero(),
            usosRestantes = Poder.entries.associateWith { usos[it.name].entero() },
            poderesUsados = m["poderesUsados"].entero(),
            abandono = m["abandono"].booleano()
        )
    }

    private fun fichaATexto(f: Ficha): String = "${f.izquierdo}-${f.derecho}"

    private fun textoAFicha(t: String): Ficha {
        val partes = t.split("-")
        return Ficha(partes[0].toInt(), partes[1].toInt())
    }

    private fun Any?.entero(): Int = (this as? Number)?.toInt() ?: 0
    private fun Any?.booleano(): Boolean = (this as? Boolean) ?: false
    private fun Any?.texto(): String = (this as? String) ?: ""
    private fun Any?.lista(): List<Any?> = (this as? List<*>) ?: emptyList<Any?>()

    @Suppress("UNCHECKED_CAST")
    private fun Any?.mapa(): Map<String, Any?> = (this as? Map<String, Any?>) ?: emptyMap()

    private inline fun <reified T : Enum<T>> String.enumOrNull(): T? =
        enumValues<T>().firstOrNull { it.name == this }
}
