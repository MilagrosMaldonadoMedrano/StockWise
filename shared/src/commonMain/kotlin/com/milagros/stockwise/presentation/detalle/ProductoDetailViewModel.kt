package com.milagros.stockwise.presentation.detalle

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

class ProductoDetailViewModel(
    private val productoId: String,
    private val repository: ProductoRepository,
) : ViewModel() {

    private val error = MutableStateFlow<String?>(null)

    // Busca el producto en la lista del repositorio: si otra pantalla lo modifica, se actualiza solo
    val uiState: StateFlow<ProductoDetailUiState> =
        combine(repository.productos, error) { productos, error ->
            val producto = productos?.find { it.id == productoId }
            when {
                producto != null -> ProductoDetailUiState.Exito(producto)
                error != null -> ProductoDetailUiState.Error(error)
                productos == null -> ProductoDetailUiState.Cargando
                else -> ProductoDetailUiState.Error("El producto no existe o fue eliminado")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProductoDetailUiState.Cargando,
        )

    init {
        // Normalmente la lista ya está cargada; solo se pide si se llegó acá sin pasar por ella
        if (repository.productos.value == null) cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            error.value = null
            try {
                repository.refreshProductos()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = e.message ?: "No se pudo cargar el producto"
            }
        }
    }
}
