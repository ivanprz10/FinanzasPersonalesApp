package com.example.myfinanceskt.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una compra a plazos cargada a una tarjeta de credito. Cubre dos casos:
 *
 * - MSI (meses sin intereses): isMsi = true, costoFinanciamiento = 0.0,
 *   montoTotal == montoOriginal.
 * - Pagos fijos con recargo: isMsi = false, costoFinanciamiento > 0.0
 *   (lo que cobra la tienda/banco por financiar), montoTotal = montoOriginal + costoFinanciamiento.
 *
 * "montoTotal" se guarda ya calculado (no se deriva en cada query) para que
 * observarCuotaMensualTotal() en el DAO sea una simple suma en SQL.
 *
 * "mesesPagados" se actualiza a mano (el usuario marca "ya paso el corte de
 * este mes"): no se infiere de la fecha, porque el usuario puede adelantar
 * pagos o el corte real puede no coincidir exactamente con el calendario.
 */
@Entity(
    tableName = "compras_plazos",
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
data class CompraPlazos(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cuentaId: Long,
    val concepto: String,
    val categoria: Categoria = Categoria.OTRO,
    val emojiPersonalizado: String? = null,
    val isMsi: Boolean = true,
    val montoOriginal: Double,
    val costoFinanciamiento: Double = 0.0,
    val montoTotal: Double,
    val numeroMeses: Int,
    val mesesPagados: Int = 0,
    val fechaInicio: Long = System.currentTimeMillis()
)
