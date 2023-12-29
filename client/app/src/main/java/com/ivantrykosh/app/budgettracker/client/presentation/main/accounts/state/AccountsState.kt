package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state

import com.ivantrykosh.app.budgettracker.client.domain.model.Account

/**
 * Accounts state
 */
data class AccountsState(
    val isLoading: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val error: String = "",
)