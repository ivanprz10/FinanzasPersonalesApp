package com.example.myfinanceskt.data.local

import androidx.room.TypeConverter

/**
 * SQLite solo guarda tipos primitivos (texto, numeros, blobs).
 * Room usa estos metodos para traducir un enum <-> texto automaticamente.
 * "@TypeConverter" marca cada direccion de la conversion.
 */
class Converters {

    @TypeConverter
    fun tipoCuentaToString(tipo: TipoCuenta): String = tipo.name

    @TypeConverter
    fun stringToTipoCuenta(valor: String): TipoCuenta = TipoCuenta.valueOf(valor)

    @TypeConverter
    fun tipoDeudaToString(tipo: TipoDeuda): String = tipo.name

    @TypeConverter
    fun stringToTipoDeuda(valor: String): TipoDeuda = TipoDeuda.valueOf(valor)
}
