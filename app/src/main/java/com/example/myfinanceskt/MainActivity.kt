package com.example.myfinanceskt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfinanceskt.data.FinanzasRepository
import com.example.myfinanceskt.data.UserPreferencesRepository
import com.example.myfinanceskt.data.local.AppDatabase
import com.example.myfinanceskt.data.calcularDisponibleMensual
import com.example.myfinanceskt.ui.theme.HomeBackground
import com.example.myfinanceskt.ui.theme.IconBlueCobalt
import com.example.myfinanceskt.ui.theme.IconBlueNavy
import com.example.myfinanceskt.ui.theme.IconBlueSteel
import com.example.myfinanceskt.ui.theme.MyFinancesKTTheme
import com.example.myfinanceskt.viewmodel.FinanzasViewModel
import com.example.myfinanceskt.viewmodel.FinanzasViewModelFactory
import com.example.myfinanceskt.viewmodel.OnboardingViewModel
import com.example.myfinanceskt.viewmodel.OnboardingViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Punto de entrada de la app.
 *
 * Java -> Kotlin:
 * - "class MainActivity : ComponentActivity()" equivale a "extends ComponentActivity".
 * - "override fun" es "@Override + metodo".
 * - No hay "new"; se instancian clases llamandolas como funcion (ej. Bundle()).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // applicationContext = vive tanto como la app, asi evitamos fugas de memoria.
        val userPrefs = UserPreferencesRepository(applicationContext)
        val db = AppDatabase.getInstance(applicationContext)
        val finanzas = FinanzasRepository(
            db.cuentaDao(), db.gastoDao(), db.personaDao(), db.deudaDao(), db.compraPlazosDao()
        )

        setContent {
            MyFinancesKTTheme {
                MyFinancesApp(userPrefs, finanzas)
            }
        }
    }
}

/**
 * Catalogo de pantallas. Un "enum class" de Kotlin es igual que el de Java.
 * Mas adelante esto se puede migrar a Navigation-Compose; por ahora basta un enum.
 */
enum class Pantalla { SPLASH, HOME, ESTADISTICAS, GRUPOS, COMPRA, COMPRAS_PLAZOS }

/**
 * Decide que mostrar segun si ya hay un nombre guardado.
 * "collectAsState" convierte el Flow del repository en un State observable:
 * cuando DataStore emite un valor nuevo, esta funcion se recompone sola.
 * - null -> aun leyendo disco (spinner)
 * - ""   -> primera vez: Onboarding
 * - "Ivan" -> ya configurado: navegacion normal
 */
@Composable
fun MyFinancesApp(
    userPrefs: UserPreferencesRepository,
    finanzas: FinanzasRepository
) {
    val perfilCompleto by userPrefs.perfilCompleto.collectAsState(initial = null)
    val storedName by userPrefs.userName.collectAsState(initial = "")
    val scope = rememberCoroutineScope()

    when (perfilCompleto) {
        null -> LoadingScreen()
        false -> {
            val onboardingFactory = OnboardingViewModelFactory(userPrefs)
            val onboardingVm: OnboardingViewModel = viewModel(factory = onboardingFactory)
            
            val finanzasFactory = FinanzasViewModelFactory(finanzas)
            val finanzasVm: FinanzasViewModel = viewModel(factory = finanzasFactory)
            
            OnboardingScreen(
                onboardingViewModel = onboardingVm,
                finanzasViewModel = finanzasVm
            )
        }
        true -> AppNavigation(userName = storedName, userPrefs = userPrefs, finanzas = finanzas)
    }
}

/**
 * Navegacion sencilla basada en estado.
 * "var actual by rememberSaveable { mutableStateOf(Pantalla.HOME) }":
 *   - mutableStateOf -> valor observable; al cambiarlo, Compose redibuja.
 *   - rememberSaveable -> lo conserva si giras la pantalla o el proceso muere.
 */
