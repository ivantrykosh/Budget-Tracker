package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import com.ivantrykosh.app.budgettracker.client.domain.model.Account

data class AccountsState(
    val isLoading: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val error: String = "",
)