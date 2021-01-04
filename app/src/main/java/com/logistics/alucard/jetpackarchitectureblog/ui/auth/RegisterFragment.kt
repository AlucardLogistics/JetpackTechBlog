package com.logistics.alucard.jetpackarchitectureblog.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.logistics.alucard.jetpackarchitectureblog.R
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.AuthStateEvent
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.state.RegistrationFields
import com.logistics.alucard.jetpackarchitectureblog.util.GenericApiResponse
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : BaseAuthFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "RegisterFragment: ${viewModel.hashCode()}")

        register_button.setOnClickListener {
            register()
        }

        subscribeObservers()

    }

    fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {authViewState ->
            authViewState.registrationFields?.let { registrationFields ->
                registrationFields.registration_email?.let { input_email.setText(it) }
                registrationFields.registration_username?.let { input_username.setText(it) }
                registrationFields.registration_password?.let { input_password.setText(it) }
                registrationFields.registration_confirm_password?.let { input_password_confirm.setText(it) }
            }
        })
    }

    fun register() {
        viewModel.setStateEvent(
            AuthStateEvent.RegisterAttemptEvent(
                input_email.text.toString(),
                input_username.text.toString(),
                input_password.text.toString(),
                input_password_confirm.text.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setRegistrationFields(
            RegistrationFields(
                input_email.text.toString(),
                input_username.text.toString(),
                input_password.text.toString(),
                input_password_confirm.text.toString()
            )
        )
    }

}