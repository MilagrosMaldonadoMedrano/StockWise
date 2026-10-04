package com.milagros.stockwise.presentation.lista

import com.milagros.stockwise.domain.model.Producto

//sealed implica que estos son todos los estados posibles
sealed interface ProductoListUiState {
    data object Cargando : ProductoListUiState
    data object Vacio : ProductoListUiState
    data class Exito(val productos: List<Producto>) : ProductoListUiState
    data class Error(val mensaje: String) : ProductoListUiState
}