package com.ivantrykosh.app.budgettracker.client.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.confirm_email.SendConfirmationEmailUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.login.LoginUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.sign_up.SignUpUseCase
import com.ivantrykosh.app.budgettracker.client.domain.use_case.user.reset_password.ResetPasswordUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.auth.confirmation_email.ConfirmationEmailState
import com.ivantrykosh.app.budgettracker.client.presentation.auth.login.LoginState
import com.ivantrykosh.app.budgettracker.client.presentation.auth.reset_password.ResetPasswordState
import com.ivantrykosh.app.budgettracker.client.presentation.auth.signup.SignUpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject

/**
 * Auth view model
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val confirmationEmailUseCase: SendConfirmationEmailUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    companion object {
        private const val EMAIL_REGEX =
            "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
        private const val PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,32}$"
    }

    private val _loginState = MutableLiveData(LoginState())
    val loginState: LiveData<LoginState> = _loginState

    private val _signUpState = MutableLiveData(SignUpState())
    val signUpState: LiveData<SignUpState> = _signUpState

    private val _confirmationEmailState = MutableLiveData(ConfirmationEmailState())
    val confirmationEmailState: LiveData<ConfirmationEmailState> = _confirmationEmailState

    private val _resetPasswordState = MutableLiveData(ResetPasswordState())
    val resetPasswordState: LiveData<ResetPasswordState> = _resetPasswordState

    private var authDto: AuthDto? = null

    /**
     * Set AuthDto value
     *
     * @param authDto AuthDto value
     */
    fun setAuthDto(authDto: AuthDto?) {
        this.authDto = authDto?.copy()
    }

    /**
     * Get AuthDto
     */
    fun getAuthDto(): AuthDto? {
        return authDto?.copy()
    }

    /**
     * Check email
     */
    fun checkEmail(email: String): Boolean {
        return email.length <= 320 && email.matches(Regex(EMAIL_REGEX))
    }

    /**
     * Check password
     */
    fun checkPassword(password: String): Boolean {
        return password.matches(Regex(PASSWORD_REGEX))
    }

    /**
     * Make login request
     */
    fun login() {
        _loginState.value = LoginState(isLoading = true)
        if (authDto == null) {
            _loginState.value = LoginState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(authDto!!.email)) {
            _loginState.value = LoginState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkPassword(authDto!!.passwordHash)) {
            _loginState.value = LoginState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            val passwordHash = hashPassword(authDto!!.passwordHash, authDto!!.email)
            val newAuthDto = AuthDto(authDto!!.email, passwordHash)

            loginUseCase(newAuthDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _loginState.value = LoginState(token = result.data ?: "")
                        AppPreferences.jwtToken = _loginState.value!!.token
                    }
                    is Resource.Error -> {
                        _loginState.value = LoginState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                    }
                    is Resource.Loading -> {
                        _loginState.value = LoginState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Make sig up request
     */
    fun signUp() {
        _signUpState.value = SignUpState(isLoading = true)
        if (authDto == null) {
            _signUpState.value = SignUpState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(authDto!!.email)) {
            _signUpState.value = SignUpState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkPassword(authDto!!.passwordHash)) {
            _signUpState.value = SignUpState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            val passwordHash = hashPassword(authDto!!.passwordHash, authDto!!.email)
            val newAuthDto = AuthDto(authDto!!.email, passwordHash)

            signUpUseCase(newAuthDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _signUpState.value = SignUpState()
                    }
                    is Resource.Error -> {
                        _signUpState.value = SignUpState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                    }
                    is Resource.Loading -> {
                        _signUpState.value = SignUpState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Send confirmation email request
     */
    fun sendConfirmationEmail() {
        _confirmationEmailState.value = ConfirmationEmailState(isLoading = true)
        if (authDto == null) {
            _confirmationEmailState.value = ConfirmationEmailState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(authDto!!.email)) {
            _confirmationEmailState.value = ConfirmationEmailState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkPassword(authDto!!.passwordHash)) {
            _confirmationEmailState.value = ConfirmationEmailState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            val passwordHash = hashPassword(authDto!!.passwordHash, authDto!!.email)
            val newAuthDto = AuthDto(authDto!!.email, passwordHash)

            confirmationEmailUseCase(newAuthDto).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _confirmationEmailState.value = ConfirmationEmailState()
                    }
                    is Resource.Error -> {
                        _confirmationEmailState.value = ConfirmationEmailState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                    }
                    is Resource.Loading -> {
                        _confirmationEmailState.value = ConfirmationEmailState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Reset password request
     */
    fun resetPassword() {
        _resetPasswordState.value = ResetPasswordState(isLoading = true)
        if (authDto == null) {
            _resetPasswordState.value = ResetPasswordState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else if (!checkEmail(authDto!!.email)) {
            _resetPasswordState.value = ResetPasswordState(error = Constants.ErrorStatusCodes.INVALID_REQUEST)
        } else {
            resetPasswordUseCase(authDto!!).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _resetPasswordState.value = ResetPasswordState()
                    }
                    is Resource.Error -> {
                        _resetPasswordState.value = ResetPasswordState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                    }
                    is Resource.Loading -> {
                        _resetPasswordState.value = ResetPasswordState(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Hash password with salt using SHA-256
     *
     * @param password user password
     * @param salt user salt
     */
    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val passwordWithSalt = (password + salt).toByteArray()
        return Base64.getEncoder().encodeToString(md.digest(passwordWithSalt))
    }
}