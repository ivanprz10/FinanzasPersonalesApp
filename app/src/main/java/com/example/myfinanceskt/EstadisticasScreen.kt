package com.example.myfinanceskt

import androidx.compose.foundation.background
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
import com.example.myfinanceskt.data.local.*
import com.example.myfinanceskt.ui.comoFecha
import com.example.myfinanceskt.ui.comoMoneda
import com.example.myfinanceskt.ui.theme.MyFinancesKTTheme
import com.example.myfinanceskt.ui.theme.SaldoAFavor
import com.example.myfinanceskt.ui.theme.SaldoAFavorContainer

@Composable
fun EstadisticasScreen(
    total: Double,
    gastoDelMesActual: Double,
    disponibleMensual: Double,
    trabaja: Boolean,
    gastoPorCategoria: List<GastoPorCategoria>,
    gastoPorCuenta: List<GastoPorCuenta>,
    gastoPorMes: List<GastoPorMes>,
    gastos: List<GastoConCuenta>,
    onVerComprasPlazos: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack, modifier = Modifier.offset(x = (-8).dp)) {
                        Text("‹  Volver")
                    }
                }
                Text(
                    text = "Flujo de caja",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 0. Tarjeta: Disponible este mes
            item {
                val colorContainer = if (disponibleMensual >= 0) SaldoAFavorContainer else MaterialTheme.colorScheme.errorContainer
                val colorContent = if (disponibleMensual >= 0) SaldoAFavor else MaterialTheme.colorScheme.error
                
                ElevatedCard(
                    onClick = onVerComprasPlazos,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = colorContainer,
                        contentColor = colorContent
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text("Disponible este mes", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = disponibleMensual.comoMoneda(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (!trabaja) {
                            Text(
                                text = "Agrega tus ingresos en tu perfil para un cálculo más preciso",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorContent.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // 1. Tarjeta principal: Gasto del mes vs Total
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text("Gastado este mes", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = gastoDelMesActual.comoMoneda(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = "Histórico total: ${total.comoMoneda()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // 2. Evolución (Gráfica de barras simple)
            if (gastoPorMes.isNotEmpty()) {
                item {
                    Text(
                        text = "Evolución mensual",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val maxGasto = gastoPorMes.maxOfOrNull { it.total }?.takeIf { it > 0 } ?: 1.0
                            // Mostrar solo los últimos 6 meses para que no se apriete demasiado
                            val ultimosMeses = gastoPorMes.takeLast(6)
                            
                            ultimosMeses.forEach { mesData ->
                                val proporcion = (mesData.total / maxGasto).toFloat()
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .fillMaxHeight(proporcion.coerceAtLeast(0.05f))
                                            .width(28.dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        // Extrae "09" de "2026-09"
                                        text = mesData.mes.takeLast(2),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Gastos por categoría
            if (gastoPorCategoria.isNotEmpty()) {
                item {
                    Text(
                        text = "Top categorías",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val maxCategoria = gastoPorCategoria.maxOfOrNull { it.total }?.takeIf { it > 0 } ?: 1.0
                            
                            gastoPorCategoria.take(5).forEach { catData ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(catData.categoria.emoji, modifier = Modifier.padding(end = 8.dp))
                                            Text(
                                                text = catData.categoria.etiqueta,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Text(
                                            text = catData.total.comoMoneda(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { (catData.total / maxCategoria).toFloat() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Dónde gastas más (Cuentas)
            if (gastoPorCuenta.isNotEmpty()) {
                item {
                    Text(
                        text = "Cuentas más usadas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            gastoPorCuenta.take(3).forEach { cuentaData ->
                                val etiquetaTipo = when(cuentaData.cuentaTipo) {
                                    TipoCuenta.EFECTIVO -> "Efectivo"
                                    TipoCuenta.DEBITO -> "Débito"
                                    TipoCuenta.CREDITO -> "Crédito"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = cuentaData.cuentaNombre,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = etiquetaTipo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = cuentaData.total.comoMoneda(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Historial reciente
            if (gastos.isNotEmpty()) {
                item {
                    Text(
                        text = "Historial reciente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(gastos.take(10), key = { it.id }) { gasto ->
                    FilaGasto(gasto)
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aún no hay gastos registrados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        TipoCuenta.DEBITO -> "Débito ${gasto.cuentaNombre}"
        TipoCuenta.CREDITO -> "Crédito ${gasto.cuentaNombre}"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = gasto.emojiPersonalizado ?: gasto.categoria.emoji,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    text = gasto.concepto,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
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
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EstadisticasPreview() {
    MyFinancesKTTheme {
        EstadisticasScreen(
            total = 10570.0,
            gastoDelMesActual = 3450.0,
            disponibleMensual = 1550.0,
            trabaja = true,
            gastoPorCategoria = listOf(
                GastoPorCategoria(Categoria.SUPER, 1500.0),
                GastoPorCategoria(Categoria.COMIDA, 800.0),
                GastoPorCategoria(Categoria.TRANSPORTE, 450.0)
            ),
            gastoPorCuenta = listOf(
                GastoPorCuenta(2L, "Nu", TipoCuenta.CREDITO, 2500.0),
                GastoPorCuenta(1L, "Efectivo", TipoCuenta.EFECTIVO, 950.0)
            ),
            gastoPorMes = listOf(
                GastoPorMes("2026-05", 1200.0),
                GastoPorMes("2026-06", 1800.0),
                GastoPorMes("2026-07", 2100.0),
                GastoPorMes("2026-08", 2900.0),
                GastoPorMes("2026-09", 3450.0)
            ),
            gastos = listOf(
                GastoConCuenta(1L, 120.0, "Super", Categoria.SUPER, null, System.currentTimeMillis(), "BBVA", TipoCuenta.DEBITO),
                GastoConCuenta(2L, 450.0, "Cena", Categoria.COMIDA, null, System.currentTimeMillis(), "Nu", TipoCuenta.CREDITO)
            ),
            onVerComprasPlazos = {},
            onBack = {}
        )
    }
}
