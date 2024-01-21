package com.ivantrykosh.app.budgettracker.client.domain.model

/**
 * Account with additional data, such as total, incomes and expenses sum
 */
data class FullAccount(
    val accountId: Long,
    val name: String,
    val incomesSum: Double,
    val expensesSum: Double,
    val total: Double,
)