package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction

data class TransactionsState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val error: String = "",
)