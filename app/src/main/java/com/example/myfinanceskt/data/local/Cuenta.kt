package com.example.myfinanceskt.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Una "cuenta" = de donde sale el dinero: Efectivo, o una tarjeta concreta.
 *
 * "@Entity" -> Room crea la tabla "cuentas" con una columna por propiedad.
 * "@PrimaryKey(autoGenerate = true)" -> id autoincremental (como IDENTITY en SQL).
 * El valor por defecto id = 0 le dice a Room "genera tu el id al insertar".
 */
@Entity(tableName = "cuentas")
data class Cuenta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val tipo: TipoCuenta,
    // Los siguientes 3 campos solo tienen sentido para TipoCuenta.CREDITO;
    // quedan en null para Efectivo/Debito y para tarjetas que aun no los capturan.
    val diaCorte: Int? = null,
    val diasParaPago: Int? = null,
    val tieneComprasAMeses: Boolean? = null
)
