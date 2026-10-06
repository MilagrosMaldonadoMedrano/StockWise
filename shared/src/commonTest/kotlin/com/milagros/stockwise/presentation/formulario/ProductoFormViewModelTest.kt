package com.milagros.stockwise.presentation.formulario

import com.milagros.stockwise.domain.model.Producto
import com.milagros.stockwise.fakes.FakeProductoRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoFormViewModelTest {

    private val cafe = Producto(id = "1", nombre = "Café", sku = "CF-002", cantidad = 5, stockMinimo = 8, precio = 6200.0)

    private val camposNuevos = ProductoFormCampos(
        nombre = "Azúcar 1kg", sku = "AZ-001", cantidad = "10", stockMinimo = "5", precio = "1500",
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun crear_conCamposValidos_guardaYDevuelveResultado() = runTest {
        val repository = FakeProductoRepository(listOf(cafe))
        val viewModel = ProductoFormViewModel(productoId = null, repository = repository)

        viewModel.onCamposChange(camposNuevos)
        viewModel.guardar()

        assertEquals("\"Azúcar 1kg\" creado", viewModel.uiState.value.resultado)
        assertTrue(repository.productos.value.orEmpty().any { it.nombre == "Azúcar 1kg" })
    }

    @Test
    fun crear_conCamposInvalidos_muestraErroresYNoGuarda() = runTest {
        val repository = FakeProductoRepository(listOf(cafe))
        val viewModel = ProductoFormViewModel(productoId = null, repository = repository)

        viewModel.guardar() // formulario vacío

        val estado = viewModel.uiState.value
        assertTrue(estado.errores.hayErrores)
        assertNull(estado.resultado)
        assertEquals(1, repository.productos.value?.size)
    }

    @Test
    fun crear_conSkuDuplicado_marcaErrorEnElSku() = runTest {
        val viewModel = ProductoFormViewModel(productoId = null, repository = FakeProductoRepository(listOf(cafe)))

        viewModel.onCamposChange(camposNuevos.copy(sku = "CF-002")) // el SKU del café
        viewModel.guardar()

        val estado = viewModel.uiState.value
        assertNotNull(estado.errores.sku)
        assertNull(estado.resultado)
        assertFalse(estado.guardando)
    }

    @Test
    fun errorDeSkuDuplicado_seMantieneHastaQueCambieElSku() = runTest {
        val viewModel = ProductoFormViewModel(productoId = null, repository = FakeProductoRepository(listOf(cafe)))
        viewModel.onCamposChange(camposNuevos.copy(sku = "CF-002"))
        viewModel.guardar()

        // Escribir en otro campo no borra el error del SKU...
        viewModel.onCamposChange(viewModel.uiState.value.campos.copy(nombre = "Azúcar 2kg"))
        assertNotNull(viewModel.uiState.value.errores.sku)

        // ...cambiar el SKU sí
        viewModel.onCamposChange(viewModel.uiState.value.campos.copy(sku = "AZ-002"))
        assertNull(viewModel.uiState.value.errores.sku)
    }

    @Test
    fun editar_cargaLosCamposDelProducto() = runTest {
        val viewModel = ProductoFormViewModel(productoId = "1", repository = FakeProductoRepository(listOf(cafe)))

        val estado = viewModel.uiState.value
        assertTrue(estado.esEdicion)
        assertEquals("Café", estado.campos.nombre)
        assertEquals("6200", estado.campos.precio)
    }

    @Test
    fun editar_guardaLosCambiosEnElMismoProducto() = runTest {
        val repository = FakeProductoRepository(listOf(cafe))
        val viewModel = ProductoFormViewModel(productoId = "1", repository = repository)

        viewModel.onCamposChange(viewModel.uiState.value.campos.copy(nombre = "Café molido 1kg"))
        viewModel.guardar()

        assertEquals("Cambios guardados", viewModel.uiState.value.resultado)
        val productos = repository.productos.value.orEmpty()
        assertEquals(1, productos.size) // no creó uno nuevo
        assertEquals("Café molido 1kg", productos.first().nombre)
    }

    @Test
    fun editar_productoInexistente_muestraErrorDeCarga() = runTest {
        val viewModel = ProductoFormViewModel(productoId = "no-existe", repository = FakeProductoRepository(listOf(cafe)))

        assertNotNull(viewModel.uiState.value.errorCarga)
    }
}
