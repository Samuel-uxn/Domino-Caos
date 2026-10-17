package com.games.dominocaos.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.games.dominocaos.ui.theme.DominoCaosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DominoCaosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            // Instanciamos la base de datos usando la ruta completa
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

// Creamos un dato de prueba
            val datosPrueba = hashMapOf(
                "mensaje" to "¡Conexión exitosa desde el Asus!",
                "jugador" to "Samuel"
            )

// Lo enviamos a Firestore
            db.collection("sala_test").document("estado").set(datosPrueba)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DominoCaosTheme {
        Greeting("Android")
    }
}