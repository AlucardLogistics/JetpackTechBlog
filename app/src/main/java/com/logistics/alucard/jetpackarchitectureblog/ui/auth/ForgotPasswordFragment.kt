package com.logistics.alucard.jetpackarchitectureblog.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import com.logistics.alucard.jetpackarchitectureblog.R
import com.logistics.alucard.jetpackarchitectureblog.ui.DataState
import com.logistics.alucard.jetpackarchitectureblog.ui.DataStateChangeListener
import com.logistics.alucard.jetpackarchitectureblog.ui.Response
import com.logistics.alucard.jetpackarchitectureblog.ui.ResponseType
import com.logistics.alucard.jetpackarchitectureblog.ui.auth.ForgotPasswordFragment.WebAppInterface.*
import com.logistics.alucard.jetpackarchitectureblog.util.Constants
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ClassCastException

class ForgotPasswordFragment : BaseAuthFragment() {

    lateinit var webView: WebView
    lateinit var stateChangeListener: DataStateChangeListener

    val webInteractionCallback: OnWebInteractionCallback = object : OnWebInteractionCallback {

        override fun onSuccess(email: String) {
            Log.d(TAG, "onSuccess: reset link will be sent to $email")
            onPasswordResetLinkSent()
        }

        override fun onError(errorMessage: String) {
            Log.e(TAG, "onError: $errorMessage")

            val dataState = DataState.error<Any>(
                response = Response(errorMessage, ResponseType.Dialog())
            )
            stateChangeListener.onDataStateChange(
                dataState = dataState
            )
        }

        override fun onLoading(isLoading: Boolean) {
            Log.d(TAG, "onLoading...")
            GlobalScope.launch(Main) {
                stateChangeListener.onDataStateChange(
                    DataState.loading(isLoading = isLoading, cacheData = null)
                )
            }
        }

    }

    private fun onPasswordResetLinkSent() {
        GlobalScope.launch(Main) {
            parent_view.removeView(webView)
            webView.destroy()

            val animation = TranslateAnimation(
                password_reset_done_container.width.toFloat(),
                0f,
                0f,
                0f
            )
            animation.duration = 500
            password_reset_done_container.startAnimation(animation)
            password_reset_done_container.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webview)

        Log.d(TAG, "ForgotPasswordFragement: ${viewModel.hashCode()}")

        loadPasswordResetWebView()

        return_to_launcher_fragment.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadPasswordResetWebView() {
        stateChangeListener.onDataStateChange(
            DataState.loading(isLoading = true, cacheData = null)
        )

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                stateChangeListener.onDataStateChange(
                    DataState.loading(isLoading = false, cacheData = null)
                )
            }
        }

        webView.loadUrl(Constants.PASSWORD_RESET_URL)
        webview.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebAppInterface(webInteractionCallback), "AndroidTextListener")
    }

    class WebAppInterface constructor(
        private val callback: OnWebInteractionCallback
    ) {

        private val TAG: String = "AppDebug"

        @JavascriptInterface
        fun onSuccess(email: String) {
            callback.onSuccess(email)
        }

        @JavascriptInterface
        fun onError(errorMessage: String) {
            callback.onError(errorMessage)
        }

        @JavascriptInterface
        fun onLoading(isLoading: Boolean) {
            callback.onLoading(isLoading)
        }

        interface OnWebInteractionCallback {
            fun onSuccess(email: String)
            fun onError(errorMessage: String)
            fun onLoading(isLoading: Boolean)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement DataStateChangeListener")
        }
    }

}