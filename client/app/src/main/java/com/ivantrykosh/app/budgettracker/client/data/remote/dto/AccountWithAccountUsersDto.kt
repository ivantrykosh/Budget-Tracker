package com.ivantrykosh.app.budgettracker.client.data.remote.dto

/**
 * Data class for getting account with account users
 */
data class AccountWithAccountUsersDto(
    val accountDto: AccountDto,
    val accountUsersDto: AccountUsersDto
)