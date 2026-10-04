package com.milagros.stockwise.presentation.detalle

import com.milagros.stockwise.domain.model.Producto

sealed interface ProductoDetailUiState {
    data object Cargando : ProductoDetailUiState
    data class Exito(val producto: Producto) : ProductoDetailUiState
    data class Error(val mensaje: String) : ProductoDetailUiState
}
