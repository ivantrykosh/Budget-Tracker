package com.ivantrykosh.app.budgettracker.client.presentation.splash_screen

data class RefreshTokenState(
    val isLoading: Boolean = false,
    val token: String = "",
    val error: String = "",
)
