package com.logistics.alucard.jetpackarchitectureblog.ui.auth

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.logistics.alucard.jetpackarchitectureblog.R
import com.logistics.alucard.jetpackarchitectureblog.ui.BaseActivity
import com.logistics.alucard.jetpackarchitectureblog.viewmodels.ViewModelProviderFactory
import javax.inject.Inject

class AuthActivity : BaseActivity() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
    }
}