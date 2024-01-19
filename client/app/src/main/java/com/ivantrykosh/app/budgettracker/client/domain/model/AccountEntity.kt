package com.ivantrykosh.app.budgettracker.client.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Account entity
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val accountId: Long = 0,
    val name: String,
)
