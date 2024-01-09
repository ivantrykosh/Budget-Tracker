package com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile.state

/**
 * Change password state
 */
data class ChangePasswordState(
    val isLoading: Boolean = false,
    val error: Int? = null,
)
