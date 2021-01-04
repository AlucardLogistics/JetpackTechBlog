package com.logistics.alucard.jetpackarchitectureblog.session

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.logistics.alucard.jetpackarchitectureblog.models.AuthToken
import com.logistics.alucard.jetpackarchitectureblog.persistence.AuthTokenDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
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
    private val TAG: String = "AppDebug"

    private val _cachedToken = MutableLiveData<AuthToken>()

    val cachedToken: LiveData<AuthToken>
    get() = _cachedToken

    fun login(newValue: AuthToken) {
         setValue(newValue)
    }

    fun logout() {
        Log.d(TAG, "logout...")
        GlobalScope.launch(IO) {
            val errorMessage: String? = null
            try {
                cachedToken.value!!.account_pk?.let {
                    authTokenDao.nullifyToken(it)
                }
            } catch (e: CancellationException) {
                Log.e(TAG, "logout... ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "logout... ${e.message}")
            } finally {
                errorMessage?.let {
                    Log.e(TAG, "logout: $errorMessage")
                }
                Log.d(TAG, " logout... done!")
                setValue(null)
            }
        }
    }

    private fun setValue(newValue: AuthToken?) {
        GlobalScope.launch(Main) {
            if(_cachedToken.value != newValue) {
                _cachedToken.value = newValue
            }
        }
    }

    fun isConnectedToTheInternet() =
        (application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false

        }

}