package com.example.screws_detector_2.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.example.screws_detector_2.MainActivity
import com.example.screws_detector_2.databinding.ActivityLoginBinding

import com.example.screws_detector_2.R

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val ip = binding.ipAddress?.text.toString().trim()
        val port = binding.port?.text.toString().trim()


        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(this@LoginActivity))
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                setResult(Activity.RESULT_OK)
                startActivity(Intent(this, MainActivity::class.java))
                //Complete and destroy login activity once successful
                finish()
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            password.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    // grab what the user typed
                    val ipValue   = binding.ipAddress?.text.toString().trim()
                    val portValue = binding.port?.text.toString().trim()

                    // log it (just for debug)
                    Log.d("HOST_IP_LOGIN",  ipValue)
                    Log.d("PORT_IP_LOGIN",  portValue)

                    // save it
                    getSharedPreferences("network_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("ip",   ipValue.ifEmpty { "10.0.2.2" })
                        .putString("port", portValue.ifEmpty { "8000" })
                        .apply()

                    // now trigger the login
                    loginViewModel.login(
                        username.text.toString(),
                        password.text.toString()
                    )
                    true          // we handled the action
                } else {
                    false         // let the IME handle other actions
                }
            }

            login.setOnClickListener {
                Log.d("HOST_IP_LOGIN", ip)
                Log.d("PORT IP_LOGIN", port)
                getSharedPreferences("network_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("ip", ip)   // sensible fall-backs
                    .putString("port", port)
                    .apply()
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}