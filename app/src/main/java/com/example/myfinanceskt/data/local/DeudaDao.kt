package com.example.myfinanceskt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Fila combinada deuda + nombre de la persona (igual idea que GastoConCuenta).
 */
data class DeudaConPersona(
    val id: Long,
    val monto: Double,
    val concepto: String,
    val tipo: TipoDeuda,
    val fechaEpochMillis: Long,
    val personaNombre: String
)

@Dao
interface DeudaDao {

    @Insert
    suspend fun insertar(deuda: Deuda): Long

    @Query("UPDATE deudas SET saldada = 1 WHERE id = :deudaId")
    suspend fun marcarSaldada(deudaId: Long)

    @Query(
        """
        SELECT d.id AS id,
               d.monto AS monto,
               d.concepto AS concepto,
               d.tipo AS tipo,
               d.fechaEpochMillis AS fechaEpochMillis,
               p.nombre AS personaNombre
        FROM deudas d
        JOIN personas p ON p.id = d.personaId
        WHERE d.saldada = 0
        ORDER BY d.fechaEpochMillis DESC
        """
    )
    fun observarActivasConPersona(): Flow<List<DeudaConPersona>>

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM deudas WHERE tipo = 'TE_DEBEN' AND saldada = 0")
    fun observarTotalTeDeben(): Flow<Double>

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM deudas WHERE tipo = 'YO_DEBO' AND saldada = 0")
    fun observarTotalDebo(): Flow<Double>
}
