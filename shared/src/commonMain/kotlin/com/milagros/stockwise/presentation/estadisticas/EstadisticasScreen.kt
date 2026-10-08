package com.milagros.stockwise.presentation.estadisticas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.milagros.stockwise.presentation.util.formatearCompacto
import com.milagros.stockwise.presentation.util.mesCorto
import com.milagros.stockwise.presentation.util.mesLargo
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
    EstadisticasContent(
        state = state,
        onVolver = onVolver,
        onReintentar = viewModel::cargar,
        onSeleccionarMes = viewModel::seleccionarMes,
    )
}

// Versión "sin estado": solo dibuja lo que recibe
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasContent(
    state: EstadisticasUiState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onSeleccionarMes: (mes: String?) -> Unit,
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

                is EstadisticasUiState.Exito -> Dashboard(state, onSeleccionarMes)
            }
        }
    }
}

@Composable
private fun Dashboard(state: EstadisticasUiState.Exito, onSeleccionarMes: (String?) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GraficoMensual(
                meses = state.meses,
                seleccionado = state.mesSeleccionado,
                onSeleccionar = onSeleccionarMes,
            )
        }
        // Período al que corresponden los totales y el ranking
        item {
            Text(
                state.mesSeleccionado?.let(::mesLargo) ?: "Todos los meses",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
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
        if (state.productos.isEmpty()) {
            item {
                Text(
                    "No hubo ventas en este mes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(state.productos, key = { it.productoId }) { producto ->
            FilaRanking(producto, state.gananciaMaxima)
        }
    }
}

// Gráfico de barras de ganancia por mes, dibujado con Compose (sin librerías: compila igual en iOS).
// Tocar una barra filtra el dashboard por ese mes; tocarla de nuevo vuelve a "todos".
@Composable
private fun GraficoMensual(
    meses: List<ResumenMes>,
    seleccionado: String?,
    onSeleccionar: (String?) -> Unit,
) {
    val maxima = meses.maxOfOrNull { it.ganancia }?.coerceAtLeast(0.0) ?: 0.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ganancia por mes", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tocá un mes para filtrar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (seleccionado != null) {
                    TextButton(onClick = { onSeleccionar(null) }) { Text("Ver todos") }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                meses.forEach { mes ->
                    BarraMes(
                        mes = mes,
                        fraccion = if (maxima > 0) (mes.ganancia / maxima).toFloat().coerceIn(0f, 1f) else 0f,
                        activa = seleccionado == null || seleccionado == mes.mes,
                        onClick = { onSeleccionar(mes.mes) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraMes(
    mes: ResumenMes,
    fraccion: Float,
    activa: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // La altura se anima al cargar y al cambiar de datos
    val altura by animateFloatAsState(targetValue = fraccion, animationSpec = tween(durationMillis = 600))
    val color = when {
        mes.ganancia < 0 -> MaterialTheme.colorScheme.error
        activa -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    }
    val descripcion = "${mesLargo(mes.mes)}: ganancia ${mes.ganancia.formatearPrecio()}"

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = descripcion },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            if (mes.unidades > 0) mes.ganancia.formatearCompacto() else "",
            style = MaterialTheme.typography.labelSmall,
            color = if (activa) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(Modifier.height(4.dp))
        // Zona de la barra: ocupa el alto disponible y la barra crece desde abajo
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // Un mínimo visible para meses con ventas pero ganancia muy chica
            val alto = if (mes.unidades > 0) altura.coerceAtLeast(0.02f) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .fillMaxHeight(alto)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(color),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            mesCorto(mes.mes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (activa) FontWeight.Bold else FontWeight.Normal,
            color = if (activa) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
