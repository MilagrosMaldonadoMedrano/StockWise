package com.milagros.stockwise.presentation.estadisticas

import com.milagros.stockwise.domain.model.EstadisticaProducto

sealed interface EstadisticasUiState {
    data object Cargando : EstadisticasUiState
    data object Vacio : EstadisticasUiState // todavía no hay ventas
    data class Error(val mensaje: String) : EstadisticasUiState

    // productos viene ordenado de mayor a menor ganancia (lo ordena la consulta)
    data class Exito(val productos: List<EstadisticaProducto>) : EstadisticasUiState {
        val ingresosTotales: Double get() = productos.sumOf { it.ingresos }
        val gananciaTotal: Double get() = productos.sumOf { it.ganancia }
        val unidadesTotales: Long get() = productos.sumOf { it.unidades }
        // La barra más larga del ranking = la mayor ganancia positiva
        val gananciaMaxima: Double get() = productos.maxOfOrNull { it.ganancia }?.coerceAtLeast(0.0) ?: 0.0
    }
}
