package com.logistics.alucard.jetpackarchitectureblog.di

import androidx.lifecycle.ViewModelProvider
import com.logistics.alucard.jetpackarchitectureblog.viewmodels.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}