package com.example.myfinanceskt

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myfinanceskt.data.local.Categoria
import com.example.myfinanceskt.data.local.Cuenta
import com.example.myfinanceskt.data.local.TipoCuenta
import com.example.myfinanceskt.data.porcentajeRecargo
import com.example.myfinanceskt.ui.etiqueta
import com.example.myfinanceskt.ui.theme.MyFinancesKTTheme

enum class PasoCompra { MONTO, CONCEPTO, CUENTA, MESES, CONFIRMACION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarCompraScreen(
    cuentas: List<Cuenta>,
    onGuardar: (Double, String, Long, Int?, Categoria, String?, Boolean, Double?) -> Unit,
    onBack: () -> Unit
) {
    var pasoActual by rememberSaveable { mutableStateOf(PasoCompra.MONTO) }
    var montoTexto by rememberSaveable { mutableStateOf("") }
    var concepto by rememberSaveable { mutableStateOf("") }
    var categoriaSeleccionada by rememberSaveable { mutableStateOf(Categoria.OTRO) }
    var emojiPersonalizado by rememberSaveable { mutableStateOf("") }
    var cuentaSeleccionadaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pagoAMeses by rememberSaveable { mutableStateOf(false) }
    var esMsi by rememberSaveable { mutableStateOf(true) }
    var totalConInteresesTexto by rememberSaveable { mutableStateOf("") }
    var mesesTexto by rememberSaveable { mutableStateOf("") }

    // toDoubleOrNull -> null si el texto no es un numero valido (no lanza excepcion).
    val monto = montoTexto.replace(',', '.').toDoubleOrNull()
    val cuentaSeleccionada = cuentas.find { it.id == cuentaSeleccionadaId }
    val cuentaEsCredito = cuentaSeleccionada?.tipo == TipoCuenta.CREDITO
    val numeroMeses = if (cuentaEsCredito && pagoAMeses) mesesTexto.toIntOrNull() else null
    val costoFinanciamiento = if (cuentaEsCredito && pagoAMeses && !esMsi) {
        val total = totalConInteresesTexto.replace(',', '.').toDoubleOrNull()
        if (total != null && monto != null && total > monto) total - monto else null
    } else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = pasoActual,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    slideInHorizontally(
                        animationSpec = tween(400),
                        initialOffsetX = { fullWidth -> fullWidth }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(400),
                        targetOffsetX = { fullWidth -> -fullWidth }
                    )
                } else {
                    slideInHorizontally(
                        animationSpec = tween(400),
                        initialOffsetX = { fullWidth -> -fullWidth }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(400),
                        targetOffsetX = { fullWidth -> fullWidth }
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
            label = "transicion_compra"
        ) { paso ->
            when (paso) {
                PasoCompra.MONTO -> TarjetaPaso(
                    titulo = "¿Cuánto gastaste?",
                    descripcion = "Ingresa el monto de la compra.",
                    onAtras = onBack,
                    siguienteHabilitado = monto != null && monto > 0.0,
                    onSiguiente = { pasoActual = PasoCompra.CONCEPTO }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                val actual = montoTexto.replace(',', '.').toDoubleOrNull() ?: 0.0
                                val nuevo = (actual - 10).coerceAtLeast(0.0)
                                montoTexto = if (nuevo == 0.0) "" else if (nuevo % 1.0 == 0.0) nuevo.toInt().toString() else nuevo.toString()
                            }
                        ) {
                            Text("−", style = MaterialTheme.typography.titleLarge)
                        }
                        
                        OutlinedTextField(
                            value = montoTexto,
                            onValueChange = { montoTexto = it },
                            label = { Text("Monto") },
                            prefix = { Text("$") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        FilledTonalIconButton(
                            onClick = {
                                val actual = montoTexto.replace(',', '.').toDoubleOrNull() ?: 0.0
                                val nuevo = actual + 10
                                montoTexto = if (nuevo % 1.0 == 0.0) nuevo.toInt().toString() else nuevo.toString()
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más")
                        }
                    }

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(5, 10, 20, 50, 100, 200, 300, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000).forEach { amt ->
                            OpcionTile(
                                texto = "$$amt",
                                seleccionado = false,
                                onClick = {
                                    val actual = montoTexto.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    val nuevo = actual + amt
                                    montoTexto = if (nuevo % 1.0 == 0.0) nuevo.toInt().toString() else nuevo.toString()
                                }
                            )
                        }
                    }
                }

                PasoCompra.CONCEPTO -> TarjetaPaso(
                    titulo = "¿Qué compraste?",
                    descripcion = "Elige una categoría o escribe un concepto.",
                    onAtras = { pasoActual = PasoCompra.MONTO },
                    siguienteHabilitado = concepto.isNotBlank(),
                    onSiguiente = {
                        if (cuentaSeleccionadaId == null && cuentas.isNotEmpty()) {
                            cuentaSeleccionadaId = cuentas.first().id
                        }
                        pasoActual = PasoCompra.CUENTA
                    }
                ) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Categoria.entries.forEach { categoria ->
                            val esOtro = categoria == Categoria.OTRO
                            OpcionTile(
                                texto = if (esOtro) "➕\nOtro" else "${categoria.emoji}\n${categoria.etiqueta}",
                                seleccionado = categoriaSeleccionada == categoria,
                                onClick = {
                                    categoriaSeleccionada = categoria
                                    if (!esOtro) {
                                        concepto = categoria.etiqueta
                                    }
                                }
                            )
                        }
                    }

                    if (categoriaSeleccionada == Categoria.OTRO) {
                        OutlinedTextField(
                            value = emojiPersonalizado,
                            onValueChange = { emojiPersonalizado = it },
                            label = { Text("Tu emoji (opcional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = concepto,
                        onValueChange = { concepto = it },
                        label = { Text("Concepto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                PasoCompra.CUENTA -> TarjetaPaso(
                    titulo = "¿Con qué cuenta?",
                    descripcion = "Selecciona la cuenta que usaste.",
                    onAtras = { pasoActual = PasoCompra.CONCEPTO },
                    siguienteHabilitado = cuentaSeleccionadaId != null,
                    onSiguiente = {
                        if (cuentas.find { it.id == cuentaSeleccionadaId }?.tipo == TipoCuenta.CREDITO) {
                            pasoActual = PasoCompra.MESES
                        } else {
                            pagoAMeses = false
                            mesesTexto = ""
                            esMsi = true
                            totalConInteresesTexto = ""
                            pasoActual = PasoCompra.CONFIRMACION
                        }
                    }
                ) {
                    if (cuentas.isEmpty()) {
                        Text(
                            text = "Cargando cuentas...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            cuentas.forEach { cuenta ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (cuentaSeleccionadaId == cuenta.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { cuentaSeleccionadaId = cuenta.id }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = cuentaSeleccionadaId == cuenta.id,
                                            onClick = { cuentaSeleccionadaId = cuenta.id }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cuenta.etiqueta(),
                                            fontWeight = if (cuentaSeleccionadaId == cuenta.id) FontWeight.Bold else FontWeight.Normal,
                                            color = if (cuentaSeleccionadaId == cuenta.id) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                PasoCompra.MESES -> {
                    val mesesValido = !pagoAMeses || (numeroMeses != null && numeroMeses > 1 && (esMsi || costoFinanciamiento != null))
                    TarjetaPaso(
                        titulo = "¿Modalidad de pago?",
                        descripcion = "Elige si la compra fue a meses.",
                        onAtras = { pasoActual = PasoCompra.CUENTA },
                        siguienteHabilitado = mesesValido,
                        onSiguiente = { pasoActual = PasoCompra.CONFIRMACION }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !pagoAMeses,
                                onClick = { pagoAMeses = false },
                                label = { 
                                    Text(
                                        text = "Una exhibición",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = pagoAMeses,
                                onClick = { pagoAMeses = true },
                                label = { 
                                    Text(
                                        text = "A plazos",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pagoAMeses) {
                            OutlinedTextField(
                                value = mesesTexto,
                                onValueChange = { mesesTexto = it },
                                label = { Text("Número de meses") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "¿Es a Meses Sin Intereses (MSI)?",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = esMsi,
                                    onCheckedChange = { esMsi = it }
                                )
                            }

                            if (!esMsi) {
                                OutlinedTextField(
                                    value = totalConInteresesTexto,
                                    onValueChange = { totalConInteresesTexto = it },
                                    label = { Text("Total final a pagar (con intereses)") },
                                    prefix = { Text("$") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                val totalConIntereses = totalConInteresesTexto.replace(',', '.').toDoubleOrNull()
                                if (totalConIntereses != null && monto != null && totalConIntereses > monto) {
                                    val recargo = totalConIntereses - monto
                                    val porcentaje = porcentajeRecargo(monto, recargo)
                                    Text(
                                        text = "Esto es %.1f%% más caro que pagarlo de contado".format(porcentaje),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                PasoCompra.CONFIRMACION -> TarjetaPaso(
                    titulo = "Confirma tu gasto",
                    descripcion = "Verifica que los datos sean correctos.",
                    textoBotonSiguiente = "Guardar",
                    onAtras = {
                        if (cuentaEsCredito) pasoActual = PasoCompra.MESES
                        else pasoActual = PasoCompra.CUENTA
                    },
                    siguienteHabilitado = true,
                    onSiguiente = {
                        val emojiFinal = if (categoriaSeleccionada == Categoria.OTRO) {
                            emojiPersonalizado.trim().ifBlank { null }
                        } else {
                            null
                        }
                        onGuardar(
                            monto!!,
                            concepto,
                            cuentaSeleccionadaId!!,
                            numeroMeses,
                            categoriaSeleccionada,
                            emojiFinal,
                            esMsi,
                            costoFinanciamiento
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val emojiAMostrar = if (categoriaSeleccionada == Categoria.OTRO && emojiPersonalizado.isNotBlank()) emojiPersonalizado else categoriaSeleccionada.emoji
                        ResumenCompraFila("Monto", "$$montoTexto")
                        ResumenCompraFila("Concepto", "$emojiAMostrar $concepto")
                        ResumenCompraFila("Cuenta", cuentaSeleccionada?.etiqueta() ?: "")
                        if (cuentaEsCredito && pagoAMeses) {
                            ResumenCompraFila("Modalidad", "$numeroMeses meses ${if(esMsi) "(MSI)" else "(Fijo)"}")
                            if (!esMsi && costoFinanciamiento != null) {
                                ResumenCompraFila("Intereses", "$$costoFinanciamiento")
                            }
                        } else {
                            ResumenCompraFila("Modalidad", "Una exhibición")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenCompraFila(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun OpcionTile(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(76.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (seleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (seleccionado) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Clip,
                color = if (seleccionado) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            onGuardar = { _, _, _, _, _, _, _, _ -> },
            onBack = {}
        )
    }
}