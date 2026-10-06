package com.milagros.stockwise.fakes

import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Repositorio en memoria para tests: no usa red ni Supabase.
class FakeProductoRepository(
    productosIniciales: List<Producto> = emptyList(),
) : ProductoRepository {

    private val _productos = MutableStateFlow<List<Producto>?>(productosIniciales)
    override val productos: StateFlow<List<Producto>?> = _productos

    // Permite simular un error de red al eliminar
    var fallarEnDelete = false

    // Registra los productos que se mandaron a actualizar, para verificarlos en los tests
    val updates = mutableListOf<Producto>()

    override suspend fun refreshProductos() = Unit

    override suspend fun getProducto(id: String): Producto =
        _productos.value.orEmpty().first { it.id == id }

    override suspend fun createProducto(producto: Producto): Producto {
        _productos.value = _productos.value.orEmpty() + producto
        return producto
    }

    override suspend fun updateProducto(producto: Producto): Producto {
        updates += producto
        _productos.value = _productos.value.orEmpty().map { if (it.id == producto.id) producto else it }
        return producto
    }

    override suspend fun deleteProducto(id: String) {
        if (fallarEnDelete) throw RuntimeException("Error de red simulado")
        _productos.value = _productos.value.orEmpty().filterNot { it.id == id }
    }
}
