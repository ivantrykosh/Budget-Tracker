package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import java.util.Date

interface TransactionRepository {

    suspend fun createTransaction(token: String, transactionDto: TransactionDto)

    suspend fun getTransaction(token: String, id: String): TransactionDto

    suspend fun getTransactionByAccount(token: String, accountId: String): List<TransactionDto>

    suspend fun getTransactionByAllAccounts(token: String, accountIds: List<Long>): List<TransactionDto>

    suspend fun getTransactionByAllAccountAndDateBetween(token: String, accountIds: List<Long>, startDate: Date, endDate: Date): List<TransactionDto>

    suspend fun updateTransaction(token: String, transactionDto: TransactionDto)

    suspend fun deleteTransaction(token: String, id: String)
}