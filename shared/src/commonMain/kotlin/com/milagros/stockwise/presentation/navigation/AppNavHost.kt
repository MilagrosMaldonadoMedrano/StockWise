package com.milagros.stockwise.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.milagros.stockwise.presentation.detalle.ProductoDetailScreen
import com.milagros.stockwise.presentation.formulario.ProductoFormScreen
import com.milagros.stockwise.presentation.lista.ProductoListScreen

// Clave para pasarle un mensaje a la pantalla anterior (patrón "devolver un resultado")
private const val KEY_MENSAJE = "mensaje"

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ProductoListRoute,
    ) {
        composable<ProductoListRoute> { entry ->
            val mensaje by entry.mensajeRecibido()
            ProductoListScreen(
                onProductoClick = { id ->
                    entry.siEstaActiva { navController.navigate(ProductoDetailRoute(id)) }
                },
                onNuevoProducto = {
                    entry.siEstaActiva { navController.navigate(ProductoFormRoute()) }
                },
                mensaje = mensaje,
                onMensajeMostrado = { entry.borrarMensaje() },
            )
        }
        composable<ProductoDetailRoute> { entry ->
            val route = entry.toRoute<ProductoDetailRoute>()
            val mensaje by entry.mensajeRecibido()
            ProductoDetailScreen(
                productoId = route.id,
                // Evita que un doble toque en "volver" saque también la lista y deje la pantalla vacía
                onVolver = dropUnlessResumed { navController.popBackStack() },
                onEditar = {
                    entry.siEstaActiva { navController.navigate(ProductoFormRoute(route.id)) }
                },
                onEliminado = { nombre -> navController.volverConMensaje("\"$nombre\" eliminado") },
                mensajeNavegacion = mensaje,
                onMensajeNavegacionMostrado = { entry.borrarMensaje() },
            )
        }
        composable<ProductoFormRoute> { entry ->
            val route = entry.toRoute<ProductoFormRoute>()
            ProductoFormScreen(
                productoId = route.id,
                onVolver = dropUnlessResumed { navController.popBackStack() },
                onGuardado = { mensaje -> navController.volverConMensaje(mensaje) },
            )
        }
    }
}

// Ignora toques mientras ya se está navegando (evita abrir la misma pantalla dos veces)
private fun NavBackStackEntry.siEstaActiva(accion: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) accion()
}

// Deja un mensaje en la pantalla anterior y vuelve a ella
private fun NavController.volverConMensaje(mensaje: String) {
    previousBackStackEntry?.savedStateHandle?.set(KEY_MENSAJE, mensaje)
    popBackStack()
}

// Mensaje que otra pantalla le dejó a esta en su SavedStateHandle
@Composable
private fun NavBackStackEntry.mensajeRecibido(): State<String?> =
    savedStateHandle.getStateFlow<String?>(KEY_MENSAJE, null).collectAsStateWithLifecycle()

private fun NavBackStackEntry.borrarMensaje() {
    savedStateHandle[KEY_MENSAJE] = null
}
