package com.ivantrykosh.app.budgettracker.client.presentation.auth.signup

/**
 * Sign up state
 */
data class SignUpState(
    val isLoading: Boolean = false,
    val error: Int? = null,
)