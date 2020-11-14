package com.logistics.alucard.jetpackarchitectureblog.ui.auth

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.logistics.alucard.jetpackarchitectureblog.viewmodels.ViewModelProviderFactory
import dagger.android.support.DaggerFragment
import java.lang.Exception
import javax.inject.Inject

abstract class BaseAuthFragment: DaggerFragment() {

    val TAG: String = "BaseAuthFragment"

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
        }?: throw Exception("Invalid Activity")
    }
}