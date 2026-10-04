package com.milagros.stockwise.presentation.detalle

import com.milagros.stockwise.domain.model.Producto

sealed interface ProductoDetailUiState {
    data object Cargando : ProductoDetailUiState
    data class Exito(
        val producto: Producto,
        val ajustando: Boolean = false, // hay un cambio de stock en curso: se deshabilitan los botones
        val mensaje: String? = null,    // mensaje para mostrar una sola vez en un snackbar
    ) : ProductoDetailUiState
    data class Error(val mensaje: String) : ProductoDetailUiState
}
