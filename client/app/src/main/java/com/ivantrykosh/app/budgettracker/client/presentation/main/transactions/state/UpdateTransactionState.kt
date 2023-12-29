package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state

/**
 * Update transaction state
 */
data class UpdateTransactionState(
    val isLoading: Boolean = false,
    val error: String = "",
)
