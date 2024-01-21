package com.ivantrykosh.app.budgettracker.client.domain.model

/**
 * Account with total sum
 */
data class SubAccount(
    val accountId: Long,
    val name: String,
    val total: Double
)
