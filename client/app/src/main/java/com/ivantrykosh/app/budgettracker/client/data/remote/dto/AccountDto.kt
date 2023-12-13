package com.ivantrykosh.app.budgettracker.client.data.remote.dto

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