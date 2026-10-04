package com.milagros.stockwise.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.domain.usecase.AjustarStockUseCase
import com.milagros.stockwise.domain.usecase.StockNegativoException
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
    private val ajustarStock: AjustarStockUseCase,
) : ViewModel() {

    private val error = MutableStateFlow<String?>(null)
    private val ajustando = MutableStateFlow(false)
    private val mensaje = MutableStateFlow<String?>(null)

    // Busca el producto en la lista del repositorio: si otra pantalla lo modifica, se actualiza solo
    val uiState: StateFlow<ProductoDetailUiState> =
        combine(repository.productos, error, ajustando, mensaje) { productos, error, ajustando, mensaje ->
            val producto = productos?.find { it.id == productoId }
            when {
                producto != null -> ProductoDetailUiState.Exito(producto, ajustando, mensaje)
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
                error.value = "No se pudo cargar el producto. Revisá tu conexión."
            }
        }
    }

    fun incrementarStock() = ajustar(+1)

    fun decrementarStock() = ajustar(-1)

    private fun ajustar(delta: Int) {
        val producto = (uiState.value as? ProductoDetailUiState.Exito)?.producto ?: return
        if (ajustando.value) return // ignora toques mientras se guarda el cambio anterior
        viewModelScope.launch {
            ajustando.value = true
            try {
                // No hace falta actualizar la pantalla a mano: el repositorio actualiza su lista
                ajustarStock(producto, delta)
            } catch (e: CancellationException) {
                throw e
            } catch (e: StockNegativoException) {
                mensaje.value = "El stock no puede quedar negativo"
            } catch (e: Exception) {
                // No se muestra e.message: trae detalles técnicos (URL, headers) que no son para el usuario
                mensaje.value = "No se pudo actualizar el stock. Revisá tu conexión."
            } finally {
                ajustando.value = false
            }
        }
    }

    fun onMensajeMostrado() {
        mensaje.value = null
    }
}
