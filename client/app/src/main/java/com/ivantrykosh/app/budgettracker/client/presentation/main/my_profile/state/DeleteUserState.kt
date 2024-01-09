package com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile.state

/**
 * Delete user state
 */
data class DeleteUserState(
    val isLoading: Boolean = false,
    val error: Int? = null,
)
