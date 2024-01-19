package com.ivantrykosh.app.budgettracker.client.domain.model

data class FullAccount(
    val accountId: Long,
    val name: String,
    val incomesSum: Double,
    val expensesSum: Double,
    val total: Double,
)