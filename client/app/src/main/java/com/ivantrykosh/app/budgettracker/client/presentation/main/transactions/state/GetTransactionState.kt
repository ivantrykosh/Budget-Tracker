package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state

import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction

/**
 * Get transaction state
 */
data class GetTransactionState (
    val isLoading: Boolean = false,
    val transaction: Transaction? = null,
    val error: Int? = null,
)
