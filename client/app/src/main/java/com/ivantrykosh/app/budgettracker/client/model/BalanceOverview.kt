package com.ivantrykosh.app.budgettracker.client.model

import java.time.LocalDate

data class BalanceOverview(
    val incomes: Double,
    val expenses: Double,
    val total: Double,
    val month: LocalDate,
)