package com.logistics.alucard.jetpackarchitectureblog.di

import com.logistics.alucard.jetpackarchitectureblog.di.auth.AuthFragmentBuildersModule
import com.logistics.alucard.jetpackarchitectureblog.di.auth.AuthModule
import com.logistics.alucard.jetpackarchitectureblog.di.auth.AuthScope
import com.logistics.alucard.jetpackarchitectureblog.di.auth.AuthViewModelModule
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.AuthActivity
import com.logistics.alucard.jetpackarchitectureblog.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @ContributesAndroidInjector
    abstract fun contributeMainActivity() : MainActivity

}