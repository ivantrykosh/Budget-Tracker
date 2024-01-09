package com.ivantrykosh.app.budgettracker.client.presentation.main.my_profile.state

import com.ivantrykosh.app.budgettracker.client.domain.model.User

/**
 * User state
 */
data class GetUserState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: Int? = null,
)