package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

data class DeleteTransactionState(
    val isLoading: Boolean = false,
    val error: String = "",
)
