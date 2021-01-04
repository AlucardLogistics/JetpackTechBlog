package com.logistics.alucard.jetpackarchitectureblog.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.logistics.alucard.jetpackarchitectureblog.R
import com.logistics.alucard.jetpackarchitectureblog.models.AuthToken
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.AuthStateEvent
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.LoginFields
import com.logistics.alucard.jetpackarchitectureblog.util.GenericApiResponse
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "LoginFragment: ${viewModel.hashCode()}")

        subscribeObservers()

        login_button.setOnClickListener {
            login()
        }

    }

    fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { authViewState ->
            authViewState.loginFields?.let { loginFields ->
                loginFields.login_email?.let { input_email.setText(it) }
                loginFields.login_password?.let { input_password.setText(it) }
            }
        })
    }

    fun login() {
        viewModel.setStateEvent(
            AuthStateEvent.LoginAttemptEvent(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }


    //save the text typed in the edit box into the viewState
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }
}