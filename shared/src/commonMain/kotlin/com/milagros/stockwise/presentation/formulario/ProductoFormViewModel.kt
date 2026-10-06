package com.milagros.stockwise.presentation.formulario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.domain.repository.SkuDuplicadoException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// productoId == null -> crear; con id -> editar
class ProductoFormViewModel(
    private val productoId: String?,
    private val repository: ProductoRepository,
) : ViewModel() {

    // A diferencia de lista y detalle, el estado NO se deriva del repositorio: el formulario es un
    // "borrador" propio. Lo que se escribe no toca la lista compartida hasta que se guarda.
    private val _uiState = MutableStateFlow(ProductoFormUiState(esEdicion = productoId != null))
    val uiState: StateFlow<ProductoFormUiState> = _uiState.asStateFlow()

    // Los errores se muestran recién después del primer intento de guardar,
    // y a partir de ahí se actualizan mientras se escribe
    private var intentoGuardar = false

    init {
        if (productoId != null) cargarProducto(productoId)
    }

    private fun cargarProducto(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorCarga = null) }
            try {
                // Primero se busca en la lista en memoria; si no está cargada, se pide a Supabase
                if (repository.productos.value == null) repository.refreshProductos()
                val producto = repository.productos.value?.find { it.id == id }
                _uiState.update {
                    if (producto != null) it.copy(cargando = false, campos = producto.toCampos())
                    else it.copy(cargando = false, errorCarga = "El producto no existe o fue eliminado")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, errorCarga = "No se pudo cargar el producto. Revisá tu conexión.")
                }
            }
        }
    }

    fun reintentarCarga() {
        productoId?.let(::cargarProducto)
    }

    fun onCamposChange(campos: ProductoFormCampos) {
        _uiState.update {
            val errores = if (intentoGuardar) {
                // El error de SKU duplicado viene del servidor: se mantiene hasta que se cambie el SKU
                val skuDuplicado = it.errores.sku.takeIf { _ -> campos.sku == it.campos.sku }
                erroresDe(campos).copy(sku = skuDuplicado)
            } else {
                it.errores
            }
            it.copy(campos = campos, errores = errores)
        }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando || estado.cargando) return
        intentoGuardar = true

        val validacion = validarProducto(estado.campos, id = productoId)
        if (validacion is ResultadoValidacion.Invalido) {
            _uiState.update { it.copy(errores = validacion.errores) }
            return
        }
        val producto = (validacion as ResultadoValidacion.Valido).producto

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errores = ProductoFormErrores()) }
            try {
                // El repositorio actualiza su lista: lista y detalle se enteran solos
                if (productoId == null) repository.createProducto(producto)
                else repository.updateProducto(producto)
                val resultado = if (productoId == null) "\"${producto.nombre}\" creado" else "Cambios guardados"
                _uiState.update { it.copy(guardando = false, resultado = resultado) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: SkuDuplicadoException) {
                // Error de un campo puntual: se muestra debajo del SKU, no en un snackbar
                _uiState.update {
                    it.copy(guardando = false, errores = ProductoFormErrores(sku = "Ya existe un producto con ese SKU"))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardando = false, mensaje = "No se pudo guardar. Revisá tu conexión.")
                }
            }
        }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private fun erroresDe(campos: ProductoFormCampos): ProductoFormErrores =
        (validarProducto(campos) as? ResultadoValidacion.Invalido)?.errores ?: ProductoFormErrores()
}
