package com.milagros.stockwise.di

import com.milagros.stockwise.data.remote.supabaseClient
import com.milagros.stockwise.data.repository.ProductoRepositoryImp
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.domain.usecase.AjustarStockUseCase
import com.milagros.stockwise.presentation.detalle.ProductoDetailViewModel
import com.milagros.stockwise.presentation.lista.ProductoListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {
    single { supabaseClient }
    single<ProductoRepository> { ProductoRepositoryImp(get()) }
    factoryOf(::AjustarStockUseCase)
    viewModelOf(::ProductoListViewModel)
    // El id llega desde la pantalla con parametersOf(productoId)
    viewModel { params ->
        ProductoDetailViewModel(productoId = params.get(), repository = get(), ajustarStock = get())
    }
}