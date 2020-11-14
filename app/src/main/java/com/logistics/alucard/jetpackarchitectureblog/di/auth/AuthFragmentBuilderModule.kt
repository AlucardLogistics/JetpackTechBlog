package com.logistics.alucard.jetpackarchitectureblog.di.auth

import com.logistics.alucard.jetpackarchitectureblog.ui.auth.ForgotPasswordFragment
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.LauncherFragment
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.LoginFragment
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment

}