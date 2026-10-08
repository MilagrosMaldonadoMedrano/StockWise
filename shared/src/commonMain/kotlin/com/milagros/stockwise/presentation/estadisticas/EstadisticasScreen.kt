package com.milagros.stockwise.presentation.estadisticas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.milagros.stockwise.domain.model.EstadisticaProducto
import com.milagros.stockwise.presentation.util.formatearPrecio
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import stockwise.shared.generated.resources.Res
import stockwise.shared.generated.resources.ic_arrow_back

// Versión "con estado": obtiene el ViewModel y observa su estado
@Composable
fun EstadisticasScreen(
    onVolver: () -> Unit,
    viewModel: EstadisticasViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EstadisticasContent(state = state, onVolver = onVolver, onReintentar = viewModel::cargar)
}

// Versión "sin estado": solo dibuja lo que recibe
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasContent(
    state: EstadisticasUiState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventas y ganancias") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                EstadisticasUiState.Cargando -> CircularProgressIndicator()

                EstadisticasUiState.Vacio -> Text(
                    "Todavía no hay ventas.\nRegistrá una desde el detalle de un producto.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(24.dp),
                )

                is EstadisticasUiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ocurrió un error", style = MaterialTheme.typography.titleMedium)
                    Text(state.mensaje, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onReintentar) { Text("Reintentar") }
                }

                is EstadisticasUiState.Exito -> Dashboard(state)
            }
        }
    }
}

@Composable
private fun Dashboard(state: EstadisticasUiState.Exito) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Totales: la ganancia es el dato principal, ingresos y unidades lo acompañan
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ganancia total", style = MaterialTheme.typography.labelLarge)
                    Text(
                        state.gananciaTotal.formatearPrecio(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Indicador("Ingresos", state.ingresosTotales.formatearPrecio(), Modifier.weight(1f))
                Indicador("Unidades vendidas", "${state.unidadesTotales} u.", Modifier.weight(1f))
            }
        }
        item {
            Text(
                "Productos que más ganancia dieron",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(state.productos, key = { it.productoId }) { producto ->
            FilaRanking(producto, state.gananciaMaxima)
        }
    }
}

@Composable
private fun Indicador(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge)
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

// Una fila del ranking: la barra es proporcional a la ganancia del producto (sin librerías de gráficos)
@Composable
private fun FilaRanking(producto: EstadisticaProducto, gananciaMaxima: Double) {
    val fraccion = if (gananciaMaxima > 0) (producto.ganancia / gananciaMaxima).toFloat().coerceIn(0f, 1f) else 0f
    val colorGanancia =
        if (producto.ganancia < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${producto.unidades} u. · ingresos ${producto.ingresos.formatearPrecio()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    producto.ganancia.formatearPrecio(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorGanancia,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraccion)
                        .fillMaxHeight()
                        .background(colorGanancia),
                )
            }
        }
    }
}
