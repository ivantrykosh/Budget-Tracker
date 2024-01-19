package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state

import com.ivantrykosh.app.budgettracker.client.domain.model.SubTransaction

/**
 * Transactions state
 */
data class GetTransactionsState(
    val isLoading: Boolean = false,
    val transactions: List<SubTransaction> = emptyList(),
    val error: Int? = null,
)