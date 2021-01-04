package com.logistics.alucard.jetpackarchitectureblog.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.logistics.alucard.jetpackarchitectureblog.models.AuthToken
import com.logistics.alucard.jetpackarchitectureblog.ui.DataState
import com.logistics.alucard.jetpackarchitectureblog.ui.Response
import com.logistics.alucard.jetpackarchitectureblog.ui.ResponseType
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.AuthViewState
import com.logistics.alucard.jetpackarchitectureblog.util.Constants.Companion.NETWORK_TIMEOUT
import com.logistics.alucard.jetpackarchitectureblog.util.Constants.Companion.TESTING_CACHE_DELAY
import com.logistics.alucard.jetpackarchitectureblog.util.Constants.Companion.TESTING_NETWORK_DELAY
import com.logistics.alucard.jetpackarchitectureblog.util.ErrorHandling
import com.logistics.alucard.jetpackarchitectureblog.util.ErrorHandling.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.logistics.alucard.jetpackarchitectureblog.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.logistics.alucard.jetpackarchitectureblog.util.ErrorHandling.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.logistics.alucard.jetpackarchitectureblog.util.ErrorHandling.Companion.UNABLE_TO_RESOLVE_HOST
import com.logistics.alucard.jetpackarchitectureblog.util.GenericApiResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

abstract class NetworkBoundResource<ResponseObject, ViewStateType>
    (
        isNetworkAvailable: Boolean,
        isNetworkRequest: Boolean
    ){

    private val TAG: String = "AppDebug"

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.loading(isLoading = false, cacheData = null))

        if (isNetworkRequest) {
            if(isNetworkAvailable) {
                coroutineScope.launch {
                    //simulate network delay for testing
                    delay(TESTING_NETWORK_DELAY)
                    withContext(Main) {
                        //make network call
                        val apiResponse = createCall()
                        result.addSource(apiResponse) { response ->
                            result.removeSource(apiResponse)

                            coroutineScope.launch {
                                handleNetworkCall(response)
                            }
                        }
                    }

                    GlobalScope.launch(IO) {
                        delay(NETWORK_TIMEOUT)

                        if(!job.isCompleted) {
                            Log.e(TAG, "NetworkBoundResources: JOB NETWORK TIMEOUT.")
                            job.cancel(CancellationException(UNABLE_TO_RESOLVE_HOST))
                        }
                    }
                }
            } else {
                onErrorReturn(UNABLE_TODO_OPERATION_WO_INTERNET, shouldUseDialog = true, shouldUseToast = false)
            }
        } else {
            coroutineScope.launch {

                // fake delay for testing cache
                delay(TESTING_CACHE_DELAY)

                //view data from cache ONLY and return
                createCacheRequestAndReturn()
            }
        }
    }



    suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>?) {
        when(response) {
            is GenericApiResponse.ApiSuccessResponse -> {
                handleApiSuccessResponse(response)
            }

            is GenericApiResponse.ApiErrorResponse -> {
                Log.e(TAG, "NetworkBoundResource: ${response.errorMessage}")
                onErrorReturn(response.errorMessage, true, false)
            }

            is GenericApiResponse.ApiEmptyResponse -> {
                Log.e(TAG, "NetworkBoundResource: Rerquest returned Nothing (HTTP 204)")
                onErrorReturn("HTTP 204. Returned nothing.", true, false)
            }
        }
    }

    fun onCopleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>) {
        result.value = dataState
    }

    fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToast: Boolean) {

        var msg = errorMessage
        var useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None()

        if(msg == null) {
            msg = ERROR_UNKNOWN
        } else if(ErrorHandling.isNetworkError(msg)) {
            msg = ERROR_CHECK_NETWORK_CONNECTION
            useDialog = false
        }

        if (shouldUseToast) {
            responseType = ResponseType.Toast()
        }

        if (useDialog) {
            responseType = ResponseType.Dialog()
        }

        //complete job and emit data state
        onCopleteJob(DataState.error(
            response = Response(
                message = msg,
                responseType = responseType
            )
        ))
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun initNewJob(): Job {

        Log.d(TAG, "InitNewJob: called...")
        job = Job()
        job.invokeOnCompletion(onCancelling = true, invokeImmediately = true, handler = object : CompletionHandler {
            override fun invoke(cause: Throwable?) {
                if (job.isCancelled) {
                    Log.e(TAG, "NetworkBoundResource: Job has been canceled.")
                    cause?.let {
                        onErrorReturn(it.message, false, true)
                    }?: onErrorReturn(ERROR_UNKNOWN, false, true)
                } else if (job.isCompleted) {
                    Log.e(TAG, "NetworkBoundResource: Job has been completed.")
                    //Do nothing. Should be handled already
                }
            }

        })

        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<ResponseObject>)
    abstract suspend fun createCacheRequestAndReturn()
    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>
    abstract fun setJob(job: Job)

}