package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state

import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction

/**
 * Transactions state
 */
data class GetTransactionsState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val error: Int? = null,
)