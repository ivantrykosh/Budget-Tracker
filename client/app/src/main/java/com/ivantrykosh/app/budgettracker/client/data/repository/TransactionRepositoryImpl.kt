package com.ivantrykosh.app.budgettracker.client.data.repository

import com.ivantrykosh.app.budgettracker.client.data.dao.TransactionDao
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.domain.model.SubTransaction
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import java.util.Date
import javax.inject.Inject

/**
 * Transaction repository implementation
 */
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    /**
     * Create transaction with token and transactionDto
     *
     * @param transaction Transaction data
     */
    override suspend fun createTransaction(transaction: Transaction) {
        return dao.insertTransaction(transaction)
    }

    /**
     * Get transaction with token and id
     *
     * @param id Transaction ID to get
     */
    override suspend fun getTransaction(id: Long): Transaction {
        return dao.getTransaction(id)
    }

    /**
     * Get transactions with token, accountIds, startDate and endDate
     *
     * @param accountIds Account IDs by which transactions are got
     * @param startDate start date to get transactions
     * @param endDate end date to get transactions
     */
    override suspend fun getTransactionByAllAccountAndDateBetween(
        accountIds: List<Long>,
        startDate: Date,
        endDate: Date
    ): List<SubTransaction> {
        return dao.getTransactions(startDate, endDate, accountIds)
    }

    /**
     * Update transaction with token and transactionDto
     *
     * @param transaction Transaction data
     */
    override suspend fun updateTransaction(transaction: Transaction) {
        return dao.updateTransaction(transaction)
    }

    /**
     * Delete transaction with token and id
     *
     * @param id Transaction ID to delete
     */
    override suspend fun deleteTransaction(id: Long) {
        return dao.deleteTransaction(id)
    }
}