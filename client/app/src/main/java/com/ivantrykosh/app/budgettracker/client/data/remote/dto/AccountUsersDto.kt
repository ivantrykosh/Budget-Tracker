package com.ivantrykosh.app.budgettracker.client.data.remote.dto

/**
 * Data class for account users
 */
data class AccountUsersDto(
    val accountUsersId: Long,
    val accountId: Long,
    val email2: String,
    val email3: String,
    val email4: String
)
