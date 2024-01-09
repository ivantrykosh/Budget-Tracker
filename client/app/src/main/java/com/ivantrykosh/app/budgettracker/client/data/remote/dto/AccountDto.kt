package com.ivantrykosh.app.budgettracker.client.data.remote.dto

import com.ivantrykosh.app.budgettracker.client.domain.model.Account

/**
 * Data class for account
 */
data class AccountDto(
    val accountId: Long,
    val userId: Long,
    val name: String,
    val incomesSum: Double,
    val expensesSum: Double
)

/**
 * Convert from AccountDto to Account
 */
fun AccountDto.toAccount(): Account {
    return Account(
        accountId = accountId,
        userId = userId,
        name = name,
        total = incomesSum + expensesSum
    )
}