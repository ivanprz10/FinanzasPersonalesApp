# Contexto del proyecto — MyfinancesKT

> Este archivo es el "handoff" entre Claude (backend) y Gemini/el desarrollador (UI).
> Si estás retomando el proyecto en una sesión nueva, pega este archivo completo
> al inicio para que el asistente tenga el contexto sin tener que re-explicarlo.
>
> Claude lo mantiene actualizado en cada sesión. Última actualización: 2026-09-13.

## Qué es la app

Gestor financiero personal **100% local y privado** para Android (Kotlin + Jetpack
Compose + Room). Sin servidores, sin cuentas, sin nube. Módulos: onboarding con
perfil de ingresos, registro manual de gastos (incluye compras a meses/plazos con
o sin intereses), y deudas grupales (quién te debe / a quién le debes).

## División de trabajo (regla fija, no cambiarla sin acuerdo explícito)

- **Claude**: Room (entidades, DAOs, `AppDatabase`), repositorios, ViewModels,
  y toda la lógica matemática/de negocio (cálculos de saldos, cuotas, flujo de caja).
- **Usuario + Gemini** (el asistente nativo de Android Studio): toda la capa visual
  en Compose — pantallas, navegación, temas, animaciones.
- Cuando un cambio de Claude obliga a tocar un archivo de UI (ej. una firma de
  función cambió), Claude solo **reporta** qué línea hay que cambiar; no la edita
  él mismo, salvo cableado trivial de una sola línea en la instanciación de
  dependencias (`onCreate()` de `MainActivity`).
- Cuando Claude revisa lógica que Gemini/el usuario aplicaron, **solo reporta**
  bugs encontrados — no los corrige directamente, aunque sean triviales.

## Arquitectura de datos (dueño: Claude)

### Entidades Room (`data/local/`)
- **`Cuenta`**: `id, nombre, tipo (TipoCuenta: EFECTIVO/DEBITO/CREDITO), diaCorte?, diasParaPago?, tieneComprasAMeses?`. Los últimos 3 campos son nullable, solo aplican a `CREDITO`.
- **`Gasto`**: `id, monto, concepto, categoria (Categoria, default OTRO), emojiPersonalizado?, fechaEpochMillis, cuentaId (FK)`. Es un gasto de una sola exhibición ya "consumado".
- **`Categoria`** (enum, no es tabla propia): cada valor trae `etiqueta` + `emoji` juntos (COMIDA🍔, SUPER🛒, TRANSPORTE🚌, GASOLINA⛽, SERVICIOS🧾, ENTRETENIMIENTO🎬, SALUD💊, HOGAR🏠, ROPA👕, EDUCACION📚, OTRO💳).
- **`Persona`**: `id, nombre` — para deudas grupales.
- **`Deuda`**: `id, personaId (FK), monto, concepto, tipo (TipoDeuda: TE_DEBEN/YO_DEBO), fechaEpochMillis, saldada`.
- **`CompraPlazos`** (antes se llamaba `CompraMsi`, renombrada): `id, cuentaId (FK), concepto, categoria, emojiPersonalizado?, isMsi (default true), montoOriginal, costoFinanciamiento (default 0.0), montoTotal, numeroMeses, mesesPagados (default 0), fechaInicio`. Cubre tanto MSI (sin intereses) como "pagos fijos" con recargo. **`montoTotal` siempre se calcula en el repositorio** (`montoOriginal + costoFinanciamiento`, forzando `costoFinanciamiento = 0` si `isMsi = true`) — nunca se recibe de afuera ya calculado, para que no se desincronice.

DB actual: **versión 7**. Sin migraciones reales todavía (`fallbackToDestructiveMigration()`) — proyecto en desarrollo, sin usuarios reales que proteger.

### Cálculos de negocio (funciones puras, no van en el ViewModel si combinan más de un repositorio)
- `data/CalculosPlazos.kt`: `cuotaMensual()`, `mesesRestantes()`, `saldoPendiente()`, `activa()`, `tieneRecargo()`, `fechaLiberacionProgramada()` — definidas TANTO para `CompraPlazos` como para `CompraPlazosConCuenta` (la proyección con JOIN que usa la UI). También `porcentajeRecargo(montoOriginal, costoFinanciamiento)`.
- `data/CalculosTarjeta.kt`: `proximaFechaCorte()`, `proximaFechaLimitePago()` sobre `Cuenta` (usa `diaCorte`/`diasParaPago`). Simplificación conocida: no distingue si ya se pagó el corte anterior.
- `data/CalculosFlujoCaja.kt`: `calcularDisponibleMensual(trabaja, gananciaNeta, frecuenciaPago, cuotaMensualTotalPlazos, gastoDelMesActual): Double` — función suelta (no método de ViewModel) porque combina datos de `UserPreferencesRepository` (ingresos) y `FinanzasRepository` (gastos/plazos). Quincenal se dobla a mensual-equivalente. Deliberadamente NO incluye deudas grupales (no son un compromiso mensual programado).

