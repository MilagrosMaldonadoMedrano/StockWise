package com.milagros.stockwise.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.milagros.stockwise.presentation.lista.ProductoListScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ProductoListRoute,
    ) {
        composable<ProductoListRoute> {
            ProductoListScreen()
        }
    }
}