@Composable
fun AppNavigation(userName: String, userPrefs: UserPreferencesRepository, finanzas: FinanzasRepository) {
    var actual by rememberSaveable { mutableStateOf(Pantalla.SPLASH) }
    val scope = rememberCoroutineScope()

    // Datos de Room, observados como State. initial = valor mientras llega el primero.
    val cuentas by finanzas.cuentas.collectAsState(initial = emptyList())
    val gastos by finanzas.gastos.collectAsState(initial = emptyList())
    val total by finanzas.totalGastado.collectAsState(initial = 0.0)
    val gastoDelMesActual by finanzas.gastoDelMesActual.collectAsState(initial = 0.0)
    val gastoPorCategoria by finanzas.gastoPorCategoria.collectAsState(initial = emptyList())
    val gastoPorCuenta by finanzas.gastoPorCuenta.collectAsState(initial = emptyList())
    val gastoPorMes by finanzas.gastoPorMes.collectAsState(initial = emptyList())
    val deudas by finanzas.deudasActivas.collectAsState(initial = emptyList())
    val totalTeDeben by finanzas.totalTeDeben.collectAsState(initial = 0.0)
    val totalDebo by finanzas.totalDebo.collectAsState(initial = 0.0)
    val comprasPlazosActivas by finanzas.comprasPlazosActivas.collectAsState(initial = emptyList())

    val trabaja by userPrefs.trabaja.collectAsState(initial = false)
    val gananciaNeta by userPrefs.gananciaNeta.collectAsState(initial = 0.0)
    val frecuenciaPago by userPrefs.frecuenciaPago.collectAsState(initial = null)
    val cuotaMensualTotalPlazos by finanzas.cuotaMensualTotalPlazos.collectAsState(initial = 0.0)

    val disponibleMensual = calcularDisponibleMensual(
        trabaja = trabaja,
        gananciaNeta = gananciaNeta,
        frecuenciaPago = frecuenciaPago,
        cuotaMensualTotalPlazos = cuotaMensualTotalPlazos,
        gastoDelMesActual = gastoDelMesActual
    )

    // Boton "atras" del sistema: si no estamos en HOME, vuelve a HOME.
    BackHandler(enabled = actual != Pantalla.HOME) { actual = Pantalla.HOME }

    // Crossfade = transicion suave (fundido) al cambiar de pantalla.
    // tween(280) -> dura 280 ms.
    Crossfade(
        targetState = actual,
        animationSpec = tween(durationMillis = 280),
        label = "navegacion"
    ) { pantalla ->
        when (pantalla) {
            Pantalla.SPLASH -> AnimatedSplashScreen(
                userName = userName,
                onFinish = { actual = Pantalla.HOME }
            )
            Pantalla.HOME -> HomeScreen(
                userName = userName,
                onVerEstadisticas = { actual = Pantalla.ESTADISTICAS },
                onCuentasGrupo = { actual = Pantalla.GRUPOS },
                onRealiceCompra = { actual = Pantalla.COMPRA }
            )
            Pantalla.ESTADISTICAS -> EstadisticasScreen(
                total = total,
                gastoDelMesActual = gastoDelMesActual,
                disponibleMensual = disponibleMensual,
                trabaja = trabaja,
                gastoPorCategoria = gastoPorCategoria,
                gastoPorCuenta = gastoPorCuenta,
                gastoPorMes = gastoPorMes,
                gastos = gastos,
                onVerComprasPlazos = { actual = Pantalla.COMPRAS_PLAZOS },
                onBack = { actual = Pantalla.HOME }
            )
            Pantalla.COMPRAS_PLAZOS -> ComprasPlazosScreen(
                cuotaTotal = cuotaMensualTotalPlazos,
                compras = comprasPlazosActivas,
                onPagarCuota = { compraId ->
                    scope.launch { finanzas.marcarMesPagadoPlazos(compraId) }
                },
                onBack = { actual = Pantalla.ESTADISTICAS }
            )
            Pantalla.GRUPOS -> GruposScreen(
                totalTeDeben = totalTeDeben,
                totalDebo = totalDebo,
                deudas = deudas,
                onAgregarDeuda = { persona, monto, concepto, tipo ->
                    scope.launch { finanzas.registrarDeuda(persona, monto, concepto, tipo) }
                },
                onSaldarDeuda = { deudaId ->
                    scope.launch { finanzas.marcarDeudaSaldada(deudaId) }
                },
                onBack = { actual = Pantalla.HOME }
            )
            Pantalla.COMPRA -> RegistrarCompraScreen(
                cuentas = cuentas,
                onGuardar = { monto, concepto, cuentaId, numeroMeses, categoria, emojiPersonalizado, esMsi, costoFinanciamiento ->
                    scope.launch { 
                        finanzas.registrarCompra(
                            monto = monto, 
                            concepto = concepto, 
                            cuentaId = cuentaId, 
                            numeroMeses = numeroMeses, 
                            categoria = categoria, 
                            emojiPersonalizado = emojiPersonalizado,
                            isMsi = esMsi,
                            costoFinanciamiento = costoFinanciamiento ?: 0.0
                        ) 
                    }
                    actual = Pantalla.HOME
                },
                onBack = { actual = Pantalla.HOME }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Pantalla de inicio: saludo centrado + 3 acciones grandes.
 * Todo dentro de una Column centrada vertical y horizontalmente.
 */
@Composable
fun HomeScreen(
    userName: String,
    onVerEstadisticas: () -> Unit,
    onCuentasGrupo: () -> Unit,
    onRealiceCompra: () -> Unit
) {
    // Fondo azul en toda la pantalla (tambien detras de la barra de estado).
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HomeBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hola, $userName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            
            Spacer(Modifier.height(24.dp))
            TarjetaDeudasGrupales()
            Spacer(Modifier.height(32.dp))

            Text(
                text = "¿Qué quieres hacer hoy?",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(16.dp))

            ActionCard(
                emoji = "📊",
                titulo = "Ver estadísticas",
                descripcion = "Gastos, ingresos y balance",
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = IconBlueNavy,
                onClick = onVerEstadisticas
            )
            Spacer(Modifier.height(14.dp))
            ActionCard(
                emoji = "👥",
                titulo = "Cuentas de grupo",
                descripcion = "Quién te debe y a quién le debes",
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = IconBlueCobalt,
                onClick = onCuentasGrupo
            )
            Spacer(Modifier.height(14.dp))
            ActionCard(
                emoji = "🛒",
                titulo = "Realicé una compra",
                descripcion = "Registra un gasto en segundos",
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                iconColor = IconBlueSteel,
                onClick = onRealiceCompra
            )
        }
    }
}

/**
 * Tarjeta-boton casi cuadrada: recuadro de icono azul centrado arriba,
 * texto centrado debajo. Esquinas redondeadas suaves (no 100% rectas).
 *
 * @param iconColor "otro tipo de azul" para el recuadro del emoji.
 */
@Composable
private fun ActionCard(
    emoji: String,
    titulo: String,
    descripcion: String,
    containerColor: Color,
    contentColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth(0.74f)          // un poco menos anchas
            .widthIn(max = 300.dp)
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = contentColor.copy(alpha = 0.75f)
            )
        }
    }
}

