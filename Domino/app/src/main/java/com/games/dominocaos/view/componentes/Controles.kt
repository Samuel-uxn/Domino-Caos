package com.games.dominocaos.view.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.games.dominocaos.domain.OpcionesTurno
import com.games.dominocaos.model.Lado
@Preview
@Composable
fun Controles(
    opciones: OpcionesTurno,
    onLado: (Lado) -> Unit,
    onRobar: () -> Unit,
    onPasar: () -> Unit,
    onAbandonar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            BotonGameboy(
                texto = "< IZQUIERDA",
                onClick = { onLado(Lado.IZQUIERDO) },
                habilitado = Lado.IZQUIERDO in opciones.lados,
                modifier = Modifier.weight(1f)
            )
            BotonGameboy(
                texto = "DERECHA >",
                onClick = { onLado(Lado.DERECHO) },
                habilitado = Lado.DERECHO in opciones.lados,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            BotonGameboy("ROBAR", onRobar, habilitado = opciones.puedeRobar, modifier = Modifier.weight(1f))
            BotonGameboy("PASAR", onPasar, habilitado = opciones.puedePasar, modifier = Modifier.weight(1f))
            BotonGameboy("SALIR", onAbandonar, modifier = Modifier.weight(1f))
        }
    }
}
