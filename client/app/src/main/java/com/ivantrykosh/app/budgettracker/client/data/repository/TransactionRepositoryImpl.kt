package com.ivantrykosh.app.budgettracker.client.data.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.TransactionApi
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import java.util.Date
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: TransactionApi
) : TransactionRepository {
    override suspend fun createTransaction(token: String, transactionDto: TransactionDto) {
        return api.createTransaction(token, transactionDto)
    }

    override suspend fun getTransaction(token: String, id: String): TransactionDto {
        return api.getTransaction(token, id)
    }

    override suspend fun getTransactionByAccount(
        token: String,
        accountId: String
    ): List<TransactionDto> {
        return api.getTransactionByAccount(token, accountId)
    }

    override suspend fun getTransactionByAllAccounts(
        token: String,
        accountIds: List<Long>
    ): List<TransactionDto> {
        return api.getTransactionByAllAccounts(token, accountIds)
    }

    override suspend fun getTransactionByAllAccountAndDateBetween(
        token: String,
        accountIds: List<Long>,
        startDate: Date,
        endDate: Date
    ): List<TransactionDto> {
        return api.getTransactionByAllAccountAndDateBetween(token, accountIds, startDate, endDate)
    }

    override suspend fun updateTransaction(token: String, transactionDto: TransactionDto) {
        return api.updateTransaction(token, transactionDto)
    }

    override suspend fun deleteTransaction(token: String, id: String) {
        return api.deleteTransaction(token, id)
    }

}