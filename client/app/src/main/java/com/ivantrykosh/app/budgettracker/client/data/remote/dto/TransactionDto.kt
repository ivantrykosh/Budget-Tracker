package com.ivantrykosh.app.budgettracker.client.data.remote.dto

import java.util.Date

/**
 * Data class for transaction
 */
data class TransactionDto(
    val transactionId: Long,
    val accountId: Long,
    val category: String,
    val value: Double,
    val date: Date,
    val toFromWhom: String,
    val note: String
)
