package com.example.myfinanceskt.data

import com.example.myfinanceskt.data.local.CompraPlazos
import java.util.Calendar

/**
 * Matematica de compras a plazos (MSI o con recargo). Vive en el backend
 * porque es logica de negocio: la pantalla solo pinta estos numeros ya
 * calculados.
 *
 * Clave: la cuota SIEMPRE se calcula sobre montoTotal (que ya incluye el
 * costoFinanciamiento cuando no es MSI), nunca sobre montoOriginal — asi
 * no importa si es MSI o pagos fijos, la formula es la misma.
 */

/** Cuota fija mensual: montoTotal ya trae el recargo (si lo hay) sumado. */
fun CompraPlazos.cuotaMensual(): Double = montoTotal / numeroMeses

fun CompraPlazos.mesesRestantes(): Int = (numeroMeses - mesesPagados).coerceAtLeast(0)

fun CompraPlazos.saldoPendiente(): Double = cuotaMensual() * mesesRestantes()

fun CompraPlazos.activa(): Boolean = mesesPagados < numeroMeses

/** true si hay un recargo real por financiamiento (pagos fijos, no MSI). */
fun CompraPlazos.tieneRecargo(): Boolean = !isMsi && costoFinanciamiento > 0.0

/**
 * Fecha programada en la que se paga la ultima mensualidad, asumiendo que
 * se paga una cuota por cada corte desde fechaInicio (sin adelantos ni atrasos).
 */
fun CompraPlazos.fechaLiberacionProgramada(): Long =
    Calendar.getInstance().apply {
        timeInMillis = fechaInicio
        add(Calendar.MONTH, numeroMeses)
    }.timeInMillis

/**
 * Que porcentaje extra representa el costo de financiamiento sobre el monto
 * original. No es una extension de CompraPlazos porque se usa ANTES de tener
 * la compra armada (mientras el formulario todavia esta capturando el total
 * con intereses) — por eso recibe los dos numeros sueltos.
 *
 * Ej: montoOriginal = 1000, costoFinanciamiento = 150 -> 15.0 (15% mas caro).
 */
fun porcentajeRecargo(montoOriginal: Double, costoFinanciamiento: Double): Double =
    if (montoOriginal <= 0.0) 0.0 else (costoFinanciamiento / montoOriginal) * 100.0
