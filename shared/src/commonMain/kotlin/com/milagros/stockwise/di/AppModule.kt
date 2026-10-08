package com.milagros.stockwise.di

import com.milagros.stockwise.data.remote.supabaseClient
import com.milagros.stockwise.data.repository.ProductoRepositoryImp
import com.milagros.stockwise.data.repository.VentaRepositoryImp
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.domain.repository.VentaRepository
import com.milagros.stockwise.domain.usecase.AjustarStockUseCase
import com.milagros.stockwise.domain.usecase.RegistrarVentaUseCase
import com.milagros.stockwise.presentation.detalle.ProductoDetailViewModel
import com.milagros.stockwise.presentation.estadisticas.EstadisticasViewModel
import com.milagros.stockwise.presentation.formulario.ProductoFormViewModel
import com.milagros.stockwise.presentation.lista.ProductoListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {
    single { supabaseClient }
    single<ProductoRepository> { ProductoRepositoryImp(get()) }
    single<VentaRepository> { VentaRepositoryImp(get()) }
    factoryOf(::AjustarStockUseCase)
    factoryOf(::RegistrarVentaUseCase)
    viewModelOf(::ProductoListViewModel)
    viewModelOf(::EstadisticasViewModel)
    // El id llega desde la pantalla con parametersOf(productoId)
    viewModel { params ->
        ProductoDetailViewModel(
            productoId = params.get(),
            repository = get(),
            ajustarStock = get(),
            registrarVentaUseCase = get(),
        )
    }
    // id null = crear; getOrNull porque parametersOf(null) no trae valor
    viewModel { params -> ProductoFormViewModel(productoId = params.getOrNull(), repository = get()) }
}
