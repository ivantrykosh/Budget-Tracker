package com.ivantrykosh.app.budgettracker.client.data.remote.dto

import com.ivantrykosh.app.budgettracker.client.domain.model.AccountDetails

/**
 * Data class for getting account with account users
 */
data class AccountWithAccountUsersDto(
    val accountDto: AccountDto,
    val accountUsersDto: AccountUsersDto
)

/**
 * Convert from AccountWithAccountUsersDto to AccountDetails
 */
fun AccountWithAccountUsersDto.toAccountDetails(): AccountDetails {
    return AccountDetails(
        accountId = accountDto.accountId,
        userId = accountDto.userId,
        name = accountDto.name,
        incomesSum = accountDto.incomesSum,
        expensesSum = accountDto.expensesSum,
        total = accountDto.incomesSum + accountDto.expensesSum,
        email2 = accountUsersDto.email2,
        email3 = accountUsersDto.email3,
        email4 = accountUsersDto.email4
    )
}