package com.games.dominocaos.domain

import com.games.dominocaos.model.Accion
import com.games.dominocaos.model.EstadoPartida
import com.games.dominocaos.model.EventoCaos
import com.games.dominocaos.model.FasePartida
import com.games.dominocaos.model.JugadorEstado
import com.games.dominocaos.model.Lado
import com.games.dominocaos.model.MotivoFin
import com.games.dominocaos.model.Poder
import com.games.dominocaos.model.Resultado
import com.games.dominocaos.model.Ficha
import kotlin.random.Random

class MotorPartida(
    private val random: Random = Random.Default,
    private val probabilidadEvento: Double = ReglasConfig.PROBABILIDAD_EVENTO
) {
    fun crearSala(
        idSala: String,
        jugadorId: String,
        nombre: String,
        maxRondas: Int = ReglasConfig.MAX_RONDAS
    ): EstadoPartida = EstadoPartida(
        idSala = idSala,
        fase = FasePartida.ESPERANDO,
        jugadores = listOf(JugadorEstado(id = jugadorId, nombre = nombre)),
        turnoActualId = jugadorId,
        maxRondas = maxRondas,
        ultimoMensaje = "Sala creada, esperando jugadores"
    )

    fun unirse(estado: EstadoPartida, jugadorId: String, nombre: String): Resultado {
        if (estado.jugador(jugadorId) != null) return Resultado.Exito(estado)
        if (estado.fase != FasePartida.ESPERANDO) return Resultado.Error("La partida ya comenzó")
        if (estado.jugadores.size >= ReglasConfig.MAX_JUGADORES) return Resultado.Error("La sala está llena")
        val nuevo = estado.copy(
            jugadores = estado.jugadores + JugadorEstado(id = jugadorId, nombre = nombre),
            ultimoMensaje = "$nombre se unió a la sala"
        )
        return Resultado.Exito(nuevo)
    }

    fun iniciar(estado: EstadoPartida, solicitanteId: String): Resultado {
        if (estado.fase != FasePartida.ESPERANDO) return Resultado.Error("La partida ya comenzó")
        if (estado.activos.firstOrNull()?.id != solicitanteId) {
            return Resultado.Error("Solo el creador de la sala puede iniciar la partida")
        }
        if (estado.activos.size < ReglasConfig.MIN_JUGADORES) {
            return Resultado.Error("Se necesitan al menos ${ReglasConfig.MIN_JUGADORES} jugadores")
        }
        return Resultado.Exito(repartir(estado))
    }

    fun reiniciar(estado: EstadoPartida, solicitanteId: String): Resultado {
        if (estado.fase != FasePartida.FINALIZADA) {
            return Resultado.Error("Solo se puede reiniciar cuando la partida terminó")
        }
        if (estado.activos.firstOrNull()?.id != solicitanteId) {
            return Resultado.Error("Solo el creador de la sala puede reiniciar")
        }
        if (estado.activos.size < ReglasConfig.MIN_JUGADORES) {
            return Resultado.Error("No hay suficientes jugadores para reiniciar")
        }
        return Resultado.Exito(repartir(estado.copy(jugadores = estado.activos)))
    }

    private fun repartir(estado: EstadoPartida): EstadoPartida {
        var restantes = generarFichas().shuffled(random)
        val jugadores = estado.jugadores.map { j ->
            val mano = restantes.take(ReglasConfig.FICHAS_INICIALES)
            restantes = restantes.drop(ReglasConfig.FICHAS_INICIALES)
            JugadorEstado(id = j.id, nombre = j.nombre, fichas = mano)
        }
        return EstadoPartida(
            idSala = estado.idSala,
            fase = FasePartida.JUGANDO,
            jugadores = jugadores,
            pozo = restantes,
            turnoActualId = jugadores.first().id,
            maxRondas = estado.maxRondas,
            ultimoMensaje = "¡Comienza la partida! Turno de ${jugadores.first().nombre}"
        )
    }

    private fun generarFichas(): List<Ficha> =
        (0..6).flatMap { i -> (i..6).map { j -> Ficha(i, j) } }

    fun aplicar(estado: EstadoPartida, accion: Accion): Resultado {
        if (accion is Accion.Abandonar) return abandonar(estado, accion.jugadorId)

        if (estado.fase != FasePartida.JUGANDO) return Resultado.Error("La partida no está en curso")
        val jugador = estado.jugador(accion.jugadorId)
            ?: return Resultado.Error("No eres parte de esta partida")
        if (jugador.abandono) return Resultado.Error("Ya abandonaste la partida")
        if (estado.turnoActualId != accion.jugadorId) return Resultado.Error("No es tu turno")

        return when (accion) {
            is Accion.JugarFicha -> jugarFicha(estado, jugador, accion)
            is Accion.Robar -> robar(estado, jugador)
            is Accion.Pasar -> pasar(estado, jugador)
            is Accion.Abandonar -> Resultado.Error("Acción no válida")
        }
    }

    private fun jugarFicha(estado: EstadoPartida, jugador: JugadorEstado, accion: Accion.JugarFicha): Resultado {
        val indice = buscarIndice(jugador.fichas, accion.ficha)
        if (indice < 0) return Resultado.Error("Esa ficha no está en tu mano")
        val fichaMano = jugador.fichas[indice]

        val poder = accion.poder
        if (poder != null && jugador.usosDe(poder) <= 0) {
            return Resultado.Error("Ya no te quedan usos de ${poder.etiqueta}")
        }

        val doblesLibres = estado.eventoActivo == EventoCaos.DOBLES_LIBRES
        val tableroVacio = estado.tablero.isEmpty()

        val colocada: Ficha = if (tableroVacio) {
            fichaMano
        } else {
            ReglasDomino.orientar(fichaMano, accion.lado, estado.tablero, doblesLibres)
                ?: return Resultado.Error("La ficha no encaja en ese extremo")
        }

        val nuevaMano = jugador.fichas.filterIndexed { i, _ -> i != indice }
        val nuevoTablero = when {
            tableroVacio -> listOf(colocada)
            accion.lado == Lado.IZQUIERDO -> listOf(colocada) + estado.tablero
            else -> estado.tablero + colocada
        }
        val puntos = Puntuacion.puntosPorColocar(colocada, estado.eventoActivo)
        val usos = if (poder != null) jugador.usosRestantes + (poder to jugador.usosDe(poder) - 1)
        else jugador.usosRestantes

        val jugadorNuevo = jugador.copy(
            fichas = nuevaMano,
            puntaje = jugador.puntaje + puntos,
            usosRestantes = usos,
            poderesUsados = jugador.poderesUsados + if (poder != null) 1 else 0
        )

        val mensaje = buildString {
            append("${jugador.nombre} colocó ${colocada.texto()} (+$puntos)")
            if (poder != null) append(" con ${poder.etiqueta}")
        }

        val nuevo = estado.copy(
            tablero = nuevoTablero,
            jugadores = estado.jugadores.con(jugadorNuevo),
            saltarSiguiente = estado.saltarSiguiente || poder == Poder.SALTO,
            ultimoMensaje = mensaje
        )

        if (nuevaMano.isEmpty()) {
            return Resultado.Exito(finalizar(nuevo, MotivoFin.DOMINO, ganadorDomino = jugador.id))
        }
        return Resultado.Exito(avanzarTurno(nuevo))
    }

    private fun robar(estado: EstadoPartida, jugador: JugadorEstado): Resultado {
        if (estado.pozo.isEmpty()) return Resultado.Error("El pozo está vacío")
        val doblesLibres = estado.eventoActivo == EventoCaos.DOBLES_LIBRES
        if (ReglasDomino.tieneJugada(jugador.fichas, estado.tablero, doblesLibres)) {
            return Resultado.Error("Tienes una jugada válida, no puedes robar")
        }
        val robada = estado.pozo.first()
        val jugadorNuevo = jugador.copy(fichas = jugador.fichas + robada)
        var nuevo = estado.copy(
            pozo = estado.pozo.drop(1),
            jugadores = estado.jugadores.con(jugadorNuevo),
            ultimoMensaje = "${jugador.nombre} robó una ficha"
        )

        if (!puedeActuar(jugadorNuevo, nuevo)) {
            nuevo = avanzarTurno(agregarMensaje(nuevo, "no puede jugar y pierde el turno"))
        }
        return Resultado.Exito(nuevo)
    }

    private fun pasar(estado: EstadoPartida, jugador: JugadorEstado): Resultado {
        if (estado.pozo.isNotEmpty()) return Resultado.Error("Aún quedan fichas en el pozo, debes robar")
        val doblesLibres = estado.eventoActivo == EventoCaos.DOBLES_LIBRES
        if (ReglasDomino.tieneJugada(jugador.fichas, estado.tablero, doblesLibres)) {
            return Resultado.Error("Tienes una jugada válida, no puedes pasar")
        }
        return Resultado.Exito(avanzarTurno(estado.copy(ultimoMensaje = "${jugador.nombre} pasa el turno")))
    }

    private fun abandonar(estado: EstadoPartida, jugadorId: String): Resultado {
        val jugador = estado.jugador(jugadorId) ?: return Resultado.Error("No eres parte de esta partida")

        if (estado.fase == FasePartida.ESPERANDO) {
            return Resultado.Exito(
                estado.copy(
                    jugadores = estado.jugadores.filterNot { it.id == jugadorId },
                    ultimoMensaje = "${jugador.nombre} salió de la sala"
                )
            )
        }
        if (jugador.abandono) return Resultado.Exito(estado)

        val jugadorNuevo = jugador.copy(fichas = emptyList(), abandono = true)
        var nuevo = estado.copy(
            jugadores = estado.jugadores.con(jugadorNuevo),
            ultimoMensaje = "${jugador.nombre} abandonó la partida"
        )
        if (estado.fase == FasePartida.FINALIZADA) return Resultado.Exito(nuevo)

        nuevo = nuevo.copy(pozo = nuevo.pozo + jugador.fichas)
        val activos = nuevo.activos
        if (activos.size < ReglasConfig.MIN_JUGADORES) {
            return Resultado.Exito(finalizar(nuevo, MotivoFin.ABANDONO, ganadorForzado = activos.firstOrNull()?.id))
        }
        if (estado.turnoActualId == jugadorId) {
            nuevo = avanzarTurno(nuevo.copy(turnoExtra = false))
        }
        return Resultado.Exito(nuevo)
    }

    private fun avanzarTurno(base: EstadoPartida): EstadoPartida {
        var e = base

        var turnos = e.turnosEnRonda + 1
        var ronda = e.ronda
        if (turnos >= e.activos.size) {
            turnos = 0
            ronda += 1
        }
        e = e.copy(turnosEnRonda = turnos, ronda = ronda)
        if (ronda > e.maxRondas) return finalizar(e, MotivoFin.RONDAS)

        e = decrementarEvento(e)

        val actual = e.jugador(e.turnoActualId)
        val repite = e.turnoExtra && actual != null && !actual.abandono
        val siguienteId = if (repite) e.turnoActualId else calcularSiguiente(e)
        e = e.copy(
            turnoActualId = siguienteId,
            turnoExtra = false,
            saltarSiguiente = if (repite) e.saltarSiguiente else false
        )

        if (nadiePuedeActuar(e)) return finalizar(e, MotivoFin.BLOQUEO)

        val jugadorTurno = e.jugadorActual
        if (jugadorTurno != null && !puedeActuar(jugadorTurno, e)) {
            return avanzarTurno(agregarMensaje(e, "${jugadorTurno.nombre} no puede jugar y pierde el turno"))
        }

        return if (random.nextDouble() < probabilidadEvento) {
            aplicarEvento(e, EventoCaos.entries.random(random))
        } else e
    }

    private fun calcularSiguiente(e: EstadoPartida): String {
        val lista = e.jugadores
        if (e.activos.isEmpty()) return e.turnoActualId
        var i = lista.indexOfFirst { it.id == e.turnoActualId }.coerceAtLeast(0)
        fun paso() {
            do {
                i = (i + 1) % lista.size
            } while (lista[i].abandono)
        }
        paso()
        if (e.saltarSiguiente) paso()
        return lista[i].id
    }

    private fun decrementarEvento(e: EstadoPartida): EstadoPartida {
        if (e.eventoActivo == null) return e
        val restantes = e.turnosEventoRestantes - 1
        return if (restantes <= 0) e.copy(eventoActivo = null, turnosEventoRestantes = 0)
        else e.copy(turnosEventoRestantes = restantes)
    }

    fun aplicarEvento(estado: EstadoPartida, evento: EventoCaos): EstadoPartida {
        val actual = estado.jugadorActual ?: return estado
        val titulo = "Evento: ${evento.titulo}"
        return when (evento) {
            EventoCaos.PUNTOS_DOBLES, EventoCaos.SIN_PUNTOS, EventoCaos.DOBLES_LIBRES ->
                agregarMensaje(
                    estado.copy(eventoActivo = evento, turnosEventoRestantes = evento.duracionTurnos),
                    "$titulo. ${evento.descripcion}"
                )

            EventoCaos.ROBO_FORZADO ->
                if (estado.pozo.isEmpty()) {
                    agregarMensaje(estado, "$titulo (sin efecto: el pozo está vacío)")
                } else {
                    val robada = estado.pozo.first()
                    agregarMensaje(
                        estado.copy(
                            pozo = estado.pozo.drop(1),
                            jugadores = estado.jugadores.con(actual.copy(fichas = actual.fichas + robada))
                        ),
                        "$titulo. ${actual.nombre} roba una ficha"
                    )
                }

            EventoCaos.TURNO_EXTRA ->
                agregarMensaje(estado.copy(turnoExtra = true), "$titulo. ${actual.nombre} juega dos veces")

            EventoCaos.DESCARTE_AL_POZO ->
                if (actual.fichas.size <= 1) {
                    agregarMensaje(estado, "$titulo (sin efecto)")
                } else {
                    val i = random.nextInt(actual.fichas.size)
                    agregarMensaje(
                        estado.copy(
                            pozo = estado.pozo + actual.fichas[i],
                            jugadores = estado.jugadores.con(
                                actual.copy(fichas = actual.fichas.filterIndexed { idx, _ -> idx != i })
                            )
                        ),
                        "$titulo. ${actual.nombre} devuelve una ficha al pozo"
                    )
                }
        }
    }

    private fun finalizar(
        e: EstadoPartida,
        motivo: MotivoFin,
        ganadorDomino: String? = null,
        ganadorForzado: String? = null
    ): EstadoPartida {
        var jugadores = e.jugadores
        if (ganadorDomino != null) {
            val rivales = jugadores.filter { it.id != ganadorDomino && !it.abandono }
            val bono = Puntuacion.bonoDomino(rivales)
            jugadores = jugadores.map {
                if (it.id == ganadorDomino) it.copy(puntaje = it.puntaje + bono) else it
            }
        }
        val ganadorId = ganadorForzado ?: Puntuacion.determinarGanador(jugadores)
        val nombreGanador = jugadores.firstOrNull { it.id == ganadorId }?.nombre

        val causa = when (motivo) {
            MotivoFin.DOMINO -> "Dominó"
            MotivoFin.BLOQUEO -> "Juego bloqueado"
            MotivoFin.RONDAS -> "Se alcanzó el máximo de rondas"
            MotivoFin.ABANDONO -> "Los demás jugadores abandonaron"
        }
        val resultado = if (nombreGanador != null) "Gana $nombreGanador" else "Empate"

        return e.copy(
            jugadores = jugadores,
            fase = FasePartida.FINALIZADA,
            ganadorId = ganadorId,
            motivoFin = motivo,
            eventoActivo = null,
            turnosEventoRestantes = 0,
            turnoExtra = false,
            saltarSiguiente = false,
            ultimoMensaje = "$causa. $resultado"
        )
    }

    private fun puedeActuar(j: JugadorEstado, e: EstadoPartida): Boolean =
        e.pozo.isNotEmpty() ||
            ReglasDomino.tieneJugada(j.fichas, e.tablero, e.eventoActivo == EventoCaos.DOBLES_LIBRES)

    private fun nadiePuedeActuar(e: EstadoPartida): Boolean =
        e.pozo.isEmpty() && e.activos.none { puedeActuar(it, e) }

    private fun buscarIndice(mano: List<Ficha>, ficha: Ficha): Int {
        val i = mano.indexOf(ficha)
        return if (i >= 0) i else mano.indexOf(ficha.voltear())
    }

    private fun agregarMensaje(e: EstadoPartida, extra: String): EstadoPartida =
        e.copy(ultimoMensaje = if (e.ultimoMensaje.isBlank()) extra else "${e.ultimoMensaje} · $extra")

    private fun Ficha.texto(): String = "[$izquierdo|$derecho]"

    private fun List<JugadorEstado>.con(nuevo: JugadorEstado): List<JugadorEstado> =
        map { if (it.id == nuevo.id) nuevo else it }
}
