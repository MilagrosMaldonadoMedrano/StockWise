package com.milagros.stockwise.fakes

import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.domain.repository.SkuDuplicadoException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Repositorio en memoria para tests: no usa red ni Supabase.
class FakeProductoRepository(
    productosIniciales: List<Producto> = emptyList(),
) : ProductoRepository {

    private val _productos = MutableStateFlow<List<Producto>?>(productosIniciales)
    override val productos: StateFlow<List<Producto>?> = _productos

    // Registra los productos que se mandaron a actualizar, para verificarlos en los tests
    val updates = mutableListOf<Producto>()

    override suspend fun refreshProductos() = Unit

    override suspend fun getProducto(id: String): Producto =
        _productos.value.orEmpty().first { it.id == id }

    override suspend fun createProducto(producto: Producto): Producto {
        verificarSku(producto)
        // Como la base: el id lo genera el "servidor"
        val creado = producto.copy(id = "nuevo-${_productos.value.orEmpty().size + 1}")
        _productos.value = _productos.value.orEmpty() + creado
        return creado
    }

    override suspend fun updateProducto(producto: Producto): Producto {
        verificarSku(producto)
        updates += producto
        _productos.value = _productos.value.orEmpty().map { if (it.id == producto.id) producto else it }
        return producto
    }

    override suspend fun deleteProducto(id: String) {
        _productos.value = _productos.value.orEmpty().filterNot { it.id == id }
    }

    // Como el "unique" de la base: otro producto no puede tener el mismo SKU
    private fun verificarSku(producto: Producto) {
        val sku = producto.sku ?: return
        if (_productos.value.orEmpty().any { it.sku == sku && it.id != producto.id }) {
            throw SkuDuplicadoException(sku)
        }
    }
}
