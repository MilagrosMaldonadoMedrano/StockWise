package com.milagros.stockwise.domain.usecase

import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.domain.repository.StockInsuficienteException
import com.milagros.stockwise.fakes.FakeProductoRepository
import com.milagros.stockwise.fakes.FakeVentaRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RegistrarVentaUseCaseTest {

    private val cafe = Producto(id = "1", nombre = "Café", cantidad = 5, stockMinimo = 8, precio = 6200.0, costo = 4000.0)

    @Test
    fun venderDentroDelStock_registraLaVentaYDescuentaStock() = runTest {
        val productos = FakeProductoRepository(listOf(cafe))
        val ventas = FakeVentaRepository(productos)

        RegistrarVentaUseCase(ventas, productos)(cafe, 2)

        assertEquals(listOf("1" to 2), ventas.ventas)
        assertEquals(3, productos.productos.value?.first()?.cantidad)
    }

    @Test
    fun venderTodoElStock_estaPermitido() = runTest {
        val productos = FakeProductoRepository(listOf(cafe))

        RegistrarVentaUseCase(FakeVentaRepository(productos), productos)(cafe, 5)

        assertEquals(0, productos.productos.value?.first()?.cantidad)
    }

    @Test
    fun venderMasQueElStock_lanzaExcepcionSinRegistrar() = runTest {
        val productos = FakeProductoRepository(listOf(cafe))
        val ventas = FakeVentaRepository(productos)

        assertFailsWith<StockInsuficienteException> { RegistrarVentaUseCase(ventas, productos)(cafe, 6) }
        assertTrue(ventas.ventas.isEmpty())
    }

    @Test
    fun venderCeroUnidades_lanzaExcepcionSinRegistrar() = runTest {
        val productos = FakeProductoRepository(listOf(cafe))
        val ventas = FakeVentaRepository(productos)

        assertFailsWith<CantidadInvalidaException> { RegistrarVentaUseCase(ventas, productos)(cafe, 0) }
        assertTrue(ventas.ventas.isEmpty())
    }
}
