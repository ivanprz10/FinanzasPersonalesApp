package com.example.myfinanceskt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myfinanceskt.data.local.Cuenta
import com.example.myfinanceskt.data.local.TipoCuenta
import com.example.myfinanceskt.ui.etiqueta
import com.example.myfinanceskt.ui.theme.MyFinancesKTTheme

/**
 * Formulario para registrar un gasto.
 *
 * @param cuentas cuentas disponibles para elegir (vienen de Room).
 * @param onGuardar (monto, concepto, cuentaId) -> lo llama cuando el form es valido.
 * @param onBack vuelve a la pantalla anterior.
 */
@Composable
fun RegistrarCompraScreen(
    cuentas: List<Cuenta>,
    onGuardar: (Double, String, Long) -> Unit,
    onBack: () -> Unit
) {
    var montoTexto by rememberSaveable { mutableStateOf("") }
    var concepto by rememberSaveable { mutableStateOf("") }
    var cuentaSeleccionadaId by rememberSaveable { mutableStateOf<Long?>(null) }

    // Cuando llega la lista de cuentas, preselecciona la primera si no hay ninguna elegida.
    LaunchedEffect(cuentas) {
        if (cuentaSeleccionadaId == null && cuentas.isNotEmpty()) {
            cuentaSeleccionadaId = cuentas.first().id
        }
    }

    // toDoubleOrNull -> null si el texto no es un numero valido (no lanza excepcion).
    val monto = montoTexto.replace(',', '.').toDoubleOrNull()
    val valido = monto != null && monto > 0.0 &&
        concepto.isNotBlank() &&
        cuentaSeleccionadaId != null

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                TextButton(onClick = onBack) { Text("‹  Volver") }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Registrar compra",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    label = { Text("Monto") },
                    prefix = { Text("$") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "¿Con que cuenta?",
                    style = MaterialTheme.typography.titleMedium
                )

                if (cuentas.isEmpty()) {
                    Text(
                        text = "Cargando cuentas...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column {
                        cuentas.forEach { cuenta ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { cuentaSeleccionadaId = cuenta.id }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = cuentaSeleccionadaId == cuenta.id,
                                    onClick = { cuentaSeleccionadaId = cuenta.id }
                                )
                                Text(text = cuenta.etiqueta())
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        // El "!!" es seguro aqui porque 'valido' ya comprobo que no son null.
                        onGuardar(monto!!, concepto, cuentaSeleccionadaId!!)
                    },
                    enabled = valido,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar gasto")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegistrarCompraPreview() {
    MyFinancesKTTheme {
        RegistrarCompraScreen(
            cuentas = listOf(
                Cuenta(1L, "Efectivo", TipoCuenta.EFECTIVO),
                Cuenta(2L, "Nu", TipoCuenta.CREDITO)
            ),
            onGuardar = { _, _, _ -> },
            onBack = {}
        )
    }
}
