package com.milagros.stockwise.domain.model

// Ventas acumuladas de un producto (calculadas en la base, vista estadisticas_productos)
data class EstadisticaProducto(
    val productoId: String,
    val nombre: String,
    val unidades: Long,
    val ingresos: Double,
    val ganancia: Double,
)
