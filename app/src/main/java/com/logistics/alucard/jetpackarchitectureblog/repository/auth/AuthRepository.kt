package com.logistics.alucard.jetpackarchitectureblog.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.logistics.alucard.jetpackarchitectureblog.api.auth.OpenApiAuthService
import com.logistics.alucard.jetpackarchitectureblog.api.auth.network_responses.LoginResponse
import com.logistics.alucard.jetpackarchitectureblog.api.auth.network_responses.RegistrationResponse
import com.logistics.alucard.jetpackarchitectureblog.models.AccountProperties
import com.logistics.alucard.jetpackarchitectureblog.models.AuthToken
import com.logistics.alucard.jetpackarchitectureblog.persistence.AccountPropertiesDao
import com.logistics.alucard.jetpackarchitectureblog.persistence.AuthTokenDao
import com.logistics.alucard.jetpackarchitectureblog.repository.NetworkBoundResource
import com.logistics.alucard.jetpackarchitectureblog.session.SessionManager
import com.logistics.alucard.jetpackarchitectureblog.ui.DataState
import com.logistics.alucard.jetpackarchitectureblog.ui.Response
import com.logistics.alucard.jetpackarchitectureblog.ui.ResponseType
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.AuthViewState
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.LoginFields
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.RegistrationFields
import com.logistics.alucard.jetpackarchitectureblog.util.AbsentLiveData
import com.logistics.alucard.jetpackarchitectureblog.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.logistics.alucard.jetpackarchitectureblog.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.logistics.alucard.jetpackarchitectureblog.util.GenericApiResponse
import com.logistics.alucard.jetpackarchitectureblog.util.GenericApiResponse.*
import com.logistics.alucard.jetpackarchitectureblog.util.PreferenceKeys
import com.logistics.alucard.jetpackarchitectureblog.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
    @Inject
    constructor(
        val authTokenDao: AuthTokenDao,
        val accountPropertiesDao: AccountPropertiesDao,
        val openApiAuthService: OpenApiAuthService,
        val sessionManager: SessionManager,
        val sharedPreferences: SharedPreferences,
        val sharedPrefsEditor: SharedPreferences.Editor
    )
{

    private val TAG: String = "AppDebug"

    private var repositoryJob: Job? = null

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {

        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (!loginFieldErrors.equals(LoginFields.LoginError.none())) {
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<LoginResponse, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true
        ) {

            //not used in this case
            override suspend fun createCacheRequestAndReturn() {
                TODO("Not yet implemented")
            }
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: $response")

                //incorrect login credentials counts as 200 response from server
                if(response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                //will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )

                if(result < 0 ) {
                    return onCopleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }

                saveAuthenticationUserToPrefs(email)

                onCopleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return openApiAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

        }.asLiveData()
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {

        val registrationFieldErrors = RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if(!registrationFieldErrors.equals(RegistrationFields.RegistrationError.none())) {
            return returnErrorResponse(registrationFieldErrors, ResponseType.Dialog())
        }

        return object: NetworkBoundResource<RegistrationResponse, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true
        ) {

            //not used in this case
            override suspend fun createCacheRequestAndReturn() {
                TODO("Not yet implemented")
            }
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: $response")

                if(response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                //will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )

                if(result < 0 ) {
                    return onCopleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }
                saveAuthenticationUserToPrefs(email)

                onCopleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return openApiAuthService.register(email, username, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

        }.asLiveData()
    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {

        val previousAuthUserEmail = sharedPreferences
            .getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if(previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previous auth user found...")
            return returnNoTokenFound()
        }

        return object : NetworkBoundResource<Void, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            false
        ) {

            //not used in this case
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {

            }

            override suspend fun createCacheRequestAndReturn() {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                    Log.d(TAG, "checkPreviousAuthUser: searching for token $accountProperties")
                    accountProperties?.let {
                        if (accountProperties.pk > -1) {
                            authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                if (authToken != null) {
                                    onCopleteJob(
                                        DataState.data(
                                            data = AuthViewState(
                                                authToken = authToken
                                            )
                                        )
                                    )
                                    return
                                }
                            }
                        }
                    }
                    Log.d(TAG, "checkPreviousAuthUser: AuthToken not found ...")
                    onCopleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                ResponseType.None()
                            )
                        )
                    )
                }
            }

            //not used in this case
            override fun createCall(): LiveData<GenericApiResponse<Void>> {
                return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

        }.asLiveData()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            }
        }
    }

    private fun returnErrorResponse(
        errorMessage: String,
        responseType: ResponseType
    ): LiveData<DataState<AuthViewState>> {
        Log.d(TAG, "returnErrorResponse: $errorMessage")
        return object: LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage,
                        responseType
                    )
                )
            }
        }
    }

    private fun saveAuthenticationUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    fun cancelActiveJobs() {
        Log.d(TAG, "AuthRepository: Canceling on-going job...")
        repositoryJob?.cancel()
    }

}