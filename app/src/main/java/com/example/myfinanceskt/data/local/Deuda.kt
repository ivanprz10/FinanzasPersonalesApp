package com.example.myfinanceskt.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una deuda grupal: "fulano me debe $X" o "yo le debo $X a fulano".
 *
 * "saldada" queda en false hasta que se marca como pagada; no se borra
 * para conservar el historial.
 */
@Entity(
    tableName = "deudas",
    foreignKeys = [
        ForeignKey(
            entity = Persona::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("personaId")]
)
data class Deuda(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personaId: Long,
    val monto: Double,
    val concepto: String,
    val tipo: TipoDeuda,
    val fechaEpochMillis: Long = System.currentTimeMillis(),
    val saldada: Boolean = false
)
