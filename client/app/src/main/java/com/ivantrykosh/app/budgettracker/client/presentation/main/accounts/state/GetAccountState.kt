package com.ivantrykosh.app.budgettracker.client.presentation.main.accounts.state

import com.ivantrykosh.app.budgettracker.client.domain.model.FullAccount

/**
 * Get account state
 */
data class GetAccountState (
    val isLoading: Boolean = false,
    val account: FullAccount? = null,
    val error: Int? = null,
)
