package com.example.myfinanceskt.data

import com.example.myfinanceskt.data.local.Cuenta
import com.example.myfinanceskt.data.local.CuentaDao
import com.example.myfinanceskt.data.local.Deuda
import com.example.myfinanceskt.data.local.DeudaConPersona
import com.example.myfinanceskt.data.local.DeudaDao
import com.example.myfinanceskt.data.local.Gasto
import com.example.myfinanceskt.data.local.GastoConCuenta
import com.example.myfinanceskt.data.local.GastoDao
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
    private val deudaDao: DeudaDao
) {

    val cuentas: Flow<List<Cuenta>> = cuentaDao.observarTodas()
    val gastos: Flow<List<GastoConCuenta>> = gastoDao.observarGastosConCuenta()
    val totalGastado: Flow<Double> = gastoDao.observarTotal()

    val deudasActivas: Flow<List<DeudaConPersona>> = deudaDao.observarActivasConPersona()
    val totalTeDeben: Flow<Double> = deudaDao.observarTotalTeDeben()
    val totalDebo: Flow<Double> = deudaDao.observarTotalDebo()

    suspend fun agregarCuenta(nombre: String, tipo: TipoCuenta): Long =
        cuentaDao.insertar(Cuenta(nombre = nombre.trim(), tipo = tipo))

    suspend fun registrarGasto(monto: Double, concepto: String, cuentaId: Long): Long =
        gastoDao.insertar(
            Gasto(monto = monto, concepto = concepto.trim(), cuentaId = cuentaId)
        )

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
}
