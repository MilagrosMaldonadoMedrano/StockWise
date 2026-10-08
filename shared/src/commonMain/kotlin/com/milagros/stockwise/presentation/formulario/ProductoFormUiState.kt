package com.milagros.stockwise.presentation.formulario

// Lo que el usuario escribe: todo como texto, tal cual está en los campos.
// Se convierte a números recién al validar (así "12a" se puede mostrar como error).
data class ProductoFormCampos(
    val nombre: String = "",
    val sku: String = "",
    val categoria: String = "",
    val cantidad: String = "",
    val stockMinimo: String = "",
    val precio: String = "",
    val costo: String = "",
)

// Mensaje de error de cada campo (null = sin error)
data class ProductoFormErrores(
    val nombre: String? = null,
    val sku: String? = null,
    val cantidad: String? = null,
    val stockMinimo: String? = null,
    val precio: String? = null,
    val costo: String? = null,
) {
    val hayErrores: Boolean
        get() = listOf(nombre, sku, cantidad, stockMinimo, precio, costo).any { it != null }
}

// A diferencia de lista y detalle, acá no es un sealed interface: el formulario siempre
// muestra los campos y los demás datos se combinan entre sí (ej. editando + guardando + con errores).
data class ProductoFormUiState(
    val esEdicion: Boolean,
    val cargando: Boolean = false,          // edición: trayendo el producto a editar
    val errorCarga: String? = null,         // edición: no se encontró el producto
    val campos: ProductoFormCampos = ProductoFormCampos(),
    val errores: ProductoFormErrores = ProductoFormErrores(),
    val guardando: Boolean = false,
    val mensaje: String? = null,            // error al guardar (snackbar)
    val resultado: String? = null,          // guardado OK: la pantalla vuelve atrás con este mensaje
)
