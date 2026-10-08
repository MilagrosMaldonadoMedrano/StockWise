package com.milagros.stockwise.presentation.estadisticas

import com.milagros.stockwise.domain.model.EstadisticaProducto

sealed interface EstadisticasUiState {
    data object Cargando : EstadisticasUiState
    data object Vacio : EstadisticasUiState // todavía no hay ventas
    data class Error(val mensaje: String) : EstadisticasUiState

    data class Exito(
        val meses: List<ResumenMes>,              // barras del gráfico, del más viejo al más nuevo
        val mesSeleccionado: String?,             // null = todos los meses
        val productos: List<EstadisticaProducto>, // ranking del período, de mayor a menor ganancia
    ) : EstadisticasUiState {
        val ingresosTotales: Double get() = productos.sumOf { it.ingresos }
        val gananciaTotal: Double get() = productos.sumOf { it.ganancia }
        val unidadesTotales: Long get() = productos.sumOf { it.unidades }
        // La barra más larga del ranking = la mayor ganancia positiva
        val gananciaMaxima: Double get() = productos.maxOfOrNull { it.ganancia }?.coerceAtLeast(0.0) ?: 0.0
    }
}