/**
 * Pantalla temporal para los destinos que aun no existen.
 * Tiene un boton "Volver" arriba y contenido centrado.
 */
@Composable
fun PlaceholderScreen(emoji: String, titulo: String, onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HomeBackground
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                TextButton(
                    onClick = onBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) { Text("‹  Volver") }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                Text(text = emoji, fontSize = 56.sp)
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Proximamente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AnimatedSplashScreen(userName: String, onFinish: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "hand_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hand_rotation"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(3000)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            Text(
                text = "👋",
                fontSize = 72.sp,
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation
                    transformOrigin = TransformOrigin(0.7f, 0.7f)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¡Hola, $userName!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomePreview() {
    MyFinancesKTTheme {
        HomeScreen(
            userName = "Ivan",
            onVerEstadisticas = {},
            onCuentasGrupo = {},
            onRealiceCompra = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlaceholderPreview() {
    MyFinancesKTTheme {
        PlaceholderScreen(emoji = "📊", titulo = "Estadisticas", onBack = {})
    }
}

@Composable
fun TarjetaDeudasGrupales(modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Balance de Grupos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Columna Izquierda: Lo que te deben
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Te deben",
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Te deben",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$350.00",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }

                // Columna Derecha: Lo que debes
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Debes",
                            tint = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Debes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$120.00",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}
