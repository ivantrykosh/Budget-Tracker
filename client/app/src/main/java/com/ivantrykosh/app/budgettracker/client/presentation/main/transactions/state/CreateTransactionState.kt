package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto

/**
 * Transaction state
 */
data class CreateTransactionState(
    val isLoading: Boolean = false,
    val transaction: TransactionDto? = null,
    val error: Int? = null,
)