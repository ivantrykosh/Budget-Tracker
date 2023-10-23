package com.ivantrykosh.app.budgettracker.client.data

import com.ivantrykosh.app.budgettracker.client.model.BalanceOverview
import com.ivantrykosh.app.budgettracker.client.model.Operation
import java.time.LocalDate
import java.time.LocalDateTime

class Datasource {
    fun loadOperations(): List<Operation> {
        return listOf(
            Operation("Food", -100.00, "Pizza, cake", LocalDateTime.of(2023, 10, 16, 20, 30)),
            Operation("Financial income", 100.00, "From mother", LocalDateTime.of(2023, 10, 16, 18, 40)),
            Operation("Entertainment", -20.00, "Book \"Clean code\"", LocalDateTime.of(2023, 10, 10, 16, 36)),
            Operation("One-time work", 50.00, "Fixed computer", LocalDateTime.of(2023, 10, 10, 14, 22)),
        )
    }

    fun loadBalanceOverview(): BalanceOverview {
        return BalanceOverview(
            200.00,
            -120.00,
            80.00,
            LocalDate.of(2023, 10, 1)
        )
    }
}