package com.ivantrykosh.app.budgettracker.client.presentation.splash_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Resource
import com.ivantrykosh.app.budgettracker.client.domain.use_case.auth.refresh_token.RefreshTokenUseCase
import com.ivantrykosh.app.budgettracker.client.presentation.splash_screen.state.RefreshTokenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Splash screen view model
 */
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val refreshTokenUseCase: RefreshTokenUseCase,
) : ViewModel() {

    private val _state = mutableStateOf(RefreshTokenState())
    val state: State<RefreshTokenState> = _state

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        refreshToken()
    }

    fun refreshToken() {
        AppPreferences.jwtToken?.let { token ->
            refreshToken(token)
        } ?: run {
            _state.value = RefreshTokenState(error = "Token is not found")
            _isLoading.value = false
        }
    }

    private fun refreshToken(token: String) {
        refreshTokenUseCase(token).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = RefreshTokenState(token = result.data ?: "")
                    _isLoading.value = false
                    AppPreferences.jwtToken = _state.value.token
                }
                is Resource.Error -> {
                    _state.value = RefreshTokenState(error = result.message ?: "An unexpected error occurred")
                    _isLoading.value = false
                }
                is Resource.Loading -> {
                    _state.value = RefreshTokenState(isLoading = true)
                    _isLoading.value = true
                }
            }
        }.launchIn(viewModelScope)
    }
}