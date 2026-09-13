package com.example.myfinanceskt.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un gasto puntual.
 *
 * "foreignKeys" -> cada gasto apunta a una Cuenta existente (integridad referencial).
 *   onDelete = RESTRICT: no deja borrar una cuenta si tiene gastos.
 * "indices" -> indice sobre cuentaId para que los JOIN y filtros sean rapidos.
 */
@Entity(
    tableName = "gastos",
    foreignKeys = [
        ForeignKey(
            entity = Cuenta::class,
            parentColumns = ["id"],
            childColumns = ["cuentaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("cuentaId")]
)
data class Gasto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monto: Double,
    val concepto: String,
    val categoria: Categoria = Categoria.OTRO,
    // Si el usuario elige su propio emoji (teclado del celular) en vez de una
    // categoria predefinida, se guarda aqui y manda sobre categoria.emoji al mostrarlo.
    val emojiPersonalizado: String? = null,
    val fechaEpochMillis: Long = System.currentTimeMillis(),
    val cuentaId: Long
)
