package com.logistics.alucard.jetpackarchitectureblog.session

import android.app.Application
import com.logistics.alucard.jetpackarchitectureblog.persistence.AuthTokenDao

class SessionManager (
    val authTokenDao: AuthTokenDao,
    val application: Application
    ) {



}