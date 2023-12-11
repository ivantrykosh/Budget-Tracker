package com.ivantrykosh.app.budgettracker.client.presentation.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.confirm_email.SendConfirmationEmailUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.login.LoginUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.sign_up.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val confirmationEmailUseCase: SendConfirmationEmailUseCase,
) : ViewModel() {

    companion object {
        private const val EMAIL_REGEX =
            "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
        private const val PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,32}$"
    }

    private val _loginState = mutableStateOf<LoginState>(LoginState())
    val loginState: State<LoginState> = _loginState
    private val _isLoginLoading = MutableLiveData<Boolean>(false)
    val isLoginLoading: LiveData<Boolean> = _isLoginLoading

    private val _signUpState = mutableStateOf<SignUpState>(SignUpState())
    val signUpState: State<SignUpState> = _signUpState
    private val _isSingUpLoading = MutableLiveData<Boolean>(false)
    val isSingUpLoading: LiveData<Boolean> = _isSingUpLoading

    private val _confirmationEmailState = mutableStateOf<ConfirmationEmailState>(ConfirmationEmailState())
    val confirmationEmailState = _confirmationEmailState
    private val _isConfirmationEmailLoading = MutableLiveData<Boolean>(false)
    val isConfirmationEmailLoading: LiveData<Boolean> = _isConfirmationEmailLoading

    private var authDto: AuthDto? = null


    fun setAuthDto(authDto: AuthDto?) {
        this.authDto = authDto?.copy()
    }

    fun getAuthDto(): AuthDto? {
        return authDto?.copy()
    }

    fun checkEmail(email: String): Boolean {
        return email.length <= 320 && email.matches(Regex(EMAIL_REGEX))
    }

    fun checkPassword(password: String): Boolean {
        return password.matches(Regex(PASSWORD_REGEX))
    }

    fun login() {
        _isLoginLoading.value = true
        if (authDto == null) {
            _loginState.value = LoginState(error = "No auth dto")
        } else if (!checkEmail(authDto!!.email)) {
            _loginState.value = LoginState(error = "Invalid email")
        } else if (!checkPassword(authDto!!.passwordHash)) {
            _loginState.value = LoginState(error = "Invalid password")
        } else {

            val passwordHash = hashPassword(authDto!!.passwordHash, authDto!!.email)
            val newAuthDto = AuthDto(
                authDto!!.email,
                passwordHash
            )

            loginUseCase(newAuthDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _loginState.value = LoginState(token = result.data ?: "")
                        _isLoginLoading.value = false
                        AppPreferences.jwtToken = _loginState.value.token
                    }

                    is Resource.Error -> {
                        _loginState.value = LoginState(error = result.message ?: "An unexpected error occurred")
                        _isLoginLoading.value = false
                    }

                    is Resource.Loading -> {
                        _loginState.value = LoginState(isLoading = true)
                        _isLoginLoading.value = true
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun signUp() {
        _isSingUpLoading.value = true
        if (authDto == null) {
            _signUpState.value = SignUpState(error = "No auth dto")
        } else if (!checkEmail(authDto!!.email)) {
            _signUpState.value = SignUpState(error = "Invalid email")
        } else if (!checkPassword(authDto!!.passwordHash)) {
            _signUpState.value = SignUpState(error = "Invalid password")
        } else {

            val passwordHash = hashPassword(authDto!!.passwordHash, authDto!!.email)
            val newAuthDto = AuthDto(
                authDto!!.email,
                passwordHash
            )

            signUpUseCase(newAuthDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _signUpState.value = SignUpState()
                        _isSingUpLoading.value = false
                    }

                    is Resource.Error -> {
                        _signUpState.value = SignUpState(error = result.message ?: "An unexpected error occurred")
                        _isSingUpLoading.value = false
                    }

                    is Resource.Loading -> {
                        _signUpState.value = SignUpState(isLoading = true)
                        _isSingUpLoading.value = true
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun sendConfirmationEmail() {
        _isConfirmationEmailLoading.value = true
        if (authDto == null) {
            _confirmationEmailState.value = ConfirmationEmailState(error = "No auth dto")
        } else if (!checkEmail(authDto!!.email)) {
            _confirmationEmailState.value = ConfirmationEmailState(error = "Invalid email")
        } else if (!checkPassword(authDto!!.passwordHash)) {
            _confirmationEmailState.value = ConfirmationEmailState(error = "Invalid password")
        } else {

            val passwordHash = hashPassword(authDto!!.passwordHash, authDto!!.email)
            val newAuthDto = AuthDto(
                authDto!!.email,
                passwordHash
            )

            confirmationEmailUseCase(newAuthDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _confirmationEmailState.value = ConfirmationEmailState()
                        _isConfirmationEmailLoading.value = false
                    }

                    is Resource.Error -> {
                        _confirmationEmailState.value = ConfirmationEmailState(error = result.message ?: "An unexpected error occurred")
                        _isConfirmationEmailLoading.value = false
                    }

                    is Resource.Loading -> {
                        _confirmationEmailState.value = ConfirmationEmailState(isLoading = true)
                        _isConfirmationEmailLoading.value = true
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

//   fun resetPassword() {
//      if (authDto == null) {
//         _confirmationEmailState.value = ConfirmationEmailState(error = "No auth dto")
//      } else if (checkEmail(authDto!!.email)) {
//         _confirmationEmailState.value = ConfirmationEmailState(error = "Invalid email")
//      } else {
//
//         val passwordHash = hashPassword(authDto!!.password, authDto!!.email.toByteArray())
//         val newAuthDto = AuthDto(
//            authDto!!.email,
//            passwordHash
//         )
//
//         confirmationEmailUseCase(newAuthDto).onEach { result ->
//            when (result) {
//               is Resource.Success -> {
//                  _confirmationEmailState.value = ConfirmationEmailState(token = result.data ?: "")
//               }
//
//               is Resource.Error -> {
//                  _confirmationEmailState.value = ConfirmationEmailState(error = result.message ?: "An unexpected error occurred")
//               }
//
//               is Resource.Loading -> {
//                  _confirmationEmailState.value = ConfirmationEmailState(isLoading = true)
//               }
//            }
//         }.launchIn(viewModelScope)
//      }
//   }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val passwordWithSalt = (password + salt).toByteArray()
        return Base64.getEncoder().encodeToString(md.digest(passwordWithSalt))
    }
}