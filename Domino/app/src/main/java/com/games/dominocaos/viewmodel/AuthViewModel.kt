package com.games.dominocaos.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Función para crear cuenta nueva
    fun registrarUsuario(correo: String, pass: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(correo, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Registro exitoso")
                } else {
                    onResult(false, task.exception?.message ?: "Error al registrar")
                }
            }
    }

    // Función para iniciar sesión
    fun iniciarSesion(correo: String, pass: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login exitoso")
                } else {
                    onResult(false, task.exception?.message ?: "Error en credenciales")
                }
            }
    }
}