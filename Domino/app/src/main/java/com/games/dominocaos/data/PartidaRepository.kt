package com.games.dominocaos.data

import com.games.dominocaos.model.EstadoPartida
import com.games.dominocaos.model.Resultado
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Transaction

/**
 * Único punto de contacto con Firestore. Cada sala es un documento en la colección "salas"
 * cuyo id es el código de la sala. Toda modificación se hace dentro de una transacción para
 * que dos jugadores no se pisen entre sí.
 */
class PartidaRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        const val COLECCION = "salas"
        const val CODIGO_OCUPADO = "CODIGO_OCUPADO"
    }

    private fun documento(codigo: String) = db.collection(COLECCION).document(codigo)

    /** Crea la sala; devuelve Error(CODIGO_OCUPADO) si ya existe una con ese código. */
    fun crearSala(estado: EstadoPartida, onResult: (Resultado) -> Unit) {
        val doc = documento(estado.idSala)
        db.runTransaction(Transaction.Function<Resultado> { tx ->
            if (tx.get(doc).exists()) {
                Resultado.Error(CODIGO_OCUPADO)
            } else {
                tx.set(doc, EstadoSerializador.aMapa(estado))
                Resultado.Exito(estado)
            }
        }).addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onResult(Resultado.Error(it.message ?: "Error de conexión")) }
    }

    /**
     * Lee la sala, le aplica [operacion] (normalmente una llamada al motor) y, si fue exitosa,
     * guarda el nuevo estado. Todo en una sola transacción.
     */
    fun ejecutar(codigo: String, operacion: (EstadoPartida) -> Resultado, onResult: (Resultado) -> Unit) {
        val doc = documento(codigo)
        db.runTransaction(Transaction.Function<Resultado> { tx ->
            val datos = tx.get(doc).data
            if (datos == null) {
                Resultado.Error("La sala no existe")
            } else {
                val resultado = operacion(EstadoSerializador.desdeMapa(datos))
                if (resultado is Resultado.Exito) {
                    tx.set(doc, EstadoSerializador.aMapa(resultado.estado))
                }
                resultado
            }
        }).addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onResult(Resultado.Error(it.message ?: "Error de conexión")) }
    }

    /** Escucha los cambios de la sala en tiempo real. Hay que llamar a remove() al salir. */
    fun escuchar(
        codigo: String,
        onEstado: (EstadoPartida?) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration =
        documento(codigo).addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error.message ?: "Error de sincronización")
            } else {
                onEstado(snapshot?.data?.let { EstadoSerializador.desdeMapa(it) })
            }
        }
}
