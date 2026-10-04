package com.milagros.stockwise.di

import com.milagros.stockwise.data.remote.supabaseClient
import com.milagros.stockwise.data.repository.ProductoRepositoryImp
import com.milagros.stockwise.domain.repository.ProductoRepository
import com.milagros.stockwise.presentation.lista.ProductoListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {
    single { supabaseClient }
    single<ProductoRepository> { ProductoRepositoryImp(get()) }
    viewModelOf(::ProductoListViewModel)
}