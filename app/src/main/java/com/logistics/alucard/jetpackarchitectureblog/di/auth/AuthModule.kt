package com.logistics.alucard.jetpackarchitectureblog.di.auth

import android.content.SharedPreferences
import com.logistics.alucard.jetpackarchitectureblog.api.auth.OpenApiAuthService
import com.logistics.alucard.jetpackarchitectureblog.persistence.AccountPropertiesDao
import com.logistics.alucard.jetpackarchitectureblog.persistence.AuthTokenDao
import com.logistics.alucard.jetpackarchitectureblog.repository.auth.AuthRepository
import com.logistics.alucard.jetpackarchitectureblog.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule{

    @AuthScope
    @Provides
    fun provideFakeApiService(retrofitBuilder: Retrofit.Builder): OpenApiAuthService {
        return retrofitBuilder
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: OpenApiAuthService,
        sharedPreferences: SharedPreferences,
        sharedPrefEditor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager,
            sharedPreferences,
            sharedPrefEditor
        )
    }

}