package com.ivantrykosh.app.budgettracker.client.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Transaction entity
 */
@Entity(tableName = "transactions", foreignKeys = [
    ForeignKey(entity = AccountEntity::class, parentColumns = ["accountId"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)
])
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val transactionId: Long = 0,
    val accountId: Long,
    val category: String,
    val value: Double,
    val date: Date,
    val toFromWhom: String?,
    val note: String?
)