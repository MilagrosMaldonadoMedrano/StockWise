/*package com.milagros.stockwise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource

import stockwise.shared.generated.resources.Res
import stockwise.shared.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}
*/
/*
package com.milagros.stockwise

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.milagros.stockwise.data.remote.supabaseClient
import com.milagros.stockwise.data.repository.ProductoRepositoryImp

@Composable
fun App() {
    MaterialTheme {
        var text by remember { mutableStateOf("Cargando...") }

        LaunchedEffect(Unit) {
            text = try {
                ProductoRepositoryImp(supabaseClient).getProductos()
                    .joinToString("\n") { "${it.nombre}: ${it.cantidad} u." }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }

        Text(text, modifier = Modifier.safeContentPadding().padding(16.dp))
    }
}*/

package com.milagros.stockwise

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.milagros.stockwise.di.appModule
import com.milagros.stockwise.presentation.lista.ProductoListScreen
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        MaterialTheme {
            ProductoListScreen()
        }
    }
}