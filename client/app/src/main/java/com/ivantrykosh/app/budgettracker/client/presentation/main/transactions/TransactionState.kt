package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto

data class TransactionState(
    val isLoading: Boolean = false,
    val transactions: TransactionDto? = null,
    val error: String = "",
)