package com.example.myfinanceskt.data

/**
 * Con que frecuencia el usuario recibe su ganancia neta.
 * No es una entidad de Room: vive en DataStore junto al resto del perfil.
 */
enum class FrecuenciaPago {
    MENSUAL,
    QUINCENAL
}
