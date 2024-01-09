package com.ivantrykosh.app.budgettracker.client.presentation.splash_screen.state

/**
 * Refresh token state
 */
data class RefreshTokenState(
    val isLoading: Boolean = false,
    val token: String = "",
    val error: Int? = null,
)
