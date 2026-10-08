package com.milagros.stockwise.fakes

import com.milagros.stockwise.domain.model.EstadisticaMensual
import com.milagros.stockwise.domain.repository.StockInsuficienteException
import com.milagros.stockwise.domain.repository.VentaRepository

// Ventas en memoria para tests. Descuenta el stock en el FakeProductoRepository,
// como lo hace la función registrar_venta en la base.
class FakeVentaRepository(
    private val productos: FakeProductoRepository,
    var estadisticas: List<EstadisticaMensual> = emptyList(),
) : VentaRepository {

    var fallarEnEstadisticas = false
    val ventas = mutableListOf<Pair<String, Int>>() // (productoId, cantidad)

    override suspend fun registrarVenta(productoId: String, cantidad: Int) {
        val producto = productos.productos.value.orEmpty().first { it.id == productoId }
        if (producto.cantidad < cantidad) throw StockInsuficienteException()
        productos.updateProducto(producto.copy(cantidad = producto.cantidad - cantidad))
        ventas += productoId to cantidad
    }

    override suspend fun getEstadisticasMensuales(): List<EstadisticaMensual> {
        if (fallarEnEstadisticas) throw RuntimeException("Error de red simulado")
        return estadisticas
    }
}
