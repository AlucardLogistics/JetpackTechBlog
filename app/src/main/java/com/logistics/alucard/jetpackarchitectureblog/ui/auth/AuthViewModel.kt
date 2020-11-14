package com.logistics.alucard.jetpackarchitectureblog.ui.auth

import androidx.lifecycle.ViewModel
import com.logistics.alucard.jetpackarchitectureblog.repository.auth.AuthRepository
import javax.inject.Inject

class AuthViewModel
    @Inject
    constructor(
        val authRepository: AuthRepository
    ) : ViewModel() {


}