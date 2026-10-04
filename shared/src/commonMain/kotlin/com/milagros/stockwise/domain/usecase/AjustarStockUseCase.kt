package com.milagros.stockwise.domain.usecase

import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.domain.repository.ProductoRepository

// Regla de negocio: el stock nunca puede quedar negativo.
// La base también lo valida (check cantidad >= 0), pero así el error se detecta antes de ir a la red.
class AjustarStockUseCase(
    private val repository: ProductoRepository,
) {
    suspend operator fun invoke(producto: Producto, delta: Int): Producto {
        val nuevaCantidad = producto.cantidad + delta
        if (nuevaCantidad < 0) throw StockNegativoException(producto.cantidad, delta)
        return repository.updateProducto(producto.copy(cantidad = nuevaCantidad))
    }
}

class StockNegativoException(cantidadActual: Int, delta: Int) :
    IllegalArgumentException("No se puede ajustar en $delta: el stock actual es $cantidadActual")
