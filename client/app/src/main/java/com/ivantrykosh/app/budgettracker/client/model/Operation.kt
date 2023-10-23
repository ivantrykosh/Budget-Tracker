package com.ivantrykosh.app.budgettracker.client.model

import java.time.LocalDateTime

data class Operation(
    val category: String,
    val value: Double,
    val note: String,
    val date: LocalDateTime,
)
