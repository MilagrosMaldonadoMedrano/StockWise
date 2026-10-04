package com.milagros.stockwise.domain.repository

import com.milagros.stockwise.domain.model.Producto

interface ProductoRepository {
    suspend fun getProductos(): List<Producto>
    suspend fun getProducto(id: String): Producto
    suspend fun createProducto(producto: Producto): Producto
    suspend fun updateProducto(producto: Producto): Producto
    suspend fun deleteProducto(id: String)
}