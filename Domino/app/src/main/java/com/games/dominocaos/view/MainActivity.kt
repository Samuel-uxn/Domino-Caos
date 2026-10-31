package com.games.dominocaos.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.games.dominocaos.model.FasePartida
import com.games.dominocaos.viewmodel.AuthViewModel
import com.games.dominocaos.viewmodel.PartidaViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val partidaViewModel: PartidaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val usuarioActivo = FirebaseAuth.getInstance().currentUser

                    var pantallaActual by remember {
                        mutableStateOf(if (usuarioActivo != null) "MENU" else "LOGIN")
                    }
                    var nombreJugador by remember {
                        mutableStateOf(usuarioActivo?.email?.substringBefore("@") ?: "")
                    }

                    val estado by partidaViewModel.estado.collectAsState()
                    val error by partidaViewModel.error.collectAsState()
                    LaunchedEffect(error) {
                        error?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            partidaViewModel.limpiarError()
                        }
                    }

                    val partida = estado
                    when {
                        pantallaActual == "LOGIN" -> Login(
                            authViewModel = authViewModel,
                            onLoginExitoso = { nombre ->
                                nombreJugador = nombre
                                pantallaActual = "MENU"
                            },
                            onIrARegistro = { pantallaActual = "REGISTRO" }
                        )

                        pantallaActual == "REGISTRO" -> Registro(
                            authViewModel = authViewModel,
                            onRegistroExitoso = { nombre ->
                                nombreJugador = nombre
                                pantallaActual = "MENU"
                            },
                            onVolver = { pantallaActual = "LOGIN" }
                        )

                        partida == null -> MenuPrincipal(
                            jugadorLogueado = nombreJugador,
                            onCrearSalaClick = { partidaViewModel.crearSala(nombreJugador) },
                            onUnirseSalaClick = { codigo -> partidaViewModel.unirse(codigo, nombreJugador) }
                        )

                        partida.fase == FasePartida.ESPERANDO -> SalaEspera(partidaViewModel)
                        partida.fase == FasePartida.JUGANDO -> Juego(partidaViewModel)
                        else -> FinPartida(partidaViewModel)
                    }
                }
            }
        }
    }
}
