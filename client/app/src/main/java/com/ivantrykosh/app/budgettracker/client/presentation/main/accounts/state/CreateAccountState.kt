package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state

/**
 * Create account state
 */
data class CreateAccountState (
    val isLoading: Boolean = false,
    val error: Int? = null,
)
