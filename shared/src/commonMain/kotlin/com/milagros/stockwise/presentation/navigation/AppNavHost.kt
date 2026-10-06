package com.milagros.stockwise.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.milagros.stockwise.presentation.detalle.ProductoDetailScreen
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
            // La lista lee el mensaje que le dejó otra pantalla en su SavedStateHandle
            val mensaje by entry.savedStateHandle
                .getStateFlow<String?>(KEY_MENSAJE, null)
                .collectAsStateWithLifecycle()

            ProductoListScreen(
                onProductoClick = { id ->
                    // Ignora toques mientras ya se está navegando (evita abrir el detalle dos veces)
                    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(ProductoDetailRoute(id))
                    }
                },
                mensaje = mensaje,
                onMensajeMostrado = { entry.savedStateHandle[KEY_MENSAJE] = null },
            )
        }
        composable<ProductoDetailRoute> { entry ->
            val route = entry.toRoute<ProductoDetailRoute>()
            ProductoDetailScreen(
                productoId = route.id,
                // Evita que un doble toque en "volver" saque también la lista y deje la pantalla vacía
                onVolver = dropUnlessResumed { navController.popBackStack() },
                onEliminado = { nombre ->
                    // Le deja el mensaje a la lista y vuelve a ella
                    navController.previousBackStackEntry?.savedStateHandle?.set(KEY_MENSAJE, "\"$nombre\" eliminado")
                    navController.popBackStack()
                },
            )
        }
    }
}
