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

@Dao
interface GastoDao {

    @Insert
    suspend fun insertar(gasto: Gasto): Long

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos")
    fun observarTotal(): Flow<Double>

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
}
