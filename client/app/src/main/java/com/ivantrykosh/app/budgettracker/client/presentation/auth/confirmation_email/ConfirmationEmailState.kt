package com.ivantrykosh.app.budgettracker.client.presentation.auth.confirmation_email

/**
 * Confirmation email state
 */
data class ConfirmationEmailState(
    val isLoading: Boolean = false,
    val error: String = "",
)