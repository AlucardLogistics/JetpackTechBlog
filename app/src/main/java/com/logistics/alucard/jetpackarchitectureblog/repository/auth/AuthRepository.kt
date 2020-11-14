package com.logistics.alucard.jetpackarchitectureblog.repository.auth

import com.logistics.alucard.jetpackarchitectureblog.api.auth.OpenApiAuthService
import com.logistics.alucard.jetpackarchitectureblog.persistence.AccountPropertiesDao
import com.logistics.alucard.jetpackarchitectureblog.persistence.AuthTokenDao
import com.logistics.alucard.jetpackarchitectureblog.session.SessionManager
import javax.inject.Inject

class AuthRepository
    @Inject
    constructor(
        val authTokenDao: AuthTokenDao,
        val accountPropertiesDao: AccountPropertiesDao,
        val openApiAuthService: OpenApiAuthService,
        val sessionManager: SessionManager
    )
{


}