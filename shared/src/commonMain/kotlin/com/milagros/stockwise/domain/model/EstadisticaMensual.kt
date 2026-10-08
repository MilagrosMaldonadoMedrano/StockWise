package com.milagros.stockwise.domain.model

// Ventas de un producto en un mes (vista estadisticas_mensuales).
// mes = "YYYY-MM": un String alcanza para agrupar y ordenar, sin sumar una librería de fechas.
data class EstadisticaMensual(
    val productoId: String,
    val nombre: String,
    val mes: String,
    val unidades: Long,
    val ingresos: Double,
    val ganancia: Double,
)
