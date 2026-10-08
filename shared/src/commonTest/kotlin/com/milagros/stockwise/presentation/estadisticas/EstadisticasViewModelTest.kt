package com.milagros.stockwise.presentation.estadisticas

import com.milagros.stockwise.domain.model.EstadisticaMensual
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class EstadisticasViewModelTest {

    private val filas = listOf(
        EstadisticaMensual("1", "Café", "2026-09", unidades = 2, ingresos = 12400.0, ganancia = 4400.0),
        EstadisticaMensual("1", "Café", "2026-10", unidades = 2, ingresos = 12400.0, ganancia = 4400.0),
        EstadisticaMensual("2", "Yerba", "2026-10", unidades = 10, ingresos = 45000.0, ganancia = 5000.0),
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun ventas(lista: List<EstadisticaMensual>) = FakeVentaRepository(FakeProductoRepository(), lista)

    @Test
    fun alCargar_muestraTodosLosMeses() = runTest {
        val viewModel = EstadisticasViewModel(ventas(filas))

        val estado = assertIs<EstadisticasUiState.Exito>(viewModel.uiState.value)
        assertNull(estado.mesSeleccionado)
        assertEquals(69800.0, estado.ingresosTotales)
        assertEquals(13800.0, estado.gananciaTotal)
        assertEquals(14L, estado.unidadesTotales)
    }

    @Test
    fun seleccionarMes_filtraTotalesYRanking() = runTest {
        val viewModel = EstadisticasViewModel(ventas(filas))

        viewModel.seleccionarMes("2026-09")

        val estado = assertIs<EstadisticasUiState.Exito>(viewModel.uiState.value)
        assertEquals("2026-09", estado.mesSeleccionado)
        assertEquals(4400.0, estado.gananciaTotal)
        assertEquals(listOf("Café"), estado.productos.map { it.nombre })
    }

    @Test
    fun seleccionarElMismoMes_vuelveATodos() = runTest {
        val viewModel = EstadisticasViewModel(ventas(filas))

        viewModel.seleccionarMes("2026-10")
        viewModel.seleccionarMes("2026-10")

        val estado = assertIs<EstadisticasUiState.Exito>(viewModel.uiState.value)
        assertNull(estado.mesSeleccionado)
    }

    @Test
    fun sinVentas_muestraVacio() = runTest {
        val viewModel = EstadisticasViewModel(ventas(emptyList()))

        assertEquals(EstadisticasUiState.Vacio, viewModel.uiState.value)
    }

    @Test
    fun errorDeRed_muestraErrorYReintentarFunciona() = runTest {
        val repository = ventas(filas).apply { fallarEnEstadisticas = true }
        val viewModel = EstadisticasViewModel(repository)
        assertIs<EstadisticasUiState.Error>(viewModel.uiState.value)

        repository.fallarEnEstadisticas = false
        viewModel.cargar()

        assertIs<EstadisticasUiState.Exito>(viewModel.uiState.value)
    }
}
