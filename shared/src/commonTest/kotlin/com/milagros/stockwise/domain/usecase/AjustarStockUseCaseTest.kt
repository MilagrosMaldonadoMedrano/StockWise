package com.milagros.stockwise.domain.usecase

import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.fakes.FakeProductoRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AjustarStockUseCaseTest {

    private val cafe = Producto(id = "1", nombre = "Café", cantidad = 5, stockMinimo = 8, precio = 6200.0)

    @Test
    fun sumarStock_incrementaLaCantidad() = runTest {
        val repository = FakeProductoRepository(listOf(cafe))
        val ajustarStock = AjustarStockUseCase(repository)

        val resultado = ajustarStock(cafe, +1)

        assertEquals(6, resultado.cantidad)
        assertEquals(6, repository.productos.value?.first()?.cantidad)
    }

    @Test
    fun restarStock_decrementaLaCantidad() = runTest {
        val repository = FakeProductoRepository(listOf(cafe))

        val resultado = AjustarStockUseCase(repository)(cafe, -1)

        assertEquals(4, resultado.cantidad)
    }

    @Test
    fun restarHastaCero_estaPermitido() = runTest {
        val repository = FakeProductoRepository(listOf(cafe))

        val resultado = AjustarStockUseCase(repository)(cafe, -5)

        assertEquals(0, resultado.cantidad)
    }

    @Test
    fun restarDebajoDeCero_lanzaExcepcionYNoLlamaAlRepositorio() = runTest {
        val sinStock = cafe.copy(cantidad = 0)
        val repository = FakeProductoRepository(listOf(sinStock))

        assertFailsWith<StockNegativoException> {
            AjustarStockUseCase(repository)(sinStock, -1)
        }
        assertTrue(repository.updates.isEmpty(), "No debería haber intentado guardar")
    }
}
