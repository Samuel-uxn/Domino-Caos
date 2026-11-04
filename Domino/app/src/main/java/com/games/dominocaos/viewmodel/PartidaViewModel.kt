package com.games.dominocaos.viewmodel

import androidx.lifecycle.ViewModel
import com.games.dominocaos.data.PartidaRepository
import com.games.dominocaos.domain.MotorPartida
import com.games.dominocaos.model.Accion
import com.games.dominocaos.model.EstadoPartida
import com.games.dominocaos.model.Ficha
import com.games.dominocaos.model.Lado
import com.games.dominocaos.model.Poder
import com.games.dominocaos.model.Resultado
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PartidaViewModel : ViewModel() {

    private val repositorio = PartidaRepository()
    private val motor = MotorPartida()

    private val _estado = MutableStateFlow<EstadoPartida?>(null)
    val estado: StateFlow<EstadoPartida?> = _estado.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _seleccion = MutableStateFlow<Ficha?>(null)
    val seleccion: StateFlow<Ficha?> = _seleccion.asStateFlow()

    private val _poderArmado = MutableStateFlow<Poder?>(null)
    val poderArmado: StateFlow<Poder?> = _poderArmado.asStateFlow()

    private var escucha: ListenerRegistration? = null
    private var codigoSala: String? = null

    val miId: String get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    fun crearSala(nombre: String) = intentarCrear(nombre, intentos = 5)

    private fun intentarCrear(nombre: String, intentos: Int) {
        val codigo = generarCodigo()
        repositorio.crearSala(motor.crearSala(codigo, miId, nombre)) { resultado ->
            when {
                resultado is Resultado.Exito -> entrar(codigo)
                resultado is Resultado.Error && resultado.mensaje == PartidaRepository.CODIGO_OCUPADO && intentos > 1 ->
                    intentarCrear(nombre, intentos - 1)
                resultado is Resultado.Error -> _error.value = resultado.mensaje
            }
        }
    }

    fun unirse(codigo: String, nombre: String) {
        val limpio = codigo.trim().uppercase()
        repositorio.ejecutar(limpio, { motor.unirse(it, miId, nombre) }) { resultado ->
            if (resultado is Resultado.Error) _error.value = resultado.mensaje else entrar(limpio)
        }
    }

    private fun entrar(codigo: String) {
        escucha?.remove()
        codigoSala = codigo
        escucha = repositorio.escuchar(
            codigo,
            onEstado = { nuevo -> _estado.value = nuevo },
            onError = { _error.value = it }
        )
    }

    fun iniciar() = ejecutar { motor.iniciar(it, miId) }

    fun reiniciar() = ejecutar { motor.reiniciar(it, miId) }

    fun seleccionar(ficha: Ficha) {
        _seleccion.value = if (_seleccion.value == ficha) null else ficha
    }

    fun armarPoder(poder: Poder) {
        _poderArmado.value = if (_poderArmado.value == poder) null else poder
    }

    fun jugarFicha(lado: Lado) {
        val ficha = _seleccion.value ?: return
        val poder = _poderArmado.value
        ejecutar { motor.aplicar(it, Accion.JugarFicha(miId, ficha, lado, poder)) }
    }

    fun robar() = ejecutar { motor.aplicar(it, Accion.Robar(miId)) }

    fun pasar() = ejecutar { motor.aplicar(it, Accion.Pasar(miId)) }

    fun abandonar() {
        val codigo = codigoSala ?: return salirLocal()
        repositorio.ejecutar(codigo, { motor.aplicar(it, Accion.Abandonar(miId)) }) { salirLocal() }
    }

    fun limpiarError() {
        _error.value = null
    }

    private fun ejecutar(operacion: (EstadoPartida) -> Resultado) {
        val codigo = codigoSala ?: return
        repositorio.ejecutar(codigo, operacion) { resultado ->
            if (resultado is Resultado.Error) {
                _error.value = resultado.mensaje
            } else {
                _seleccion.value = null
                _poderArmado.value = null
            }
        }
    }

    private fun salirLocal() {
        escucha?.remove()
        escucha = null
        codigoSala = null
        _estado.value = null
        _seleccion.value = null
        _poderArmado.value = null
    }

    private fun generarCodigo(): String =
        (1..4).map { ALFABETO.random() }.joinToString("")

    override fun onCleared() {
        escucha?.remove()
    }

    private companion object {
        const val ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
