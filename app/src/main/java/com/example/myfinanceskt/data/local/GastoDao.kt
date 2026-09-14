package com.example.myfinanceskt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Fila combinada gasto + datos de su cuenta.
 * No es una @Entity: es solo el "molde" del resultado de la consulta con JOIN.
 * Room rellena cada propiedad por el nombre de columna del SELECT.
 */
data class GastoConCuenta(
    val id: Long,
    val monto: Double,
    val concepto: String,
    val categoria: Categoria,
    val emojiPersonalizado: String?,
    val fechaEpochMillis: Long,
    val cuentaNombre: String,
    val cuentaTipo: TipoCuenta
)

data class GastoPorCategoria(
    val categoria: Categoria,
    val total: Double
)

data class GastoPorCuenta(
    val cuentaId: Long,
    val cuentaNombre: String,
    val cuentaTipo: TipoCuenta,
    val total: Double
)

/** "mes" viene como "2026-09" (strftime %Y-%m), listo para mostrar o para ordenar como texto. */
data class GastoPorMes(
    val mes: String,
    val total: Double
)

@Dao
interface GastoDao {

    @Insert
    suspend fun insertar(gasto: Gasto): Long

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos")
    fun observarTotal(): Flow<Double>

    /** Suma de gastos SOLO del mes en curso (segun la zona horaria del dispositivo). */
    @Query(
        """
        SELECT COALESCE(SUM(monto), 0.0)
        FROM gastos
        WHERE strftime('%Y-%m', fechaEpochMillis / 1000, 'unixepoch', 'localtime')
            = strftime('%Y-%m', 'now', 'localtime')
        """
    )
    fun observarGastoMesActual(): Flow<Double>

    @Query(
        """
        SELECT g.id AS id,
               g.monto AS monto,
               g.concepto AS concepto,
               g.categoria AS categoria,
               g.emojiPersonalizado AS emojiPersonalizado,
               g.fechaEpochMillis AS fechaEpochMillis,
               c.nombre AS cuentaNombre,
               c.tipo AS cuentaTipo
        FROM gastos g
        JOIN cuentas c ON c.id = g.cuentaId
        ORDER BY g.fechaEpochMillis DESC
        """
    )
    fun observarGastosConCuenta(): Flow<List<GastoConCuenta>>

    @Query(
        """
        SELECT categoria AS categoria, SUM(monto) AS total
        FROM gastos
        GROUP BY categoria
        ORDER BY total DESC
        """
    )
    fun observarGastoPorCategoria(): Flow<List<GastoPorCategoria>>

    @Query(
        """
        SELECT c.id AS cuentaId, c.nombre AS cuentaNombre, c.tipo AS cuentaTipo, SUM(g.monto) AS total
        FROM gastos g
        JOIN cuentas c ON c.id = g.cuentaId
        GROUP BY c.id
        ORDER BY total DESC
        """
    )
    fun observarGastoPorCuenta(): Flow<List<GastoPorCuenta>>

    /** Ultimos 6 meses con gasto, en orden cronologico (mas viejo primero) para graficar. */
    @Query(
        """
        SELECT * FROM (
            SELECT strftime('%Y-%m', fechaEpochMillis / 1000, 'unixepoch', 'localtime') AS mes,
                   SUM(monto) AS total
            FROM gastos
            GROUP BY mes
            ORDER BY mes DESC
            LIMIT 6
        )
        ORDER BY mes ASC
        """
    )
    fun observarGastoPorMes(): Flow<List<GastoPorMes>>
}
