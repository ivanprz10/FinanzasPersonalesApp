package com.example.myfinanceskt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.myfinanceskt.data.local.DeudaConPersona
import com.example.myfinanceskt.data.local.TipoDeuda
import com.example.myfinanceskt.ui.comoFecha
import com.example.myfinanceskt.ui.comoMoneda
import com.example.myfinanceskt.ui.theme.MyFinancesKTTheme
import com.example.myfinanceskt.ui.theme.SaldoAFavor
import com.example.myfinanceskt.ui.theme.SaldoAFavorContainer

/**
 * "Quien te debe y a quien le debes": totales por direccion + formulario
 * para registrar una deuda nueva + lista de deudas activas.
 *
 * La pantalla no sabe de Room: recibe los datos ya resueltos y avisa
 * hacia arriba (onAgregarDeuda / onSaldarDeuda) cuando algo cambia.
 */
@Composable
fun GruposScreen(
    totalTeDeben: Double,
    totalDebo: Double,
    deudas: List<DeudaConPersona>,
    onAgregarDeuda: (persona: String, monto: Double, concepto: String, tipo: TipoDeuda) -> Unit,
    onSaldarDeuda: (Long) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                    TextButton(onClick = onBack) { Text("‹  Volver") }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        text = "Cuentas de grupo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ResumenCard(
                            modifier = Modifier.weight(1f),
                            etiqueta = "Te deben",
                            monto = totalTeDeben,
                            containerColor = SaldoAFavorContainer,
                            contentColor = SaldoAFavor
                        )
                        ResumenCard(
                            modifier = Modifier.weight(1f),
                            etiqueta = "Debes",
                            monto = totalDebo,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    }

                    FormularioDeuda(onAgregarDeuda = onAgregarDeuda)
                }
            }

            if (deudas.isEmpty()) {
                item {
                    Text(
                        text = "No hay deudas activas",
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(deudas, key = { it.id }) { deuda ->
                    FilaDeuda(deuda = deuda, onSaldar = { onSaldarDeuda(deuda.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ResumenCard(
    modifier: Modifier = Modifier,
    etiqueta: String,
    monto: Double,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.labelLarge)
            Text(
                text = monto.comoMoneda(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FormularioDeuda(
    onAgregarDeuda: (persona: String, monto: Double, concepto: String, tipo: TipoDeuda) -> Unit
) {
    var persona by rememberSaveable { mutableStateOf("") }
    var montoTexto by rememberSaveable { mutableStateOf("") }
    var concepto by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf(TipoDeuda.TE_DEBEN) }

    val monto = montoTexto.replace(',', '.').toDoubleOrNull()
    val valido = persona.isNotBlank() && monto != null && monto > 0.0 && concepto.isNotBlank()

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Agregar deuda", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tipo == TipoDeuda.TE_DEBEN,
                onClick = { tipo = TipoDeuda.TE_DEBEN },
                label = { Text("Me deben") }
            )
            FilterChip(
                selected = tipo == TipoDeuda.YO_DEBO,
                onClick = { tipo = TipoDeuda.YO_DEBO },
                label = { Text("Yo debo") }
            )
        }

        OutlinedTextField(
            value = persona,
            onValueChange = { persona = it },
            label = { Text("Persona") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
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

        Button(
            onClick = {
                onAgregarDeuda(persona, monto!!, concepto, tipo)
                persona = ""
                montoTexto = ""
                concepto = ""
            },
            enabled = valido,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar")
        }
    }
}

@Composable
private fun FilaDeuda(deuda: DeudaConPersona, onSaldar: () -> Unit) {
    val esAFavor = deuda.tipo == TipoDeuda.TE_DEBEN
    val colorMonto = if (esAFavor) SaldoAFavor else MaterialTheme.colorScheme.error
    val signo = if (esAFavor) "+" else "-"

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(deuda.personaNombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = "${deuda.concepto}  ·  ${deuda.fechaEpochMillis.comoFecha()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$signo${deuda.monto.comoMoneda()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorMonto
            )
            TextButton(onClick = onSaldar) { Text("Saldar") }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GruposPreview() {
    MyFinancesKTTheme {
        GruposScreen(
            totalTeDeben = 350.0,
            totalDebo = 120.0,
            deudas = listOf(
                DeudaConPersona(1L, 200.0, "Cena", TipoDeuda.TE_DEBEN, System.currentTimeMillis(), "Ana"),
                DeudaConPersona(2L, 120.0, "Uber", TipoDeuda.YO_DEBO, System.currentTimeMillis(), "Luis")
            ),
            onAgregarDeuda = { _, _, _, _ -> },
            onSaldarDeuda = {},
            onBack = {}
        )
    }
}
