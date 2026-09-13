package com.example.myfinanceskt

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myfinanceskt.data.FrecuenciaPago
import com.example.myfinanceskt.data.local.TipoCuenta
import com.example.myfinanceskt.viewmodel.FinanzasViewModel
import com.example.myfinanceskt.viewmodel.OnboardingViewModel

enum class PasoOnboarding { NOMBRE, INGRESOS, TARJETAS_DEBITO, TARJETAS_CREDITO, RESUMEN }

@Composable
fun OnboardingScreen(
    onboardingViewModel: OnboardingViewModel,
    finanzasViewModel: FinanzasViewModel
) {
    var pasoActual by rememberSaveable { mutableStateOf(PasoOnboarding.NOMBRE) }

    // Fondo limpio para hacer resaltar la tarjeta minimalista
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = pasoActual,
            transitionSpec = {
                // Compara el índice (ordinal) del enum para saber si desliza a la izquierda o derecha
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
            label = "transicion_tarjetas"
        ) { paso ->
            when (paso) {
                PasoOnboarding.NOMBRE -> PasoNombre(
                    onboardingViewModel = onboardingViewModel,
                    onSiguiente = { pasoActual = PasoOnboarding.INGRESOS }
                )
                PasoOnboarding.INGRESOS -> PasoIngresos(
                    onboardingViewModel = onboardingViewModel,
                    onSiguiente = { pasoActual = PasoOnboarding.TARJETAS_DEBITO },
                    onAtras = { pasoActual = PasoOnboarding.NOMBRE }
                )
                PasoOnboarding.TARJETAS_DEBITO -> PasoTarjetasDebito(
                    finanzasViewModel = finanzasViewModel,
                    onSiguiente = { pasoActual = PasoOnboarding.TARJETAS_CREDITO },
                    onAtras = { pasoActual = PasoOnboarding.INGRESOS }
                )
                PasoOnboarding.TARJETAS_CREDITO -> PasoTarjetasCredito(
                    finanzasViewModel = finanzasViewModel,
                    onSiguiente = { pasoActual = PasoOnboarding.RESUMEN },
                    onAtras = { pasoActual = PasoOnboarding.TARJETAS_DEBITO }
                )
                PasoOnboarding.RESUMEN -> PasoResumen(
                    onboardingViewModel = onboardingViewModel,
                    finanzasViewModel = finanzasViewModel,
                    onAtras = { pasoActual = PasoOnboarding.TARJETAS_CREDITO }
                )
            }
        }
    }
}

/**
 * Componente base que define el diseño y comportamiento estándar de la tarjeta.
 * Usar para cualquier flujo de múltiples pasos (onboarding, creación de tarjetas complejas, etc).
 */
