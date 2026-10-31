package com.games.dominocaos.view.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.games.dominocaos.model.JugadorEstado
import com.games.dominocaos.model.Poder

/**
 * Botones de los poderes con los usos que le quedan al jugador.
 * El poder se "arma" antes de colocar la ficha y se gasta al colocarla.
 */
@Composable
fun PanelPoderes(
    jugador: JugadorEstado,
    poderArmado: Poder?,
    esMiTurno: Boolean,
    onPoder: (Poder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TextoGameboy("PODER (actívalo ANTES de colocar)", tamano = 11)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Poder.entries.forEach { poder ->
                val usos = jugador.usosDe(poder)
                BotonGameboy(
                    texto = "${poder.etiqueta} x$usos",
                    onClick = { onPoder(poder) },
                    habilitado = esMiTurno && usos > 0,
                    marcado = poderArmado == poder,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
