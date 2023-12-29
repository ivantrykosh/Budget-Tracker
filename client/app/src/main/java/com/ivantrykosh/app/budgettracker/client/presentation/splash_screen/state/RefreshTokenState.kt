package com.ivantrykosh.app.budgettracker.client.presentation.splash_screen.state

/**
 * Refresh JWT state
 */
data class RefreshTokenState(
    val isLoading: Boolean = false,
    val token: String = "",
    val error: String = "",
)
