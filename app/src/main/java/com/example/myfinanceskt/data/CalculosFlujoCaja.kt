package com.example.myfinanceskt.data

/**
 * "Cuanto me queda del sueldo este mes": combina el ingreso (perfil del
 * usuario, en UserPreferencesRepository) con los compromisos que ya salen
 * de Room (FinanzasRepository) — por eso es una funcion pura en vez de vivir
 * dentro de un solo repositorio/ViewModel: no le pertenece a ninguno de los
 * dos, junta datos de ambos.
 *
 * No incluye el saldo neto de deudas grupales a proposito: una deuda entre
 * amigos no es un compromiso mensual programado (se puede saldar cuando sea),
 * a diferencia de una cuota de MSI/plazos que sí tiene fecha fija cada mes.
 *
 * @param trabaja si el usuario no tiene ingresos capturados, el disponible
 *   es simplemente negativo por lo que ya debe/gasto (no hay con que compararlo).
 * @param gananciaNeta el monto que el usuario captura en el onboarding, en
 *   la frecuencia que le paguen (no necesariamente mensual).
 * @param frecuenciaPago null se trata como MENSUAL (dato aun no capturado).
 * @param cuotaMensualTotalPlazos suma de todas las cuotas activas de MSI/plazos
 *   (FinanzasRepository.cuotaMensualTotalPlazos).
 * @param gastoDelMesActual gastos normales (no a plazos) ya registrados este
 *   mes (FinanzasRepository.gastoDelMesActual via GastoDao.observarGastoMesActual).
 */
fun calcularDisponibleMensual(
    trabaja: Boolean,
    gananciaNeta: Double,
    frecuenciaPago: FrecuenciaPago?,
    cuotaMensualTotalPlazos: Double,
    gastoDelMesActual: Double
): Double {
    val ingresoMensual = if (trabaja) {
        when (frecuenciaPago) {
            FrecuenciaPago.QUINCENAL -> gananciaNeta * 2
            FrecuenciaPago.MENSUAL, null -> gananciaNeta
        }
    } else {
        0.0
    }
    return ingresoMensual - cuotaMensualTotalPlazos - gastoDelMesActual
}