### Repositorios
- **`FinanzasRepository`** (constructor: `CuentaDao, GastoDao, PersonaDao, DeudaDao, CompraPlazosDao`): fachada única para cuentas, gastos, deudas grupales y compras a plazos. Punto de entrada clave: `registrarCompra(monto, concepto, cuentaId, numeroMeses?, categoria, emojiPersonalizado?, isMsi, costoFinanciamiento)` — decide internamente si guarda como `Gasto` (numeroMeses null o 1) o como `CompraPlazos` (numeroMeses > 1).
- **`UserPreferencesRepository`** (DataStore, no Room): `userName`, `trabaja`, `gananciaNeta`, `frecuenciaPago` (enum `FrecuenciaPago`: MENSUAL/QUINCENAL), `perfilCompleto` (bandera separada del nombre — el onboarding tiene varios pasos y se puede interrumpir a medias).

### ViewModels (`viewmodel/`)
- **`FinanzasViewModel`** (+ `FinanzasViewModelFactory`): envuelve `FinanzasRepository`, expone todo como `StateFlow` (`WhileSubscribed(5_000)`). Incluye analítica: `gastoDelMesActual`, `gastoPorCategoria`, `gastoPorCuenta`, `gastoPorMes` (últimos 6 meses, orden cronológico), `saldoNetoGrupos`, `comprasPlazosActivas`, `cuotaMensualTotalPlazos`.
- **`OnboardingViewModel`** (+ Factory): envuelve `UserPreferencesRepository`.
- **Nota de arquitectura pendiente de decidir (no es de Claude decidirlo solo):** hoy `AppNavigation` en `MainActivity.kt` todavía usa `FinanzasRepository` crudo + `scope.launch` en vez de `FinanzasViewModel` para la navegación principal (solo el onboarding usa ViewModels de verdad). Es inconsistente pero funcional.

## Pantallas actuales (dueño: usuario + Gemini)

| Pantalla | Estado | Notas |
|---|---|---|
| `OnboardingScreen.kt` | Wizard completo (nombre → ingresos → tarjetas) | Estilo "tarjeta por pregunta", `TarjetaPaso` reusable, slide horizontal con `AnimatedContent` |
| `MainActivity.kt` (Home) | 4 acciones: Estadísticas, Grupos, Compra, (Plazos accesible indirecto) | Ver "Pendiente" abajo |
| `RegistrarCompraScreen.kt` | Wizard completo: Monto → Concepto → Cuenta → Modalidad de pago (MSI/plazos con recargo) → Confirmación | Usa `OpcionTile` (tiles cuadrados de tamaño fijo en `FlowRow`) para montos rápidos y categorías |
| `EstadisticasScreen.kt` | "Flujo de caja" real: disponible del mes, gasto del mes, por categoría, por cuenta, tendencia 6 meses, historial | — |
| `ComprasPlazosScreen.kt` | Lista de compras a plazos activas, cuota/meses restantes/saldo pendiente, botón "Pagar cuota" | **Pendiente de corregir**: ver abajo |
| `GruposScreen.kt` | Funcional pero con el estilo VIEJO (no el de tarjetas tipo wizard) | Rediseño explícitamente pospuesto |

### Pendiente conocido / próximos pasos
1. **`ComprasPlazosScreen` no tiene entrada propia desde Home.** Se pidió un botón dedicado en Home; Gemini en vez de eso hizo clickeable la tarjeta "Disponible este mes" en Estadísticas (sin ningún indicio visual de que es tocable) — confuso, hay que corregirlo.
2. **`GruposScreen.kt`** sigue con el diseño anterior a la ola de "tarjetas minimalistas" — pendiente si se quiere unificar el estilo.
3. **`agregarCuenta` en el ViewModel no devuelve el id** — si algún día se quiere, al dar de alta una tarjeta de crédito, encadenar directo el registro de una compra a plazos ya existente, hace falta una versión que devuelva el id generado (hoy es "fire and forget").
4. Toolchain estable y funcionando: Gradle 8.9, AGP 8.7.3, Kotlin 2.0.20, KSP, Compose BOM 2024.09.02, compileSdk/targetSdk 35, minSdk 24. No tiene core library desugaring — por eso los cálculos de fecha usan `java.util.Calendar`, no `java.time`.

## Convenciones establecidas
- Todo el código/comentarios en español, sin tildes en identificadores (pero sí en strings de UI visibles al usuario).
- Paleta azul fija en `ui/theme/Color.kt` (`dynamicColor = false` a propósito). Verde `SaldoAFavor`/`SaldoAFavorContainer` para saldos a favor; `colorScheme.error`/`errorContainer` para negativos/deudas.
- Formato de dinero: siempre `Double.comoMoneda()` de `ui/Formato.kt` (nunca interpolar un Double crudo en un string).
- Wizards de varios pasos: enum de pasos + `AnimatedContent` con slide horizontal + componente `TarjetaPaso` reusable (definido en `RegistrarCompraScreen.kt`).
- Opciones de selección de tamaño fijo (montos rápidos, categorías): componente `OpcionTile` (`RegistrarCompraScreen.kt`) — mismo tamaño para todas, en `FlowRow`, sin `weight()`.
- Repositorio en GitHub: `https://github.com/ivanprz10/FinanzasPersonalesApp.git`, rama `main`. Recordar hacer commits periódicos (no solo al principio) — se ha dejado acumular trabajo sin confirmar más de una vez.
