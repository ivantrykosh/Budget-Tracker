package com.ivantrykosh.app.budgettracker.client.domain.model

/**
 * Account model
 */
data class Account(
    val accountId: Long,
    val userId: Long,
    val name: String,
    val total: Double
)
