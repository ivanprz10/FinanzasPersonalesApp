package com.example.myfinanceskt.data

import com.example.myfinanceskt.data.local.Categoria
import com.example.myfinanceskt.data.local.CompraPlazos
import com.example.myfinanceskt.data.local.CompraPlazosConCuenta
import com.example.myfinanceskt.data.local.CompraPlazosDao
import com.example.myfinanceskt.data.local.Cuenta
import com.example.myfinanceskt.data.local.CuentaDao
import com.example.myfinanceskt.data.local.Deuda
import com.example.myfinanceskt.data.local.DeudaConPersona
import com.example.myfinanceskt.data.local.DeudaDao
import com.example.myfinanceskt.data.local.Gasto
import com.example.myfinanceskt.data.local.GastoConCuenta
import com.example.myfinanceskt.data.local.GastoDao
import com.example.myfinanceskt.data.local.GastoPorCategoria
import com.example.myfinanceskt.data.local.GastoPorCuenta
import com.example.myfinanceskt.data.local.GastoPorMes
import com.example.myfinanceskt.data.local.Persona
import com.example.myfinanceskt.data.local.PersonaDao
import com.example.myfinanceskt.data.local.TipoCuenta
import com.example.myfinanceskt.data.local.TipoDeuda
import kotlinx.coroutines.flow.Flow

/**
 * Fachada unica para la UI: esconde que por debajo hay DAOs de Room.
 * Si maniana cambiamos Room por otra cosa, solo se toca aqui.
 */
