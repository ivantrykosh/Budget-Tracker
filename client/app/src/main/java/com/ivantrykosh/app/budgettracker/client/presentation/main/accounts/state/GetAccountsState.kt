package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state

import com.ivantrykosh.app.budgettracker.client.domain.model.SubAccount

/**
 * Accounts state
 */
data class GetAccountsState(
    val isLoading: Boolean = false,
    val accounts: List<SubAccount> = emptyList(),
    val error: Int? = null,
)