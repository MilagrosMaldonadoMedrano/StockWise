package com.milagros.stockwise.presentation.estadisticas

import com.milagros.stockwise.domain.model.EstadisticaProducto
import com.milagros.stockwise.fakes.FakeProductoRepository
import com.milagros.stockwise.fakes.FakeVentaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class EstadisticasViewModelTest {

    private val estadisticas = listOf(
        EstadisticaProducto("1", "Café", unidades = 4, ingresos = 24800.0, ganancia = 8800.0),
        EstadisticaProducto("2", "Yerba", unidades = 10, ingresos = 45000.0, ganancia = 5000.0),
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun ventas(lista: List<EstadisticaProducto>) = FakeVentaRepository(FakeProductoRepository(), lista)

    @Test
    fun conVentas_calculaLosTotales() = runTest {
        val viewModel = EstadisticasViewModel(ventas(estadisticas))

        val estado = assertIs<EstadisticasUiState.Exito>(viewModel.uiState.value)
        assertEquals(69800.0, estado.ingresosTotales)
        assertEquals(13800.0, estado.gananciaTotal)
        assertEquals(14L, estado.unidadesTotales)
        assertEquals(8800.0, estado.gananciaMaxima)
    }

    @Test
    fun sinVentas_muestraVacio() = runTest {
        val viewModel = EstadisticasViewModel(ventas(emptyList()))

        assertEquals(EstadisticasUiState.Vacio, viewModel.uiState.value)
    }

    @Test
    fun errorDeRed_muestraErrorYReintentarFunciona() = runTest {
        val repository = ventas(estadisticas).apply { fallarEnEstadisticas = true }
        val viewModel = EstadisticasViewModel(repository)
        assertIs<EstadisticasUiState.Error>(viewModel.uiState.value)

        repository.fallarEnEstadisticas = false
        viewModel.cargar()

        assertIs<EstadisticasUiState.Exito>(viewModel.uiState.value)
    }

    @Test
    fun gananciaNegativa_noAlargaLaBarraMaxima() = runTest {
        val conPerdida = listOf(EstadisticaProducto("3", "Leche", unidades = 2, ingresos = 1000.0, ganancia = -200.0))
        val viewModel = EstadisticasViewModel(ventas(conPerdida))

        val estado = assertIs<EstadisticasUiState.Exito>(viewModel.uiState.value)
        assertEquals(0.0, estado.gananciaMaxima)
    }
}
