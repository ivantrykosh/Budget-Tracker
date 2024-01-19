package com.ivantrykosh.app.budgettracker.client.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Sub Transaction
 */
data class SubTransaction(
    val transactionId: Long,
    val name: String,
    val category: String,
    val value: Double,
    val date: Date,
)
