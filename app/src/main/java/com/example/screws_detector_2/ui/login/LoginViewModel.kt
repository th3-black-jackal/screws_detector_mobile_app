package com.example.screws_detector_2.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screws_detector_2.data.LoginRepository

import com.example.screws_detector_2.R
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) = viewModelScope.launch {
        loginRepository.login(username, password)          // Result<User>
            .fold(
                onSuccess = { user ->
                    _loginResult.value = LoginResult(
                        success = LoggedInUserView(displayName = user.displayName)
                    )
                },
                onFailure = {
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                }
            )
    }


    fun loginDataChanged(username: String, password: String) {
        _loginForm.value = when {
            !isUserNameValid(username) -> LoginFormState(usernameError = R.string.invalid_username)
            !isPasswordValid(password) -> LoginFormState(passwordError = R.string.invalid_password)
            else                       -> LoginFormState(isDataValid = true)
        }
    }

    /** Username must be 3-20 chars, start with a letter, allowed chars [a-zA-Z0-9_.-] */
    private fun isUserNameValid(username: String): Boolean {
        val pattern = Regex("^[A-Za-z][A-Za-z0-9_.-]{2,19}$")
        return pattern.matches(username)
    }


    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}