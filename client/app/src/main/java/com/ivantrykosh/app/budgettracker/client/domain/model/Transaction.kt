package com.ivantrykosh.app.budgettracker.client.domain.model

import java.util.Date

/**
 * Transaction model
 */
data class Transaction(
    val transactionId: Long?,
    val accountName: String,
    val category: String,
    val value: Double,
    val date: Date
)