package com.ivantrykosh.app.budgettracker.client.presentation.auth.login

/**
 * Login state class
 */
data class LoginState(
    val isLoading: Boolean = false,
    val token: String = "",
    val error: Int? = null,
)