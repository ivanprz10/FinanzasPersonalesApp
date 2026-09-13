package com.example.myfinanceskt.data.local

/**
 * Categoria generica de una compra. Cada una trae su emoji para no depender
 * de un set de iconos aparte: la UI puede pintar el chip/selector directo con
 * "${categoria.emoji} ${categoria.etiqueta}", y la lista de gastos usa el
 * mismo emoji para representar visualmente que fue esa compra.
 *
 * "OTRO" es el valor por defecto cuando no se elige ninguna (o para gastos
 * viejos que no tenian categoria antes de este cambio).
 */
enum class Categoria(val etiqueta: String, val emoji: String) {
    COMIDA("Comida", "🍔"),
    SUPER("Super", "🛒"),
    TRANSPORTE("Transporte", "🚌"),
    GASOLINA("Gasolina", "⛽"),
    SERVICIOS("Servicios", "🧾"),
    ENTRETENIMIENTO("Entretenimiento", "🎬"),
    SALUD("Salud", "💊"),
    HOGAR("Hogar", "🏠"),
    ROPA("Ropa", "👕"),
    EDUCACION("Educacion", "📚"),
    OTRO("Otro", "💳")
}
