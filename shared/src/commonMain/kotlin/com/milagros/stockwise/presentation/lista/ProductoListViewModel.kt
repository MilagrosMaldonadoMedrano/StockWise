package com.milagros.stockwise.presentation.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milagros.stockwise.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductoListViewModel(
    private val repository: ProductoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductoListUiState>(ProductoListUiState.Cargando)
    val uiState: StateFlow<ProductoListUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.value = ProductoListUiState.Cargando
            _uiState.value = try {
                val productos = repository.getProductos()
                if (productos.isEmpty()) ProductoListUiState.Vacio
                else ProductoListUiState.Exito(productos)
            } catch (e: Exception) {
                ProductoListUiState.Error(e.message ?: "No se pudieron cargar los productos")
            }
        }
    }
}