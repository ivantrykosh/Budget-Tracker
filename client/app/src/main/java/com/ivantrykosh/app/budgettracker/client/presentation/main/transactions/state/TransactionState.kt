package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions.state

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto

/**
 * Transaction state
 */
data class TransactionState(
    val isLoading: Boolean = false,
    val transaction: TransactionDto? = null,
    val error: String = "",
)