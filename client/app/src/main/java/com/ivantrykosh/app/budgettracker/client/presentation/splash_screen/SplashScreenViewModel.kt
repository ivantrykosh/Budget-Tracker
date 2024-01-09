package com.ivantrykosh.app.budgettracker.client.presentation.splash_screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivantrykosh.app.budgettracker.client.common.AppPreferences
import com.ivantrykosh.app.budgettracker.client.common.Constants
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

    private val _refreshTokenState = MutableLiveData(RefreshTokenState())
    val refreshTokenState: LiveData<RefreshTokenState>
        get() = _refreshTokenState

    init {
        refreshToken()
    }

    /**
     * Refresh token
     */
    fun refreshToken() {
        AppPreferences.jwtToken?.let { token ->
            refreshToken(token)
        } ?: run {
            _refreshTokenState.value = RefreshTokenState(error = Constants.ErrorStatusCodes.TOKEN_NOT_FOUND)
        }
    }

    /**
     * Refresh token with JWT
     *
     * @param token user's JWT
     */
    private fun refreshToken(token: String) {
        _refreshTokenState.value = RefreshTokenState(isLoading = true)
        refreshTokenUseCase(token).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _refreshTokenState.value = RefreshTokenState(token = result.data ?: "")
                    AppPreferences.jwtToken = _refreshTokenState.value!!.token
                }
                is Resource.Error -> {
                    _refreshTokenState.value = RefreshTokenState(error = result.statusCode ?: Constants.ErrorStatusCodes.CLIENT_ERROR)
                }
                is Resource.Loading -> {
                    _refreshTokenState.value = RefreshTokenState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}