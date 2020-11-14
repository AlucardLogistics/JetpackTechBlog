package com.logistics.alucard.jetpackarchitectureblog.di.auth

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

    // TEMPORARY
    @AuthScope
    @Provides
    fun provideFakeApiService(): OpenApiAuthService {
        return Retrofit.Builder()
            .baseUrl("https://open-api.xyz")
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: OpenApiAuthService
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager
        )
    }

}