class FinanzasRepository(
    private val cuentaDao: CuentaDao,
    private val gastoDao: GastoDao,
    private val personaDao: PersonaDao,
    private val deudaDao: DeudaDao,
    private val compraPlazosDao: CompraPlazosDao
) {

    val cuentas: Flow<List<Cuenta>> = cuentaDao.observarTodas()
    val gastos: Flow<List<GastoConCuenta>> = gastoDao.observarGastosConCuenta()
    val totalGastado: Flow<Double> = gastoDao.observarTotal()
    val gastoDelMesActual: Flow<Double> = gastoDao.observarGastoMesActual()
    val gastoPorCategoria: Flow<List<GastoPorCategoria>> = gastoDao.observarGastoPorCategoria()
    val gastoPorCuenta: Flow<List<GastoPorCuenta>> = gastoDao.observarGastoPorCuenta()
    val gastoPorMes: Flow<List<GastoPorMes>> = gastoDao.observarGastoPorMes()

    val deudasActivas: Flow<List<DeudaConPersona>> = deudaDao.observarActivasConPersona()
    val totalTeDeben: Flow<Double> = deudaDao.observarTotalTeDeben()
    val totalDebo: Flow<Double> = deudaDao.observarTotalDebo()

    val comprasPlazosActivas: Flow<List<CompraPlazosConCuenta>> = compraPlazosDao.observarActivasConCuenta()
    val cuotaMensualTotalPlazos: Flow<Double> = compraPlazosDao.observarCuotaMensualTotal()

    /**
     * diaCorte/diasParaPago/tieneComprasAMeses solo aplican si tipo == CREDITO;
     * se ignoran (quedan null) para Efectivo/Debito. Sirve tanto para el
     * onboarding como para agregar una cuenta despues, en cualquier momento.
     */
    suspend fun agregarCuenta(
        nombre: String,
        tipo: TipoCuenta,
        diaCorte: Int? = null,
        diasParaPago: Int? = null,
        tieneComprasAMeses: Boolean? = null
    ): Long =
        cuentaDao.insertar(
            Cuenta(
                nombre = nombre.trim(),
                tipo = tipo,
                diaCorte = diaCorte,
                diasParaPago = diasParaPago,
                tieneComprasAMeses = tieneComprasAMeses
            )
        )

    suspend fun registrarGasto(
        monto: Double,
        concepto: String,
        cuentaId: Long,
        categoria: Categoria = Categoria.OTRO,
        emojiPersonalizado: String? = null
    ): Long =
        gastoDao.insertar(
            Gasto(
                monto = monto,
                concepto = concepto.trim(),
                categoria = categoria,
                emojiPersonalizado = emojiPersonalizado.normalizado(),
                cuentaId = cuentaId
            )
        )

    /**
     * Punto de entrada unico para "Realice una compra", pensado para cuando la
     * cuenta elegida es una tarjeta de credito y hay que preguntar "a meses o
     * en una sola exhibicion":
     *
     * - numeroMeses == null (o 1): compra normal -> se guarda como Gasto.
     * - numeroMeses > 1: compra a plazos -> se guarda como CompraPlazos, NO
     *   como Gasto (todavia no es un gasto ya pagado por el monto completo;
     *   es un compromiso que se refleja en el widget de plazos con su cuota
     *   mensual).
     *
     * "monto" aqui es siempre el monto ORIGINAL (de contado); si no es MSI,
     * "costoFinanciamiento" es el recargo que se suma para calcular la cuota.
     * "isMsi"/"costoFinanciamiento" se ignoran en la rama de Gasto (una
     * compra de una sola exhibicion no tiene concepto de financiamiento).
     */
    suspend fun registrarCompra(
        monto: Double,
        concepto: String,
        cuentaId: Long,
        numeroMeses: Int? = null,
        categoria: Categoria = Categoria.OTRO,
        emojiPersonalizado: String? = null,
        isMsi: Boolean = true,
        costoFinanciamiento: Double = 0.0
    ): Long =
        if (numeroMeses != null && numeroMeses > 1) {
            registrarCompraPlazos(
                cuentaId = cuentaId,
                concepto = concepto,
                montoOriginal = monto,
                numeroMeses = numeroMeses,
                categoria = categoria,
                emojiPersonalizado = emojiPersonalizado,
                isMsi = isMsi,
                costoFinanciamiento = costoFinanciamiento
            )
        } else {
            registrarGasto(monto, concepto, cuentaId, categoria, emojiPersonalizado)
        }

    /**
     * Registra una deuda grupal. Si la persona no existe todavia (por nombre), la crea.
     */
    suspend fun registrarDeuda(
        personaNombre: String,
        monto: Double,
        concepto: String,
        tipo: TipoDeuda
    ): Long {
        val nombre = personaNombre.trim()
        val personaId = personaDao.buscarPorNombre(nombre)?.id
            ?: personaDao.insertar(Persona(nombre = nombre))
        return deudaDao.insertar(
            Deuda(personaId = personaId, monto = monto, concepto = concepto.trim(), tipo = tipo)
        )
    }

    suspend fun marcarDeudaSaldada(deudaId: Long) = deudaDao.marcarSaldada(deudaId)

    /**
     * Registra una compra a plazos (MSI o con recargo), cargada a una tarjeta (cuentaId).
     *
     * "montoTotal" NUNCA se recibe de afuera: siempre se calcula aqui como
     * montoOriginal + costoFinanciamiento (y costoFinanciamiento se fuerza a
     * 0.0 si isMsi es true), para que jamas queden desincronizados.
     */
    suspend fun registrarCompraPlazos(
        cuentaId: Long,
        concepto: String,
        montoOriginal: Double,
        numeroMeses: Int,
        categoria: Categoria = Categoria.OTRO,
        emojiPersonalizado: String? = null,
        isMsi: Boolean = true,
        costoFinanciamiento: Double = 0.0
    ): Long {
        val recargo = if (isMsi) 0.0 else costoFinanciamiento
        return compraPlazosDao.insertar(
            CompraPlazos(
                cuentaId = cuentaId,
                concepto = concepto.trim(),
                categoria = categoria,
                emojiPersonalizado = emojiPersonalizado.normalizado(),
                isMsi = isMsi,
                montoOriginal = montoOriginal,
                costoFinanciamiento = recargo,
                montoTotal = montoOriginal + recargo,
                numeroMeses = numeroMeses
            )
        )
    }

    suspend fun marcarMesPagadoPlazos(compraId: Long) = compraPlazosDao.marcarMesPagado(compraId)
}

/** "" o solo espacios cuenta como "no eligio nada" -> null, no un string vacio guardado. */
private fun String?.normalizado(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
