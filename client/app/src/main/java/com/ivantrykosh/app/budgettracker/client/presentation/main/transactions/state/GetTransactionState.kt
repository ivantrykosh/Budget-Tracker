package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state

import com.ivantrykosh.app.budgettracker.client.domain.model.TransactionDetails

/**
 * Get transaction state
 */
data class GetTransactionState (
    val isLoading: Boolean = false,
    val transaction: TransactionDetails? = null,
    val error: String = "",
)
