package com.milagros.stockwise.presentation.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milagros.stockwise.domain.model.EstadisticaMensual
import com.milagros.stockwise.domain.repository.VentaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sin caso de uso: el agrupado lo hace la base y el armado es una función pura (armarDashboard)
class EstadisticasViewModel(
    private val ventaRepository: VentaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EstadisticasUiState>(EstadisticasUiState.Cargando)
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()

    // Se guardan las filas para cambiar de mes sin volver a pedirlas a la red
    private var filas: List<EstadisticaMensual> = emptyList()

    init {
        cargar()
    }

    // Se pide cada vez que se abre la pantalla: así incluye las ventas recién registradas
    fun cargar() {
        viewModelScope.launch {
            _uiState.value = EstadisticasUiState.Cargando
            _uiState.value = try {
                filas = ventaRepository.getEstadisticasMensuales()
                if (filas.isEmpty()) EstadisticasUiState.Vacio else armarDashboard(filas, mesSeleccionado = null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                EstadisticasUiState.Error("No se pudieron cargar las estadísticas. Revisá tu conexión.")
            }
        }
    }

    // mes = null vuelve a "todos los meses". Tocar el mes ya elegido también lo deselecciona.
    fun seleccionarMes(mes: String?) {
        val actual = _uiState.value as? EstadisticasUiState.Exito ?: return
        val nuevo = if (mes == actual.mesSeleccionado) null else mes
        _uiState.value = armarDashboard(filas, nuevo)
    }
}
