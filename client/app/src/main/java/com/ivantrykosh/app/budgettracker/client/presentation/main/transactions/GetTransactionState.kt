package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

import com.ivantrykosh.app.budgettracker.client.domain.model.TransactionDetails

data class GetTransactionState (
    val isLoading: Boolean = false,
    val transaction: TransactionDetails? = null,
    val error: String = "",
)
