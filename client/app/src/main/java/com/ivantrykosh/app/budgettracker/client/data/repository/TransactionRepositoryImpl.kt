package com.ivantrykosh.app.budgettracker.client.data.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.TransactionApi
import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Transaction repository implementation
 */
class TransactionRepositoryImpl @Inject constructor(
    private val api: TransactionApi
) : TransactionRepository {

    /**
     * Create transaction with token and transactionDto
     *
     * @param token user's JWT
     * @param transactionDto Transaction data
     */
    override suspend fun createTransaction(token: String, transactionDto: TransactionDto) {
        return api.createTransaction(token, transactionDto)
    }

    /**
     * Get transaction with token and id
     *
     * @param token user's JWT
     * @param id Transaction ID to get
     */
    override suspend fun getTransaction(token: String, id: String): TransactionDto {
        return api.getTransaction(token, id)
    }

    /**
     * Get transactions with token and accountId
     *
     * @param token user's JWT
     * @param accountId Account ID by which transactions are got
     */
    override suspend fun getTransactionByAccount(
        token: String,
        accountId: String
    ): List<TransactionDto> {
        return api.getTransactionByAccount(token, accountId)
    }

    /**
     * Get transactions with token and accountIds
     *
     * @param token user's JWT
     * @param accountIds Account IDs by which transactions are got
     */
    override suspend fun getTransactionByAllAccounts(
        token: String,
        accountIds: List<Long>
    ): List<TransactionDto> {
        return api.getTransactionByAllAccounts(token, accountIds)
    }

    /**
     * Get transactions with token, accountIds, startDate and endDate
     *
     * @param token user's JWT
     * @param accountIds Account IDs by which transactions are got
     * @param startDate start date to get transactions
     * @param endDate end date to get transactions
     */
    override suspend fun getTransactionByAllAccountAndDateBetween(
        token: String,
        accountIds: List<Long>,
        startDate: String,
        endDate: String
    ): List<TransactionDto> {
        return api.getTransactionByAllAccountAndDateBetween(token, accountIds, startDate, endDate)
    }

    /**
     * Update transaction with token and transactionDto
     *
     * @param token user's JWT
     * @param transactionDto Transaction data
     */
    override suspend fun updateTransaction(token: String, transactionDto: TransactionDto) {
        return api.updateTransaction(token, transactionDto)
    }

    /**
     * Delete transaction with token and id
     *
     * @param token user's JWT
     * @param id Transaction ID to delete
     */
    override suspend fun deleteTransaction(token: String, id: String) {
        return api.deleteTransaction(token, id)
    }
}