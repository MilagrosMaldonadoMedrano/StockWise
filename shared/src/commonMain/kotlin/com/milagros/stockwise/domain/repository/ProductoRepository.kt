package com.milagros.stockwise.domain.repository

import com.milagros.stockwise.domain.model.Producto
import kotlinx.coroutines.flow.StateFlow

interface ProductoRepository {
    // Única fuente de verdad: se actualiza sola después de cada create/update/delete.
    // null = todavía no se cargó nada (distinto de una lista vacía).
    val productos: StateFlow<List<Producto>?>

    suspend fun refreshProductos()
    suspend fun getProducto(id: String): Producto
    suspend fun createProducto(producto: Producto): Producto
    suspend fun updateProducto(producto: Producto): Producto
    suspend fun deleteProducto(id: String)
}
