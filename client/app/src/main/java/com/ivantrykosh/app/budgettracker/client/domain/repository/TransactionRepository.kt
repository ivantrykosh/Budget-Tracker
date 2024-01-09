package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.data.remote.dto.TransactionDto

/**
 * Transaction repository interface
 */
interface TransactionRepository {

    /**
     * Create transaction with token and transactionDto
     *
     * @param token user's JWT
     * @param transactionDto Transaction data
     */
    suspend fun createTransaction(token: String, transactionDto: TransactionDto)

    /**
     * Get transaction with token and id
     *
     * @param token user's JWT
     * @param id Transaction ID to get
     */
    suspend fun getTransaction(token: String, id: String): TransactionDto

    /**
     * Get transactions with token and accountId
     *
     * @param token user's JWT
     * @param accountId Account ID by which transactions are got
     */
    suspend fun getTransactionByAccount(token: String, accountId: String): List<TransactionDto>

    /**
     * Get transactions with token and accountIds
     *
     * @param token user's JWT
     * @param accountIds Account IDs by which transactions are got
     */
    suspend fun getTransactionByAllAccounts(token: String, accountIds: List<Long>): List<TransactionDto>

    /**
     * Get transactions with token, accountIds, startDate and endDate
     *
     * @param token user's JWT
     * @param accountIds Account IDs by which transactions are got
     * @param startDate start date to get transactions
     * @param endDate end date to get transactions
     */
    suspend fun getTransactionByAllAccountAndDateBetween(token: String, accountIds: List<Long>, startDate: String, endDate: String): List<TransactionDto>

    /**
     * Update transaction with token and transactionDto
     *
     * @param token user's JWT
     * @param transactionDto Transaction data
     */
    suspend fun updateTransaction(token: String, transactionDto: TransactionDto)

    /**
     * Delete transaction with token and id
     *
     * @param token user's JWT
     * @param id Transaction ID to delete
     */
    suspend fun deleteTransaction(token: String, id: String)
}