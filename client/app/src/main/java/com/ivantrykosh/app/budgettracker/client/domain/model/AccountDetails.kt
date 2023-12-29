package com.ivantrykosh.app.budgettracker.client.domain.model

/**
 * Account details model
 */
data class AccountDetails(
    val accountId: Long,
    val userId: Long,
    val name: String,
    val incomesSum: Double,
    val expensesSum: Double,
    val total: Double,
    val email2: String?,
    val email3: String?,
    val email4: String?
)