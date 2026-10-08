package com.milagros.stockwise.presentation.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milagros.stockwise.domain.repository.VentaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sin caso de uso: mostrar estadísticas no tiene reglas de negocio, los cálculos los hace la base
class EstadisticasViewModel(
    private val ventaRepository: VentaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EstadisticasUiState>(EstadisticasUiState.Cargando)
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    // Se pide cada vez que se abre la pantalla: así incluye las ventas recién registradas
    fun cargar() {
        viewModelScope.launch {
            _uiState.value = EstadisticasUiState.Cargando
            _uiState.value = try {
                val productos = ventaRepository.getEstadisticas()
                if (productos.isEmpty()) EstadisticasUiState.Vacio else EstadisticasUiState.Exito(productos)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                EstadisticasUiState.Error("No se pudieron cargar las estadísticas. Revisá tu conexión.")
            }
        }
    }
}