@Composable
fun TarjetaPaso(
    titulo: String,
    descripcion: String? = null,
    onSiguiente: () -> Unit,
    onAtras: (() -> Unit)? = null,
    siguienteHabilitado: Boolean = true,
    textoBotonSiguiente: String = "Siguiente",
    contenido: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (descripcion != null) {
                        Text(
                            text = descripcion,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Aquí se inyecta el contenido específico de cada paso
                contenido()

                // Fila de botones principales del paso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onAtras != null) {
                        TextButton(onClick = onAtras) {
                            Text("Atrás")
                        }
                        Spacer(Modifier.width(16.dp))
                    }
                    Button(
                        onClick = onSiguiente,
                        enabled = siguienteHabilitado,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(textoBotonSiguiente, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PasoNombre(
    onboardingViewModel: OnboardingViewModel,
    onSiguiente: () -> Unit
) {
    val currentName by onboardingViewModel.userName.collectAsState()
    var name by rememberSaveable { mutableStateOf(currentName) }

    LaunchedEffect(currentName) {
        if (name.isBlank() && currentName.isNotBlank()) name = currentName
    }

    TarjetaPaso(
        titulo = "¿Cómo te llamas?",
        descripcion = "Tu dinero, 100% en tu teléfono. Sin servidores.",
        siguienteHabilitado = name.trim().isNotEmpty(),
        onSiguiente = {
            onboardingViewModel.guardarNombre(name.trim())
            onSiguiente()
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tu nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoIngresos(
    onboardingViewModel: OnboardingViewModel,
    onSiguiente: () -> Unit,
    onAtras: () -> Unit
) {
    val savedTrabaja by onboardingViewModel.trabaja.collectAsState()
    val savedGanancia by onboardingViewModel.gananciaNeta.collectAsState()
    val savedFrecuencia by onboardingViewModel.frecuenciaPago.collectAsState()

    var trabaja by rememberSaveable { mutableStateOf(savedTrabaja) }
    var gananciaStr by rememberSaveable { mutableStateOf(if (savedGanancia > 0) savedGanancia.toString() else "") }
    var frecuencia by rememberSaveable { mutableStateOf(savedFrecuencia ?: FrecuenciaPago.MENSUAL) }

    val gananciaDbl = gananciaStr.replace(',', '.').toDoubleOrNull() ?: 0.0
    val isValid = !trabaja || gananciaDbl > 0

    TarjetaPaso(
        titulo = "Tus ingresos",
        descripcion = "Para ayudarte a planear tu presupuesto.",
        onAtras = onAtras,
        siguienteHabilitado = isValid,
        onSiguiente = {
            if (trabaja) {
                onboardingViewModel.guardarIngresos(true, gananciaDbl, frecuencia)
            } else {
                onboardingViewModel.guardarIngresos(false, 0.0, null)
            }
            onSiguiente()
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "¿Trabajas actualmente?",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            Switch(checked = trabaja, onCheckedChange = { trabaja = it })
        }

        if (trabaja) {
            OutlinedTextField(
                value = gananciaStr,
                onValueChange = { gananciaStr = it },
                label = { Text("Ganancia neta (ej. $15000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Frecuencia de pago:", style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = frecuencia == FrecuenciaPago.MENSUAL,
                    onClick = { frecuencia = FrecuenciaPago.MENSUAL },
                    label = { Text("Mensual") }
                )
                FilterChip(
                    selected = frecuencia == FrecuenciaPago.QUINCENAL,
                    onClick = { frecuencia = FrecuenciaPago.QUINCENAL },
                    label = { Text("Quincenal") }
                )
            }
        }
    }
}

@Composable
fun PasoTarjetasDebito(
    finanzasViewModel: FinanzasViewModel,
    onSiguiente: () -> Unit,
    onAtras: () -> Unit
) {
    val cuentas by finanzasViewModel.cuentas.collectAsState()
    val tarjetasDebito = cuentas.filter { it.tipo == TipoCuenta.DEBITO }
    var nombreNueva by rememberSaveable { mutableStateOf("") }

    TarjetaPaso(
        titulo = "Cuentas de Débito",
        descripcion = "Agrega tus cuentas de débito o ahorro.",
        onAtras = onAtras,
        onSiguiente = onSiguiente,
        siguienteHabilitado = true
    ) {
        if (tarjetasDebito.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tarjetasDebito.forEach { cuenta ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cuenta.nombre,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nombreNueva,
                onValueChange = { nombreNueva = it },
                label = { Text("Nueva cuenta (ej. Nu)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            IconButton(
                onClick = {
                    if (nombreNueva.isNotBlank()) {
                        finanzasViewModel.agregarCuenta(nombreNueva.trim(), TipoCuenta.DEBITO)
                        nombreNueva = ""
                    }
                },
                enabled = nombreNueva.isNotBlank(),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (nombreNueva.isNotBlank()) MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = "Agregar tarjeta",
                    tint = if (nombreNueva.isNotBlank()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PasoTarjetasCredito(
    finanzasViewModel: FinanzasViewModel,
    onSiguiente: () -> Unit,
    onAtras: () -> Unit
) {
    val cuentas by finanzasViewModel.cuentas.collectAsState()
    val tarjetasCredito = cuentas.filter { it.tipo == TipoCuenta.CREDITO }
    var mostrandoFormulario by rememberSaveable { mutableStateOf(false) }

    var nombreNueva by rememberSaveable { mutableStateOf("") }
    var diaCorteStr by rememberSaveable { mutableStateOf("") }
    var diasPagoStr by rememberSaveable { mutableStateOf("") }
    var tieneMSI by rememberSaveable { mutableStateOf(false) }

    TarjetaPaso(
        titulo = "Tarjetas de Crédito",
        descripcion = "Controla tus gastos y compras a meses.",
        onAtras = onAtras,
        onSiguiente = onSiguiente,
        siguienteHabilitado = !mostrandoFormulario
    ) {
        if (tarjetasCredito.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tarjetasCredito.forEach { cuenta ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cuenta.nombre,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (mostrandoFormulario) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "Nueva tarjeta",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = nombreNueva,
                    onValueChange = { nombreNueva = it },
                    label = { Text("Nombre de la tarjeta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = diaCorteStr,
                        onValueChange = { diaCorteStr = it },
                        label = { Text("Día corte") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = diasPagoStr,
                        onValueChange = { diasPagoStr = it },
                        label = { Text("Días límite") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Tiene compras a meses?", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = tieneMSI, onCheckedChange = { tieneMSI = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { mostrandoFormulario = false }) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nombreNueva.isNotBlank()) {
                                finanzasViewModel.agregarCuenta(
                                    nombre = nombreNueva.trim(),
                                    tipo = TipoCuenta.CREDITO,
                                    diaCorte = diaCorteStr.toIntOrNull(),
                                    diasParaPago = diasPagoStr.toIntOrNull(),
                                    tieneComprasAMeses = tieneMSI
                                )
                                nombreNueva = ""
                                diaCorteStr = ""
                                diasPagoStr = ""
                                tieneMSI = false
                                mostrandoFormulario = false
                            }
                        },
                        enabled = nombreNueva.isNotBlank()
                    ) {
                        Text("Agregar")
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = { mostrandoFormulario = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Añadir tarjeta de crédito")
            }
        }
    }
}

@Composable
fun PasoResumen(
    onboardingViewModel: OnboardingViewModel,
    finanzasViewModel: FinanzasViewModel,
    onAtras: () -> Unit
) {
    val name by onboardingViewModel.userName.collectAsState()
    val trabaja by onboardingViewModel.trabaja.collectAsState()
    val cuentas by finanzasViewModel.cuentas.collectAsState()
    val debitoCount = cuentas.count { it.tipo == TipoCuenta.DEBITO }
    val creditoCount = cuentas.count { it.tipo == TipoCuenta.CREDITO }

    TarjetaPaso(
        titulo = "¡Todo listo!",
        descripcion = "Resumen de tu perfil.",
        textoBotonSiguiente = "Empezar",
        onAtras = onAtras,
        onSiguiente = { onboardingViewModel.marcarPerfilCompleto() }
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
            ResumenFila("Nombre", name)
            ResumenFila("Trabaja", if (trabaja) "Sí" else "No")
            ResumenFila("Tarjetas Débito", debitoCount.toString())
            ResumenFila("Tarjetas Crédito", creditoCount.toString())
        }
    }
}

@Composable
private fun ResumenFila(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
