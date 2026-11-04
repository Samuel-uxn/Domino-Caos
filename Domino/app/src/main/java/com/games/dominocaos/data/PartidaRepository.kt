package com.games.dominocaos.data

import com.games.dominocaos.model.EstadoPartida
import com.games.dominocaos.model.Resultado
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Transaction

class PartidaRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        const val COLECCION = "salas"
        const val CODIGO_OCUPADO = "CODIGO_OCUPADO"
    }
    private fun documento(codigo: String) = db.collection(COLECCION).document(codigo)

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
