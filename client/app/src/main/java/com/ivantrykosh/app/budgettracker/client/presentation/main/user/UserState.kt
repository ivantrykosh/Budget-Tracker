package com.ivantrykosh.app.budgettracker.client.presentation.main.user

import com.ivantrykosh.app.budgettracker.client.domain.model.User

data class UserState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String = "",
)