package com.logistics.alucard.jetpackarchitectureblog.ui.auth

import androidx.lifecycle.LiveData
import com.logistics.alucard.jetpackarchitectureblog.api.auth.network_responses.LoginResponse
import com.logistics.alucard.jetpackarchitectureblog.api.auth.network_responses.RegistrationResponse
import com.logistics.alucard.jetpackarchitectureblog.models.AuthToken
import com.logistics.alucard.jetpackarchitectureblog.repository.auth.AuthRepository
import com.logistics.alucard.jetpackarchitectureblog.ui.BaseViewModel
import com.logistics.alucard.jetpackarchitectureblog.ui.DataState
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.AuthStateEvent
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.AuthStateEvent.*
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.AuthViewState
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.LoginFields
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.RegistrationFields
import com.logistics.alucard.jetpackarchitectureblog.util.AbsentLiveData
import com.logistics.alucard.jetpackarchitectureblog.util.GenericApiResponse
import javax.inject.Inject
import kotlin.math.log

class AuthViewModel
    @Inject
    constructor(
        val authRepository: AuthRepository
    ) : BaseViewModel<AuthStateEvent, AuthViewState>() {

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        return when(stateEvent) {
            is LoginAttemptEvent -> {
                return authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }

            is RegisterAttemptEvent -> {
                return authRepository.attemptRegistration(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirm_password
                )
            }

            is CheckPreviousAuthEvent -> {
                return authRepository.checkPreviousAuthUser()
            }
        }
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        _viewState.value = update
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        _viewState.value = update
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        _viewState.value = update
    }

    fun cancelActiveJobs() {
        authRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }


}