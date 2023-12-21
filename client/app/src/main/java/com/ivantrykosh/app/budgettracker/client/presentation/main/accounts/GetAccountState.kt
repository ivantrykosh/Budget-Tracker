package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts

import com.ivantrykosh.app.budgettracker.client.domain.model.AccountDetails

data class GetAccountState (
    val isLoading: Boolean = false,
    val account: AccountDetails? = null,
    val error: String = "",
)
