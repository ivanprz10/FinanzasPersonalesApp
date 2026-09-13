package com.example.myfinanceskt.data

import com.example.myfinanceskt.data.local.Cuenta
import java.util.Calendar

/**
 * Matematica de fechas de tarjeta de credito. Vive en el backend porque es
 * logica de negocio (no de presentacion): la pantalla solo debe pintar el
 * resultado, no calcularlo.
 *
 * Simplificacion actual: siempre se calcula sobre el PROXIMO corte a partir
 * de "ahora". No distingue si ya pagaste el corte anterior o no (eso vendria
 * con el modulo de seguimiento de pagos/MSI, todavia no construido).
 *
 * Si la cuenta no es de credito o le faltan estos datos, ambas funciones
 * devuelven null en vez de reventar.
 */

fun Cuenta.proximaFechaCorte(ahora: Long = System.currentTimeMillis()): Long? {
    val dia = diaCorte ?: return null

    val hoy = diaSinHora(ahora)
    val corte = diaSinHora(ahora).apply {
        set(Calendar.DAY_OF_MONTH, minOf(dia, getActualMaximum(Calendar.DAY_OF_MONTH)))
    }

    // Si el corte de este mes ya paso (o es hoy pero ya se cerro), el proximo es el mes que sigue.
    if (!corte.after(hoy)) {
        corte.add(Calendar.MONTH, 1)
        corte.set(Calendar.DAY_OF_MONTH, minOf(dia, corte.getActualMaximum(Calendar.DAY_OF_MONTH)))
    }
    return corte.timeInMillis
}

fun Cuenta.proximaFechaLimitePago(ahora: Long = System.currentTimeMillis()): Long? {
    val corte = proximaFechaCorte(ahora) ?: return null
    val plazoDias = diasParaPago ?: return null
    return diaSinHora(corte).apply { add(Calendar.DAY_OF_MONTH, plazoDias) }.timeInMillis
}

private fun diaSinHora(millis: Long): Calendar = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
