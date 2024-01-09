package com.ivantrykosh.app.budgettracker.client.presentation.auth.reset_password

/**
 * Reset password state
 */
data class ResetPasswordState(
    val isLoading: Boolean = false,
    val error: Int? = null,
)