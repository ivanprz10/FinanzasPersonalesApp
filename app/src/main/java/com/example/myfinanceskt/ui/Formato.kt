package com.example.myfinanceskt.ui

import com.example.myfinanceskt.data.local.Cuenta
import com.example.myfinanceskt.data.local.TipoCuenta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helpers de presentacion. Son funciones de extension: se llaman como si
 * fueran metodos de Cuenta / Double / Long, pero viven fuera de esas clases.
 */

fun Cuenta.etiqueta(): String = when (tipo) {
    TipoCuenta.EFECTIVO -> "Efectivo"
    TipoCuenta.DEBITO -> "Debito $nombre"
    TipoCuenta.CREDITO -> "Credito $nombre"
}

fun Double.comoMoneda(): String = "$" + String.format(Locale.getDefault(), "%,.2f", this)

private val formatoFecha = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

fun Long.comoFecha(): String = formatoFecha.format(Date(this))
