package com.logistics.alucard.jetpackarchitectureblog.session

import android.app.Application
import com.logistics.alucard.jetpackarchitectureblog.persistence.AuthTokenDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
    @Inject
    constructor(
        val authTokenDao: AuthTokenDao,
        val application: Application
    )
{



}