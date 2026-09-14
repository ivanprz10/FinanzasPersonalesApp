package com.example.myfinanceskt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myfinanceskt.data.cuotaMensual
import com.example.myfinanceskt.data.local.Categoria
import com.example.myfinanceskt.data.local.CompraPlazosConCuenta
import com.example.myfinanceskt.data.mesesRestantes
import com.example.myfinanceskt.data.saldoPendiente
import com.example.myfinanceskt.data.tieneRecargo
import com.example.myfinanceskt.ui.comoMoneda
import com.example.myfinanceskt.ui.theme.MyFinancesKTTheme

@Composable
fun ComprasPlazosScreen(
    cuotaTotal: Double,
    compras: List<CompraPlazosConCuenta>,
    onPagarCuota: (Long) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("‹  Volver")
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = "Compras a plazos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Tarjeta resumen de la cuota total
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text("Cuota total del mes", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = cuotaTotal.comoMoneda(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = "Por todas tus compras activas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (compras.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tienes compras a plazos activas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(compras, key = { it.id }) { compra ->
                        TarjetaCompraPlazos(
                            compra = compra,
                            onPagarCuota = { onPagarCuota(compra.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaCompraPlazos(
    compra: CompraPlazosConCuenta,
    onPagarCuota: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fila superior: Concepto, cuenta e info de recargo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = compra.emojiPersonalizado ?: compra.categoria.emoji,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = compra.concepto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = compra.cuentaNombre,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (compra.tieneRecargo()) {
                                Text(
                                    text = " • Fijo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = " • MSI",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Cuota",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = compra.cuotaMensual().comoMoneda(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Fila central: Progreso y meses restantes
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${compra.mesesPagados} de ${compra.numeroMeses} pagos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Faltan ${compra.mesesRestantes()} meses",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                val progreso = if (compra.numeroMeses > 0) compra.mesesPagados.toFloat() / compra.numeroMeses.toFloat() else 0f
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Fila inferior: Saldo pendiente y botón
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resta pagar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = compra.saldoPendiente().comoMoneda(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Button(
                    onClick = onPagarCuota,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Pagar cuota")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ComprasPlazosPreview() {
    val compras = listOf(
        CompraPlazosConCuenta(
            id = 1,
            montoOriginal = 12000.0,
            concepto = "Laptop",
            montoTotal = 12000.0,
            numeroMeses = 12,
            mesesPagados = 3,
            categoria = Categoria.OTRO,
            emojiPersonalizado = "💻",
            isMsi = true,
            costoFinanciamiento = 0.0,
            fechaInicio = System.currentTimeMillis(),
            cuentaNombre = "Nu"
        ),
        CompraPlazosConCuenta(
            id = 2,
            montoOriginal = 5000.0,
            concepto = "Muebles",
            montoTotal = 5500.0,
            numeroMeses = 6,
            mesesPagados = 1,
            categoria = Categoria.HOGAR,
            emojiPersonalizado = null,
            isMsi = false,
            costoFinanciamiento = 500.0,
            fechaInicio = System.currentTimeMillis(),
            cuentaNombre = "BBVA"
        )
    )
    
    MyFinancesKTTheme {
        ComprasPlazosScreen(
            cuotaTotal = 1916.66,
            compras = compras,
            onPagarCuota = {},
            onBack = {}
        )
    }
}