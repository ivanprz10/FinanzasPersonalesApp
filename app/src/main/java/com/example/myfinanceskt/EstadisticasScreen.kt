package com.example.myfinanceskt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myfinanceskt.data.local.Categoria
import com.example.myfinanceskt.data.local.GastoConCuenta
import com.example.myfinanceskt.data.local.TipoCuenta
import com.example.myfinanceskt.ui.comoFecha
import com.example.myfinanceskt.ui.comoMoneda
import com.example.myfinanceskt.ui.theme.MyFinancesKTTheme

/**
 * Lista de gastos + total. Recibe datos ya resueltos (la pantalla no sabe de Room).
 */
@Composable
fun EstadisticasScreen(
    total: Double,
    gastos: List<GastoConCuenta>,
    onBack: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                TextButton(onClick = onBack) { Text("‹  Volver") }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = "Estadisticas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Total gastado", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = total.comoMoneda(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (gastos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Aun no hay gastos registrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // LazyColumn = RecyclerView de Compose: solo dibuja lo visible.
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(gastos, key = { it.id }) { gasto ->
                        FilaGasto(gasto)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaGasto(gasto: GastoConCuenta) {
    val etiquetaCuenta = when (gasto.cuentaTipo) {
        TipoCuenta.EFECTIVO -> "Efectivo"
        TipoCuenta.DEBITO -> "Debito ${gasto.cuentaNombre}"
        TipoCuenta.CREDITO -> "Credito ${gasto.cuentaNombre}"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = gasto.emojiPersonalizado ?: gasto.categoria.emoji,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(gasto.concepto, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = "$etiquetaCuenta  ·  ${gasto.fechaEpochMillis.comoFecha()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = gasto.monto.comoMoneda(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EstadisticasPreview() {
    MyFinancesKTTheme {
        EstadisticasScreen(
            total = 570.0,
            gastos = listOf(
                GastoConCuenta(1L, 120.0, "Super", Categoria.SUPER, null, System.currentTimeMillis(), "BBVA", TipoCuenta.DEBITO),
                GastoConCuenta(2L, 450.0, "Cena", Categoria.COMIDA, null, System.currentTimeMillis(), "Nu", TipoCuenta.CREDITO)
            ),
            onBack = {}
        )
    }
}
