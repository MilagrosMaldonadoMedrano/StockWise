package com.milagros.stockwise.presentation.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milagros.stockwise.domain.repository.ProductoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductoListViewModel(
    private val repository: ProductoRepository,
) : ViewModel() {

    // Mensaje del último error de carga (null si no hubo error)
    private val error = MutableStateFlow<String?>(null)

    // El estado se deriva de los productos del repositorio + el error de carga
    val uiState: StateFlow<ProductoListUiState> =
        combine(repository.productos, error) { productos, error ->
            when {
                productos == null && error != null -> ProductoListUiState.Error(error)
                productos == null -> ProductoListUiState.Cargando
                productos.isEmpty() -> ProductoListUiState.Vacio
                else -> ProductoListUiState.Exito(productos)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProductoListUiState.Cargando,
        )

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            error.value = null
            try {
                repository.refreshProductos()
            } catch (e: CancellationException) {
                throw e // la corrutina se canceló (ej. se cerró la pantalla): no es un error
            } catch (e: Exception) {
                error.value = "No se pudieron cargar los productos. Revisá tu conexión."
            }
        }
    }
}
