package com.milagros.stockwise.domain.usecase

import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.domain.repository.StockInsuficienteException
import com.milagros.stockwise.domain.repository.VentaRepository

// Regla de negocio: se vende al menos 1 unidad y nunca más de lo que hay en stock.
// La base vuelve a validarlo dentro de registrar_venta (defensa en profundidad).
class RegistrarVentaUseCase(
    private val ventaRepository: VentaRepository,
    private val productoRepository: ProductoRepository,
) {
    suspend operator fun invoke(producto: Producto, cantidad: Int) {
        val id = requireNotNull(producto.id) { "No se puede vender un producto sin id" }
        if (cantidad <= 0) throw CantidadInvalidaException()
        if (cantidad > producto.cantidad) throw StockInsuficienteException()

        ventaRepository.registrarVenta(id, cantidad)

        // El stock lo descontó la base: se refresca la única fuente de verdad
        // para que lista y detalle muestren la cantidad nueva.
        productoRepository.refreshProductos()
    }
}

class CantidadInvalidaException : IllegalArgumentException("La cantidad tiene que ser mayor a 0")
