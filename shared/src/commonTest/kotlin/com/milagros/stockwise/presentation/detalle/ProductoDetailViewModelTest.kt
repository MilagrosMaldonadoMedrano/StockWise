package com.milagros.stockwise.presentation.detalle

import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.domain.usecase.AjustarStockUseCase
import com.milagros.stockwise.domain.usecase.RegistrarVentaUseCase
import com.milagros.stockwise.fakes.FakeProductoRepository
import com.milagros.stockwise.fakes.FakeVentaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoDetailViewModelTest {

    private val cafe = Producto(id = "1", nombre = "Café", cantidad = 5, stockMinimo = 8, precio = 6200.0)

    // viewModelScope usa Dispatchers.Main, que en tests no existe: se reemplaza por uno de test
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun crearViewModel(repository: FakeProductoRepository) =
        ProductoDetailViewModel(
            "1",
            repository,
            AjustarStockUseCase(repository),
            RegistrarVentaUseCase(FakeVentaRepository(repository), repository),
        )

    @Test
    fun registrarVenta_descuentaStockYMuestraMensaje() = runTest {
        val viewModel = crearViewModel(FakeProductoRepository(listOf(cafe)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.registrarVenta(2)

        val estado = assertIs<ProductoDetailUiState.Exito>(viewModel.uiState.value)
        assertEquals(3, estado.producto.cantidad)
        assertEquals("Venta registrada: 2 u.", estado.mensaje)
    }

    @Test
    fun registrarVentaMayorAlStock_muestraErrorYNoDescuenta() = runTest {
        val viewModel = crearViewModel(FakeProductoRepository(listOf(cafe)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.registrarVenta(10)

        val estado = assertIs<ProductoDetailUiState.Exito>(viewModel.uiState.value)
        assertEquals(5, estado.producto.cantidad)
        assertEquals("No hay stock suficiente para esa venta", estado.mensaje)
    }

    @Test
    fun alCargar_muestraElProducto() = runTest {
        val viewModel = crearViewModel(FakeProductoRepository(listOf(cafe)))
        // uiState usa WhileSubscribed: hay que observarlo para que se calcule
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        val estado = assertIs<ProductoDetailUiState.Exito>(viewModel.uiState.value)
        assertEquals(cafe, estado.producto)
    }

    @Test
    fun eliminar_pasaAEstadoEliminadoYLoSacaDelRepositorio() = runTest {
        val repository = FakeProductoRepository(listOf(cafe))
        val viewModel = crearViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.eliminarProducto()

        assertEquals(ProductoDetailUiState.Eliminado("Café"), viewModel.uiState.value)
        assertTrue(repository.productos.value.orEmpty().isEmpty())
    }

    @Test
    fun eliminarConErrorDeRed_muestraMensajeYConservaElProducto() = runTest {
        val repository = FakeProductoRepository(listOf(cafe)).apply { fallarEnDelete = true }
        val viewModel = crearViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.eliminarProducto()

        val estado = assertIs<ProductoDetailUiState.Exito>(viewModel.uiState.value)
        assertEquals(cafe, estado.producto)
        assertFalse(estado.eliminando)
        assertNotNull(estado.mensaje)
    }

    @Test
    fun incrementarStock_actualizaElEstado() = runTest {
        val viewModel = crearViewModel(FakeProductoRepository(listOf(cafe)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.incrementarStock()

        val estado = assertIs<ProductoDetailUiState.Exito>(viewModel.uiState.value)
        assertEquals(6, estado.producto.cantidad)
        assertFalse(estado.ajustando)
    }
}
