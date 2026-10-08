package com.milagros.stockwise.presentation.formulario

import com.milagros.stockwise.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductoFormValidatorTest {

    private val camposValidos = ProductoFormCampos(
        nombre = "  Yerba Mate 1kg  ",
        sku = " YM-001 ",
        categoria = "",
        cantidad = "24",
        stockMinimo = "10",
        precio = "4500,50",
    )

    @Test
    fun camposValidos_devuelveProductoLimpio() {
        val resultado = assertIs<ResultadoValidacion.Valido>(validarProducto(camposValidos))

        with(resultado.producto) {
            assertEquals("Yerba Mate 1kg", nombre) // sin espacios de más
            assertEquals("YM-001", sku)
            assertNull(categoria)                  // vacío -> null
            assertEquals(24, cantidad)
            assertEquals(10, stockMinimo)
            assertEquals(4500.50, precio)
        }
    }

    @Test
    fun precioConPunto_tambienEsValido() {
        val resultado = assertIs<ResultadoValidacion.Valido>(validarProducto(camposValidos.copy(precio = "4500.50")))
        assertEquals(4500.50, resultado.producto.precio)
    }

    @Test
    fun formularioVacio_marcaTodosLosObligatorios() {
        val errores = assertIs<ResultadoValidacion.Invalido>(validarProducto(ProductoFormCampos())).errores

        assertNotNull(errores.nombre)
        assertNotNull(errores.cantidad)
        assertNotNull(errores.stockMinimo)
        assertNotNull(errores.precio)
        assertNull(errores.sku) // el SKU es opcional
    }

    @Test
    fun cantidadNegativa_esInvalida() {
        val errores = assertIs<ResultadoValidacion.Invalido>(validarProducto(camposValidos.copy(cantidad = "-1"))).errores
        assertEquals("No puede ser negativo", errores.cantidad)
    }

    @Test
    fun cantidadConDecimales_esInvalida() {
        val errores = assertIs<ResultadoValidacion.Invalido>(validarProducto(camposValidos.copy(cantidad = "2.5"))).errores
        assertEquals("Ingresá un número entero", errores.cantidad)
    }

    @Test
    fun precioConLetras_esInvalido() {
        val errores = assertIs<ResultadoValidacion.Invalido>(validarProducto(camposValidos.copy(precio = "12a"))).errores
        assertNotNull(errores.precio)
    }

    @Test
    fun nombreDemasiadoLargo_esInvalido() {
        val errores = assertIs<ResultadoValidacion.Invalido>(
            validarProducto(camposValidos.copy(nombre = "a".repeat(NOMBRE_MAX + 1)))
        ).errores
        assertNotNull(errores.nombre)
    }

    @Test
    fun costoVacio_esCero() {
        val resultado = assertIs<ResultadoValidacion.Valido>(validarProducto(camposValidos.copy(costo = "")))
        assertEquals(0.0, resultado.producto.costo)
    }

    @Test
    fun costoConComa_seConvierte() {
        val resultado = assertIs<ResultadoValidacion.Valido>(validarProducto(camposValidos.copy(costo = "3000,25")))
        assertEquals(3000.25, resultado.producto.costo)
    }

    @Test
    fun costoNegativo_esInvalido() {
        val errores = assertIs<ResultadoValidacion.Invalido>(validarProducto(camposValidos.copy(costo = "-5"))).errores
        assertEquals("No puede ser negativo", errores.costo)
    }

    @Test
    fun toCampos_convierteUnProductoParaEditar() {
        val producto = Producto(id = "1", nombre = "Café", cantidad = 5, stockMinimo = 8, precio = 1234.5)

        val campos = producto.toCampos()

        assertEquals("1234,5", campos.precio)
        assertEquals("5", campos.cantidad)
        assertEquals("", campos.sku)
        assertEquals("6200", Producto(nombre = "x", cantidad = 0, stockMinimo = 0, precio = 6200.0).toCampos().precio)
    }
}
