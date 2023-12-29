package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state

import com.ivantrykosh.app.budgettracker.client.domain.model.AccountDetails

/**
 * Get account state
 */
data class GetAccountState (
    val isLoading: Boolean = false,
    val account: AccountDetails? = null,
    val error: String = "",
)
