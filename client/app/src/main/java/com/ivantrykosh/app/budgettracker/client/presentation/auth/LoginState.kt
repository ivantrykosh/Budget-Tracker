package com.ivantrykosh.app.budgettracker.client.presentation.auth

data class LoginState(
    val isLoading: Boolean = false,
    val token: String = "",
    val error: String = "",
)