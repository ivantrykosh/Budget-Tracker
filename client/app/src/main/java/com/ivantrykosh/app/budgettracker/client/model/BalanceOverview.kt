package com.ivantrykosh.app.budgettracker.client.model

import java.util.Date

data class BalanceOverview(
    val incomes: Double,
    val expenses: Double,
    val total: Double,
    val month: Date,
)