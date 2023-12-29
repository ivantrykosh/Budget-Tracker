package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state

/**
 * Delete account state
 */
data class DeleteAccountState (
    val isLoading: Boolean = false,
    val error: String = "",
)