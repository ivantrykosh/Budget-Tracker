package com.ivantrykosh.app.budgettracker.client.presentation.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.AuthDto
import com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.login.LoginUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.splash_screen.RefreshTokenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _state = mutableStateOf<LoginState>(LoginState())
    val state: State<LoginState> = _state

    fun login(email: String, password: String) {
        val passwordHash = hashPassword(password, email.toByteArray())
        val authDto = AuthDto(
            email,
            passwordHash
        )

        loginUseCase(authDto).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = LoginState(token = result.data ?: "")
                }
                is Resource.Error -> {
                    _state.value = LoginState(error = result.message ?: "An unexpected error occurred")
                }
                is Resource.Loading -> {
                    _state.value = LoginState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val passwordWithSalt = password.toByteArray() + salt
        return Base64.getEncoder().encodeToString(md.digest(passwordWithSalt))
    }
}