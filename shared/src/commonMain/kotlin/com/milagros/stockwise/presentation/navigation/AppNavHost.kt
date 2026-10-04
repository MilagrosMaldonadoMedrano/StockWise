package com.milagros.stockwise.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.milagros.stockwise.presentation.detalle.ProductoDetailScreen
import com.milagros.stockwise.presentation.lista.ProductoListScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ProductoListRoute,
    ) {
        composable<ProductoListRoute> { entry ->
            ProductoListScreen(
                onProductoClick = { id ->
                    // Ignora toques mientras ya se está navegando (evita abrir el detalle dos veces)
                    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(ProductoDetailRoute(id))
                    }
                },
            )
        }
        composable<ProductoDetailRoute> { entry ->
            val route = entry.toRoute<ProductoDetailRoute>()
            ProductoDetailScreen(
                productoId = route.id,
                // Evita que un doble toque en "volver" saque también la lista y deje la pantalla vacía
                onVolver = dropUnlessResumed { navController.popBackStack() },
            )
        }
    }
}
