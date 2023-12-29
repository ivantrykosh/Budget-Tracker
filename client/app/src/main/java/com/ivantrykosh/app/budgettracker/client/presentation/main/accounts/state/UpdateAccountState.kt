package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state

/**
 * Update account state
 */
data class UpdateAccountState(
    val isLoading: Boolean = false,
    val error: String = "",
)
