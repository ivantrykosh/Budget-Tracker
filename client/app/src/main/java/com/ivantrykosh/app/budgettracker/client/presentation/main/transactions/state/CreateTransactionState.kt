package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state

/**
 * Transaction state
 */
data class CreateTransactionState(
    val isLoading: Boolean = false,
    val error: Int? = null,
)