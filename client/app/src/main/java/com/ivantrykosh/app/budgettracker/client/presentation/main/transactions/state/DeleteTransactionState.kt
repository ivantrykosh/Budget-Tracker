package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state

/**
 * Delete transaction state
 */
data class DeleteTransactionState(
    val isLoading: Boolean = false,
    val error: Int? = null,
)
