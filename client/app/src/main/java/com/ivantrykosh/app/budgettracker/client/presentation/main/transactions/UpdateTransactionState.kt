package com.ivantrykosh.app.budgettracker.client.presentation.main.transactions

data class UpdateTransactionState(
    val isLoading: Boolean = false,
    val error: String = "",
)
