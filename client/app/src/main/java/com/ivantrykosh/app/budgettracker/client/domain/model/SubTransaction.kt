package com.ivantrykosh.app.budgettracker.client.domain.model

import java.util.Date

/**
 * Sub Transaction with needed data
 */
data class SubTransaction(
    val transactionId: Long,
    val name: String,
    val category: String,
    val value: Double,
    val date: Date,
)
