package com.ivantrykosh.app.budgettracker.client.domain.model

import java.util.Date

/**
 * User model
 */
data class User(
    val userId: Long,
    val email: String,
    val registrationDate: Date,
)
