package com.example.myfinanceskt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Fila combinada compra a plazos + nombre de la tarjeta (igual idea que GastoConCuenta).
 */
data class CompraPlazosConCuenta(
    val id: Long,
    val concepto: String,
    val categoria: Categoria,
    val emojiPersonalizado: String?,
    val isMsi: Boolean,
    val montoOriginal: Double,
    val costoFinanciamiento: Double,
    val montoTotal: Double,
    val numeroMeses: Int,
    val mesesPagados: Int,
    val fechaInicio: Long,
    val cuentaNombre: String
)

@Dao
interface CompraPlazosDao {

    @Insert
    suspend fun insertar(compra: CompraPlazos): Long

    @Query("UPDATE compras_plazos SET mesesPagados = mesesPagados + 1 WHERE id = :compraId AND mesesPagados < numeroMeses")
    suspend fun marcarMesPagado(compraId: Long)

    @Query(
        """
        SELECT m.id AS id,
               m.concepto AS concepto,
               m.categoria AS categoria,
               m.emojiPersonalizado AS emojiPersonalizado,
               m.isMsi AS isMsi,
               m.montoOriginal AS montoOriginal,
               m.costoFinanciamiento AS costoFinanciamiento,
               m.montoTotal AS montoTotal,
               m.numeroMeses AS numeroMeses,
               m.mesesPagados AS mesesPagados,
               m.fechaInicio AS fechaInicio,
               c.nombre AS cuentaNombre
        FROM compras_plazos m
        JOIN cuentas c ON c.id = m.cuentaId
        WHERE m.mesesPagados < m.numeroMeses
        ORDER BY m.fechaInicio DESC
        """
    )
    fun observarActivasConCuenta(): Flow<List<CompraPlazosConCuenta>>

    @Query("SELECT COALESCE(SUM(montoTotal * 1.0 / numeroMeses), 0.0) FROM compras_plazos WHERE mesesPagados < numeroMeses")
    fun observarCuotaMensualTotal(): Flow<Double>
}
