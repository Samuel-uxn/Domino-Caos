package com.games.dominocaos.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.games.dominocaos.viewmodel.AuthViewModel
import com.games.dominocaos.viewmodel.PartidaViewModel
import com.google.firebase.auth.FirebaseAuth // Importante agregar esto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val partidaViewModel = remember { PartidaViewModel() }
                    val authViewModel = remember { AuthViewModel() }

                    // Magia de Firebase: Revisamos si ya hay alguien logueado de antes
                    val usuarioActivo = FirebaseAuth.getInstance().currentUser

                    // Si hay usuario, arrancamos en MENU. Si no, en LOGIN.
                    var pantallaActual by remember {
                        mutableStateOf(if (usuarioActivo != null) "MENU" else "LOGIN")
                    }

                    // Extraemos el nombre del correo guardado (ej: samuel@juego.com -> samuel)
                    var nombreJugador by remember {
                        mutableStateOf(usuarioActivo?.email?.substringBefore("@") ?: "")
                    }

                    when (pantallaActual) {
                        "LOGIN" -> {
                            Login(
                                authViewModel = authViewModel,
                                onLoginExitoso = { nombre ->
                                    nombreJugador = nombre
                                    pantallaActual = "MENU"
                                },
                                onIrARegistro = { pantallaActual = "REGISTRO" }
                            )
                        }

                        "REGISTRO" -> {
                            Registro(
                                authViewModel = authViewModel,
                                onRegistroExitoso = { nombre ->
                                    nombreJugador = nombre
                                    pantallaActual = "MENU"
                                },
                                onVolver = { pantallaActual = "LOGIN" }
                            )
                        }

                        "MENU" -> {
                            MenuPrincipal(
                                jugadorLogueado = nombreJugador,
                                onCrearSalaClick = {
                                    partidaViewModel.iniciarPartida()
                                    pantallaActual = "JUEGO"
                                },
                                onUnirseSalaClick = { codigo ->
                                    pantallaActual = "JUEGO"
                                }
                            )
                        }

                        "JUEGO" -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("¡Sala Activa!")
                            }
                        }
                    }
                }
            }
        }
    }
}